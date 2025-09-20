package com.smartpark.api.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permite usar @PreAuthorize nos métodos dos controllers/services
public class SecurityConfig {

    // Define nosso UserDetailsService customizado
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl(); // Implementaremos esta classe a seguir
    }

    // Configura o PasswordEncoder para criptografar senhas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configura o provedor de autenticação que usa nosso UserDetailsService e PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    // Configuração principal da cadeia de filtros de segurança
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Desabilita CSRF para APIs REST
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita CORS
                .authorizeHttpRequests(authorize -> authorize
                        // Permite acesso irrestrito ao Swagger UI e documentação OpenAPI
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Permite acesso irrestrito para endpoints de criação de usuário (se houver, ou para teste inicial)
                        // .requestMatchers("/api/v1/usuarios/registrar").permitAll() // Exemplo, se você tiver um endpoint público para registro
                        // Exige autenticação para todos os outros endpoints da API
                        .requestMatchers("/api/v1/**").authenticated()
                        // Qualquer outra requisição que não foi explicitamente permitida ou negada
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults()) // Habilita autenticação Basic (username/password no header)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // API REST é stateless

        return http.build();
    }

    // Configuração de CORS para permitir requisições de outras origens
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*")); // Permite todas as origens (ajustar para produção)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")); // Métodos permitidos
        configuration.setAllowedHeaders(List.of("*")); // Permite todos os headers
        configuration.setAllowCredentials(true); // Permite credenciais (cookies, HTTP authentication)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a configuração para todas as rotas
        return source;
    }
}