package fluvial.model.job.goal;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by superttmm on 30/06/2017.
 */
@JsonDeserialize(as = FlagGoal.class)
public class FlagGoal extends Goal {

    public FlagGoal(){
        goalType = "FlagGoal";
    }

    @Override
    public boolean isReached(Object status) {
        return (boolean)status;
    }
}
