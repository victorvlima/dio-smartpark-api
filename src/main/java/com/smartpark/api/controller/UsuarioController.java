package com.smartpark.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartpark.api.dto.UsuarioRequestDTO;
import com.smartpark.api.dto.UsuarioResponseDTO;
import com.smartpark.api.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // Apenas ADMIN pode criar usuários
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO novoUsuario = usuarioService.criarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    // Apenas ADMIN pode listar todos os usuários
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodosUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarTodosUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    // ADMIN pode buscar qualquer usuário por ID
    // USER pode buscar apenas a si mesmo por ID (precisaria de lógica adicional no service para verificar o ID do usuário logado)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Por simplicidade, apenas ADMIN pode buscar por ID.
                                       // Para permitir que USER busque a si mesmo: "hasRole('ADMIN') or #id == authentication.principal.id"
                                       // onde authentication.principal.id precisa ser o ID do usuário logado.
    public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(@PathVariable Long id) {
        UsuarioResponseDTO usuario = usuarioService.buscarUsuarioPorId(id);
        return ResponseEntity.ok(usuario);
    }

    // Apenas ADMIN pode atualizar usuários (ou USER pode atualizar a si mesmo)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Similar ao GET por ID, USER poderia atualizar a si mesmo
    public ResponseEntity<UsuarioResponseDTO> atualizarUsuario(@PathVariable Long id, @RequestBody @Valid UsuarioRequestDTO dto) {
        UsuarioResponseDTO usuarioAtualizado = usuarioService.atualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    // Apenas ADMIN pode deletar usuários
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.deletarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}