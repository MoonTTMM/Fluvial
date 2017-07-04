package fluvial.model.job;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fluvial.util.ApplicationContextProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by superttmm on 01/06/2017.
 */
public class JobFactory {

    public static HashMap<String, JobMetadata> jobMetadataMap = new HashMap<>();
    public static HashMap<String, Object> jobConfig = new HashMap<>();

    /**
     * We support json string as input to INIT job metadata.
     * @param jobMetaDataContent
     * ex. [{"jobType": "A", "subJobs":["a1","a2"]},{"jobType":"B", "subJobs":["b1", "b2"]}]
     * @return
     */
    public void initJobMetadataMap(InputStream jobMetaDataContent){
        ObjectMapper mapper = new ObjectMapper();
        List<LinkedHashMap> jobMetadataArray = new ArrayList<>();
        try {
            jobConfig = mapper.readValue(jobMetaDataContent, new TypeReference<HashMap<String, Object>>(){});
            jobMetadataArray = (List<LinkedHashMap>)jobConfig.get("jobs");
        }catch (IOException e){
            e.printStackTrace();
        }
        for(LinkedHashMap metadata : jobMetadataArray){
            jobMetadataMap.put((String)metadata.get("jobType"), new JobMetadata(metadata));
        }
    }

    public void initJobMetadataMap(){
        InputStream jsonStream = JobFactory.class.getResourceAsStream("/fluvial.json");
        initJobMetadataMap(jsonStream);
    }

    /**
     * Create job according to metadata in metadata map.
     * @param jobStorage
     * @return
     */
    public JobStorage setupJob(JobStorage jobStorage){
        JobStorageAdapter adapter = ApplicationContextProvider.getApplicationContext().getBean(JobStorageAdapter.class);
        Job specificJob = jobStorage.getSpecificJob();
        String jobType = jobStorage.getJobType();
        jobStorage = adapter.save(jobStorage);

        JobMetadata metadata = jobMetadataMap.get(jobType);
        if(metadata == null){
            return jobStorage;
        }
        for(String subJobType : metadata.subJobs){
            for(JobStorage subJob : specificJob.init(subJobType)){
                subJob.setParentJob(jobStorage);
                subJob.setJobType(subJobType);
                subJob = setupJob(subJob);
                jobStorage.addSubJob(subJob);
            }
        }
        return jobStorage;
    }

    /**
     * create specific job instance using jobType.
     * @param jobType
     * @return
     */
    public Job createSpecificJob(String jobType) {
        try{
            Class jobClass = getJobClass(jobType);
            if(jobClass == null){
                return null;
            }
            Job job = (Job)jobClass.newInstance();
            job.jobType = jobType;
            return job;
        }catch (IllegalAccessException e){
            e.printStackTrace();
            return null;
        }catch (InstantiationException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Class getJobClass(String jobType){
        JobMetadata metadata = jobMetadataMap.get(jobType);
        String className = jobConfig.get("basePackage") + "." + jobType;
        if(metadata != null && metadata.className != null){
            className = metadata.className;
        }
        try{
            Class jobClass = Class.forName(className);
            return jobClass;
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }
}
