package com.smartpark.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartpark.api.entity.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuário pelo seu username. Essencial para a autenticação com Spring Security.
     * @param username O username do usuário a ser buscado.
     * @return Um Optional contendo o Usuario, se encontrado.
     */
    Optional<Usuario> findByUsername(String username);
}