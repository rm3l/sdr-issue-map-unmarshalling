package org.rm3l.sdr_issue_map_deserialization.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractBaseJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long idDb;

  public Long getIdDb() {
    return idDb;
  }

  public void setIdDb(Long idDb) {
    this.idDb = idDb;
  }
}
