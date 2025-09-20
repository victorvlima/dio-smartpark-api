package com.smartpark.api.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpark.api.dto.UsuarioRequestDTO;
import com.smartpark.api.dto.UsuarioResponseDTO;
import com.smartpark.api.entity.Usuario;
import com.smartpark.api.exception.RecursoNaoEncontradoException;
import com.smartpark.api.repository.UsuarioRepository;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO dto) {
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' já existe.");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(dto.getUsername());
        novoUsuario.setPassword(passwordEncoder.encode(dto.getPassword())); // Criptografa a senha
        novoUsuario.setRole(dto.getRole().toUpperCase()); // Garante que a role seja em maiúsculas

        novoUsuario = usuarioRepository.save(novoUsuario);
        return toUsuarioResponseDTO(novoUsuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::toUsuarioResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id));
        return toUsuarioResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarUsuarioPorUsername(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com username: " + username));
        return toUsuarioResponseDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(Long id, UsuarioRequestDTO dto) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id));

        // Verifica se o username está sendo alterado para um existente (que não seja o próprio)
        if (!usuarioExistente.getUsername().equals(dto.getUsername()) && usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' já existe.");
        }

        usuarioExistente.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(dto.getPassword())); // Atualiza a senha se fornecida
        }
        usuarioExistente.setRole(dto.getRole().toUpperCase());

        usuarioExistente = usuarioRepository.save(usuarioExistente);
        return toUsuarioResponseDTO(usuarioExistente);
    }

    @Transactional
    public void deletarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // Método utilitário para converter Entidade em DTO
    private UsuarioResponseDTO toUsuarioResponseDTO(Usuario usuario) {
        return new UsuarioResponseDTO(usuario.getId(), usuario.getUsername(), usuario.getRole());
    }
}