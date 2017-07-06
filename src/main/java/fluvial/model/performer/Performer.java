package fluvial.model.performer;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by superttmm on 31/05/2017.
 */
public abstract class Performer {

    public String performerType;

    public List<PerformerModule> modules;

    public void addModule(PerformerModule module){
        if(modules == null){
            modules = new ArrayList<>();
        }
        modules.add(module);
    }

    public void moveTo(String target){
        throw new NotImplementedException();
    }
}
