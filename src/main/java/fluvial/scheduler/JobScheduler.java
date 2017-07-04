package fluvial.scheduler;

import fluvial.model.job.*;
import fluvial.model.performer.PerformerAllocator;
import fluvial.model.storage.StoreSetter;
import fluvial.model.storage.StoreSetterCondition;
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
    private JobSafeSaver jobSafeSaver;

    @Autowired
    private PerformerAllocator performerAllocator;

    private StoreSetter<JobStorage> rescheduleJob = entity -> {
        Job job = entity.getSpecificJob();
        job.triedTimes++;
        entity.setJobStatus(JobStatus.Scheduled);
        return entity;
    };

    private StoreSetterCondition<JobStorage> rescheduleJobCondition = entity -> {
        // When using safe save, which can make sure the update happens.
        // But in some cases, it is not right, like the status set by scheduler may be over written.
        // So when doing reschedule, some states set by scheduler should be make sure not over written.
        return entity.getJobStatus().equals(JobStatus.InProcess);
    };

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
                job.setJobStatus(targetStatus);
                updatedJob = (JobStorage) jobStorageAdapter.save(job);
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
        return jobSafeSaver.safeSetJobStatus(jobStorage, JobStatus.Scheduled);
    }

    public void scheduleNextJob(JobStorage jobStorage, JobStatus targetJobStatus){
        // For parent job, go on doing its sub job and make itself hang on.
        if(jobStorage.getSubJobs().size() > 0){
            jobStorage = jobSafeSaver.safeSetJobStatus(jobStorage, JobStatus.InProcess);
            JobStorage firstSubJob = jobStorage.getSubJobs().get(0);
            jobSafeSaver.safeSetJobStatus(firstSubJob, JobStatus.Scheduled);
        }
        // For leaf job, find its next incomplete sibling and make itself complete.
        // If not any, make its parent complete recursively.
        else {
            jobStorage.getSpecificJob().stop();
            jobSafeSaver.safeSetJobStatus(jobStorage, targetJobStatus);

            // Find next job in the job tree.
            JobStorage parentJob = jobStorage.getParentJob();
            while (parentJob != null) {
                for (JobStorage job : parentJob.getSubJobs()) {
                    if (!job.getJobStatus().equals(JobStatus.Completed)
                            && !job.getJobStatus().equals(JobStatus.Failure)) {
                        jobSafeSaver.safeSetJobStatus(job, JobStatus.Scheduled);
                        return;
                    }
                }
                Job parentSpecificJob = parentJob.getSpecificJob();
                parentSpecificJob.stop();
                jobSafeSaver.safeSetJobStatus(parentJob, JobStatus.Completed);
                parentJob = parentJob.getParentJob();
            }

            performerAllocator.releasePerformer(jobStorage);
        }
    }

    public JobStorage rescheduleJob(JobStorage jobStorage){
        return jobSafeSaver.safeSave(jobStorage, rescheduleJob, rescheduleJobCondition);
    }
}
