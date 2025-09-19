package com.smartpark.api.entity;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password; // Senha já criptografada

    @Column(nullable = false, length = 20)
    private String role; // Ex: "ADMIN", "USER" (poderia ser uma entidade separada para múltiplos roles)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Retorna as autoridades (roles) do usuário.
        // Neste exemplo, cada usuário tem apenas uma role.
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Conta não expirada
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Conta não bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credenciais não expiradas
    }

    @Override
    public boolean isEnabled() {
        return true; // Conta habilitada
    }
}