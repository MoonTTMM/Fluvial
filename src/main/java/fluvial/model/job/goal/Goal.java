package fluvial.model.job.goal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by superttmm on 25/05/2017.
 */
@JsonDeserialize(using = GoalDeserializer.class)
public abstract class Goal {

    public String goalType;
    public String className = this.getClass().getName();

    /**
     * Check job's current status reach the goal.
     * @param status
     * @return
     */
    public boolean isReached(Object status){return true;}
}
