package fluvial.model.performer.command;

import fluvial.model.performer.Performer;

/**
 * Created by superttmm on 05/07/2017.
 */
public abstract class Command {

    protected Performer performer;

    public Command(Performer performer){
        this.performer = performer;
    }

    public void execute(String... args){}
}
