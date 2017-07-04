package fluvial.model.job.goal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by superttmm on 03/07/2017.
 */
public class GoalDeserializer extends JsonDeserializer<Goal> {

    @Override
    public Goal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper)jsonParser.getCodec();
        Goal goal = null;
        try {
            JsonNode root = mapper.readTree(jsonParser);
            JsonNode className = root.get("className");
            if(className == null){
                return goal;
            }
            Class goalClass = Class.forName(className.asText());
            goal = (Goal) mapper.readValue(root.toString(), goalClass);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex){
            ex.printStackTrace();
        }
        return goal;
    }
}
