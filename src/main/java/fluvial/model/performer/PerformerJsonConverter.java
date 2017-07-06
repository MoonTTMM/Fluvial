package fluvial.model.performer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import java.io.IOException;

/**
 * Created by superttmm on 05/07/2017.
 */
public class PerformerJsonConverter implements AttributeConverter<Performer, String> {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Performer performer) {
        String performerType = performer.performerType;
        Class performerClass = PerformerFactory.getPerformerClass(performerType);
        String jsonString = "";
        try {
            jsonString = objectMapper.writeValueAsString(performerClass.cast(performer));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return jsonString;
    }

    @Override
    public Performer convertToEntityAttribute(String s) {
        Performer performer = null;
        try {
            JsonNode root = objectMapper.readTree(s);
            JsonNode type = root.get("performerType");
            if(type == null){
                return performer;
            }
            Class performerClass = PerformerFactory.getPerformerClass(type.asText());
            performer = (Performer) objectMapper.readValue(s, performerClass);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return performer;
    }
}
