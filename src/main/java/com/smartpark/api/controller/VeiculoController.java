package com.smartpark.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartpark.api.entity.Veiculo;
import com.smartpark.api.service.VeiculoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/veiculos")
public class VeiculoController {

    @Autowired
    private VeiculoService veiculoService;

    @PostMapping
    public ResponseEntity<Veiculo> criarVeiculo(@RequestBody @Valid Veiculo veiculo) {
        Veiculo novoVeiculo = veiculoService.criarVeiculo(veiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoVeiculo);
    }

    @GetMapping
    public ResponseEntity<List<Veiculo>> listarTodosVeiculos() {
        List<Veiculo> veiculos = veiculoService.listarTodosVeiculos();
        return ResponseEntity.ok(veiculos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Veiculo> buscarVeiculoPorId(@PathVariable Long id) {
        Veiculo veiculo = veiculoService.buscarVeiculoPorId(id);
        return ResponseEntity.ok(veiculo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Veiculo> atualizarVeiculo(@PathVariable Long id, @RequestBody @Valid Veiculo veiculo) {
        Veiculo veiculoAtualizado = veiculoService.atualizarVeiculo(id, veiculo);
        return ResponseEntity.ok(veiculoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarVeiculo(@PathVariable Long id) {
        veiculoService.deletarVeiculo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<Veiculo> buscarVeiculoPorPlaca(@PathVariable String placa) {
        // Retorna 200 OK se encontrar, 404 Not Found se não
        return veiculoService.findByPlaca(placa)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}