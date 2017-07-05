package fluvial.model.job;

/**
 * Created by superttmm on 04/07/2017.
 * Indicates operation on job. Low level operation cannot overwrite last high level operation.
 */
public enum OperationLevel {
    SCHEDULER(   0,     "Scheduler"),
    CONTROLLER(  10,    "Controller");

    private int level;
    private String name;

    OperationLevel(int level, String name){
        this.level = level;
        this.name = name;
    }

    public int getLevel() {return level;}
    public String getName() {return name;}
}
