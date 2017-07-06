package fluvial.model.performer;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by superttmm on 06/07/2017.
 */
public class PerformerMetadata {

    public PerformerMetadata(){}

    public PerformerMetadata(LinkedHashMap<String, Object> metadata){
        performerType = (String)metadata.get("performerType");
        modules = (List<String>)metadata.get("modules");
        className = (String)metadata.get("className");
    }

    public String performerType;
    public List<String> modules;
    public String className;
}
