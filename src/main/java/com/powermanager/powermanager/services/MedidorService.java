package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.medidor.MedidorAtualizacaoDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.exception.ClienteNotFoundException;
import com.powermanager.powermanager.exception.MedidorNotFoundException;
import com.powermanager.powermanager.exception.MedidorValidationException;
import com.powermanager.powermanager.mapper.MedidorMapper;
import com.powermanager.powermanager.repository.ClienteRepo;
import com.powermanager.powermanager.repository.MedidorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedidorService {

    private final MedidorRepo medidorRepo;
    private final ClienteRepo clienteRepo;
    private final MedidorMapper medidorMapper;

    public MedidorResponseDTO cadastrarMedidor(MedidorCadastroDTO medidorDTO){

        if (medidorDTO == null){

            throw new MedidorValidationException("Os dados do medidor são obrigatórios.");
        }

        if (medidorDTO.numero() == null || medidorDTO.numero().isBlank()) {

            throw new MedidorValidationException("O número do medidor é obrigatório.");
        }

        if (medidorDTO.clienteId() == null) {

            throw new MedidorValidationException("O cliente associado é obrigatório.");
        }

        if (medidorRepo.findByNumero(medidorDTO.numero()).isPresent()){

            throw new MedidorValidationException(
                    "Já existe um medidor cadastrado com este número: " + medidorDTO.numero());
        }

        Cliente cliente = clienteRepo.findById(medidorDTO.clienteId())
                .orElseThrow(()-> new ClienteNotFoundException("Cliente não encontrado."));


        Medidor medidor = medidorMapper.paraEntidade(medidorDTO);

        medidor.setCliente(cliente);

        Medidor medidorSalvo = medidorRepo.save(medidor);

        return medidorMapper.paraResponseDTO(medidorSalvo);
    }

    public MedidorResponseDTO buscarPorNumero(String numero){

        if (numero == null || numero.isBlank()) {

            throw new MedidorValidationException("O número do medidor é obrigatório.");
        }

        Medidor medidor = medidorRepo.findByNumero(numero).orElseThrow(
                () -> new MedidorNotFoundException("Medidor não encontrado pelo número:" + numero));

        return medidorMapper.paraResponseDTO(medidor);
    }

    public MedidorResponseDTO buscarPorId(UUID id){

        if (id == null) {

            throw new MedidorValidationException("O ID do medidor é obrigatório.");
        }

        Medidor medidor = medidorRepo.findById(id).orElseThrow( ()->
                new MedidorNotFoundException("medidor não encontrado: " + id));

        return medidorMapper.paraResponseDTO(medidor);
    }

    public List<MedidorResponseDTO> listarMedidores(){

        return medidorRepo.findAll()
                .stream()
                .map(medidorMapper::paraResponseDTO)
                .toList();
    }

    public MedidorResponseDTO atualizarMedidor(UUID id, MedidorAtualizacaoDTO medidorAttDTO) {

        if (id == null) {

            throw new MedidorValidationException("O ID do medidor é obrigatório.");
        }

        if (medidorAttDTO == null) {

            throw new MedidorValidationException("Os dados de atualização são obrigatórios.");
        }

        if (medidorAttDTO.numero() == null || medidorAttDTO.numero().isBlank()) {

            throw new MedidorValidationException("O número do medidor é obrigatório.");
        }


        Medidor medidor = medidorRepo.findById(id).orElseThrow(
                ()-> new MedidorNotFoundException("Medidor não encontrado: " + id));

        //verifica se o numero ja pertence a outro medidor

        Optional<Medidor> medidorExistente = medidorRepo.findByNumero(medidorAttDTO.numero());

        if (medidorExistente.isPresent() && !medidorExistente.get().getId().equals(id)){

            throw new MedidorValidationException("Já existe outro medidor cadastrado com esse número");
        }

        medidorMapper.atualizarEntidade(medidorAttDTO, medidor);

        Medidor medidorAtualizado = medidorRepo.save(medidor);

        return medidorMapper.paraResponseDTO(medidorAtualizado);
    }

    public void excluirMedidor(UUID medidorId){

        if (medidorId == null) {

            throw new MedidorValidationException("O ID do medidor é obrigatório.");
        }

        medidorRepo.findById(medidorId).orElseThrow(
                ()-> new MedidorNotFoundException("Medidor não encontrado: " + medidorId));

        medidorRepo.deleteById(medidorId);
    }
}
