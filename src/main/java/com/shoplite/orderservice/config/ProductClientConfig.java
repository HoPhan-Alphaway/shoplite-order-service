package com.shoplite.orderservice.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;

@Configuration
public class ProductClientConfig {

	/** The token belongs to order-service itself, not to the end user who placed the order. */
	private static final Authentication SERVICE_PRINCIPAL = new AnonymousAuthenticationToken("order-service",
			"order-service", AuthorityUtils.createAuthorityList("ROLE_SERVICE"));

	@Bean
	OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository registrations,
			OAuth2AuthorizedClientService clientService) {
		var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clientService);
		manager.setAuthorizedClientProvider(OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build());
		return manager;
	}

	@Bean
	RestClient productRestClient(OAuth2AuthorizedClientManager authorizedClientManager,
			@Value("${shoplite.product-service-url}") String baseUrl) {
		// Adds "Authorization: Bearer <service token>", fetching/refreshing it from Keycloak as needed.
		var oauth2 = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
		oauth2.setClientRegistrationIdResolver(request -> "product-api");
		oauth2.setPrincipalResolver(request -> SERVICE_PRINCIPAL);

		var requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(Duration.ofSeconds(2));
		requestFactory.setReadTimeout(Duration.ofSeconds(3));

		return RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).requestInterceptor(oauth2).build();
	}

}
