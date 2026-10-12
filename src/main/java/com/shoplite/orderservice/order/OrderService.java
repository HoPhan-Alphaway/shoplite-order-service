package com.shoplite.orderservice.order;

import com.shoplite.orderservice.product.ProductClient;
import com.shoplite.orderservice.product.ProductClient.ProductInfo;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class OrderService {

	private static final Logger log = LoggerFactory.getLogger(OrderService.class);

	private final OrderRepository repository;

	private final ProductClient productClient;

	private final KafkaTemplate<String, String> kafka;

	private final ObjectMapper json;

	private final String topic;

	public OrderService(OrderRepository repository, ProductClient productClient, KafkaTemplate<String, String> kafka,
			ObjectMapper json, @Value("${shoplite.kafka.order-created-topic}") String topic) {
		this.repository = repository;
		this.productClient = productClient;
		this.kafka = kafka;
		this.json = json;
		this.topic = topic;
	}

	/**
	 * 1. sync REST: read product + reserve stock (product-service)
	 * 2. local DB write
	 * 3. async event for side effects (notification-service)
	 *
	 * Known simplifications (good follow-up exercises): no compensation if step 2 fails after
	 * step 1 (Saga), and DB write + Kafka publish are not atomic (Transactional Outbox).
	 */
	public Order placeOrder(String userId, String username, long productId, int quantity) {
		ProductInfo product = productClient.get(productId);
		productClient.reserve(productId, quantity);

		Order order = repository.save(new Order(userId, username, product.id(), product.name(), quantity,
				product.price()));
		log.info("Order {} placed by {}: {} x {}", order.getId(), username, quantity, product.name());

		publish(OrderCreatedEvent.of(order));
		return order;
	}

	public List<Order> findMine(String userId) {
		return repository.findTop50ByUserIdOrderByCreatedAtDesc(userId);
	}

	private void publish(OrderCreatedEvent event) {
		String key = String.valueOf(event.payload().orderId());
		kafka.send(topic, key, json.writeValueAsString(event)).whenComplete((result, error) -> {
			if (error != null) {
				log.error("Failed to publish {} for order {}", event.eventType(), key, error);
			}
			else {
				log.info("Published {} for order {} to partition {}", event.eventType(), key,
						result.getRecordMetadata().partition());
			}
		});
	}

}
