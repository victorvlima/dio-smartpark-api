package com.smartpark.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartpark.api.entity.Estacionamento;
import com.smartpark.api.enums.StatusEstacionamento;

@Repository
public interface EstacionamentoRepository extends JpaRepository<Estacionamento, Long> {

    /**
     * Busca um registro de estacionamento ativo para um veículo específico pela placa.
     * Um registro é considerado ativo se o StatusEstacionamento for ATIVO.
     * @param placa A placa do veículo.
     * @param status O status do estacionamento (deve ser StatusEstacionamento.ATIVO).
     * @return Um Optional contendo o registro de Estacionamento ativo, se encontrado.
     */
    Optional<Estacionamento> findByVeiculoPlacaAndStatus(String placa, StatusEstacionamento status);
}