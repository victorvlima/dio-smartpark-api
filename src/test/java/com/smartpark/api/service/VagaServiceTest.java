package com.smartpark.api.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.smartpark.api.entity.Vaga;
import com.smartpark.api.enums.StatusVaga;
import com.smartpark.api.exception.RecursoNaoEncontradoException;
import com.smartpark.api.exception.VagaIndisponivelException;
import com.smartpark.api.repository.VagaRepository;

@ExtendWith(MockitoExtension.class) // Habilita a extensão Mockito para JUnit 5
class VagaServiceTest {

    @Mock // Cria um mock do VagaRepository
    private VagaRepository vagaRepository;

    @InjectMocks // Injeta os mocks (vagaRepository) no VagaService
    private VagaService vagaService;

    // Opcional, mas útil para inicializar mocks se não usar @ExtendWith(MockitoExtension.class)
    @BeforeEach
    void setUp() {
        // MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Deve retornar o número total de vagas")
    void testGetTotalVagas() {
        // Cenário: vagaRepository.count() retorna 5
        when(vagaRepository.count()).thenReturn(5L);

        // Ação: Chamar o método do serviço
        int totalVagas = vagaService.getTotalVagas();

        // Verificação: O resultado deve ser 5 e o método do mock deve ter sido chamado uma vez
        assertEquals(5, totalVagas);
        verify(vagaRepository, times(1)).count();
    }

    @Test
    @DisplayName("Deve retornar o número de vagas ocupadas")
    void testGetVagasOcupadas() {
        // Cenário: vagaRepository.countByStatus(OCUPADA) retorna 2
        when(vagaRepository.countByStatus(StatusVaga.OCUPADA)).thenReturn(2);

        // Ação: Chamar o método do serviço
        int vagasOcupadas = vagaService.getVagasOcupadas();

        // Verificação: O resultado deve ser 2 e o método do mock deve ter sido chamado uma vez
        assertEquals(2, vagasOcupadas);
        verify(vagaRepository, times(1)).countByStatus(StatusVaga.OCUPADA);
    }

    @Test
    @DisplayName("Deve retornar o número de vagas livres")
    void testGetVagasLivres() {
        // Cenário: vagaRepository.countByStatus(LIVRE) retorna 3
        when(vagaRepository.countByStatus(StatusVaga.LIVRE)).thenReturn(3);

        // Ação: Chamar o método do serviço
        int vagasLivres = vagaService.getVagasLivres();

        // Verificação: O resultado deve ser 3 e o método do mock deve ter sido chamado uma vez
        assertEquals(3, vagasLivres);
        verify(vagaRepository, times(1)).countByStatus(StatusVaga.LIVRE);
    }

    @Test
    @DisplayName("Deve indicar que o estacionamento está cheio quando não há vagas livres")
    void testIsEstacionamentoCheio_True() {
        // Cenário: vagaRepository.countByStatus(LIVRE) retorna 0
        when(vagaRepository.countByStatus(StatusVaga.LIVRE)).thenReturn(0);

        // Ação & Verificação
        assertTrue(vagaService.isEstacionamentoCheio());
        verify(vagaRepository, times(1)).countByStatus(StatusVaga.LIVRE);
    }

    @Test
    @DisplayName("Deve indicar que o estacionamento não está cheio quando há vagas livres")
    void testIsEstacionamentoCheio_False() {
        // Cenário: vagaRepository.countByStatus(LIVRE) retorna 1
        when(vagaRepository.countByStatus(StatusVaga.LIVRE)).thenReturn(1);

        // Ação & Verificação
        assertFalse(vagaService.isEstacionamentoCheio());
        verify(vagaRepository, times(1)).countByStatus(StatusVaga.LIVRE);
    }

    @Test
    @DisplayName("Deve encontrar e retornar a próxima vaga livre")
    void testEncontrarProximaVagaLivre_Success() {
        // Cenário: Há uma vaga livre disponível
        Vaga vagaLivre = new Vaga(1L, "A1", StatusVaga.LIVRE);
        when(vagaRepository.findTopByStatus(StatusVaga.LIVRE)).thenReturn(Optional.of(vagaLivre));

        // Ação: Chamar o método do serviço
        Vaga vagaEncontrada = vagaService.encontrarProximaVagaLivre();

        // Verificação: A vaga encontrada deve ser a mesma mockada
        assertNotNull(vagaEncontrada);
        assertEquals("A1", vagaEncontrada.getNumero());
        verify(vagaRepository, times(1)).findTopByStatus(StatusVaga.LIVRE);
    }

    @Test
    @DisplayName("Deve lançar VagaIndisponivelException quando não há vagas livres")
    void testEncontrarProximaVagaLivre_NotFound() {
        // Cenário: Não há vagas livres
        when(vagaRepository.findTopByStatus(StatusVaga.LIVRE)).thenReturn(Optional.empty());

        // Ação & Verificação: Deve lançar a exceção esperada
        assertThrows(VagaIndisponivelException.class, () -> vagaService.encontrarProximaVagaLivre());
        verify(vagaRepository, times(1)).findTopByStatus(StatusVaga.LIVRE);
    }

    @Test
    @DisplayName("Deve ocupar uma vaga e salvar o status OCUPADA")
    void testOcuparVaga() {
        Vaga vaga = new Vaga(1L, "A1", StatusVaga.LIVRE);
        Vaga vagaOcupada = new Vaga(1L, "A1", StatusVaga.OCUPADA);
        when(vagaRepository.save(any(Vaga.class))).thenReturn(vagaOcupada);

        Vaga resultado = vagaService.ocuparVaga(vaga);

        assertNotNull(resultado);
        assertEquals(StatusVaga.OCUPADA, resultado.getStatus());
        verify(vagaRepository, times(1)).save(vaga);
    }

    @Test
    @DisplayName("Deve criar uma nova vaga com status LIVRE")
    void testCriarVaga() {
        Vaga novaVaga = new Vaga(null, "B5", null); // ID nulo, status nulo - será definido no service
        Vaga vagaSalva = new Vaga(1L, "B5", StatusVaga.LIVRE);

        when(vagaRepository.findByNumero(novaVaga.getNumero())).thenReturn(Optional.empty()); // Vaga não existe
        when(vagaRepository.save(any(Vaga.class))).thenReturn(vagaSalva);

        Vaga resultado = vagaService.criarVaga(novaVaga);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("B5", resultado.getNumero());
        assertEquals(StatusVaga.LIVRE, resultado.getStatus());
        verify(vagaRepository, times(1)).findByNumero("B5");
        verify(vagaRepository, times(1)).save(novaVaga); // Verifica que o save foi chamado com a vaga preparada
    }

    @Test
    @DisplayName("Não deve criar vaga se o número já existe")
    void testCriarVaga_NumeroExistente() {
        Vaga vagaExistente = new Vaga(1L, "B5", StatusVaga.LIVRE);
        when(vagaRepository.findByNumero("B5")).thenReturn(Optional.of(vagaExistente));

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            vagaService.criarVaga(new Vaga(null, "B5", null));
        });

