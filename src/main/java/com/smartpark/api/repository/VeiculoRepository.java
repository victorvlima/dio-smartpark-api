package com.smartpark.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartpark.api.entity.Veiculo;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    /**
     * Busca um veículo pela sua placa.
     * @param placa A placa do veículo a ser buscado.
     * @return Um Optional contendo o Veiculo, se encontrado.
     */
    Optional<Veiculo> findByPlaca(String placa);

    /**
     * Verifica se um veículo com a placa especificada já existe.
     * @param placa A placa a ser verificada.
     * @return true se um veículo com a placa existe, false caso contrário.
     */
    boolean existsByPlaca(String placa);
}