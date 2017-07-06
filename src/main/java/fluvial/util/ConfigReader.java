package fluvial.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by superttmm on 05/07/2017.
 */
public class ConfigReader {
    public static HashMap<String, Object> fluvialConfig;

    public static HashMap<String, Object> getConfig(){
        if(fluvialConfig != null && fluvialConfig.size() > 0){
            return fluvialConfig;
        }
        fluvialConfig = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        InputStream jsonStream = ConfigReader.class.getResourceAsStream("/fluvial.json");
        try {
            fluvialConfig = mapper.readValue(jsonStream, new TypeReference<HashMap<String, Object>>(){});
        }catch (IOException e){
            e.printStackTrace();
        }
        return fluvialConfig;
    }
}
