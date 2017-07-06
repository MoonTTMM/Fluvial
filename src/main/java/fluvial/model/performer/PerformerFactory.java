package fluvial.model.performer;

import fluvial.util.ConfigReader;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by superttmm on 05/07/2017.
 */
public class PerformerFactory {
    public static Map<String, PerformerMetadata> performerMetadata = new HashMap<>();

    @Autowired
    private PerformerStorageAdapter adapter;

    public void initPerformerMetadata(){
        for(LinkedHashMap metadata : (List<LinkedHashMap>) ConfigReader.getConfig().get("performers")){
            performerMetadata.put((String)metadata.get("performerType"), new PerformerMetadata(metadata));
        }
    }

    public PerformerStorage setup(PerformerStorage storage){
        String performerType = storage.getPerformerType();
        Performer performer = createPerformer(performerType);
        PerformerMetadata metadata = performerMetadata.get(performerType);
        for(String moduleName : metadata.modules){
            performer.addModule(new PerformerModule(moduleName));
        }
        storage.setPerformer(performer);
        return adapter.save(storage);
    }

    private Performer createPerformer(String performerType) {
        try{
            Class performerClass = getPerformerClass(performerType);
            if(performerClass == null){
                return null;
            }
            Performer performer = (Performer) performerClass.newInstance();
            performer.performerType = performerType;
            return performer;
        }catch (IllegalAccessException e){
            e.printStackTrace();
            return null;
        }catch (InstantiationException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Class getPerformerClass(String performerType){
        PerformerMetadata metadata = performerMetadata.get(performerType);
        String className = ConfigReader.getConfig().get("performerPackage") + "." + performerType;
        if(metadata != null && metadata.className != null){
            className = metadata.className;
        }
        try{
            Class performerClass = Class.forName(className);
            return performerClass;
        }catch (ClassNotFoundException e){
            e.printStackTrace();
            return null;
        }
    }
}
