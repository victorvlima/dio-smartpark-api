package com.smartpark.api.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartpark.api.dto.VeiculoEntradaDTO;
import com.smartpark.api.entity.Veiculo;
import com.smartpark.api.enums.StatusEstacionamento;
import com.smartpark.api.exception.RecursoNaoEncontradoException;
import com.smartpark.api.repository.VeiculoRepository;

@Service
public class VeiculoService {

    @Autowired
    private VeiculoRepository veiculoRepository;

    @Transactional(readOnly = true)
    public Optional<Veiculo> findByPlaca(String placa) {
        return veiculoRepository.findByPlaca(placa);
    }

    @Transactional
    public Veiculo buscarOuCriarVeiculo(VeiculoEntradaDTO dto) {
        return veiculoRepository.findByPlaca(dto.getPlaca())
                .orElseGet(() -> {
                    Veiculo novoVeiculo = new Veiculo();
                    novoVeiculo.setPlaca(dto.getPlaca());
                    novoVeiculo.setMarca(dto.getMarca());
                    novoVeiculo.setModelo(dto.getModelo());
                    novoVeiculo.setCor(dto.getCor());
                    novoVeiculo.setTipoVeiculo(dto.getTipoVeiculo());
                    return veiculoRepository.save(novoVeiculo);
                });
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listarTodosVeiculos() {
        return veiculoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Veiculo buscarVeiculoPorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado com ID: " + id));
    }

    @Transactional
    public Veiculo criarVeiculo(Veiculo veiculo) {
        if (veiculoRepository.existsByPlaca(veiculo.getPlaca())) {
            throw new IllegalArgumentException("Veículo com a placa " + veiculo.getPlaca() + " já existe.");
        }
        return veiculoRepository.save(veiculo);
    }

    @Transactional
    public Veiculo atualizarVeiculo(Long id, Veiculo veiculoAtualizado) {
        Veiculo veiculoExistente = buscarVeiculoPorId(id); // Garante que o veículo existe
        veiculoExistente.setMarca(veiculoAtualizado.getMarca());
        veiculoExistente.setModelo(veiculoAtualizado.getModelo());
        veiculoExistente.setCor(veiculoAtualizado.getCor());
        veiculoExistente.setTipoVeiculo(veiculoAtualizado.getTipoVeiculo());
        return veiculoRepository.save(veiculoExistente);
    }

    @Transactional
    public void deletarVeiculo(Long id) {
        Veiculo veiculoExistente = buscarVeiculoPorId(id);
        // Adicionar validação se o veículo tem estacionamentos ativos
        if (!veiculoExistente.getEstacionamentos().stream().filter(e -> e.getStatus() == StatusEstacionamento.ATIVO).toList().isEmpty()) {
            throw new IllegalStateException("Não é possível deletar um veículo com estacionamento ativo.");
        }
        veiculoRepository.delete(veiculoExistente);
    }
}