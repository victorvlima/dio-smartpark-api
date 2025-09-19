package com.smartpark.api.service;

import com.smartpark.api.entity.Vaga;
import com.smartpark.api.enums.StatusVaga;
import com.smartpark.api.exception.RecursoNaoEncontradoException;
import com.smartpark.api.exception.VagaIndisponivelException;
import com.smartpark.api.repository.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VagaService {

    @Autowired
    private VagaRepository vagaRepository;

    @Transactional(readOnly = true)
    public int getTotalVagas() {
        return (int) vagaRepository.count();
    }

    @Transactional(readOnly = true)
    public int getVagasOcupadas() {
        return vagaRepository.countByStatus(StatusVaga.OCUPADA);
    }

    @Transactional(readOnly = true)
    public int getVagasLivres() {
        return vagaRepository.countByStatus(StatusVaga.LIVRE);
    }

    @Transactional(readOnly = true)
    public boolean isEstacionamentoCheio() {
        return getVagasLivres() == 0;
    }

    @Transactional
    public Vaga encontrarProximaVagaLivre() {
        return vagaRepository.findTopByStatus(StatusVaga.LIVRE)
                .orElseThrow(() -> new VagaIndisponivelException("Não há vagas livres disponíveis no momento."));
    }

    @Transactional
    public Vaga ocuparVaga(Vaga vaga) {
        vaga.setStatus(StatusVaga.OCUPADA);
        return vagaRepository.save(vaga);
    }

    @Transactional
    public Vaga liberarVaga(Vaga vaga) {
        vaga.setStatus(StatusVaga.LIVRE);
        return vagaRepository.save(vaga);
    }

    @Transactional(readOnly = true)
    public List<Vaga> listarTodasVagas() {
        return vagaRepository.findAll();
    }

    @Transactional
    public Vaga criarVaga(Vaga vaga) {
        // Poderia ter uma validação para evitar números de vaga duplicados
        if (vagaRepository.findByNumero(vaga.getNumero()).isPresent()) {
            throw new IllegalArgumentException("Já existe uma vaga com o número " + vaga.getNumero());
        }
        vaga.setStatus(StatusVaga.LIVRE); // Nova vaga sempre começa livre
        return vagaRepository.save(vaga);
    }

    @Transactional(readOnly = true)
    public Vaga buscarVagaPorId(Long id) {
        return vagaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Vaga não encontrada com ID: " + id));
    }

    @Transactional
    public Vaga atualizarVaga(Long id, Vaga vagaAtualizada) {
        Vaga vagaExistente = buscarVagaPorId(id);
        vagaExistente.setNumero(vagaAtualizada.getNumero());
        vagaExistente.setStatus(vagaAtualizada.getStatus());
        return vagaRepository.save(vagaExistente);
    }

    @Transactional
    public void deletarVaga(Long id) {
        Vaga vagaExistente = buscarVagaPorId(id);
        // Adicionar validação se a vaga está ocupada antes de deletar
        if (vagaExistente.getStatus() == StatusVaga.OCUPADA) {
            throw new IllegalStateException("Não é possível deletar uma vaga ocupada.");
        }
        vagaRepository.delete(vagaExistente);
    }
}