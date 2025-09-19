package com.smartpark.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartpark.api.enums.StatusEstacionamento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstacionamentoResponseDTO {
    private Long id;
    private String placaVeiculo;
    private String numeroVaga;
    private LocalDateTime dataHoraEntrada;
    private LocalDateTime dataHoraSaida;
    private BigDecimal valorCobrado;
    private StatusEstacionamento status;
}