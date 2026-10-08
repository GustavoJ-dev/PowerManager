package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.exception.ClienteNotFoundException;
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

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deveCadastrarCliente(){

        Cliente cliente = new Cliente();

        when(clienteRepo.save(cliente)).thenReturn(cliente);

        Cliente resultado = clienteService.cadastrarCliente(cliente);

        assertEquals(cliente, resultado);

        verify(clienteRepo).save(cliente);
    }

    @Test
    void deveListarClientes(){

        List<Cliente> clientes = List.of(new Cliente(), new Cliente());

        when(clienteRepo.findAll()).thenReturn(clientes);

        List<Cliente> resultado = clienteService.listarClientes();

        assertEquals(clientes, resultado);

        verify(clienteRepo).findAll();
    }

    @Test
    void deveBuscarClientePorId(){

        UUID id = UUID.randomUUID();

        Cliente cliente = new Cliente();

        when(clienteRepo.findById(id)).thenReturn(Optional.of(cliente));

        Cliente resultado = clienteService.buscarPorId(id);

        assertEquals(cliente, resultado);

        verify(clienteRepo).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoForEncontrado() {

        UUID id = UUID.randomUUID();

        when(clienteRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                ClienteNotFoundException.class,
                () -> clienteService.buscarPorId(id));

        verify(clienteRepo).findById(id);
    }

    @Test
    void deveExcluirCliente() {

        UUID id = UUID.randomUUID();
        Cliente cliente = new Cliente();

        when(clienteRepo.findById(id)).thenReturn(Optional.of(cliente));

        clienteService.excluir(id);

        verify(clienteRepo).findById(id);
        verify(clienteRepo).deleteById(id);
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
    }
}
