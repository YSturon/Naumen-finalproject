package ru.sturov.testing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Конфигурация безопасности приложения.
 *
 * <p>Определяет правила доступа к страницам, форму входа, выход из системы
 * и способ хеширования паролей пользователей.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Создает цепочку фильтров Spring Security.
     *
     * <p>Публичными остаются страницы входа, регистрации и статические ресурсы.
     * Раздел администрирования доступен только пользователям с ролью {@code ADMIN},
     * остальные страницы требуют авторизации.</p>
     *
     * @param http объект настройки HTTP-безопасности
     * @return настроенная цепочка фильтров безопасности
     * @throws Exception если конфигурация Spring Security не может быть построена
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/js/**", "/register", "/login").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/tests", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .build();
    }

    /**
     * Создает кодировщик паролей.
     *
     * @return реализация BCrypt для безопасного хранения паролей
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
