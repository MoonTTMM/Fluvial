package fluvial.model.job;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fluvial.comm.WebSocket;
import fluvial.model.job.goal.Goal;
import fluvial.model.performer.PerformerAllocator;
import fluvial.model.performer.PerformerStorage;
import fluvial.model.performer.PerformerStorageAdapter;
import fluvial.model.storage.StoreSetter;
import fluvial.model.storage.StoreSetterCondition;
import fluvial.util.ApplicationContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by superttmm on 22/05/2017.
 */
@JsonDeserialize(using = JobDeserializer.class)
public abstract class Job {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected List<Goal> goals;

    public String jobType;

    public int triedTimes;

    private StoreSetter<JobStorage> setJobInitInfo = entity -> {
        System.out.println("Set job init info.");
        Job job = entity.getSpecificJob();
        job.createGoals();
        entity.setStartTime(new Date());
        return entity;
    };

    private StoreSetter<JobStorage> setTriedTimes = entity -> {
        Job job = entity.getSpecificJob();
        job.triedTimes = this.triedTimes;
        return entity;
    };

    @JsonIgnore
    public JobStorage jobStorage;

    @JsonIgnore
    public ApplicationContext context;

    @JsonIgnore
    public JobStorageAdapter jobStorageAdapter;

    @JsonIgnore
    public PerformerStorageAdapter performerStorageAdapter;

    @JsonIgnore
    public JobSafeSaver jobSafeSaver;

    @JsonIgnore
    public PerformerAllocator performerAllocator;

    @JsonIgnore
    public WebSocket webSocket;

    @JsonIgnore
    public JobFactory jobFactory;

    @JsonIgnore
    protected JobScheduler jobScheduler;

    @JsonIgnore
    protected JobStatusArbiter statusArbiter;

    public List<Goal> getGoals(){
        return goals;
    }

    public Job(){
        context = ApplicationContextProvider.getApplicationContext();
        jobStorageAdapter = context.getBean(JobStorageAdapter.class);
        performerStorageAdapter = context.getBean(PerformerStorageAdapter.class);
        jobSafeSaver = context.getBean(JobSafeSaver.class);
        performerAllocator = context.getBean(PerformerAllocator.class);
        jobFactory = context.getBean(JobFactory.class);
        jobScheduler = context.getBean(JobScheduler.class);
        statusArbiter = context.getBean(JobStatusArbiter.class);

        goals = new ArrayList<>();
        try {
            webSocket = context.getBean(WebSocket.class);
        }catch (NoSuchBeanDefinitionException e){
            e.printStackTrace();
        }
    }

    /*
    ----------------------------------------------------------
    Public APIs.
     */

    /**
     * Scheduler would call this function.
     */
    public void run() {
        if(triedTimes == 0) {
            jobStorage = updateJob(setJobInitInfo);
            // Only first time would delay
            if(getDelayTimes() > 0 && triedTimes < getDelayTimes()){
                return;
            }
        }
        execute();
        if(isJobFinished()){
            return;
        }
        rescheduleJob();
    }

    /**
     * Init specific job. This is the function to create sub jobs.
     * @param jobType
     * @return
     */
    public List<JobStorage> init(String jobType){
        List<JobStorage> subJobs = new ArrayList<>();
        return subJobs;
    }

    /**
     * The actual execute method for different jobs.
     */
    public void execute(){}

    /**
     * Execute some action when stop if needed.
     */
    public void stop(){
        logger.info("*************************\n" + jobType + " stop");
        shutJobActions();
    }

    /**
     * Pause current job.
     */
    public void pause(){
        logger.info("*************************\n" + jobType + " paused");
        shutJobActions();
    }

    /**
     * Resume job from pause.
     */
    public void resume(){ logger.info("*************************\n" + jobType + " resumed"); }

    /**
     * Cancel current job.
     */
    public void cancel(){
        logger.info("*************************\n" + jobType + " canceled");
        shutJobActions();
    }

    /**
     * Reset job when doing stop, pause or cancel.
     */
    public void shutJobActions(){}

    /**
     * Restart job from redo.
     */
    public JobStorage restart(){
        List<JobStorage> jobListPath = new ArrayList<>();
        return setJobPathRequestedRecursively(jobStorage, jobListPath);
    }

    /**
     * Way to check or set data during job sequence.
     * @param value
     * @return
     */
    public void setJobData(Object value){ }

    /**
     * Way to check or set data during job sequence.
     * @param value
     * @param force If force set the data and ignore data checking.
     */
    public void setJobData(Object value, boolean force){
        if(!force){
            setJobData(value);
        }
    }

    /**
     * Way to clean data when initialization needed.
     */
    public void cleanJobData(){
        this.triedTimes =0;
        jobStorage = updateJob(setTriedTimes);
    }

    /**
     * Some job would delay some time to execute.
     * @return
     */
    protected long getDelayTimes(){return 0;}

