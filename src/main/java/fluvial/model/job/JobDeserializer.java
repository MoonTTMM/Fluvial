package fluvial.model.job;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by superttmm on 28/06/2017.
 */
public class JobDeserializer extends JsonDeserializer<Job> {

    @Override
    public Job deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper)jsonParser.getCodec();
        Job job = null;
        try {
            JsonNode root = mapper.readTree(jsonParser);
            JsonNode jobType = root.get("jobType");
            if(jobType == null){
                return job;
            }
            Class jobClass = JobFactory.getJobClass(jobType.asText());
            job = (Job) mapper.readValue(root.toString(), jobClass);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return job;
    }
}
