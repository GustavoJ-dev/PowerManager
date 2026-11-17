
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
import com.powermanager.powermanager.exception.taxa.TaxaNotFoundException;
import com.powermanager.powermanager.exception.taxa.TaxaValidationException;
import com.powermanager.powermanager.mapper.TaxaMapper;
import com.powermanager.powermanager.repository.TaxaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxaService {

    private final TaxaRepo taxaRepo;
    private final TaxaMapper taxaMapper;

    @Transactional
    public TaxaResponseDTO cadastrarTaxa(TaxaCadastroDTO dto) {

        validarCadastro(dto);

        Taxa novaTaxa = taxaMapper.paraEntidade(dto);

        validarSobreposicao(novaTaxa);

        Taxa taxaSalva = taxaRepo.save(novaTaxa);

        return taxaMapper.paraResponseDTO(taxaSalva);
    }

    @Transactional(readOnly = true)
    public List<TaxaResponseDTO> listarTaxas() {

        return taxaRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(Taxa::getInicioVigencia))
                .map(taxaMapper::paraResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaxaResponseDTO buscarPorId(UUID id) {

        if (id == null) {

            throw new TaxaValidationException("O identificador da taxa não pode ser nulo.");
        }

        Taxa taxa = taxaRepo.findById(id)
                .orElseThrow(() -> new TaxaNotFoundException(
                        "Taxa não encontrada para o ID: " + id
                ));

        return taxaMapper.paraResponseDTO(taxa);
    }

    @Transactional(readOnly = true)
    public TaxaResponseDTO buscarTaxaVigenteEm(String distribuidora, SubgrupoTarifario subgrupo, ClasseConsumo classe,
                                               ModalidadeTarifaria modalidade, LocalDate data) {

        validarParametrosBusca(
                distribuidora, subgrupo, classe, modalidade, data
        );

        Taxa taxa = taxaRepo.buscarTaxaVigenteEm(
                distribuidora.trim(),
                subgrupo,
                classe,
                modalidade,
                data
        ).orElseThrow(() -> new TaxaNotFoundException(
                "Nenhuma taxa vigente encontrada para a combinação informada na data: "
                        + data
        ));

        return taxaMapper.paraResponseDTO(taxa);
    }

    @Transactional
    public TaxaResponseDTO reajustarTaxa(TaxaCadastroDTO dto) {

        validarCadastro(dto);

        Taxa taxaAtual = taxaRepo.buscarTaxaVigenteEm(
                dto.distribuidora().trim(),
                dto.subgrupoTarifario(),
                dto.classeConsumo(),
                dto.modalidadeTarifaria(),
                dto.inicioVigencia()
        ).orElseThrow(() -> new TaxaNotFoundException(
                "Não existe uma taxa vigente para encerrar na data informada."
        ));

        if (!dto.inicioVigencia().isAfter(taxaAtual.getInicioVigencia())) {

            throw new TaxaValidationException("A nova vigência deve começar depois do início da taxa anterior.");
        }

        Taxa novaTaxa = taxaMapper.paraEntidade(dto);

        validarSobreposicaoNoReajuste(novaTaxa, taxaAtual);

        // Encerra a vigência anterior no início da nova.
        taxaAtual.setFimVigencia(dto.inicioVigencia());

        // O registro anterior permanece preservado.
        // O dirty checking persiste seu encerramento na transação.
        Taxa taxaSalva = taxaRepo.save(novaTaxa);

        return taxaMapper.paraResponseDTO(taxaSalva);
    }

    private void validarCadastro(TaxaCadastroDTO dto) {

        if (dto == null) {

            throw new TaxaValidationException("Os dados da taxa não podem ser nulos.");
        }

        if (dto.distribuidora() == null || dto.distribuidora().isBlank()) {

            throw new TaxaValidationException("A distribuidora é obrigatória.");
        }

        if (dto.subgrupoTarifario() == null || dto.classeConsumo() == null || dto.modalidadeTarifaria() == null) {

            throw new TaxaValidationException("A classificação tarifária é obrigatória.");
        }

        validarValor(dto.tarifaEnergia(), "TE");
        validarValor(dto.tarifaUsoDistribuicao(), "TUSD");

        if (dto.inicioVigencia() == null) {

            throw new TaxaValidationException("O início da vigência é obrigatório.");
        }

        validarPeriodo(dto.inicioVigencia(), dto.fimVigencia());
    }

    private void validarValor(BigDecimal valor, String campo) {

        if (valor == null) {

            throw new TaxaValidationException(campo + " não pode ser nula.");
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {

            throw new TaxaValidationException(campo + " não pode ser negativa.");
        }
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {

        if (fim != null && !fim.isAfter(inicio)) {

            throw new TaxaValidationException("O fim da vigência deve ser posterior ao início.");
        }
    }

    private void validarParametrosBusca(String distribuidora, SubgrupoTarifario subgrupo, ClasseConsumo classe,
                                        ModalidadeTarifaria modalidade, LocalDate data
    ) {

        if (distribuidora == null || distribuidora.isBlank() || subgrupo == null || classe == null ||
                modalidade == null || data == null) {

            throw new TaxaValidationException("Todos os parâmetros da consulta de taxa vigente são obrigatórios.");
        }
    }

    private void validarSobreposicao(Taxa taxa) {

        List<Taxa> sobrepostas = taxaRepo.buscarTaxasComSobreposicao(
                taxa.getDistribuidora().trim(),
                taxa.getSubgrupoTarifario(),
                taxa.getClasseConsumo(),
                taxa.getModalidadeTarifaria(),
                taxa.getInicioVigencia(),
                taxa.getFimVigencia()
        );

        if (!sobrepostas.isEmpty()) {
            throw new TaxaValidationException(
                    "Já existe uma taxa com vigência sobreposta para essa combinação tarifária."
            );
        }
    }

    private void validarSobreposicaoNoReajuste(Taxa novaTaxa, Taxa taxaAtual) {

        List<Taxa> sobrepostas = taxaRepo.buscarTaxasComSobreposicao(
                novaTaxa.getDistribuidora().trim(),
                novaTaxa.getSubgrupoTarifario(),
                novaTaxa.getClasseConsumo(),
                novaTaxa.getModalidadeTarifaria(),
                novaTaxa.getInicioVigencia(),
                novaTaxa.getFimVigencia()
        );

        boolean existeOutraSobreposicao = sobrepostas.stream()
                .anyMatch(taxa -> !taxa.getId().equals(taxaAtual.getId()));

        if (existeOutraSobreposicao) {
            throw new TaxaValidationException(
                    "A nova taxa possui sobreposição com outro registro tarifário."
            );
        }
    }
}
