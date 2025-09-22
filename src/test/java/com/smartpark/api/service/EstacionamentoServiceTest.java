package com.smartpark.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartpark.api.dto.EstacionamentoResponseDTO;
import com.smartpark.api.dto.VeiculoEntradaDTO;
import com.smartpark.api.entity.Estacionamento;
import com.smartpark.api.entity.Vaga;
import com.smartpark.api.entity.Veiculo;
import com.smartpark.api.enums.StatusEstacionamento;
import com.smartpark.api.enums.StatusVaga;
import com.smartpark.api.enums.TipoVeiculo;
import com.smartpark.api.exception.RecursoNaoEncontradoException;
import com.smartpark.api.exception.VagaIndisponivelException;
import com.smartpark.api.exception.VeiculoJaEstacionadoException;
import com.smartpark.api.repository.EstacionamentoRepository;

@ExtendWith(MockitoExtension.class)
class EstacionamentoServiceTest {

    @Mock
    private EstacionamentoRepository estacionamentoRepository;
    @Mock
    private VeiculoService veiculoService;
    @Mock
    private VagaService vagaService;

    @InjectMocks
    private EstacionamentoService estacionamentoService;

    private Veiculo veiculo;
    private Vaga vaga;
    private Estacionamento estacionamentoAtivo;
    private VeiculoEntradaDTO veiculoEntradaDTO;

    @BeforeEach
    void setUp() {
        // Inicializa Veiculo
        veiculo = new Veiculo(1L, "ABC1234", "Fiat", "Palio", "Prata", TipoVeiculo.CARRO);

        // Inicializa Vaga
        vaga = new Vaga(1L, "A1", StatusVaga.LIVRE);

        // Inicializa Estacionamento Ativo
        estacionamentoAtivo = new Estacionamento(10L, veiculo, vaga, LocalDateTime.now().minusHours(1), null, null, StatusEstacionamento.ATIVO);

        // Inicializa VeiculoEntradaDTO
        veiculoEntradaDTO = new VeiculoEntradaDTO("ABC1234", "Fiat", "Palio", "Prata", TipoVeiculo.CARRO);
    }

    // --- Testes para registrarEntrada ---
    @Test
    @DisplayName("Deve registrar a entrada de um veículo com sucesso")
    void registrarEntrada_ShouldRegisterEntrySuccessfully() {
        // Cenário
        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculoEntradaDTO.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.empty()); // Veículo não está estacionado
        when(vagaService.encontrarProximaVagaLivre()).thenReturn(vaga); // Encontra vaga livre
        
        // Simula o comportamento do vagaService.ocuparVaga
        doAnswer(invocation -> {
            Vaga vagaToBeOccupied = invocation.getArgument(0);
            vagaToBeOccupied.setStatus(StatusVaga.OCUPADA);
            return vagaToBeOccupied;
        }).when(vagaService).ocuparVaga(any(Vaga.class));

        when(veiculoService.buscarOuCriarVeiculo(veiculoEntradaDTO)).thenReturn(veiculo); // Busca/Cria veículo
        when(estacionamentoRepository.save(any(Estacionamento.class))).thenAnswer(invocation -> {
            Estacionamento est = invocation.getArgument(0);
            est.setId(1L); // Simula o ID gerado
            est.setStatus(StatusEstacionamento.ATIVO);
            return est;
        });

        // Ação
        EstacionamentoResponseDTO result = estacionamentoService.registrarEntrada(veiculoEntradaDTO);

        // Verificação
        assertNotNull(result);
        assertEquals(veiculoEntradaDTO.getPlaca(), result.getPlacaVeiculo());
        assertEquals(vaga.getNumero(), result.getNumeroVaga());
        assertEquals(StatusEstacionamento.ATIVO, result.getStatus());
        assertNotNull(result.getDataHoraEntrada());
        assertNull(result.getDataHoraSaida());
        assertNull(result.getValorCobrado());
        assertEquals(StatusVaga.OCUPADA, vaga.getStatus()); // Verifica se o status da vaga foi alterado para OCUPADA

