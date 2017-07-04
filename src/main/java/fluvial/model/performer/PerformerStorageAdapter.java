package fluvial.model.performer;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by superttmm on 31/05/2017.
 */
public interface PerformerStorageAdapter extends CrudRepository<PerformerStorage, Long> {
    List<PerformerStorage> findByStatus(PerformerStatus status);
}
