package fluvial.model.storage;

/**
 * Created by superttmm on 26/05/2017.
 */
public interface StoreSetterCondition<S extends Store> {
    boolean meetCondition(S store);
}
