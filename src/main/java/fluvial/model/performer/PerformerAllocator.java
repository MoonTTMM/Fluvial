package fluvial.model.performer;

import fluvial.model.job.JobStorage;

/**
 * Created by superttmm on 31/05/2017.
 */
public interface PerformerAllocator {

    void releasePerformer(JobStorage jobStorage);

    JobStorage assignPerformer(JobStorage jobStorage);

    JobStorage assignPerformer(JobStorage jobStorage, PerformerSelector selector);
}
