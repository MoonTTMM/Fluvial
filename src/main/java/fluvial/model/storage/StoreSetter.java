package fluvial.model.storage;

/**
 * Created by superttmm on 26/05/2017.
 */
public interface StoreSetter<S extends Store> {
    S setStore(S store);
}
