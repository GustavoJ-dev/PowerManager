
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.taxa.TaxaAtualizacaoDTO;
import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.exception.TaxaNotFoundException;
import com.powermanager.powermanager.exception.TaxaValidationException;
import com.powermanager.powermanager.mapper.TaxaMapper;
import com.powermanager.powermanager.repository.TaxaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxaService {

    private final TaxaRepo taxaRepo;
    private final TaxaMapper taxaMapper;

    /**
     * Cadastra a primeira taxa, desde que não exista
     * outra vigência sobreposta.
     */
    @Transactional
    public TaxaResponseDTO cadastrarNovaTaxa(TaxaCadastroDTO taxaCadastroDTO) {

        if (taxaCadastroDTO == null) {

            throw new TaxaValidationException("A taxa não pode ser nula");
        }

        LocalDateTime agora = LocalDateTime.now();

        Taxa novaTaxa = taxaMapper.paraEntidade(taxaCadastroDTO);

        // As datas de vigência são controladas pelo Service.
        novaTaxa.setInicioVigencia(agora);
        novaTaxa.setFimVigencia(null);

        validarTaxa(novaTaxa);
        validarPeriodo(novaTaxa.getInicioVigencia(), novaTaxa.getFimVigencia());
        validarSobreposicao(agora, null);

        Taxa taxaSalva = taxaRepo.save(novaTaxa);

        return taxaMapper.paraResponseDTO(taxaSalva);
    }

    /**
     * Retorna a taxa vigente no momento da consulta.
     */
    @Transactional(readOnly = true)
    public TaxaResponseDTO obterTaxaVigente() {

        return obterTaxaNaData(LocalDateTime.now());
    }

    /**
     * Retorna a taxa vigente na data informada.
     */
    @Transactional(readOnly = true)
    public TaxaResponseDTO obterTaxaNaData(LocalDateTime data) {

        if (data == null) {

            throw new TaxaValidationException("A data para consulta não pode ser nula");
        }

        Taxa taxa = buscarTaxaVigenteEntidade(data);

        return taxaMapper.paraResponseDTO(taxa);
    }

    /**
     * Retorna o histórico de taxas em ordem cronológica.
     */
    @Transactional(readOnly = true)
    public List<TaxaResponseDTO> listarHistorico() {

        return taxaRepo.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(Taxa::getInicioVigencia)
                                .thenComparing(Taxa::getId)
                )
                .map(taxaMapper::paraResponseDTO)
                .toList();
    }

    /**
     * Encerra a vigência da taxa atual e cadastra uma nova,
     * preservando os dados da taxa anterior.
     */
    @Transactional
    public TaxaResponseDTO atualizarTaxa(TaxaAtualizacaoDTO novaTaxaDTO) {

        if (novaTaxaDTO == null) {

            throw new TaxaValidationException("A taxa não pode ser nula");
        }

        LocalDateTime agora = LocalDateTime.now();

        Taxa taxaAtual = buscarTaxaVigenteEntidade(agora);

        // Impede uma vigência com duração igual a zero.
        if (!agora.isAfter(taxaAtual.getInicioVigencia())) {

            throw new TaxaValidationException("A taxa atual precisa ter iniciado antes da atualização");
        }

        // Verifica sobreposições, desconsiderando a taxa atual.
        validarSobreposicaoNaAtualizacao(agora, taxaAtual.getId());

        // Cria uma nova entidade para preservar o histórico.
        Taxa novaTaxa = new Taxa();

        taxaMapper.atualizarEntidade(novaTaxaDTO, novaTaxa);

        novaTaxa.setInicioVigencia(agora);
        novaTaxa.setFimVigencia(null);

        validarTaxa(novaTaxa);
        validarPeriodo(novaTaxa.getInicioVigencia(), novaTaxa.getFimVigencia());

        // Encerra a vigência da taxa anterior.
        taxaAtual.setFimVigencia(agora);

        validarPeriodo(taxaAtual.getInicioVigencia(), taxaAtual.getFimVigencia());

        // O dirty checking persiste a alteração da taxa atual.
        Taxa taxaSalva = taxaRepo.save(novaTaxa);

        return taxaMapper.paraResponseDTO(taxaSalva);
    }

    /**
     * Busca a entidade da taxa vigente em determinada data.
     */
    private Taxa buscarTaxaVigenteEntidade(LocalDateTime data) {

        return taxaRepo.buscarTaxaVigenteEm(data).orElseThrow(
                        () -> new TaxaNotFoundException(
                                "Nenhuma taxa vigente encontrada na data: " + data));
    }

    /**
     * Valida todos os valores monetários da taxa.
     */
    private void validarTaxa(Taxa taxa) {

        if (taxa == null) {

            throw new TaxaValidationException("A taxa não pode ser nula");
        }

        validarValor(taxa.getCustoPorUnidade(), "Custo por unidade");
        validarValor(taxa.getAluguelMedidor(), "Aluguel do medidor");
        validarValor(taxa.getTaxaServico(), "Taxa de serviço");
        validarValor(taxa.getImpostoServico(), "Imposto de serviço");
        validarValor(taxa.getCess(), "CESS");
        validarValor(taxa.getTaxaFixa(), "Taxa fixa");
    }

    /**
     * Garante que um valor monetário seja informado e não negativo.
     */
    private void validarValor(BigDecimal valor, String campo) {

        if (valor == null) {

            throw new TaxaValidationException(campo + " não pode ser nulo");
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {

            throw new TaxaValidationException(campo + " não pode ser negativo");
        }
    }

    /**
     * Valida o intervalo de vigência.
     */
    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {

        if (inicio == null) {

            throw new TaxaValidationException("O início da vigência não pode ser nulo");
        }

        if (fim != null && !fim.isAfter(inicio)) {

            throw new TaxaValidationException("O fim da vigência deve ser posterior ao início");
        }
    }

    /**
     * Impede o cadastro de uma taxa com vigência sobreposta.
     */
    private void validarSobreposicao(LocalDateTime inicio, LocalDateTime fim) {

        List<Taxa> taxasSobrepostas = taxaRepo.buscarTaxasComSobreposicao(inicio, fim);

        if (!taxasSobrepostas.isEmpty()) {

            throw new TaxaValidationException("O período informado se sobrepõe a uma taxa já existente");
        }
    }

    /**
     * Verifica sobreposições durante uma atualização,
     * desconsiderando a taxa que será encerrada.
     */
    private void validarSobreposicaoNaAtualizacao(LocalDateTime inicio, UUID idTaxaAtual) {

        List<Taxa> taxasSobrepostas = taxaRepo.buscarTaxasComSobreposicao(inicio, null);

        boolean existeOutraTaxa = taxasSobrepostas.stream()
                .anyMatch(taxa -> !taxa.getId().equals(idTaxaAtual));

        if (existeOutraTaxa) {

            throw new TaxaValidationException("Existe outra taxa com vigência sobreposta à nova taxa");
        }
    }
}
