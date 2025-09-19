package com.smartpark.api.entity;

import java.util.List;

import com.smartpark.api.enums.StatusVaga;

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
@Table(name = "tb_vagas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vaga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String numero; // Número ou identificador da vaga (ex: "A1", "B2")

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVaga status;

    @OneToMany(mappedBy = "vaga", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Estacionamento> estacionamentos; // Histórico de veículos que estacionaram nesta vaga

    // Construtor sem a lista de estacionamentos
    public Vaga(Long id, String numero, StatusVaga status) {
        this.id = id;
        this.numero = numero;
        this.status = status;
    }
}