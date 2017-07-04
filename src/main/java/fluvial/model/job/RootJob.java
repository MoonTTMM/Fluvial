package fluvial.model.job;

import fluvial.model.job.goal.FlagGoal;
import fluvial.model.job.goal.Goal;

/**
 * Created by superttmm on 04/07/2017.
 */
public abstract class RootJob extends Job {

    @Override
    public void execute(){
        if(getPerformer() != null){
            return;
        }
        jobStorage = performerAllocator.assignPerformer(jobStorage);
    }

    @Override
    public void createGoals(){
        goals.add(new FlagGoal());
    }

    @Override
    public boolean checkGoal(Goal goal){
        return goal.isReached(getPerformer() != null);
    }
}
