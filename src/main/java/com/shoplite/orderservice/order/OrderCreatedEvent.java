package com.shoplite.orderservice.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event contract published on topic {@code order.created} (JSON, key = orderId).
 * Consumers must treat {@code eventId} as the idempotency key.
 */
public record OrderCreatedEvent(String eventId, String eventType, Instant occurredAt, Payload payload) {

	public static final String TYPE = "order.created";

	public record Payload(long orderId, String userId, String username, long productId, String productName,
			int quantity, BigDecimal totalAmount) {
	}

	static OrderCreatedEvent of(Order order) {
		return new OrderCreatedEvent(UUID.randomUUID().toString(), TYPE, Instant.now(),
				new Payload(order.getId(), order.getUserId(), order.getUsername(), order.getProductId(),
						order.getProductName(), order.getQuantity(), order.getTotalAmount()));
	}

}
