package fluvial.model.job;

import fluvial.util.ConfigReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by superttmm on 01/06/2017.
 */
public class JobFactory {

    public static HashMap<String, JobMetadata> jobMetadataMap = new HashMap<>();

    @Autowired
    private JobStorageAdapter adapter;

    /**
     * We support json string as input to INIT job metadata.
     * ex. [{"jobType": "A", "subJobs":["a1","a2"]},{"jobType":"B", "subJobs":["b1", "b2"]}]
     * @return
     */
    public void initJobMetadataMap(){
        for(LinkedHashMap metadata : (List<LinkedHashMap>)ConfigReader.getConfig().get("jobs")){
            jobMetadataMap.put((String)metadata.get("jobType"), new JobMetadata(metadata));
        }
    }

    /**
     * Create job according to metadata in metadata map.
     * @param jobStorage
     * @return
     */
    public JobStorage setupJob(JobStorage jobStorage){
        //JobStorageAdapter adapter = ApplicationContextProvider.getApplicationContext().getBean(JobStorageAdapter.class);
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
        String className = ConfigReader.fluvialConfig.get("jobPackage") + "." + jobType;
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
