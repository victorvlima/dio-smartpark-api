package com.smartpark.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartpark.api.entity.Vaga;
import com.smartpark.api.enums.StatusVaga;

@Repository
public interface VagaRepository extends JpaRepository<Vaga, Long> {

    /**
     * Busca uma vaga pelo seu número identificador.
     * @param numero O número da vaga a ser buscada.
     * @return Um Optional contendo a Vaga, se encontrada.
     */
    Optional<Vaga> findByNumero(String numero);

    /**
     * Conta o número de vagas com um determinado status.
     * @param status O status da vaga (LIVRE, OCUPADA).
     * @return O número de vagas com o status especificado.
     */
    int countByStatus(StatusVaga status);

    /**
     * Encontra a primeira vaga com o status especificado. Útil para pegar a primeira vaga livre.
     * @param status O status da vaga a ser buscada.
     * @return Um Optional contendo a primeira Vaga encontrada com o status.
     */
    Optional<Vaga> findTopByStatus(StatusVaga status); // findFirstByStatus também funcionaria
}