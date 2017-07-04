package fluvial.model.performer;

import fluvial.model.job.JobSafeSaver;
import fluvial.model.job.JobStorage;
import fluvial.model.job.JobStorageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by superttmm on 29/06/2017.
 */
@Service
public class DefaultAllocatorImp implements PerformerAllocator {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Object robotLock = new Object();

    @Autowired
    protected PerformerStorageAdapter performerStorageAdapter;

    @Autowired
    protected JobStorageAdapter jobStorageAdapter;

    @Autowired
    protected JobSafeSaver jobSafeSaver;

    @Override
    public void releasePerformer(JobStorage jobStorage) {

    }

    @Override
    public JobStorage assignPerformer(JobStorage jobStorage) {
        return assignPerformer(jobStorage, () -> getTopPriorAvailablePerformer());
    }

    @Override
    public JobStorage assignPerformer(JobStorage jobStorage, PerformerSelector selector){
        synchronized (robotLock) {
            PerformerStorage reservedRobot = jobStorage.getPerformer();
            if (reservedRobot != null) {
                reservedRobot = performerStorageAdapter.findOne(reservedRobot.getId());
            }
            final PerformerStorage robot = reservedRobot != null ? reservedRobot : selector.getTopPriorAvailablePerformer();
            if (robot != null) {
                logger.info("Assign robot " + robot.getName() + " to job " + jobStorage.getId());
                robot.setStatus(PerformerStatus.Busy);
                performerStorageAdapter.save(robot);
                assignRobotRecursively(jobStorage, robot);
            }
        }
        return jobStorage;
    }

    private void assignRobotRecursively(JobStorage jobStorage, PerformerStorage robot){
        jobSafeSaver.safeSave(jobStorage, entity -> {
            entity.setPerformer(robot);
            return entity;
        });
        for (JobStorage subJob : jobStorage.getSubJobs()){
            assignRobotRecursively(subJob,robot);
        }
    }

    private PerformerStorage getTopPriorAvailablePerformer(){
        List<PerformerStorage> performers = performerStorageAdapter.findByStatus(PerformerStatus.Available);
        if(performers.size()>0){
            return performers.get(0);
        }
        return null;
    }
}
