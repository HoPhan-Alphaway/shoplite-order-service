package com.shoplite.orderservice.product;

import java.math.BigDecimal;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

/**
 * East-west call to product-service via its Kubernetes Service DNS name (not through Kong).
 * Protected by a circuit breaker: when product-service is down, orders fail fast with 503
 * instead of piling up threads waiting on timeouts.
 */
@Component
public class ProductClient {

	private final RestClient restClient;

	private final CircuitBreaker circuitBreaker;

	public ProductClient(RestClient productRestClient, CircuitBreakerFactory<?, ?> circuitBreakerFactory) {
		this.restClient = productRestClient;
		this.circuitBreaker = circuitBreakerFactory.create("product-service");
	}

	public ProductInfo get(long productId) {
		return call(() -> restClient.get().uri("/api/products/{id}", productId).retrieve().body(ProductInfo.class));
	}

	public void reserve(long productId, int quantity) {
		call(() -> restClient.post()
			.uri("/api/products/{id}/reserve", productId)
			.body(Map.of("quantity", quantity))
			.retrieve()
			.toBodilessEntity());
	}

	private <T> T call(Supplier<T> supplier) {
		return circuitBreaker.run(supplier, ProductClient::fallback);
	}

	private static <T> T fallback(Throwable error) {
		if (error instanceof HttpClientErrorException e) {
			// Business answer from product-service (404 / 409): pass it through unchanged.
			ProblemDetail problem = e.getResponseBodyAs(ProblemDetail.class);
			throw new ResponseStatusException(e.getStatusCode(),
					problem != null && problem.getDetail() != null ? problem.getDetail() : e.getStatusText());
		}
		throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
				"Product service unavailable, please retry later", error);
	}

	public record ProductInfo(long id, String name, BigDecimal price, int stock) {
	}

}
