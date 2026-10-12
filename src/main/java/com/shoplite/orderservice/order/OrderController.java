package com.shoplite.orderservice.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

	private final OrderService service;

	public OrderController(OrderService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderView place(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PlaceOrderRequest req) {
		Order order = service.placeOrder(jwt.getSubject(), jwt.getClaimAsString("preferred_username"),
				req.productId(), req.quantity());
		return OrderView.of(order);
	}

	@GetMapping
	public List<OrderView> mine(@AuthenticationPrincipal Jwt jwt) {
		return service.findMine(jwt.getSubject()).stream().map(OrderView::of).toList();
	}

	public record PlaceOrderRequest(long productId, @Min(1) @Max(10) int quantity) {
	}

	public record OrderView(Long id, long productId, String productName, int quantity, BigDecimal unitPrice,
			BigDecimal totalAmount, String status, Instant createdAt) {

		static OrderView of(Order o) {
			return new OrderView(o.getId(), o.getProductId(), o.getProductName(), o.getQuantity(), o.getUnitPrice(),
					o.getTotalAmount(), o.getStatus().name(), o.getCreatedAt());
		}
	}

}
