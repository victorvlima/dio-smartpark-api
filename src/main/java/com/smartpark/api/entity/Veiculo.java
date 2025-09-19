package com.smartpark.api.entity;

import java.util.List;

import com.smartpark.api.enums.TipoVeiculo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_veiculos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String placa;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 50)
    private String modelo;

    @Column(nullable = false, length = 30)
    private String cor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVeiculo tipoVeiculo;

    @OneToMany(mappedBy = "veiculo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Estacionamento> estacionamentos; // Histórico de estacionamentos

    // Construtor sem a lista de estacionamentos, útil para criação inicial
    public Veiculo(Long id, String placa, String marca, String modelo, String cor, TipoVeiculo tipoVeiculo) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.cor = cor;
        this.tipoVeiculo = tipoVeiculo;
    }
}