package com.example.hhimportdata

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Contact
import io.swagger.v3.oas.annotations.info.Info

@Configuration
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/login.html",
                        "/",
                        "/api/auth/telegram",
                        "/api/auth/me",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { it.disable() }
            .logout { }

        return http.build()
    }
}

@Configuration
@OpenAPIDefinition(
    info = Info(
        title = "HH Import Data API",
        version = "1.0",
        description = "Vacancies from HH.uz + Telegram authentication",
        contact = Contact(
            name = "Your Name",
            email = "your@email.com"
        )
    )
)
class OpenApiConfig