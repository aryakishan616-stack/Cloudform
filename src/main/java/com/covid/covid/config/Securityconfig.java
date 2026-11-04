package com.covid.covid.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.beans.factory.annotation.Value;
import com.covid.covid.repository.Userrepository;

import java.util.List;

@Configuration
public class Securityconfig {

    private final List<String> allowedOrigins;

    public Securityconfig(
            @Value("${app.cors.allowed-origins:http://localhost:5500,http://127.0.0.1:5500,http://localhost:5501,http://127.0.0.1:5501}")
            List<String> allowedOrigins) {
        this.allowedOrigins = List.copyOf(allowedOrigins);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(Userrepository userRepository) {
        return username -> {
        com.covid.covid.model.User user =
                userRepository.findByUsernameIgnoreCase(username.trim().toLowerCase(java.util.Locale.ROOT));
        if (user == null) {
                throw new UsernameNotFoundException("User not found");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles("USER")
                .build();
        };
}

       @Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN", "Authorization", "X-Requested-With"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, SecurityContextRepository securityContextRepository)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(
                                "/api/user/login",
                                "/api/user/signup",
                                "/api/covid/add"))
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/user/login",
                                "/api/user/signup",
                                "/api/user/csrf")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/covid/add").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/api/covid/**")
                        .permitAll()
                        .requestMatchers(
                                "/index2.html",
                                "/index2.js",
                                "/style2.css",
                                "/style.css")
                        .permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/covid/country/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/covid/country/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/*.js", "/*.css")
                        .authenticated()
                        .anyRequest().permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authenticationException) ->
                                response.sendRedirect("/index2.html")))
                .logout(logout -> logout
                .logoutUrl("/api/user/logout")
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)));

        return http.build();
        }
}