package com.shoplite.orderservice.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findTop50ByUserIdOrderByCreatedAtDesc(String userId);

}
