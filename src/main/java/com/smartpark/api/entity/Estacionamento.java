package com.smartpark.api.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartpark.api.enums.StatusEstacionamento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_estacionamentos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Estacionamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @ManyToOne
    @JoinColumn(name = "vaga_id", nullable = false)
    private Vaga vaga;

    @Column(nullable = false)
    private LocalDateTime dataHoraEntrada;

    @Column
    private LocalDateTime dataHoraSaida; // Nullable, preenchido na saída

    @Column(precision = 10, scale = 2) // Ex: 99999999.99
    private BigDecimal valorCobrado; // Nullable, calculado na saída

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEstacionamento status; // ATIVO ou FINALIZADO

    // Construtor para entrada de veículo
    public Estacionamento(Veiculo veiculo, Vaga vaga, LocalDateTime dataHoraEntrada, StatusEstacionamento status) {
        this.veiculo = veiculo;
        this.vaga = vaga;
        this.dataHoraEntrada = dataHoraEntrada;
        this.status = status;
    }
}