package fluvial.model.performer.command;

import fluvial.model.performer.Performer;

/**
 * Created by superttmm on 05/07/2017.
 */
public class MoveCommand extends Command {

    public MoveCommand(Performer performer) {
        super(performer);
    }

    @Override
    public void execute(String... args){
        if(args.length > 0){
            performer.moveTo(args[0]);
        }
    }
}
