
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.cliente.ClienteAtualizacaoDTO;
import com.powermanager.powermanager.dto.cliente.ClienteCadastroDTO;
import com.powermanager.powermanager.dto.cliente.ClienteResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.exception.cliente.ClienteNotFoundException;
import com.powermanager.powermanager.mapper.ClienteMapper;
import com.powermanager.powermanager.repository.ClienteRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepo clienteRepo;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deveCadastrarCliente() {

        ClienteCadastroDTO dto = new ClienteCadastroDTO(
                "João Silva",
                "joao@email.com",
                "77999999999"
        );

        Cliente cliente = mock(Cliente.class);

        ClienteResponseDTO response = new ClienteResponseDTO(
                UUID.randomUUID(),
                "João Silva",
                "joao@email.com",
                "77999999999"
        );

        when(clienteMapper.paraEntidade(dto)).thenReturn(cliente);
        when(clienteRepo.save(cliente)).thenReturn(cliente);
        when(clienteMapper.paraResponseDTO(cliente)).thenReturn(response);

        ClienteResponseDTO resultado = clienteService.cadastrarCliente(dto);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(clienteMapper).paraEntidade(dto);
        verify(clienteRepo).save(cliente);
        verify(clienteMapper).paraResponseDTO(cliente);
    }

    @Test
    void deveListarClientes() {

        Cliente cliente1 = mock(Cliente.class);
        Cliente cliente2 = mock(Cliente.class);

        ClienteResponseDTO response1 = new ClienteResponseDTO(
                UUID.randomUUID(), "João", "joao@email.com", "77999999999"
        );

        ClienteResponseDTO response2 = new ClienteResponseDTO(
                UUID.randomUUID(), "Maria", "maria@email.com", "77988888888"
        );

        when(clienteRepo.findAll()).thenReturn(List.of(cliente1, cliente2));
        when(clienteMapper.paraResponseDTO(cliente1)).thenReturn(response1);
        when(clienteMapper.paraResponseDTO(cliente2)).thenReturn(response2);

        List<ClienteResponseDTO> resultado = clienteService.listarClientes();

        assertEquals(2, resultado.size());
        assertEquals(response1, resultado.get(0));
        assertEquals(response2, resultado.get(1));

        verify(clienteRepo).findAll();
        verify(clienteMapper).paraResponseDTO(cliente1);
        verify(clienteMapper).paraResponseDTO(cliente2);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremClientes() {

        when(clienteRepo.findAll()).thenReturn(List.of());

        List<ClienteResponseDTO> resultado = clienteService.listarClientes();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(clienteRepo).findAll();
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void deveBuscarClientePorId() {

        UUID id = UUID.randomUUID();

        Cliente cliente = mock(Cliente.class);

        ClienteResponseDTO response = new ClienteResponseDTO(
                id, "João Silva", "joao@email.com", "77999999999"
        );

        when(clienteRepo.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteMapper.paraResponseDTO(cliente)).thenReturn(response);

        ClienteResponseDTO resultado = clienteService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(clienteRepo).findById(id);
        verify(clienteMapper).paraResponseDTO(cliente);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {

        UUID id = UUID.randomUUID();

        when(clienteRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                ClienteNotFoundException.class,
                () -> clienteService.buscarPorId(id)
        );

        verify(clienteRepo).findById(id);
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void deveAtualizarCliente() {

        UUID id = UUID.randomUUID();

        ClienteAtualizacaoDTO dto = new ClienteAtualizacaoDTO(
                "João Atualizado",
                "joao.atualizado@email.com",
                "77999999999"
        );

        Cliente cliente = mock(Cliente.class);

        ClienteResponseDTO response = new ClienteResponseDTO(
                id,
                "João Atualizado",
                "joao.atualizado@email.com",
                "77999999999"
        );

        when(clienteRepo.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteRepo.save(cliente)).thenReturn(cliente);
        when(clienteMapper.paraResponseDTO(cliente)).thenReturn(response);

        ClienteResponseDTO resultado = clienteService.atualizarCliente(id, dto);

        assertNotNull(resultado);
        assertEquals(response, resultado);

        verify(clienteRepo).findById(id);
        verify(clienteMapper).atualizarEntidade(dto, cliente);
        verify(clienteRepo).save(cliente);
        verify(clienteMapper).paraResponseDTO(cliente);
    }

    @Test
    void deveLancarExcecaoAoAtualizarClienteInexistente() {

        UUID id = UUID.randomUUID();

        ClienteAtualizacaoDTO dto = new ClienteAtualizacaoDTO(
                "João",
                "joao@email.com",
                "77999999999"
        );

        when(clienteRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                ClienteNotFoundException.class,
                () -> clienteService.atualizarCliente(id, dto)
        );

        verify(clienteRepo).findById(id);
        verifyNoInteractions(clienteMapper);
        verify(clienteRepo, never()).save(any(Cliente.class));
    }
}

