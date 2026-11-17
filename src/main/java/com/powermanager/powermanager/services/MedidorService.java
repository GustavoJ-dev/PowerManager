
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.medidor.MedidorAtualizacaoDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.exception.medidor.MedidorNotFoundException;
import com.powermanager.powermanager.exception.medidor.MedidorValidationException;
import com.powermanager.powermanager.exception.unidade_consumidora.UnidadeConsumidoraNotFoundException;
import com.powermanager.powermanager.mapper.MedidorMapper;
import com.powermanager.powermanager.repository.MedidorRepo;
import com.powermanager.powermanager.repository.UnidadeConsumidoraRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedidorService {

    private final MedidorRepo medidorRepo;
    private final UnidadeConsumidoraRepo unidadeConsumidoraRepo;
    private final MedidorMapper medidorMapper;

    @Transactional
    public MedidorResponseDTO cadastrarMedidor(MedidorCadastroDTO medidorDTO) {

        validarDadosCadastro(medidorDTO);

        UnidadeConsumidora unidade = unidadeConsumidoraRepo
                .findById(medidorDTO.unidadeConsumidoraId())
                .orElseThrow(() -> new UnidadeConsumidoraNotFoundException(
                        "Unidade consumidora não encontrada."));

        if (medidorRepo.findByNumero(medidorDTO.numero()).isPresent()) {

            throw new MedidorValidationException("Já existe um medidor cadastrado com este número.");
        }

        if (medidorRepo.findByUnidadeConsumidora_IdAndAtivoTrue(unidade.getId()).isPresent()) {

            throw new MedidorValidationException("A unidade consumidora já possui um medidor ativo. "
                            + "Realize a retirada do medidor atual antes de cadastrar outro.");
        }

        Medidor medidor = medidorMapper.paraEntidade(medidorDTO);

        medidor.setUnidadeConsumidora(unidade);
        medidor.setDataInstalacao(medidorDTO.dataInstalacao());
        medidor.setDataRetirada(null);
        medidor.setAtivo(true);

        Medidor medidorSalvo = medidorRepo.save(medidor);

        return medidorMapper.paraResponseDTO(medidorSalvo);
    }

    public MedidorResponseDTO buscarPorId(UUID id) {

        Medidor medidor = buscarEntidadePorId(id);

        return medidorMapper.paraResponseDTO(medidor);
    }

    public MedidorResponseDTO buscarPorNumero(String numero) {

        if (numero == null || numero.isBlank()) {

            throw new MedidorValidationException("O número do medidor é obrigatório.");
        }

        Medidor medidor = medidorRepo.findByNumero(numero)
                .orElseThrow(() -> new MedidorNotFoundException("Medidor não encontrado."));

        return medidorMapper.paraResponseDTO(medidor);
    }

    public List<MedidorResponseDTO> listarMedidores() {

        return medidorRepo.findAll()
                .stream()
                .map(medidorMapper::paraResponseDTO)
                .toList();
    }

    public List<MedidorResponseDTO> listarPorUnidadeConsumidora(UUID unidadeConsumidoraId) {

        if (unidadeConsumidoraId == null) {

            throw new MedidorValidationException("O ID da unidade consumidora é obrigatório.");
        }

        if (!unidadeConsumidoraRepo.existsById(unidadeConsumidoraId)) {

            throw new UnidadeConsumidoraNotFoundException("Unidade consumidora não encontrada.");
        }

        return medidorRepo
                .findByUnidadeConsumidora_Id(unidadeConsumidoraId)
                .stream()
                .map(medidorMapper::paraResponseDTO)
                .toList();
    }

    @Transactional
    public MedidorResponseDTO atualizarMedidor(UUID id, MedidorAtualizacaoDTO medidorDTO) {

        if (medidorDTO == null) {

            throw new MedidorValidationException("Os dados de atualização do medidor são obrigatórios.");
        }

        Medidor medidor = buscarEntidadePorId(id);

        medidorMapper.atualizarEntidade(medidorDTO, medidor);

        Medidor medidorAtualizado = medidorRepo.save(medidor);

        return medidorMapper.paraResponseDTO(medidorAtualizado);
    }

    @Transactional
    public MedidorResponseDTO retirarMedidor(UUID id, LocalDate dataRetirada) {

        if (dataRetirada == null) {

            throw new MedidorValidationException("A data de retirada é obrigatória.");
        }

        Medidor medidor = buscarEntidadePorId(id);

        if (!Boolean.TRUE.equals(medidor.getAtivo()) || medidor.getDataRetirada() != null) {

            throw new MedidorValidationException("O medidor informado já está retirado ou possui situação inconsistente.");
        }

        if (medidor.getDataInstalacao() == null || !dataRetirada.isAfter(medidor.getDataInstalacao())) {

            throw new MedidorValidationException("A data de retirada deve ser posterior à data de instalação.");
        }

        medidor.setAtivo(false);
        medidor.setDataRetirada(dataRetirada);

        Medidor medidorRetirado = medidorRepo.save(medidor);

        return medidorMapper.paraResponseDTO(medidorRetirado);
    }

    private Medidor buscarEntidadePorId(UUID id) {

        if (id == null) {

            throw new MedidorValidationException("O ID do medidor é obrigatório.");
        }

        return medidorRepo.findById(id)
                .orElseThrow(() -> new MedidorNotFoundException("Medidor não encontrado."));
    }

    private void validarDadosCadastro(MedidorCadastroDTO medidorDTO) {

        if (medidorDTO == null) {

            throw new MedidorValidationException("Os dados do medidor são obrigatórios.");
        }

        if (medidorDTO.numero() == null || medidorDTO.numero().isBlank()) {

            throw new MedidorValidationException("O número do medidor é obrigatório.");
        }

        if (medidorDTO.dataInstalacao() == null) {

            throw new MedidorValidationException("A data de instalação é obrigatória.");
        }

        if (medidorDTO.unidadeConsumidoraId() == null) {

            throw new MedidorValidationException("A unidade consumidora associada é obrigatória.");
        }
    }
}

