package fluvial.model.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by superttmm on 22/05/2017.
 */
@Component
public class JobScheduler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<Long> jobsInScheduler = Collections.synchronizedList(new ArrayList<>());
    private ThreadPoolTaskExecutor pool;

    @Autowired
    private JobStorageAdapter jobStorageAdapter;

    @Autowired
    private JobStatusArbiter statusArbiter;

    public JobScheduler(){
        initializeThreadPool();
    }

    private void initializeThreadPool(){
        pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(10);
        pool.setMaxPoolSize(20);
        pool.setQueueCapacity(500);
        pool.setWaitForTasksToCompleteOnShutdown(true);
        pool.initialize();
    }

    @PostConstruct
    private void scheduleJobs(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            while (true) {
                schedule(JobStatus.ToPause, JobStatus.Paused);
                schedule(JobStatus.ToResume, JobStatus.InProcess);
                schedule(JobStatus.ToCancel, JobStatus.Canceled);
                schedule(JobStatus.Scheduled, JobStatus.InProcess);

                Thread.sleep(100);
            }
        });
    }

    private void schedule(JobStatus sourceStatus, JobStatus targetStatus) throws InterruptedException{
        List<JobStorage> targetJobs = jobStorageAdapter.findByJobStatus(sourceStatus);
        for(JobStorage job : targetJobs){
            // If the job is not finished executing, do not schedule it again.
            if(jobsInScheduler.contains(job.getId())){
                logger.info("Job " + job.getId() + " has already been in jobsInScheduler!");
                continue;
            }
            JobStorage updatedJob;
            try {
                // This is controller level operation in default, so the scheduler can operate status set by controller.
                updatedJob = statusArbiter.setJobStatus(job, targetStatus, OperationLevel.CONTROLLER, OperationLevel.SCHEDULER);
            }catch (OptimisticLockingFailureException e){
                continue;
            }

            jobsInScheduler.add(job.getId());
            switch (sourceStatus){
                case ToPause:
                    pool.execute(() -> {
                        updatedJob.getSpecificJob().pause();
                        jobsInScheduler.remove(updatedJob.getId());
                    });
                    break;
                case Scheduled:
                    pool.execute(() -> {
                        updatedJob.getSpecificJob().run();
                        jobsInScheduler.remove(updatedJob.getId());
                    });
                    break;
                case ToResume:
                    pool.execute(() -> {
                        updatedJob.getSpecificJob().resume();
                        jobsInScheduler.remove(updatedJob.getId());
                    });
                    break;
                case ToCancel:
                    pool.execute(() -> {
                        updatedJob.getSpecificJob().cancel();
                        jobsInScheduler.remove(updatedJob.getId());
                    });
            }
        }
    }

    /*
    Schedule APIs.
     */

    public JobStorage scheduleCurrentJob(JobStorage jobStorage){
        return statusArbiter.setJobStatus(jobStorage, JobStatus.Scheduled);
    }
}
