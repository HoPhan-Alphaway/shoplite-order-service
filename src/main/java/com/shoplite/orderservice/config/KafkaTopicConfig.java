package com.shoplite.orderservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * The producer owns its topic. On Kubernetes the topic is also declared as a Strimzi
 * KafkaTopic in gitops; this bean is a no-op when the topic already exists.
 */
@Configuration
public class KafkaTopicConfig {

	@Bean
	NewTopic orderCreatedTopic(@Value("${shoplite.kafka.order-created-topic}") String name,
			@Value("${shoplite.kafka.replicas}") int replicas) {
		return TopicBuilder.name(name).partitions(3).replicas(replicas).build();
	}

}
