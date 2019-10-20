package org.rm3l.sdr_issue_map_deserialization.repository;

import java.io.Serializable;
import org.rm3l.sdr_issue_map_deserialization.domain.AbstractBaseJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository interface for any entity that have the fields specified in the JPQL queries below.
 *
 * <p>Can be extended by other repositories that wish to have a 'findByXXX' capability exposed via
 * the API
 */
@NoRepositoryBean
@Transactional
public interface AbstractBaseRepository<
        TYPE extends AbstractBaseJpaEntity, PK_TYPE extends Serializable>
    extends JpaRepository<TYPE, PK_TYPE> {
}
