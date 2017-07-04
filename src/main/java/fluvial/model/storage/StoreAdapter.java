package fluvial.model.storage;

/**
 * Created by superttmm on 26/05/2017.
 */
public interface StoreAdapter {
    Store findOne(Long id);
    Store save(Store store);
}