    /**
     * Max times the execute would be called.
     * If <0 means the times are unlimited.
     * @return
     */
    protected int getMaxTriedTimes(){return -1;}

    /**
     * Set goal for current job.
     */
    public void createGoals() {
    }

    /**
     * Set this job ready.
     */
    public void setReady() {}

    /**
     * check if meet the goal.
     */
    public boolean checkGoal(Goal goal){
        return true;
    }

    /**
     * handle if the job fails.(like timeout)
     */
    public void handleFail(){
        scheduleNextJob(JobStatus.Failure);
    }

    /**
     * Create a storage.
     * @return
     */
    public JobStorage createStorage(){
        throw new NotImplementedException();
    }


    /*
    -------------------------------------------------------------
    Protected APIs.
     */
    protected PerformerStorage getPerformer(){
        return jobStorage.getPerformer();
    }

    protected JobStorage updateJob(StoreSetter<JobStorage> setJob){
        return jobSafeSaver.safeSave(this.jobStorage, setJob);
    }

    protected void failSiblingJobs(){
        JobStorage parent = jobStorage.getParentJob();
        if(parent != null){
            for(JobStorage subJob : parent.getSubJobs()){
                statusArbiter.setJobStatus(subJob, JobStatus.Failure);
            }
        }
        scheduleNextJob(JobStatus.Failure);
    }

    protected void failParentJobs(){
        jobStorage = statusArbiter.setJobStatus(jobStorage, JobStatus.Failure);
        JobStorage parent = jobStorage.getParentJob();
        while (parent != null){
            statusArbiter.setJobStatus(parent, JobStatus.Failure);
            parent = parent.getParentJob();
        }
    }

    /*
    -------------------------------------------------------------
    Private Functions
     */

    private boolean isJobFinished(){
        if(goals.size() == 0 ||
                (triedTimes > getMaxTriedTimes() && getMaxTriedTimes() > 0)){
            handleFail();
            return true;
        }
        if(checkGoals()){
            scheduleNextJob(JobStatus.Completed);
            return true;
        }
        return false;
    }

    private JobStorage setJobPathRequestedRecursively(JobStorage currentJob, List<JobStorage> path){
        if(currentJob.getJobStatus().equals(JobStatus.Failure)) {
            path.add(currentJob);
            path.forEach(jobNode -> {
                jobNode.getSpecificJob().cleanJobData();
                statusArbiter.setJobStatus(jobNode, JobStatus.Requested);});
        }
        List<JobStorage> currentJobPath = new ArrayList<>();
        currentJobPath.addAll(path);
        currentJobPath.add(currentJob);
        //restart the necessary preAndAfter jobs about failed jobs
        currentJobPath.addAll(currentJob.getSubJobs().stream()
                // TODO:JobType
                .filter(subJob -> subJob.getJobType().equals(""))
                .collect(Collectors.toList()));
        for (JobStorage subJob : currentJob.getSubJobs()){
            setJobPathRequestedRecursively(subJob, currentJobPath);
        }
        return currentJob;
    }

    /**
     * check if all statuses are met.
     * @return
     */
    private boolean checkGoals() {
        boolean flag = true;
        for(Goal goal : goals){
            flag = flag && checkGoal(goal);
        }
        return flag;
    }

    private void scheduleNextJob(JobStatus targetJobStatus){
        // For parent job, go on doing its sub job and make itself hang on.
        if(jobStorage.getSubJobs().size() > 0){
            jobStorage = setJobStatusInScheduler(jobStorage, JobStatus.InProcess);
            JobStorage firstSubJob = jobStorage.getSubJobs().get(0);
            setJobStatusInScheduler(firstSubJob, JobStatus.Scheduled);
        }
        // For leaf job, find its next incomplete sibling and make itself complete.
        // If not any, make its parent complete recursively.
        else {
            jobStorage.getSpecificJob().stop();
            setJobStatusInScheduler(jobStorage, targetJobStatus);

            // Find next job in the job tree.
            JobStorage parentJob = jobStorage.getParentJob();
            while (parentJob != null) {
                for (JobStorage job : parentJob.getSubJobs()) {
                    if (!job.getJobStatus().equals(JobStatus.Completed)
                            && !job.getJobStatus().equals(JobStatus.Failure)) {
                        setJobStatusInScheduler(job, JobStatus.Scheduled);
                        return;
                    }
                }
                Job parentSpecificJob = parentJob.getSpecificJob();
                parentSpecificJob.stop();
                setJobStatusInScheduler(parentJob, JobStatus.Completed);
                parentJob = parentJob.getParentJob();
            }

            performerAllocator.releasePerformer(jobStorage);
        }
    }

    private JobStorage setJobStatusInScheduler(JobStorage job, JobStatus status){
        return statusArbiter.setJobStatus(job, status, OperationLevel.SCHEDULER);
    }

    private JobStorage rescheduleJob(){
        return jobSafeSaver.safeSave(jobStorage, rescheduleJob, rescheduleJobCondition);
    }

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
}
