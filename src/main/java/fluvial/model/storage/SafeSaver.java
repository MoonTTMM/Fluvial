package fluvial.model.storage;

import fluvial.model.job.JobStorageAdapter;
import fluvial.model.storage.transaction.SimpleTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;

/**
 * Created by superttmm on 26/05/2017.
 */
@Service
public class SafeSaver<Store extends fluvial.model.storage.Store, Repository extends CrudRepository> {

    private static final long maxWaitTime = 1000;
    private static final long interval = 50;

    @Autowired
    private SimpleTransactionService transactionService;

    @PersistenceContext
    private EntityManager em;

    public Store safeSave(Store store, Repository adapter,
                          StoreSetter<Store> setEntity, StoreSetterCondition<Store> condition, long interval){
        if(!condition.meetCondition(store)){
            return store;
        }
        store = setEntity.setStore(store);
        //store.setUpdateTime(new Date());
        try {
            // If no optimistic exception, just save it normally.
            store = (Store) adapter.save(store);
        }catch (OptimisticLockingFailureException e){
            // Optimistic lock exception occurs, and already tried some times,
            // use transaction and pessimistic lock to save.
            if(interval > maxWaitTime){
                // Transaction implementation is not forced,
                // if there is none, just won't use transaction to make sure the save works.
                if(transactionService != null) {
                    long entityId = store.getId();
                    final Class entityClass = store.getClass();
                    transactionService.executeTransaction(() -> {
                        Store newStore = findOneForUpdate(entityId, entityClass, adapter);
                        newStore = setEntity.setStore(newStore);
                        //newStore.setUpdateTime(new Date());
                        adapter.save(newStore);
                    });
                    return (Store) adapter.findOne(entityId);
                }
                return store;
            }
            // Optimistic lock exception occurs, sleep a little and try again.
            try{
                Thread.sleep(interval);
                store = (Store) adapter.findOne(store.getId());
                return safeSave(store, adapter, setEntity, condition, interval * 2);
            }catch (InterruptedException e1){
                e1.printStackTrace();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return store;
    }

    public Store safeSave(Store store, Repository adapter, StoreSetter<Store> setStore, StoreSetterCondition<Store> condition){
        return safeSave(store, adapter, setStore, condition, interval);
    }

    public Store safeSave(Store store, Repository adapter, StoreSetter<Store> setStore){
        StoreSetterCondition<Store> condition = store1 -> true;
        return safeSave(store, adapter, setStore, condition, interval);
    }

    public Store findOneForUpdate(Long id, Class entityClass, Repository repository) {
        JobStorageAdapter jobRepository = (JobStorageAdapter) repository;
        if(jobRepository != null){
            return (Store) jobRepository.findOneForUpdate(id);
        }

        Store store = (Store) em.find(entityClass, id);
        if (store != null) {
            em.lock(store, LockModeType.PESSIMISTIC_WRITE);
        }
        return store;
    }
}
