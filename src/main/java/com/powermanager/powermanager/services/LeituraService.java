package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.leitura.LeituraCadastroDTO;
import com.powermanager.powermanager.dto.leitura.LeituraResponseDTO;
import com.powermanager.powermanager.entity.Leitura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.exception.leitura.LeituraNotFoundException;
import com.powermanager.powermanager.exception.leitura.LeituraValidationException;
import com.powermanager.powermanager.exception.medidor.MedidorNotFoundException;
import com.powermanager.powermanager.mapper.LeituraMapper;
import com.powermanager.powermanager.repository.LeituraRepo;
import com.powermanager.powermanager.repository.MedidorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class LeituraService {

    private final LeituraRepo leituraRepo;
    private final MedidorRepo medidorRepo;
    private final LeituraMapper leituraMapper;

    public LeituraResponseDTO registrarLeitura(LeituraCadastroDTO dto) {

        validarDadosCadastro(dto);

        Medidor medidor = medidorRepo.findById(dto.medidorId())
                .orElseThrow(() -> new MedidorNotFoundException("Medidor não encontrado."
                ));

        validarPeriodoMedidor(medidor, dto.dataLeitura());

        boolean leituraDuplicada =
                leituraRepo.existsByMedidor_IdAndDataLeitura(
                        dto.medidorId(),
                        dto.dataLeitura()
                );

        if (leituraDuplicada) {

            throw new LeituraValidationException("Já existe uma leitura registrada para este medidor nesse instante.");
        }

        validarSequenciaLeituras(dto.medidorId(), dto.dataLeitura(), dto.valorLeitura());

        Leitura leitura = leituraMapper.paraEntidade(dto);
        leitura.setMedidor(medidor);

        Leitura leituraSalva = leituraRepo.save(leitura);

        return leituraMapper.paraResponseDTO(leituraSalva);
    }

    @Transactional(readOnly = true)
    public List<LeituraResponseDTO> listarLeituras() {

        return leituraRepo.findAll()
                .stream()
                .map(leituraMapper::paraResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public LeituraResponseDTO buscarPorId(UUID id) {

        Leitura leitura = buscarEntidadePorId(id);

        return leituraMapper.paraResponseDTO(leitura);
    }

    @Transactional(readOnly = true)
    public List<LeituraResponseDTO> listarPorMedidor(UUID medidorId) {

        validarExistenciaMedidor(medidorId);

        return leituraRepo.findByMedidor_IdOrderByDataLeituraAsc(medidorId)
                .stream()
                .map(leituraMapper::paraResponseDTO)
                .toList();
    }

    private void validarDadosCadastro(LeituraCadastroDTO dto) {

        if (dto == null) {

            throw new LeituraValidationException("Os dados da leitura são obrigatórios.");
        }

        if (dto.valorLeitura() == null || dto.valorLeitura().compareTo(BigDecimal.ZERO) < 0) {

            throw new LeituraValidationException("O valor da leitura deve ser maior ou igual a zero.");
        }

        if (dto.dataLeitura() == null) {

            throw new LeituraValidationException("A data e hora da leitura são obrigatórias.");
        }

        if (dto.tipoLeitura() == null) {

            throw new LeituraValidationException("O tipo da leitura é obrigatório.");
        }

        if (dto.medidorId() == null) {

            throw new LeituraValidationException("O medidor associado é obrigatório.");
        }
    }

    private void validarPeriodoMedidor(Medidor medidor, LocalDateTime dataLeitura) {

        LocalDateTime inicioInstalacao = medidor.getDataInstalacao().atStartOfDay();

        if (dataLeitura.isBefore(inicioInstalacao)) {

            throw new LeituraValidationException("A leitura não pode ser anterior à instalação do medidor.");
        }

        if (Boolean.TRUE.equals(medidor.getAtivo())) {

            if (medidor.getDataRetirada() != null) {

                throw new LeituraValidationException("O medidor possui situação inconsistente.");
            }

            return;
        }

        if (medidor.getDataRetirada() == null) {

            throw new LeituraValidationException("O medidor retirado precisa possuir data de retirada.");
        }

        if (dataLeitura.toLocalDate().isAfter(medidor.getDataRetirada())) {

            throw new LeituraValidationException("Não é permitido registrar leitura após a retirada do medidor.");
        }
    }

    private void validarSequenciaLeituras(UUID medidorId, LocalDateTime dataLeitura, BigDecimal valorLeitura) {

        leituraRepo
                .findTopByMedidor_IdAndDataLeituraBeforeOrderByDataLeituraDesc(
                        medidorId,
                        dataLeitura
                )
                .ifPresent(leituraAnterior -> {
                    if (valorLeitura.compareTo(
                            leituraAnterior.getValorLeitura()
                    ) < 0) {
                        throw new LeituraValidationException(
                                "O valor da leitura não pode ser inferior ao da leitura anterior."
                        );
                    }
                });

        leituraRepo
                .findTopByMedidor_IdAndDataLeituraAfterOrderByDataLeituraAsc(
                        medidorId,
                        dataLeitura
                )
                .ifPresent(proximaLeitura -> {
                    if (valorLeitura.compareTo(
                            proximaLeitura.getValorLeitura()
                    ) > 0) {
                        throw new LeituraValidationException(
                                "O valor da leitura não pode ser superior ao da próxima leitura registrada."
                        );
                    }
                });
    }

    private void validarExistenciaMedidor(UUID medidorId) {

        if (medidorId == null || !medidorRepo.existsById(medidorId)) {

            throw new MedidorNotFoundException("Medidor não encontrado.");
        }
    }

    private Leitura buscarEntidadePorId(UUID id) {

        return leituraRepo.findById(id)
                .orElseThrow(() -> new LeituraNotFoundException("Leitura não encontrada."
                ));
    }
}
