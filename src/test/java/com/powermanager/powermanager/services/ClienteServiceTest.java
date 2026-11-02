package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.cliente.ClienteCadastroDTO;
import com.powermanager.powermanager.dto.cliente.ClienteResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.exception.ClienteNotFoundException;
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
public class ClienteServiceTest {

    @Mock
    private ClienteRepo clienteRepo;

    @Mock
    private ClienteMapper clienteMapper;

    @InjectMocks
    private ClienteService clienteService;


    //DTO convertido para entidade, entidade salva e resposta é convertida em DTO
    @Test
    void deveCadastrarCliente(){

        ClienteCadastroDTO dto = new ClienteCadastroDTO(
                "João Silva",
                "Rua Central, 100",
                "Salvador",
                "Bahia",
                "João@gmail.com",
                "71999999999");

        Cliente cliente = new Cliente();

        Cliente clienteSalvo = new Cliente();

        ClienteResponseDTO resposta = new ClienteResponseDTO(
                UUID.randomUUID(),
                "João Silva",
                "Rua Central, 100",
                "Salvador",
                "Bahia",
                "João@gmail.com",
                "71999999999" );

        when(clienteMapper.paraEntidade(dto)).thenReturn(cliente);
        when(clienteRepo.save(cliente)).thenReturn(clienteSalvo);
        when(clienteMapper.paraResponseDTO(clienteSalvo)).thenReturn(resposta);

        ClienteResponseDTO resultado = clienteService.cadastrarCliente(dto);

        assertEquals(resposta, resultado);

        verify(clienteMapper).paraEntidade(dto);
        verify(clienteRepo).save(cliente);
        verify(clienteMapper).paraResponseDTO(clienteSalvo);
    }

    //cada entidade é convertida para dto de resposta
    @Test
    void deveListarClientes(){

        Cliente cliente1 = new Cliente();
        Cliente cliente2 = new Cliente();

        List<Cliente> clientes = List.of(cliente1, cliente2);

        ClienteResponseDTO resposta1 = new ClienteResponseDTO(
                UUID.randomUUID(),
                "Jõao",
                "RUA A",
                "Salvador",
                "BA",
                "joao@email.com",
                "71111111111");

        ClienteResponseDTO resposta2 = new ClienteResponseDTO(
                UUID.randomUUID(),
                "Maria",
                "Rua B",
                "Salvador",
                "BA",
                "maria@email.com",
                "72222222222");

        when(clienteRepo.findAll()).thenReturn(clientes);
        when(clienteMapper.paraResponseDTO(cliente1)).thenReturn(resposta1);
        when(clienteMapper.paraResponseDTO(cliente2)).thenReturn(resposta2);

        List<ClienteResponseDTO> resultado = clienteService.listarClientes();

        assertEquals(List.of(resposta1, resposta2), resultado);

        verify(clienteRepo).findAll();
        verify(clienteMapper).paraResponseDTO(cliente1);
        verify(clienteMapper).paraResponseDTO(cliente2);
    }

    //cliente encontrado é convertido para DTO
    @Test
    void deveBuscarClientePorId(){

        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente();

        ClienteResponseDTO resposta = new ClienteResponseDTO(
                id,
                "João",
                "Rua A",
                "Salvador",
                "BA",
                "joao@email.com",
                "71111111111");

        when(clienteRepo.findById(id)).thenReturn(Optional.of(cliente));
        when(clienteMapper.paraResponseDTO(cliente)).thenReturn(resposta);

        ClienteResponseDTO resultado = clienteService.buscarPorId(id);

        assertEquals(resposta, resultado);

        verify(clienteRepo).findById(id);
        verify(clienteMapper).paraResponseDTO(cliente);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {

        UUID id = UUID.randomUUID();

        when(clienteRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                ClienteNotFoundException.class,
                () -> clienteService.buscarPorId(id));

        verify(clienteRepo).findById(id);
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void deveExcluirCliente() {

        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente();

        when(clienteRepo.findById(id)).thenReturn(Optional.of(cliente));

        clienteService.excluir(id);

        verify(clienteRepo).findById(id);
        verify(clienteRepo).deleteById(id);
        verifyNoInteractions(clienteMapper);
    }

    @Test
    void deveLancarExcecaoAoExcluirClienteInexistente() {

        UUID id = UUID.randomUUID();

        when(clienteRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ClienteNotFoundException.class,
                () -> clienteService.excluir(id));

        verify(clienteRepo).findById(id);
        verify(clienteRepo, never()).deleteById(id);
        verifyNoInteractions(clienteMapper);
    }
}
