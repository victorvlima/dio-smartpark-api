package com.smartpark.api.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartpark.api.dto.UsuarioRequestDTO;
import com.smartpark.api.dto.UsuarioResponseDTO;
import com.smartpark.api.entity.Usuario;
import com.smartpark.api.exception.RecursoNaoEncontradoException;
import com.smartpark.api.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario adminUser;
    private Usuario commonUser;
    private UsuarioRequestDTO adminRequestDTO;
    private UsuarioRequestDTO commonRequestDTO;

    @BeforeEach
    void setUp() {
        // Usuário ADMIN
        adminUser = new Usuario(1L, "admin", "encodedAdminPassword", "ADMIN");
        adminRequestDTO = new UsuarioRequestDTO("admin", "admin123", "ADMIN");

        // Usuário comum
        commonUser = new Usuario(2L, "user", "encodedUserPassword", "USER");
        commonRequestDTO = new UsuarioRequestDTO("user", "user123", "USER");
    }

    // --- Testes para criarUsuario ---
    @Test
    @DisplayName("Deve criar um novo usuário com senha criptografada e retornar DTO")
    void criarUsuario_ShouldCreateNewUserWithEncodedPassword() {
        when(usuarioRepository.findByUsername(adminRequestDTO.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(adminRequestDTO.getPassword())).thenReturn("encodedAdminPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioSalvo = invocation.getArgument(0);
            usuarioSalvo.setId(1L);
            return usuarioSalvo;
        });

        UsuarioResponseDTO result = usuarioService.criarUsuario(adminRequestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(adminRequestDTO.getUsername(), result.getUsername());
        assertEquals(adminRequestDTO.getRole().toUpperCase(), result.getRole());
        verify(usuarioRepository, times(1)).findByUsername(adminRequestDTO.getUsername());
        verify(passwordEncoder, times(1)).encode(adminRequestDTO.getPassword());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se o username já existir ao criar")
    void criarUsuario_ShouldThrowException_WhenUsernameAlreadyExists() {
        when(usuarioRepository.findByUsername(adminRequestDTO.getUsername())).thenReturn(Optional.of(adminUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.criarUsuario(adminRequestDTO));

        assertEquals("Username '" + adminRequestDTO.getUsername() + "' já existe.", exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsername(adminRequestDTO.getUsername());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- Testes para listarTodosUsuarios ---
    @Test
    @DisplayName("Deve retornar uma lista de todos os usuários como DTOs")
    void listarTodosUsuarios_ShouldReturnListOfUserDTOs() {
        List<Usuario> usuarios = Arrays.asList(adminUser, commonUser);
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        List<UsuarioResponseDTO> result = usuarioService.listarTodosUsuarios();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(adminUser.getUsername(), result.get(0).getUsername());
        assertEquals(commonUser.getUsername(), result.get(1).getUsername());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma lista vazia se não houver usuários cadastrados")
    void listarTodosUsuarios_ShouldReturnEmptyList_WhenNoUsersExist() {
        when(usuarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<UsuarioResponseDTO> result = usuarioService.listarTodosUsuarios();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(usuarioRepository, times(1)).findAll();
    }

    // --- Testes para buscarUsuarioPorId ---
    @Test
    @DisplayName("Deve retornar DTO do usuário se o ID existir")
    void buscarUsuarioPorId_ShouldReturnUserDTO_WhenIdExists() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        UsuarioResponseDTO result = usuarioService.buscarUsuarioPorId(1L);

        assertNotNull(result);
        assertEquals(adminUser.getId(), result.getId());
        assertEquals(adminUser.getUsername(), result.getUsername());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se o ID não existir ao buscar por ID")
    void buscarUsuarioPorId_ShouldThrowException_WhenIdDoesNotExist() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> usuarioService.buscarUsuarioPorId(99L));

        assertEquals("Usuário não encontrado com ID: 99", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(99L);
    }

    // --- Testes para buscarUsuarioPorUsername ---
    @Test
    @DisplayName("Deve retornar DTO do usuário se o username existir")
    void buscarUsuarioPorUsername_ShouldReturnUserDTO_WhenUsernameExists() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        UsuarioResponseDTO result = usuarioService.buscarUsuarioPorUsername("admin");

        assertNotNull(result);
        assertEquals(adminUser.getUsername(), result.getUsername());
        verify(usuarioRepository, times(1)).findByUsername("admin");
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se o username não existir ao buscar por username")
    void buscarUsuarioPorUsername_ShouldThrowException_WhenUsernameDoesNotExist() {
        when(usuarioRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> usuarioService.buscarUsuarioPorUsername("nonexistent"));

        assertEquals("Usuário não encontrado com username: nonexistent", exception.getMessage());
        verify(usuarioRepository, times(1)).findByUsername("nonexistent");
    }

    // --- Testes para atualizarUsuario ---
    @Test
    @DisplayName("Deve atualizar username, senha (criptografada) e role de um usuário existente")
    void atualizarUsuario_ShouldUpdateUserWithEncodedPassword() {
        UsuarioRequestDTO updateDTO = new UsuarioRequestDTO("newadmin", "newpass123", "USER");
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUser)); // Encontra o usuário a ser atualizado
        when(usuarioRepository.findByUsername("newadmin")).thenReturn(Optional.empty()); // Novo username não existe
        when(passwordEncoder.encode("newpass123")).thenReturn("encodedNewPass");
        
        // Simula o save do repositório, modificando o objeto adminUser que foi encontrado
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioToSave = invocation.getArgument(0);
            usuarioToSave.setPassword("encodedNewPass"); // Simula a senha atualizada
            usuarioToSave.setUsername("newadmin");
            usuarioToSave.setRole("USER");
            return usuarioToSave;
        });

        UsuarioResponseDTO result = usuarioService.atualizarUsuario(1L, updateDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("newadmin", result.getUsername());
        assertEquals("USER", result.getRole());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).findByUsername("newadmin");
        verify(passwordEncoder, times(1)).encode("newpass123");
        verify(usuarioRepository, times(1)).save(adminUser); // Save é chamado com o objeto original, mas modificado
    }

    @Test
    @DisplayName("Deve atualizar apenas username e role se a senha não for fornecida")
    void atualizarUsuario_ShouldUpdateUsernameAndRoleOnly_WhenPasswordNotProvided() {
        UsuarioRequestDTO updateDTO = new UsuarioRequestDTO("newusername", "", "ADMIN"); // Senha vazia
        // adminUser já tem id=1, username="admin", password="encodedAdminPassword", role="ADMIN"
        
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(usuarioRepository.findByUsername("newusername")).thenReturn(Optional.empty());
        
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioToSave = invocation.getArgument(0);
            usuarioToSave.setUsername("newusername");
            usuarioToSave.setRole("ADMIN");
            // password não é alterada
            return usuarioToSave;
        });

        UsuarioResponseDTO result = usuarioService.atualizarUsuario(1L, updateDTO);

        assertNotNull(result);
        assertEquals("newusername", result.getUsername());
        assertEquals("ADMIN", result.getRole());
        // A senha não deve ter sido codificada novamente se o DTO estava vazio
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, times(1)).save(adminUser); // adminUser agora tem os novos dados
    }


    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se usuário não for encontrado para atualização")
    void atualizarUsuario_ShouldThrowException_WhenUserNotFound() {
        UsuarioRequestDTO updateDTO = new UsuarioRequestDTO("admin", "newpass", "ADMIN");
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> usuarioService.atualizarUsuario(99L, updateDTO));

        assertEquals("Usuário não encontrado com ID: 99", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(99L);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar IllegalArgumentException se novo username já existe para outro usuário")
    void atualizarUsuario_ShouldThrowException_WhenNewUsernameAlreadyExistsForAnotherUser() {
        Usuario existingOtherUser = new Usuario(3L, "otheruser", "pass", "USER");
        UsuarioRequestDTO updateDTO = new UsuarioRequestDTO("otheruser", "newpass", "ADMIN");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(adminUser)); // adminUser tenta mudar para otheruser
        when(usuarioRepository.findByUsername("otheruser")).thenReturn(Optional.of(existingOtherUser)); // otheruser já existe

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.atualizarUsuario(1L, updateDTO));

        assertEquals("Username 'otheruser' já existe.", exception.getMessage());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).findByUsername("otheruser");
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }


    // --- Testes para deletarUsuario ---
    @Test
    @DisplayName("Deve deletar o usuário com sucesso")
    void deletarUsuario_ShouldDeleteUserSuccessfully() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        doNothing().when(usuarioRepository).deleteById(1L);

        assertDoesNotThrow(() -> usuarioService.deletarUsuario(1L));

        verify(usuarioRepository, times(1)).existsById(1L);
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar RecursoNaoEncontradoException se o ID não existir ao deletar")
    void deletarUsuario_ShouldThrowException_WhenIdDoesNotExist() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        RecursoNaoEncontradoException exception = assertThrows(RecursoNaoEncontradoException.class,
                () -> usuarioService.deletarUsuario(99L));

        assertEquals("Usuário não encontrado com ID: 99", exception.getMessage());
        verify(usuarioRepository, times(1)).existsById(99L);
        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}