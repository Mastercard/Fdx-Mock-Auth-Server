package com.mastercard.fdx.mock.oauth2.server.config;

import static com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant.DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI;
import static com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant.OAUTH2_INTROSPECT_URI;
import static com.mastercard.fdx.mock.oauth2.server.common.ApplicationConstant.OAUTH2_PAR_URI;
import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import com.mastercard.fdx.mock.oauth2.server.security.user.FdxUserDetailsManager;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableWebSecurity
@Slf4j
public class DefaultSecurityConfig {

    @Bean
    SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        allowH2(http); //Since, using H2 as database.
        applySecurity(http);
        return http.build();
    }

    private static void applySecurity(HttpSecurity http) throws Exception {

        http
            .authorizeRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/health", "/startupStatus", "/shutdown", "/client/register", "/client/register/**", "/swagger-ui/index.html", "/v3/api-docs",
                            "/callback", "/callback/requests",
                            OAUTH2_PAR_URI, DEFAULT_TOKEN_REVOCATION_ENDPOINT_URI, "/error", "/favicon.ico",
                            OAUTH2_INTROSPECT_URI,"/introspect",
                            "/connect/register","/connect/register/**", "/add-accounts", "/fdx/v6/register", "/fdx/v6/register/**", "/fdx/v6/**").permitAll()
                    .anyRequest().authenticated())
                .csrf((csrf) -> csrf.disable())
                .formLogin(withDefaults())
                .headers((headers) ->
                        headers.httpStrictTransportSecurity(hstsConfig -> hstsConfig.maxAgeInSeconds(31536000).includeSubDomains(true)));
    }

    private static void allowH2(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers("/h2/**")
                .permitAll()
                .and()
                .headers()
                .frameOptions()
                .sameOrigin();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    @Profile("!local")
    UserDetailsService fdxUsers() {
        return new FdxUserDetailsManager();
    }

    @Bean
    @Profile("local")
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

}
