package com.smartpark.api.service;

import com.smartpark.api.dto.EstacionamentoResponseDTO;
import com.smartpark.api.dto.VeiculoEntradaDTO;
import com.smartpark.api.entity.Estacionamento;
import com.smartpark.api.entity.Vaga;
import com.smartpark.api.entity.Veiculo;
import com.smartpark.api.enums.StatusEstacionamento;
import com.smartpark.api.exception.RecursoNaoEncontradoException;
import com.smartpark.api.exception.VeiculoJaEstacionadoException;
import com.smartpark.api.repository.EstacionamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EstacionamentoService {

    @Autowired
    private EstacionamentoRepository estacionamentoRepository;

    @Autowired
    private VeiculoService veiculoService; // Usaremos para buscar/criar veículos

    @Autowired
    private VagaService vagaService; // Usaremos para gerenciar vagas

    // Regras de tarifação
    private static final BigDecimal TARIFA_PRIMEIRA_HORA = new BigDecimal("5.00");
    private static final BigDecimal TARIFA_HORA_ADICIONAL = new BigDecimal("2.00");
    private static final BigDecimal TARIFA_DIARIA = new BigDecimal("25.00"); // Exemplo: para mais de X horas ou por dia

    @Transactional
    public EstacionamentoResponseDTO registrarEntrada(VeiculoEntradaDTO veiculoDto) {
        // 1. Verificar se o veículo já está estacionado
        estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculoDto.getPlaca(), StatusEstacionamento.ATIVO)
                .ifPresent(e -> {
                    throw new VeiculoJaEstacionadoException("Veículo com placa " + veiculoDto.getPlaca() + " já está estacionado na vaga " + e.getVaga().getNumero() + ".");
                });

        // 2. Encontrar e ocupar uma vaga livre
        Vaga vagaLivre = vagaService.encontrarProximaVagaLivre();
        vagaService.ocuparVaga(vagaLivre); // Atualiza o status da vaga no DB

        // 3. Buscar ou criar o veículo
        Veiculo veiculo = veiculoService.buscarOuCriarVeiculo(veiculoDto);

        // 4. Registrar a entrada
        Estacionamento estacionamento = new Estacionamento(veiculo, vagaLivre, LocalDateTime.now(), StatusEstacionamento.ATIVO);
        estacionamento = estacionamentoRepository.save(estacionamento);

        return toEstacionamentoResponseDTO(estacionamento);
    }

    @Transactional
    public EstacionamentoResponseDTO registrarSaida(String placa) {
        // 1. Encontrar o registro de estacionamento ativo
        Estacionamento estacionamento = estacionamentoRepository.findByVeiculoPlacaAndStatus(placa, StatusEstacionamento.ATIVO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo com placa " + placa + " não encontrado no estacionamento ou já saiu."));

        // 2. Registrar a hora de saída
        estacionamento.setDataHoraSaida(LocalDateTime.now());

        // 3. Calcular o valor a ser cobrado
        BigDecimal valor = calcularValor(estacionamento.getDataHoraEntrada(), estacionamento.getDataHoraSaida());
        estacionamento.setValorCobrado(valor);

        // 4. Finalizar o registro de estacionamento
        estacionamento.setStatus(StatusEstacionamento.FINALIZADO);
        estacionamento = estacionamentoRepository.save(estacionamento);

        // 5. Liberar a vaga
        vagaService.liberarVaga(estacionamento.getVaga());

        return toEstacionamentoResponseDTO(estacionamento);
    }

    @Transactional(readOnly = true)
    public List<EstacionamentoResponseDTO> listarEstacionamentosAtivos() {
        return estacionamentoRepository.findAll().stream()
                .filter(e -> e.getStatus() == StatusEstacionamento.ATIVO)
                .map(this::toEstacionamentoResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EstacionamentoResponseDTO> listarHistoricoEstacionamentos() {
        return estacionamentoRepository.findAll().stream()
                .map(this::toEstacionamentoResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EstacionamentoResponseDTO buscarEstacionamentoPorId(Long id) {
        return estacionamentoRepository.findById(id)
                .map(this::toEstacionamentoResponseDTO)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro de estacionamento não encontrado com ID: " + id));
    }


    /**
     * Calcula o valor a ser cobrado com base no tempo de permanência.
     * Exemplo de lógica de tarifação:
     * - Primeira hora: R$ 5,00
     * - Horas adicionais: R$ 2,00 por hora ou fração de hora
     * - Diária: R$ 25,00 (após 10 horas, por exemplo)
     * @param entrada Data e hora de entrada.
     * @param saida Data e hora de saída.
     * @return O valor total a ser cobrado.
     */
    private BigDecimal calcularValor(LocalDateTime entrada, LocalDateTime saida) {
        if (entrada == null || saida == null || saida.isBefore(entrada)) {
            throw new IllegalArgumentException("Datas de entrada e saída inválidas para cálculo.");
        }

        Duration duracao = Duration.between(entrada, saida);
        long minutos = duracao.toMinutes();

        if (minutos <= 15) { // Exemplo: Carência de 15 minutos
            return BigDecimal.ZERO;
        }

        BigDecimal valorTotal = BigDecimal.ZERO;

        // Converter minutos para horas e frações
        double horasCompletas = Math.floor(minutos / 60.0);
        double fracaoDeHora = (minutos % 60) / 60.0;

        if (horasCompletas == 0 && minutos > 0) { // Menos de 1 hora, mas acima da carência
            valorTotal = TARIFA_PRIMEIRA_HORA;
        } else if (horasCompletas > 0) {
            valorTotal = TARIFA_PRIMEIRA_HORA; // Primeira hora completa

            // Adicionar horas adicionais
            for (int i = 0; i < horasCompletas - 1; i++) { // Desconta a primeira hora já cobrada
                valorTotal = valorTotal.add(TARIFA_HORA_ADICIONAL);
            }
            
            // Cobrar fração da última hora, se houver
            if (fracaoDeHora > 0) {
                valorTotal = valorTotal.add(TARIFA_HORA_ADICIONAL);
            }
        }
        
        // Exemplo de lógica de diária (após X horas, cobra-se diária)
        // Se a duração for maior que, digamos, 10 horas, cobra uma diária
        if (duracao.toHours() >= 10 && duracao.toDays() < 1) { // Mais de 10h, mas menos de 24h
            valorTotal = TARIFA_DIARIA;
        } else if (duracao.toDays() >= 1) { // Cobrar diárias completas
             long diasCompletos = duracao.toDays();
             valorTotal = TARIFA_DIARIA.multiply(new BigDecimal(diasCompletos));
             // Adicionar lógica para a fração do último dia, se necessário
             Duration restoDoDia = duracao.minusDays(diasCompletos);
             if (restoDoDia.toMinutes() > 0) {
                 // Poderia ser outra tarifa ou uma fração da diária
                 valorTotal = valorTotal.add(calcularValor(entrada.plusDays(diasCompletos), saida)); // Recalcula a parte restante
             }
        }

        // Simplicidade para este exemplo, você pode refinar as regras
        return valorTotal.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private EstacionamentoResponseDTO toEstacionamentoResponseDTO(Estacionamento estacionamento) {
        return new EstacionamentoResponseDTO(
                estacionamento.getId(),
                estacionamento.getVeiculo().getPlaca(),
                estacionamento.getVaga().getNumero(),
                estacionamento.getDataHoraEntrada(),
                estacionamento.getDataHoraSaida(),
                estacionamento.getValorCobrado(),
                estacionamento.getStatus()
        );
    }
}