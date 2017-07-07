package fluvial.model.performer.command;

/**
 * Created by superttmm on 05/07/2017.
 */
public abstract class Command {
    // TODO: Command pattern seems not suitable here. Currently we new command in specific job, and the job already have reference to performer.
    // Command pattern here does not decouple the invoker and the command receiver here.
    public void execute(String... args){}
}
