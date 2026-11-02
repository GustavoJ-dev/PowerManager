package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.cliente.ClienteCadastroDTO;
import com.powermanager.powermanager.dto.cliente.ClienteResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.exception.ClienteNotFoundException;
import com.powermanager.powermanager.mapper.ClienteMapper;
import com.powermanager.powermanager.repository.ClienteRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepo clienteRepo;
    private final ClienteMapper clienteMapper;

    public ClienteResponseDTO cadastrarCliente(ClienteCadastroDTO clienteDTO){

        Cliente cliente = clienteMapper.paraEntidade(clienteDTO);

        Cliente clienteSalvo = clienteRepo.save(cliente);

        return clienteMapper.paraResponseDTO(clienteSalvo);
    }

    public List<ClienteResponseDTO> listarClientes(){

        return clienteRepo.findAll().stream()
                .map(clienteMapper::paraResponseDTO)
                .toList();
    }

    public ClienteResponseDTO buscarPorId(UUID id){

        Cliente cliente = clienteRepo.findById(id)
                .orElseThrow(()-> new ClienteNotFoundException("Cliente não encontrado."));

        return clienteMapper.paraResponseDTO(cliente);
    }

    public void excluir(UUID id){

        clienteRepo.findById(id).orElseThrow(
                ()-> new ClienteNotFoundException("Cliente não encontrado"));

        clienteRepo.deleteById(id);
    }
}
