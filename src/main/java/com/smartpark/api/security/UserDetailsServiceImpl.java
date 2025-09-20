package com.smartpark.api.security; // Ou .security, dependendo da sua organização

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smartpark.api.entity.Usuario;
import com.smartpark.api.repository.UsuarioRepository;

// Não precisa de @Service se for injetada diretamente como um Bean no SecurityConfig,
// mas adicionar @Service é uma boa prática para caso ela seja injetada em outros lugares.
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Usuario> usuarioOptional = usuarioRepository.findByUsername(username);
        return usuarioOptional.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }
}