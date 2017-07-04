package fluvial.model.job;

/**
 * Created by superttmm on 22/05/2017.
 */
public enum JobStatus {
    Requested,
    Waiting,
    Scheduled,
    InProcess,
    Completed,
    //Hangon is for subjobs to move on.
    Hangon,
    Failure,
    ToCancel,
    Canceled,
    ToPause,
    Paused,
    ToResume
}
