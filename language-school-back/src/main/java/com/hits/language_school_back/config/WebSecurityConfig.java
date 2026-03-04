package com.hits.language_school_back.config;

import com.hits.language_school_back.filter.JwtAuthentificationFilter;
import com.hits.language_school_back.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@AllArgsConstructor
@Configuration
public class WebSecurityConfig extends WebSecurityConfiguration {
    @Lazy
    private final JwtAuthentificationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                        .requestMatchers("/v2/api-docs",
                                "/configuration/ui",
                                "/swagger-resources/**",
                                "/configuration/security",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/error").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/request/**").hasAuthority(Role.STUDENT.toString())
//                        .requestMatchers(HttpMethod.GET, "/{userId}/grant-role").hasAuthority(Role.DEANERY.toString())
//                        .requestMatchers(HttpMethod.PATCH, "/request/{id}/status").hasAnyAuthority(Role.DEANERY.toString(), Role.ADMIN.toString())
//                        .requestMatchers(HttpMethod.PUT, "/api/users/{userId}/grant-dean-role").hasAuthority(Role.ADMIN.toString())
//                        .requestMatchers(HttpMethod.GET, "/request/info/**").hasAnyAuthority(Role.DEANERY.toString(), Role.STUDENT.toString(), Role.TEACHER.toString())
//                        .requestMatchers(HttpMethod.GET, "/request/list").hasAnyAuthority(Role.DEANERY.toString(), Role.STUDENT.toString(), Role.TEACHER.toString(), Role.ADMIN.toString())
//                        .requestMatchers(HttpMethod.GET, "/users").hasAnyAuthority(Role.DEANERY.toString(), Role.ADMIN.toString(), Role.TEACHER.toString())

                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserService userService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:8081",
                "http://185.76.242.253:8081",
                "http://localhost:4201",
                "http://localhost:4200",
                "http://localhost:8080"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(List<AuthenticationProvider> authenticationProviders) {
        return new ProviderManager(authenticationProviders);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}