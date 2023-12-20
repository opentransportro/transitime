package org.transitclock.core.domain.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import org.transitclock.core.domain.Agency;

@Repository
public interface AgencyRepository extends BaseRepository<Agency, Agency.Key> {
    @Query(value = "{'_id': {'configRev' : $0}}", delete = true)
    int deleteFromConfigRev(int configRev);
}
