package com.smartpark.api.dto;

import com.smartpark.api.enums.TipoVeiculo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoEntradaDTO {

    @NotBlank(message = "A placa é obrigatória.")
    @Pattern(regexp = "^[A-Z]{3}[0-9][0-9A-Z][0-9]{2}$", message = "Formato de placa inválido (ex: ABC1B23 ou ABC1234).")
    @Size(min = 7, max = 7, message = "A placa deve ter 7 caracteres.")
    private String placa;

    @NotBlank(message = "A marca é obrigatória.")
    @Size(max = 50, message = "A marca deve ter no máximo 50 caracteres.")
    private String marca;

    @NotBlank(message = "O modelo é obrigatório.")
    @Size(max = 50, message = "O modelo deve ter no máximo 50 caracteres.")
    private String modelo;

    @NotBlank(message = "A cor é obrigatória.")
    @Size(max = 30, message = "A cor deve ter no máximo 30 caracteres.")
    private String cor;

    @NotNull(message = "O tipo de veículo é obrigatório.")
    private TipoVeiculo tipoVeiculo;
}