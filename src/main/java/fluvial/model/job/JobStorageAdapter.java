package fluvial.model.job;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.util.List;

/**
 * Created by superttmm on 23/05/2017.
 */
public interface JobStorageAdapter extends CrudRepository<JobStorage, Long> {
    List<JobStorage> findByJobStatus(JobStatus jobStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from JobStorage j where j.id = :id")
    @QueryHints({@QueryHint(name="javax.persistence.lock.timeout", value="3000")})
    JobStorage findOneForUpdate(@Param("id") Long id);
}
