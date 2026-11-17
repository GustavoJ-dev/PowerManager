
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraAtualizacaoDTO;
import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraCadastroDTO;
import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.exception.cliente.ClienteNotFoundException;
import com.powermanager.powermanager.exception.unidade_consumidora.UnidadeConsumidoraAlreadyExistsException;
import com.powermanager.powermanager.exception.unidade_consumidora.UnidadeConsumidoraNotFoundException;
import com.powermanager.powermanager.mapper.UnidadeConsumidoraMapper;
import com.powermanager.powermanager.repository.ClienteRepo;
import com.powermanager.powermanager.repository.UnidadeConsumidoraRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnidadeConsumidoraService {

    private final UnidadeConsumidoraRepo unidadeConsumidoraRepo;
    private final ClienteRepo clienteRepo;
    private final UnidadeConsumidoraMapper unidadeConsumidoraMapper;

    @Transactional
    public UnidadeConsumidoraResponseDTO cadastrarUnidadeConsumidora(UnidadeConsumidoraCadastroDTO unidadeDTO) {

        Cliente cliente = clienteRepo.findById(unidadeDTO.clienteId())
                .orElseThrow(() ->
                        new ClienteNotFoundException("Cliente não encontrado."));

        verificarDuplicidade(
                unidadeDTO.distribuidora(),
                unidadeDTO.codigo()
        );

        UnidadeConsumidora unidade =
                unidadeConsumidoraMapper.paraEntidade(unidadeDTO);

        unidade.setCliente(cliente);

        UnidadeConsumidora unidadeSalva =
                unidadeConsumidoraRepo.save(unidade);

        return unidadeConsumidoraMapper.paraResponseDTO(unidadeSalva);
    }

    public List<UnidadeConsumidoraResponseDTO> listarUnidadesConsumidoras() {

        return unidadeConsumidoraRepo.findAll()
                .stream()
                .map(unidadeConsumidoraMapper::paraResponseDTO)
                .toList();
    }

    public UnidadeConsumidoraResponseDTO buscarPorId(UUID id) {

        UnidadeConsumidora unidade = buscarEntidadePorId(id);

        return unidadeConsumidoraMapper.paraResponseDTO(unidade);
    }

    public List<UnidadeConsumidoraResponseDTO> listarPorCliente(UUID clienteId) {

        if (!clienteRepo.existsById(clienteId)) {
            throw new ClienteNotFoundException("Cliente não encontrado.");
        }

        return unidadeConsumidoraRepo.findByCliente_Id(clienteId)
                .stream()
                .map(unidadeConsumidoraMapper::paraResponseDTO)
                .toList();
    }

    @Transactional
    public UnidadeConsumidoraResponseDTO atualizarUnidadeConsumidora(UUID id,
                                                                     UnidadeConsumidoraAtualizacaoDTO unidadeDTO) {

        UnidadeConsumidora unidade = buscarEntidadePorId(id);

        verificarDuplicidadeNaAtualizacao(unidadeDTO.distribuidora(), unidadeDTO.codigo(), id);

        unidadeConsumidoraMapper.atualizarEntidade(unidadeDTO, unidade);

        UnidadeConsumidora unidadeAtualizada =
                unidadeConsumidoraRepo.save(unidade);

        return unidadeConsumidoraMapper.paraResponseDTO(unidadeAtualizada);
    }

    private UnidadeConsumidora buscarEntidadePorId(UUID id) {

        return unidadeConsumidoraRepo.findById(id)
                .orElseThrow(() ->
                        new UnidadeConsumidoraNotFoundException(
                                "Unidade consumidora não encontrada."
                        ));
    }

    private void verificarDuplicidade(String distribuidora, String codigo) {

        if (unidadeConsumidoraRepo
                .existsByDistribuidoraAndCodigo(distribuidora, codigo)) {

            throw new UnidadeConsumidoraAlreadyExistsException(
                    "Já existe uma unidade consumidora com este código nesta distribuidora."
            );
        }
    }

    private void verificarDuplicidadeNaAtualizacao(String distribuidora, String codigo, UUID unidadeId) {

        unidadeConsumidoraRepo
                .findByDistribuidoraAndCodigo(distribuidora, codigo)
                .filter(unidadeEncontrada ->
                        !unidadeEncontrada.getId().equals(unidadeId))
                .ifPresent(unidadeEncontrada -> {
                    throw new UnidadeConsumidoraAlreadyExistsException(
                            "Já existe outra unidade consumidora com este código nesta distribuidora."
                    );
                });
    }
}
