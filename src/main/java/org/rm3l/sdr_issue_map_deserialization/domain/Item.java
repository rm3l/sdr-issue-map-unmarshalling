package org.rm3l.sdr_issue_map_deserialization.domain;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Entity;

@Entity
public class Item extends AbstractBaseJpaEntity {

  @Basic(optional = false)
  @Access(AccessType.PROPERTY)
  @com.fasterxml.jackson.annotation.JsonProperty("itemName")
  private String itemName;

  @Basic
  @Access(AccessType.PROPERTY)
  @com.fasterxml.jackson.annotation.JsonProperty("myIntegerAttribute")
  private Integer myIntegerAttribute;

  @com.fasterxml.jackson.annotation.JsonProperty("itemName")
  public String getItemName() {
    return itemName;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("itemName")
  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("myIntegerAttribute")
  public Integer getMyIntegerAttribute() {
    return myIntegerAttribute;
  }

  @com.fasterxml.jackson.annotation.JsonProperty("myIntegerAttribute")
  public void setMyIntegerAttribute(Integer myIntegerAttribute) {
    this.myIntegerAttribute = myIntegerAttribute;
  }
}
