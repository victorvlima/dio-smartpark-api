package com.smartpark.api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartpark.api.entity.Usuario;
import com.smartpark.api.repository.UsuarioRepository;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Cria um usuário ADMIN se ele não existir
            if (usuarioRepository.findByUsername("admin").isEmpty()) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123")); // Criptografa a senha
                admin.setRole("ADMIN");
                usuarioRepository.save(admin);
                System.out.println("Usuário 'admin' criado com sucesso!");
            }

            // Cria um usuário USER se ele não existir
            if (usuarioRepository.findByUsername("user").isEmpty()) {
                Usuario user = new Usuario();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123")); // Criptografa a senha
                user.setRole("USER");
                usuarioRepository.save(user);
                System.out.println("Usuário 'user' criado com sucesso!");
            }
        };
    }
}