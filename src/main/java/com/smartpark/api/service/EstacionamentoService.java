package com.smartpark.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional; // Importe Optional

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpark.api.entity.Estacionamento;
import com.smartpark.api.repository.EstacionamentoRepository;

@Service // Indica que esta classe é um componente de serviço do Spring
@Transactional // Define o contexto transacional para os métodos da classe
public class EstacionamentoService {

    private final EstacionamentoRepository estacionamentoRepository;

    // Injeção de dependência do EstacionamentoRepository via construtor
    public EstacionamentoService(EstacionamentoRepository estacionamentoRepository) {
        this.estacionamentoRepository = estacionamentoRepository;
    }

    @Transactional(readOnly = true) // Otimiza a transação para leitura
    public List<Estacionamento> findAll() {
        return estacionamentoRepository.findAll();
    }

    @Transactional(readOnly = true) // Otimiza a transação para leitura
    public Estacionamento findById(Long id) {
        return estacionamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estacionamento não encontrado com o ID: " + id));
    }

    @Transactional
    public Estacionamento create(Estacionamento estacionamentoCreate) {
        estacionamentoCreate.setDataHoraEntrada(LocalDateTime.now());
        estacionamentoRepository.save(estacionamentoCreate);
        return estacionamentoCreate;
    }

    @Transactional
    public void delete(Long id) {
        estacionamentoRepository.deleteById(id);
    }

    @Transactional
    public Estacionamento update(Long id, Estacionamento estacionamentoUpdate) {
        Optional<Estacionamento> optionalEstacionamento = estacionamentoRepository.findById(id);
        if (optionalEstacionamento.isPresent()) {
            Estacionamento existingEstacionamento = optionalEstacionamento.get();
            existingEstacionamento.setPlaca(estacionamentoUpdate.getPlaca());
            existingEstacionamento.setCor(estacionamentoUpdate.getCor());
            existingEstacionamento.setModelo(estacionamentoUpdate.getModelo());
            existingEstacionamento.setEstado(estacionamentoUpdate.getEstado());
            return estacionamentoRepository.save(existingEstacionamento);
        }
        throw new RuntimeException("Estacionamento não encontrado com o ID: " + id);
    }
}