        assertTrue(thrown.getMessage().contains("Já existe uma vaga com o número B5"));
        verify(vagaRepository, times(1)).findByNumero("B5");
        verify(vagaRepository, never()).save(any(Vaga.class)); // Garante que save não foi chamado
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException ao buscar vaga por ID inexistente")
    void testBuscarVagaPorId_NotFound() {
        when(vagaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> vagaService.buscarVagaPorId(99L));
        verify(vagaRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("Deve deletar uma vaga livre com sucesso")
    void testDeletarVaga_Success() {
        Vaga vagaLivre = new Vaga(1L, "A1", StatusVaga.LIVRE);
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vagaLivre));
        doNothing().when(vagaRepository).delete(vagaLivre); // Mockar void methods

        assertDoesNotThrow(() -> vagaService.deletarVaga(1L));
        verify(vagaRepository, times(1)).findById(1L);
        verify(vagaRepository, times(1)).delete(vagaLivre);
    }

    @Test
    @DisplayName("Não deve deletar uma vaga ocupada")
    void testDeletarVaga_Occupied() {
        Vaga vagaOcupada = new Vaga(1L, "A1", StatusVaga.OCUPADA);
        when(vagaRepository.findById(1L)).thenReturn(Optional.of(vagaOcupada));

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> vagaService.deletarVaga(1L));

        assertTrue(thrown.getMessage().contains("Não é possível deletar uma vaga ocupada."));
        verify(vagaRepository, times(1)).findById(1L);
        verify(vagaRepository, never()).delete(any(Vaga.class));
    }
}