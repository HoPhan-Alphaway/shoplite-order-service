package com.shoplite.orderservice.order;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {

	public enum Status {
		CONFIRMED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String userId;

	private String username;

	private long productId;

	private String productName;

	private int quantity;

	private BigDecimal unitPrice;

	private BigDecimal totalAmount;

	@Enumerated(EnumType.STRING)
	private Status status;

	private Instant createdAt = Instant.now();

	protected Order() {
	}

	public Order(String userId, String username, long productId, String productName, int quantity,
			BigDecimal unitPrice) {
		this.userId = userId;
		this.username = username;
		this.productId = productId;
		this.productName = productName;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
		this.status = Status.CONFIRMED;
	}

	public Long getId() {
		return id;
	}

	public String getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public long getProductId() {
		return productId;
	}

	public String getProductName() {
		return productName;
	}

	public int getQuantity() {
		return quantity;
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Status getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
