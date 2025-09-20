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

import com.smartpark.api.entity.Vaga;
import com.smartpark.api.service.VagaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/vagas")
public class VagaController {

    @Autowired
    private VagaService vagaService;

    @PostMapping
    public ResponseEntity<Vaga> criarVaga(@RequestBody @Valid Vaga vaga) {
        Vaga novaVaga = vagaService.criarVaga(vaga);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaVaga);
    }

    @GetMapping
    public ResponseEntity<List<Vaga>> listarTodasVagas() {
        List<Vaga> vagas = vagaService.listarTodasVagas();
        return ResponseEntity.ok(vagas);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vaga> buscarVagaPorId(@PathVariable Long id) {
        Vaga vaga = vagaService.buscarVagaPorId(id);
        return ResponseEntity.ok(vaga);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vaga> atualizarVaga(@PathVariable Long id, @RequestBody @Valid Vaga vaga) {
        Vaga vagaAtualizada = vagaService.atualizarVaga(id, vaga);
        return ResponseEntity.ok(vagaAtualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarVaga(@PathVariable Long id) {
        vagaService.deletarVaga(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Integer> getTotalVagas() {
        int total = vagaService.getTotalVagas();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/ocupadas")
    public ResponseEntity<Integer> getVagasOcupadas() {
        int ocupadas = vagaService.getVagasOcupadas();
        return ResponseEntity.ok(ocupadas);
    }

    @GetMapping("/livres")
    public ResponseEntity<Integer> getVagasLivres() {
        int livres = vagaService.getVagasLivres();
        return ResponseEntity.ok(livres);
    }

    @GetMapping("/cheio")
    public ResponseEntity<Boolean> isEstacionamentoCheio() {
        boolean cheio = vagaService.isEstacionamentoCheio();
        return ResponseEntity.ok(cheio);
    }
}