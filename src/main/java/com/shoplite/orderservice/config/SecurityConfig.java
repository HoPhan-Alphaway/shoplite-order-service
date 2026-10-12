package com.shoplite.orderservice.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Defense in depth: Kong already verified the token at the edge, but the service verifies it
 * again (signature via Keycloak JWKS) and enforces role-based authorization itself.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(auth -> auth.requestMatchers("/actuator/**")
			.permitAll()
			.anyRequest()
			.authenticated())
			.oauth2ResourceServer(rs -> rs.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRoles())))
			.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.csrf(csrf -> csrf.disable());
		return http.build();
	}

	/** Maps Keycloak realm roles ({@code realm_access.roles}) to {@code ROLE_*} authorities. */
	static JwtAuthenticationConverter keycloakRoles() {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setPrincipalClaimName("preferred_username");
		converter.setJwtGrantedAuthoritiesConverter(SecurityConfig::realmRoles);
		return converter;
	}

	@SuppressWarnings("unchecked")
	private static Collection<GrantedAuthority> realmRoles(Jwt jwt) {
		Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
		if (realmAccess == null || !(realmAccess.get("roles") instanceof List<?> roles)) {
			return List.of();
		}
		return ((List<String>) roles).stream()
			.map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
			.toList();
	}

}
