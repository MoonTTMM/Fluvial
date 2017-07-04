package fluvial.model.job;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by superttmm on 01/06/2017.
 */
public class JobMetadata {

    public JobMetadata(){}

    public JobMetadata(LinkedHashMap<String, Object> metadata){
        jobType = (String)metadata.get("jobType");
        subJobs = (List<String>)metadata.get("subJobs");
    }

    public String jobType;

    public List<String> subJobs;

    public String className;
}
