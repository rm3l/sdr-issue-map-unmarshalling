package org.rm3l.sdr_issue_map_deserialization.repository;

import org.rm3l.sdr_issue_map_deserialization.domain.Order;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
    exported = true,
    path = "Order",
    collectionResourceRel = "OrderCollection",
    itemResourceRel = "Order")
public interface OrderRepository extends AbstractBaseRepository<Order, Long> {}
