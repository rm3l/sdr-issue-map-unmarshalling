package org.rm3l.sdr_issue_map_deserialization.domain;

import java.util.Map;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

@Entity
public class Order extends AbstractBaseJpaEntity {

  @Basic
  @Access(AccessType.PROPERTY)
  @com.fasterxml.jackson.annotation.JsonProperty("internalId")
  private Long internalId;

  @Basic
  @Access(AccessType.PROPERTY)
  @com.fasterxml.jackson.annotation.JsonProperty("name")
  private String name;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
  @JoinTable(name = "order_item_map")
  @Access(AccessType.PROPERTY)
  @com.fasterxml.jackson.annotation.JsonProperty("ItemMap")
  private Map<String, Item> itemMap;

  @com.fasterxml.jackson.annotation.JsonProperty("internalId")
  public Long getInternalId() {
    return internalId;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("internalId")
  public void setInternalId(Long internalId) {
    this.internalId = internalId;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("name")
  public String getName() {
    return name;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("ItemMap")
  public Map<String, Item> getItemMap() {
    return itemMap;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("ItemMap")
  public void setItemMap(Map<String, Item> itemMap) {
    this.itemMap = itemMap;
  }
}
