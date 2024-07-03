package com.mastercard.fdx.mock.oauth2.server.config;

import com.mastercard.fdx.mock.oauth2.server.security.user.FdxUserDetailsManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Slf4j
public class DefaultSecurityConfig {

    @Bean(name = "NonMTLSFilterChain")
    SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers("/h2/**","/h2-console/**", "/health", "/startupStatus", "/shutdown",
                        "/callback", "/callback/requests",
                        "/error", "/favicon.ico",
                        "/swagger-ui/index.html", "/v3/api-docs","/error", "/favicon.ico",
                        "/.well-known/openid-configuration", "/oauth2/jwks", "/v1/browser-callback"
                ,"/oauth2/authorize","/consent")
                .permitAll();
        setHttpHeader(http);
        return http.build();
    }

    @Bean(name = "CustomMTLSFilterChain")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    SecurityFilterChain privateApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .requestMatchers("/fdx/v6/**", "/oauth2/token", "/oauth2/par", "/oauth2/introspect")
                .authenticated().and()
                .csrf(csrf -> csrf.disable())
                .x509()
                .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
                .userDetailsService(userDetailsService());

        http
                .authorizeRequests()
                .requestMatchers("/login") // Apply form login only for form-login-url
                .authenticated().and()
                .formLogin()
                .permitAll()
                .and().userDetailsService(fdxUsers());

        setHttpHeader(http);
        return http.build();
    }

    void setHttpHeader(HttpSecurity http) throws Exception {
        http.headers((headers) ->
                headers.httpStrictTransportSecurity(hstsConfig -> hstsConfig.maxAgeInSeconds(31536000).includeSubDomains(true))
                        .frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin()));
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    @Profile("local")
    UserDetailsService fdxUsers() {
        return new FdxUserDetailsManager();
    }

    @Bean
    @Profile("!local")
    UserDetailsService localUsers() {
        UserDetails user = User.withUsername("fdxuser")
                .password("password")
                .roles("USER")
                .passwordEncoder(new BCryptPasswordEncoder()::encode)
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    AuthenticationEventPublisher myPublisher() {
        return new AuthenticationEventPublisher() {

            @Override
            public void publishAuthenticationSuccess(Authentication authentication) {
                log.info("Authentication Success: " + authentication.getName());
            }

            @Override
            public void publishAuthenticationFailure(AuthenticationException ex, Authentication authentication) {
                log.error("Authentication Failure: " + ex.getLocalizedMessage(), ex);
            }
        };

    }

    @Bean
    UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(final String username) {
                log.info("UserDetailsService username={}", username);

                if (username == null || username.length() == 0) {
                    throw new UsernameNotFoundException(username);
                }

                User user = new User(username, "", AuthorityUtils.createAuthorityList("ROLE_SSL_USER"));
                return user;
            }
        };
    }
}
