package com.example.demo.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/login").permitAll() // 公開エンドポイントのパスを設定
                    .anyRequest().authenticated()
            }
            .formLogin { login ->
                login.permitAll() // Spring Securityデフォルトのログインページを有効化
            }
            .logout { logout ->
                logout.permitAll() // Spring Securityデフォルトのログアウトページを有効化
            }
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) //CSRF対策
            }
        return http.build()
    }
}
