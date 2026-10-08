package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.exception.ClienteNotFoundException;
import com.powermanager.powermanager.repository.ClienteRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepo clienteRepo;

    public Cliente cadastrarCliente(Cliente cliente){

        return clienteRepo.save(cliente);
    }

    public List<Cliente> listarClientes(){

        return clienteRepo.findAll();
    }

    public Cliente buscarPorId(UUID id){

        return clienteRepo.findById(id).orElseThrow(() ->
                new ClienteNotFoundException("Cliente não encontrado"));
    }

    public void excluir(UUID id){

        Cliente cliente = clienteRepo.findById(id).orElseThrow(()->
                new ClienteNotFoundException("Cliente não encontrado"));

        clienteRepo.deleteById(id);
    }
}
