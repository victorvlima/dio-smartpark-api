package com.smartpark.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.smartpark.api.dto.VeiculoEntradaDTO;
import com.smartpark.api.entity.Estacionamento;
import com.smartpark.api.entity.Veiculo;
import com.smartpark.api.enums.StatusEstacionamento;
import com.smartpark.api.enums.TipoVeiculo;
import com.smartpark.api.exception.RecursoNaoEncontradoException;
import com.smartpark.api.repository.VeiculoRepository;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @InjectMocks
    private VeiculoService veiculoService;

    private Veiculo veiculoExistente;
    private VeiculoEntradaDTO veiculoEntradaDTO;

    @BeforeEach
    void setUp() {
        veiculoExistente = new Veiculo(1L, "ABC1234", "Fiat", "Palio", "Prata", TipoVeiculo.CARRO);
        veiculoEntradaDTO = new VeiculoEntradaDTO("DEF5678", "GM", "Onix", "Preto", TipoVeiculo.CARRO);
    }

    // --- Testes para findByPlaca ---
    @Test
    @DisplayName("Deve retornar Optional com Veiculo se a placa existir")
    void findByPlaca_ShouldReturnOptionalWithVeiculo_WhenPlacaExists() {
        when(veiculoRepository.findByPlaca("ABC1234")).thenReturn(Optional.of(veiculoExistente));

        Optional<Veiculo> result = veiculoService.findByPlaca("ABC1234");

        assertTrue(result.isPresent());
        assertEquals(veiculoExistente.getPlaca(), result.get().getPlaca());
        verify(veiculoRepository, times(1)).findByPlaca("ABC1234");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio se a placa não existir")
    void findByPlaca_ShouldReturnEmptyOptional_WhenPlacaDoesNotExist() {
        when(veiculoRepository.findByPlaca("XYZ9876")).thenReturn(Optional.empty());

        Optional<Veiculo> result = veiculoService.findByPlaca("XYZ9876");

        assertFalse(result.isPresent());
        verify(veiculoRepository, times(1)).findByPlaca("XYZ9876");
    }

    // --- Testes para buscarOuCriarVeiculo ---
    @Test
    @DisplayName("Deve retornar Veiculo existente se a placa já estiver cadastrada")
    void buscarOuCriarVeiculo_ShouldReturnExistingVeiculo_WhenPlacaExists() {
        when(veiculoRepository.findByPlaca(veiculoExistente.getPlaca())).thenReturn(Optional.of(veiculoExistente));

        Veiculo result = veiculoService.buscarOuCriarVeiculo(
                new VeiculoEntradaDTO(veiculoExistente.getPlaca(), "Marca", "Modelo", "Cor", TipoVeiculo.CARRO)
        );

        assertNotNull(result);
        assertEquals(veiculoExistente.getId(), result.getId());
        assertEquals(veiculoExistente.getPlaca(), result.getPlaca());
        verify(veiculoRepository, times(1)).findByPlaca(veiculoExistente.getPlaca());
        verify(veiculoRepository, never()).save(any(Veiculo.class)); // Não deve salvar se já existe
    }

    @Test
    @DisplayName("Deve criar e retornar um novo Veiculo se a placa não existir")
    void buscarOuCriarVeiculo_ShouldCreateNewVeiculo_WhenPlacaDoesNotExist() {
        when(veiculoRepository.findByPlaca(veiculoEntradaDTO.getPlaca())).thenReturn(Optional.empty());
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
            Veiculo newVeiculo = invocation.getArgument(0);
            newVeiculo.setId(2L); // Simula o ID gerado pelo banco
            return newVeiculo;
        });

        Veiculo result = veiculoService.buscarOuCriarVeiculo(veiculoEntradaDTO);

        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals(veiculoEntradaDTO.getPlaca(), result.getPlaca());
        verify(veiculoRepository, times(1)).findByPlaca(veiculoEntradaDTO.getPlaca());
        verify(veiculoRepository, times(1)).save(any(Veiculo.class)); // Deve salvar o novo veículo
    }

    // --- Testes para listarTodosVeiculos ---
    @Test
    @DisplayName("Deve retornar uma lista de todos os Veiculos cadastrados")
    void listarTodosVeiculos_ShouldReturnListOfAllVeiculos() {
        List<Veiculo> veiculos = Arrays.asList(veiculoExistente, new Veiculo(2L, "DEF5678", "GM", "Onix", "Preto", TipoVeiculo.CARRO));
        when(veiculoRepository.findAll()).thenReturn(veiculos);

        List<Veiculo> result = veiculoService.listarTodosVeiculos();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("ABC1234", result.get(0).getPlaca());
        verify(veiculoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver Veiculos")
    void listarTodosVeiculos_ShouldReturnEmptyList_WhenNoVeiculosExist() {
        when(veiculoRepository.findAll()).thenReturn(Collections.emptyList());

        List<Veiculo> result = veiculoService.listarTodosVeiculos();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(veiculoRepository, times(1)).findAll();
    }

    // --- Testes para buscarVeiculoPorId ---
    @Test
    @DisplayName("Deve retornar o Veiculo se o ID existir")
    void buscarVeiculoPorId_ShouldReturnVeiculo_WhenIdExists() {
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoExistente));

        Veiculo result = veiculoService.buscarVeiculoPorId(1L);

        assertNotNull(result);
        assertEquals(veiculoExistente.getId(), result.getId());
        verify(veiculoRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se o ID não existir")
    void buscarVeiculoPorId_ShouldThrowException_WhenIdDoesNotExist() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> veiculoService.buscarVeiculoPorId(99L));

        assertEquals("Veículo não encontrado com ID: 99", exception.getMessage());
        verify(veiculoRepository, times(1)).findById(99L);
    }

    // --- Testes para criarVeiculo ---
    @Test
    @DisplayName("Deve criar e retornar o novo Veiculo com sucesso")
    void criarVeiculo_ShouldCreateNewVeiculo_Success() {
        Veiculo novoVeiculo = new Veiculo(null, "XYZ7890", "VW", "Golf", "Azul", TipoVeiculo.CARRO);
        when(veiculoRepository.existsByPlaca(novoVeiculo.getPlaca())).thenReturn(false);
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> {
            Veiculo savedVeiculo = invocation.getArgument(0);
            savedVeiculo.setId(3L);
            return savedVeiculo;
        });

        Veiculo result = veiculoService.criarVeiculo(novoVeiculo);

        assertNotNull(result);
        assertEquals(3L, result.getId());
        assertEquals("XYZ7890", result.getPlaca());
        verify(veiculoRepository, times(1)).existsByPlaca("XYZ7890");
        verify(veiculoRepository, times(1)).save(novoVeiculo);
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se já existir um veículo com a mesma placa")
    void criarVeiculo_ShouldThrowException_WhenPlacaAlreadyExists() {
        Veiculo veiculoDuplicado = new Veiculo(null, "ABC1234", "Ford", "Ka", "Vermelho", TipoVeiculo.CARRO);
        when(veiculoRepository.existsByPlaca("ABC1234")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> veiculoService.criarVeiculo(veiculoDuplicado));

        assertEquals("Veículo com a placa ABC1234 já existe.", exception.getMessage());
        verify(veiculoRepository, times(1)).existsByPlaca("ABC1234");
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    // --- Testes para atualizarVeiculo ---
    @Test
    @DisplayName("Deve atualizar os dados de um Veiculo existente e retorná-lo")
    void atualizarVeiculo_ShouldUpdateExistingVeiculo_Success() {
        Veiculo veiculoAtualizadoInfo = new Veiculo(null, "ABC1234", "Honda", "Civic", "Branco", TipoVeiculo.CARRO);
        
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoExistente));
        when(veiculoRepository.save(any(Veiculo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Veiculo result = veiculoService.atualizarVeiculo(1L, veiculoAtualizadoInfo);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ABC1234", result.getPlaca()); // Placa não muda no update
        assertEquals("Honda", result.getMarca());
        assertEquals("Civic", result.getModelo());
        assertEquals("Branco", result.getCor());
        verify(veiculoRepository, times(1)).findById(1L);
        verify(veiculoRepository, times(1)).save(veiculoExistente);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar atualizar Veiculo inexistente")
    void atualizarVeiculo_ShouldThrowException_WhenVeiculoDoesNotExist() {
        Veiculo veiculoAtualizadoInfo = new Veiculo(null, "ABC1234", "Honda", "Civic", "Branco", TipoVeiculo.CARRO);
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> veiculoService.atualizarVeiculo(99L, veiculoAtualizadoInfo));

        assertEquals("Veículo não encontrado com ID: 99", exception.getMessage());
        verify(veiculoRepository, times(1)).findById(99L);
        verify(veiculoRepository, never()).save(any(Veiculo.class));
    }

    // --- Testes para deletarVeiculo ---
    @Test
    @DisplayName("Deve deletar um Veiculo com sucesso se não tiver estacionamentos ativos")
    void deletarVeiculo_ShouldDeleteVeiculo_WhenNoActiveEstacionamentos() {
        // Simula que o veículo não tem estacionamentos, ou que todos estão FINALIZADO
        veiculoExistente.setEstacionamentos(Collections.emptyList());
        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoExistente));
        doNothing().when(veiculoRepository).delete(any(Veiculo.class));

        assertDoesNotThrow(() -> veiculoService.deletarVeiculo(1L));

        verify(veiculoRepository, times(1)).findById(1L);
        verify(veiculoRepository, times(1)).delete(veiculoExistente);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao tentar deletar Veiculo inexistente")
    void deletarVeiculo_ShouldThrowException_WhenVeiculoDoesNotExist() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> veiculoService.deletarVeiculo(99L));

        assertEquals("Veículo não encontrado com ID: 99", exception.getMessage());
        verify(veiculoRepository, times(1)).findById(99L);
        verify(veiculoRepository, never()).delete(any(Veiculo.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException ao tentar deletar Veiculo com estacionamento ativo")
    void deletarVeiculo_ShouldThrowException_WhenVeiculoHasActiveEstacionamentos() {
        // Cria um estacionamento ativo para o veículo
        Estacionamento estacionamentoAtivo = new Estacionamento();
        estacionamentoAtivo.setStatus(StatusEstacionamento.ATIVO);
        veiculoExistente.setEstacionamentos(List.of(estacionamentoAtivo));

        when(veiculoRepository.findById(1L)).thenReturn(Optional.of(veiculoExistente));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> veiculoService.deletarVeiculo(1L));

        assertEquals("Não é possível deletar um veículo com estacionamento ativo.", exception.getMessage());
        verify(veiculoRepository, times(1)).findById(1L);
        verify(veiculoRepository, never()).delete(any(Veiculo.class));
    }
}