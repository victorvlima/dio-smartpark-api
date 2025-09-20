package com.smartpark.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartpark.api.dto.EstacionamentoResponseDTO;
import com.smartpark.api.dto.VeiculoEntradaDTO;
import com.smartpark.api.service.EstacionamentoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/estacionamentos")
public class EstacionamentoController {

    @Autowired
    private EstacionamentoService estacionamentoService;

    @PostMapping("/entrar")
    public ResponseEntity<EstacionamentoResponseDTO> registrarEntrada(@RequestBody @Valid VeiculoEntradaDTO veiculoDto) {
        EstacionamentoResponseDTO response = estacionamentoService.registrarEntrada(veiculoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/sair/{placa}")
    public ResponseEntity<EstacionamentoResponseDTO> registrarSaida(@PathVariable String placa) {
        EstacionamentoResponseDTO response = estacionamentoService.registrarSaida(placa);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<EstacionamentoResponseDTO>> listarEstacionamentosAtivos() {
        List<EstacionamentoResponseDTO> ativos = estacionamentoService.listarEstacionamentosAtivos();
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/historico")
    public ResponseEntity<List<EstacionamentoResponseDTO>> listarHistoricoEstacionamentos() {
        List<EstacionamentoResponseDTO> historico = estacionamentoService.listarHistoricoEstacionamentos();
        return ResponseEntity.ok(historico);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstacionamentoResponseDTO> buscarEstacionamentoPorId(@PathVariable Long id) {
        EstacionamentoResponseDTO estacionamento = estacionamentoService.buscarEstacionamentoPorId(id);
        return ResponseEntity.ok(estacionamento);
    }
}