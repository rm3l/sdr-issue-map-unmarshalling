package org.rm3l.sdr_issue_map_deserialization.repository;

import org.rm3l.sdr_issue_map_deserialization.domain.Item;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
    exported = true,
    path = "Item",
    collectionResourceRel = "ItemCollection",
    itemResourceRel = "Item")
public interface ItemRepository extends AbstractBaseRepository<Item, Long> {}