        verify(estacionamentoRepository, times(1)).findByVeiculoPlacaAndStatus(veiculoEntradaDTO.getPlaca(), StatusEstacionamento.ATIVO);
        verify(vagaService, times(1)).encontrarProximaVagaLivre();
        verify(vagaService, times(1)).ocuparVaga(vaga);
        verify(veiculoService, times(1)).buscarOuCriarVeiculo(veiculoEntradaDTO);
        verify(estacionamentoRepository, times(1)).save(any(Estacionamento.class));
    }

    @Test
    @DisplayName("Deve lançar VeiculoJaEstacionadoException se o veículo já estiver estacionado")
    void registrarEntrada_ShouldThrowVeiculoJaEstacionadoException_WhenVeiculoAlreadyParked() {
        // Cenário
        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculoEntradaDTO.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.of(estacionamentoAtivo)); // Veículo já estacionado

        // Ação & Verificação
        VeiculoJaEstacionadoException exception = assertThrows(VeiculoJaEstacionadoException.class,
                () -> estacionamentoService.registrarEntrada(veiculoEntradaDTO));

        assertTrue(exception.getMessage().contains("já está estacionado"));
        verify(estacionamentoRepository, times(1)).findByVeiculoPlacaAndStatus(veiculoEntradaDTO.getPlaca(), StatusEstacionamento.ATIVO);
        verify(vagaService, never()).encontrarProximaVagaLivre(); // Não deve tentar encontrar vaga
    }

    @Test
    @DisplayName("Deve lançar VagaIndisponivelException se não houver vagas livres")
    void registrarEntrada_ShouldThrowVagaIndisponivelException_WhenNoVagasLivres() {
        // Cenário
        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculoEntradaDTO.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.empty());
        when(vagaService.encontrarProximaVagaLivre()).thenThrow(new VagaIndisponivelException("Não há vagas livres disponíveis no momento."));

        // Ação & Verificação
        VagaIndisponivelException exception = assertThrows(VagaIndisponivelException.class,
                () -> estacionamentoService.registrarEntrada(veiculoEntradaDTO));

        assertTrue(exception.getMessage().contains("Não há vagas livres"));
        verify(estacionamentoRepository, times(1)).findByVeiculoPlacaAndStatus(veiculoEntradaDTO.getPlaca(), StatusEstacionamento.ATIVO);
        verify(vagaService, times(1)).encontrarProximaVagaLivre();
        verify(estacionamentoRepository, never()).save(any(Estacionamento.class)); // Não deve salvar
    }

    // --- Testes para registrarSaida ---
    @Test
    @DisplayName("Deve registrar a saída de um veículo e calcular o valor corretamente para 1 hora")
    void registrarSaida_ShouldRegisterExitAndCalculateValue_ForOneHour() {
        // Cenário: Estacionamento de 1 hora
        LocalDateTime entrada = LocalDateTime.now().minusMinutes(60); // 1 hora atrás
        estacionamentoAtivo.setDataHoraEntrada(entrada);
        estacionamentoAtivo.setVaga(vaga); // Garante que a vaga está associada
        vaga.setStatus(StatusVaga.OCUPADA); // Manually set vaga status to OCUPADA for the scenario

        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculo.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.of(estacionamentoAtivo));
        when(estacionamentoRepository.save(any(Estacionamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // CORREÇÃO APLICADA AQUI: Simula a mudança de status da vaga pelo vagaService
        doAnswer(invocation -> {
            Vaga vagaToBeLiberated = invocation.getArgument(0);
            vagaToBeLiberated.setStatus(StatusVaga.LIVRE); // Simula o VagaService.liberarVaga
            return vagaToBeLiberated;
        }).when(vagaService).liberarVaga(any(Vaga.class)); 


        // Ação
        EstacionamentoResponseDTO result = estacionamentoService.registrarSaida(veiculo.getPlaca());

        // Verificação
        assertNotNull(result);
        assertEquals(StatusEstacionamento.FINALIZADO, result.getStatus());
        assertNotNull(result.getDataHoraSaida());
        assertEquals(new BigDecimal("5.00"), result.getValorCobrado()); // Tarifa da primeira hora

        verify(estacionamentoRepository, times(1)).findByVeiculoPlacaAndStatus(veiculo.getPlaca(), StatusEstacionamento.ATIVO);
        verify(estacionamentoRepository, times(1)).save(estacionamentoAtivo);
        verify(vagaService, times(1)).liberarVaga(estacionamentoAtivo.getVaga()); // Verify with the exact vaga passed
        assertEquals(StatusVaga.LIVRE, vaga.getStatus()); // Agora, esta verificação deve passar
    }

    @Test
    @DisplayName("Deve registrar a saída de um veículo e calcular o valor corretamente para 1 hora e 30 minutos")
    void registrarSaida_ShouldRegisterExitAndCalculateValue_ForOneHourAndThirtyMinutes() {
        // Cenário: Estacionamento de 1 hora e 30 minutos
        LocalDateTime entrada = LocalDateTime.now().minusMinutes(90); // 1 hora e 30 minutos atrás
        estacionamentoAtivo.setDataHoraEntrada(entrada);
        estacionamentoAtivo.setVaga(vaga);
        vaga.setStatus(StatusVaga.OCUPADA);

        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculo.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.of(estacionamentoAtivo));
        when(estacionamentoRepository.save(any(Estacionamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // CORREÇÃO APLICADA AQUI
        doAnswer(invocation -> {
            Vaga vagaToBeLiberated = invocation.getArgument(0);
            vagaToBeLiberated.setStatus(StatusVaga.LIVRE);
            return vagaToBeLiberated;
        }).when(vagaService).liberarVaga(any(Vaga.class));

        // Ação
        EstacionamentoResponseDTO result = estacionamentoService.registrarSaida(veiculo.getPlaca());

        // Verificação: 5.00 (primeira hora) + 2.00 (fração da segunda hora) = 7.00
        assertEquals(new BigDecimal("7.00"), result.getValorCobrado());
        assertEquals(StatusVaga.LIVRE, vaga.getStatus());
    }

    @Test
    @DisplayName("Deve registrar a saída de um veículo e calcular o valor corretamente para 15 minutos (carência)")
    void registrarSaida_ShouldRegisterExitAndCalculateValue_ForFifteenMinutesGracePeriod() {
        // Cenário: Estacionamento de 15 minutos (carência)
        LocalDateTime entrada = LocalDateTime.now().minusMinutes(15);
        estacionamentoAtivo.setDataHoraEntrada(entrada);
        estacionamentoAtivo.setVaga(vaga);
        vaga.setStatus(StatusVaga.OCUPADA);

        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculo.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.of(estacionamentoAtivo));
        when(estacionamentoRepository.save(any(Estacionamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // CORREÇÃO APLICADA AQUI
        doAnswer(invocation -> {
            Vaga vagaToBeLiberated = invocation.getArgument(0);
            vagaToBeLiberated.setStatus(StatusVaga.LIVRE);
            return vagaToBeLiberated;
        }).when(vagaService).liberarVaga(any(Vaga.class));

        // Ação
        EstacionamentoResponseDTO result = estacionamentoService.registrarSaida(veiculo.getPlaca());

        // Verificação: Valor zero devido à carência
        assertEquals(BigDecimal.ZERO, result.getValorCobrado());
        assertEquals(StatusVaga.LIVRE, vaga.getStatus());
    }

    @Test
    @DisplayName("Deve registrar a saída de um veículo e calcular o valor corretamente para diária (ex: 12 horas)")
    void registrarSaida_ShouldRegisterExitAndCalculateValue_ForDailyRate() {
        // Cenário: Estacionamento de 12 horas (deve ser cobrada a diária de 25.00)
        LocalDateTime entrada = LocalDateTime.now().minusHours(12);
        estacionamentoAtivo.setDataHoraEntrada(entrada);
        estacionamentoAtivo.setVaga(vaga);
        vaga.setStatus(StatusVaga.OCUPADA);

        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculo.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.of(estacionamentoAtivo));
        when(estacionamentoRepository.save(any(Estacionamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // CORREÇÃO APLICADA AQUI
        doAnswer(invocation -> {
            Vaga vagaToBeLiberated = invocation.getArgument(0);
            vagaToBeLiberated.setStatus(StatusVaga.LIVRE);
            return vagaToBeLiberated;
        }).when(vagaService).liberarVaga(any(Vaga.class));

        // Ação
        EstacionamentoResponseDTO result = estacionamentoService.registrarSaida(veiculo.getPlaca());

        // Verificação: Valor da diária
        assertEquals(new BigDecimal("25.00"), result.getValorCobrado());
        assertEquals(StatusVaga.LIVRE, vaga.getStatus());
    }

    @Test
    @DisplayName("Deve registrar a saída de um veículo e calcular o valor corretamente para 2 dias e 1 hora")
    void registrarSaida_ShouldRegisterExitAndCalculateValue_ForTwoDaysAndOneHour() {
        // Cenário: Estacionamento de 2 dias e 1 hora (2 diárias + 1 hora avulsa ou fração)
        // Isso depende da lógica exata do calcularValor. O atual vai recursivamente.
        // Vamos simular entrada: 2 dias atrás, e saida: 1h depois de 2 dias.
        LocalDateTime entrada = LocalDateTime.now().minusDays(2).minusHours(1);
        estacionamentoAtivo.setDataHoraEntrada(entrada);
        estacionamentoAtivo.setVaga(vaga);
        vaga.setStatus(StatusVaga.OCUPADA);

        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculo.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.of(estacionamentoAtivo));
        when(estacionamentoRepository.save(any(Estacionamento.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // CORREÇÃO APLICADA AQUI
        doAnswer(invocation -> {
            Vaga vagaToBeLiberated = invocation.getArgument(0);
            vagaToBeLiberated.setStatus(StatusVaga.LIVRE);
            return vagaToBeLiberated;
        }).when(vagaService).liberarVaga(any(Vaga.class));

        // Ação
        EstacionamentoResponseDTO result = estacionamentoService.registrarSaida(veiculo.getPlaca());

        // Verificação: 2 diárias * 25.00 = 50.00. A lógica recursiva adicionaria a primeira hora da fração.
        // 50.00 + 5.00 = 55.00 (2 diárias + primeira hora avulsa)
        assertEquals(new BigDecimal("55.00"), result.getValorCobrado());
        assertEquals(StatusVaga.LIVRE, vaga.getStatus());
    }


    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se o veículo não estiver no estacionamento")
    void registrarSaida_ShouldThrowRecursoNaoEncontradoException_WhenVeiculoNotParked() {
        // Cenário
        when(estacionamentoRepository.findByVeiculoPlacaAndStatus(veiculo.getPlaca(), StatusEstacionamento.ATIVO))
                .thenReturn(Optional.empty()); // Veículo não encontrado/ativo

        // Ação & Verificação
        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> estacionamentoService.registrarSaida(veiculo.getPlaca()));

        assertTrue(exception.getMessage().contains("não encontrado no estacionamento ou já saiu."));
        verify(estacionamentoRepository, times(1)).findByVeiculoPlacaAndStatus(veiculo.getPlaca(), StatusEstacionamento.ATIVO);
        verify(estacionamentoRepository, never()).save(any(Estacionamento.class)); // Não deve salvar
        verify(vagaService, never()).liberarVaga(any(Vaga.class)); // Não deve liberar vaga
    }

    // --- Testes para listarEstacionamentosAtivos ---
    @Test
    @DisplayName("Deve retornar uma lista de Estacionamentos Ativos")
    void listarEstacionamentosAtivos_ShouldReturnListOfActiveEstacionamentos() {
        Estacionamento estacionamentoFinalizado = new Estacionamento(11L, veiculo, vaga, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2), new BigDecimal("5.00"), StatusEstacionamento.FINALIZADO);
        List<Estacionamento> allEstacionamentos = Arrays.asList(estacionamentoAtivo, estacionamentoFinalizado);

        when(estacionamentoRepository.findAll()).thenReturn(allEstacionamentos);

        List<EstacionamentoResponseDTO> result = estacionamentoService.listarEstacionamentosAtivos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(estacionamentoAtivo.getVeiculo().getPlaca(), result.get(0).getPlacaVeiculo());
        assertEquals(StatusEstacionamento.ATIVO, result.get(0).getStatus());
        verify(estacionamentoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver Estacionamentos Ativos")
    void listarEstacionamentosAtivos_ShouldReturnEmptyList_WhenNoActiveEstacionamentos() {
        Estacionamento estacionamentoFinalizado = new Estacionamento(11L, veiculo, vaga, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2), new BigDecimal("5.00"), StatusEstacionamento.FINALIZADO);
        when(estacionamentoRepository.findAll()).thenReturn(Collections.singletonList(estacionamentoFinalizado));

        List<EstacionamentoResponseDTO> result = estacionamentoService.listarEstacionamentosAtivos();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(estacionamentoRepository, times(1)).findAll();
    }

    // --- Testes para listarHistoricoEstacionamentos ---
    @Test
    @DisplayName("Deve retornar uma lista de todos os Estacionamentos (histórico)")
    void listarHistoricoEstacionamentos_ShouldReturnListOfAllEstacionamentos() {
        Estacionamento estacionamentoFinalizado = new Estacionamento(11L, veiculo, vaga, LocalDateTime.now().minusHours(3), LocalDateTime.now().minusHours(2), new BigDecimal("5.00"), StatusEstacionamento.FINALIZADO);
        List<Estacionamento> allEstacionamentos = Arrays.asList(estacionamentoAtivo, estacionamentoFinalizado);

        when(estacionamentoRepository.findAll()).thenReturn(allEstacionamentos);

        List<EstacionamentoResponseDTO> result = estacionamentoService.listarHistoricoEstacionamentos();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(estacionamentoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver histórico de Estacionamentos")
    void listarHistoricoEstacionamentos_ShouldReturnEmptyList_WhenNoHistory() {
        when(estacionamentoRepository.findAll()).thenReturn(Collections.emptyList());

        List<EstacionamentoResponseDTO> result = estacionamentoService.listarHistoricoEstacionamentos();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(estacionamentoRepository, times(1)).findAll();
    }

    // --- Testes para buscarEstacionamentoPorId ---
    @Test
    @DisplayName("Deve retornar EstacionamentoResponseDTO se o ID existir")
    void buscarEstacionamentoPorId_ShouldReturnDTO_WhenIdExists() {
        when(estacionamentoRepository.findById(estacionamentoAtivo.getId())).thenReturn(Optional.of(estacionamentoAtivo));

        EstacionamentoResponseDTO result = estacionamentoService.buscarEstacionamentoPorId(estacionamentoAtivo.getId());

        assertNotNull(result);
        assertEquals(estacionamentoAtivo.getId(), result.getId());
        assertEquals(estacionamentoAtivo.getVeiculo().getPlaca(), result.getPlacaVeiculo());
        verify(estacionamentoRepository, times(1)).findById(estacionamentoAtivo.getId());
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se o ID não existir")
    void buscarEstacionamentoPorId_ShouldThrowException_WhenIdDoesNotExist() {
        when(estacionamentoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> estacionamentoService.buscarEstacionamentoPorId(99L));

        assertEquals("Registro de estacionamento não encontrado com ID: 99", exception.getMessage());
        verify(estacionamentoRepository, times(1)).findById(99L);
    }
}