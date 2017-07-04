package fluvial.model.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;

/**
 * Created by superttmm on 27/06/2017.
 */
public class SpecificJobJsonConverter implements AttributeConverter<Job, String> {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    /*
     * serialize, from object to json string.
     */
    @Override
    public String convertToDatabaseColumn(Job meta) {
        String jobType = meta.jobType;
        Class jobClass = JobFactory.getJobClass(jobType);
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(jobClass.cast(meta));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return jsonString;
    }

    /*
     * deserialize, from json string to object
     */
    @Override
    public Job convertToEntityAttribute(String dbData) {
        Job job = null;
        try {
            JsonNode root = objectMapper.readTree(dbData);
            JsonNode jobType = root.get("jobType");
            if(jobType == null){
                return job;
            }
            Class jobClass = JobFactory.getJobClass(jobType.asText());
            job = (Job) objectMapper.readValue(dbData, jobClass);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return job;
    }
}
