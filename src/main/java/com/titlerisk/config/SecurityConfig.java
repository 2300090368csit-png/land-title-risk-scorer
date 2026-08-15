package com.titlerisk.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Wires up session-based authentication for the app.
 *
 * <p>The design is deliberately simple: the static frontend (every *.html
 * file, css/, js/) is always servable — a browser has to be able to load
 * login.html itself before it can log in. Authentication is enforced at the
 * data layer instead: every {@code /api/parcels/**} and {@code /api/history/**}
 * call requires a valid session, and each page's own JavaScript checks
 * {@code GET /api/auth/me} on load and redirects to login.html if that
 * comes back 401. See {@code common.js}'s {@code requireAuth()}.</p>
 *
 * <p><b>CSRF is intentionally disabled.</b> This API is only ever called by
 * same-origin {@code fetch()} requests from our own frontend — there's no
 * third-party or cross-origin client to protect against forging a request
 * with a stolen session cookie from another site. A production app serving
 * external clients would enable it (e.g. with
 * {@code CookieCsrfTokenRepository}); for this single-origin app it would
 * only add complexity without closing a real gap.</p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Deliberately no explicit AuthenticationProvider bean here: with exactly one
    // UserDetailsService bean (CustomUserDetailsService) and one PasswordEncoder
    // bean on the classpath, Spring Boot's auto-configuration wires a
    // DaoAuthenticationProvider using both automatically. Defining one by hand
    // as well would just shadow that and log a warning about it.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // H2 console renders itself inside a frame; allow same-origin framing for it only.
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/logout").permitAll()
                        .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        // Spring's default handling of a thrown ResponseStatusException (e.g. our
                        // 409 on a duplicate username, or 401 on bad credentials) internally forwards
                        // the request to /error to build the JSON body. That forward re-enters this
                        // same filter chain — without this line it would hit anyRequest().authenticated()
                        // on an unauthenticated request and get overwritten with a generic 401 from the
                        // entry point below, silently replacing the real error message and status.
                        .requestMatchers("/error").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    // A protected endpoint hit without a valid session gets a plain 401 JSON body,
                    // never Spring Security's default login-page redirect — the frontend's
                    // requireAuth() helper looks for exactly this status to send the user to login.html.
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Not authenticated. Please log in.\"}");
                }));
        return http.build();
    }
}
