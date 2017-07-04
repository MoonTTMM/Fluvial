package fluvial.model.job;

import fluvial.model.storage.SafeSaver;
import fluvial.model.storage.StoreSetter;
import fluvial.model.storage.StoreSetterCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by superttmm on 31/05/2017.
 */
@Repository
public class JobSafeSaver {
    @Autowired
    SafeSaver<JobStorage, JobStorageAdapter> safeSaver;

    @Autowired
    JobStorageAdapter jobStorageAdapter;

    public JobStorage safeSave(JobStorage job, StoreSetter<JobStorage> setter){
        StoreSetterCondition<JobStorage> condition = store -> true;
        return safeSave(job, setter, condition);
    }

    // If only set property in json object, mysql seems cannot detect that, and saving would fail.
    // So a property in store should also be set.
    public JobStorage safeSave(JobStorage job, StoreSetter<JobStorage> setter, StoreSetterCondition<JobStorage> condition){
        StoreSetter<JobStorage> setStore = store -> {
            setter.setStore(store);
            store.setUpdateTime(new Date());
            return store;
        };
        return safeSaver.safeSave(job, jobStorageAdapter, setStore, condition);
    }

    public JobStorage safeSetJobStatus(JobStorage jobStorage, JobStatus targetStatus){
        StoreSetter<JobStorage> setJobStatus = entity -> {
            entity.setJobStatus(targetStatus);
            return entity;
        };
        return safeSave(jobStorage, setJobStatus);
    }
}
