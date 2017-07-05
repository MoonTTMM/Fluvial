package fluvial.model.job;

import fluvial.model.storage.StoreSetter;
import fluvial.model.storage.StoreSetterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by superttmm on 04/07/2017.
 */
@Service
public class JobStatusArbiter {
    @Autowired
    JobSafeSaver jobSafeSaver;

    /**
     * This should be used when set job status in controllers.
     * @return
     */
    public JobStorage setJobStatus(JobStorage jobStorage, JobStatus jobStatus){
        return setJobStatus(jobStorage, jobStatus, OperationLevel.CONTROLLER);
    }

    /*
    internal functions....
     */

    JobStorage setJobStatus(JobStorage jobStorage, JobStatus jobStatus, OperationLevel level){
        return setJobStatus(jobStorage, jobStatus, level, level);
    }

    /**
     *
     * @param jobStorage
     * @param jobStatus
     * @param operationLevel This defines if the operation has enough level priority.
     * @param targetLevel This defines the target level the job would be, after status set.
     * @return
     */
    JobStorage setJobStatus(JobStorage jobStorage, JobStatus jobStatus, OperationLevel operationLevel, OperationLevel targetLevel){
        StoreSetter<JobStorage> setJobStatus = store -> {
            store.setJobStatus(jobStatus);
            store.setOperationLevel(targetLevel);
            return store;
        };
        StoreSetterCondition<JobStorage> condition = store -> store.getOperationLevel() == null
                || operationLevel.getLevel() >= store.getOperationLevel().getLevel();
        return jobSafeSaver.safeSave(jobStorage, setJobStatus, condition);
    }
}
