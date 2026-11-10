package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.fatura.FaturaCadastroDTO;
import com.powermanager.powermanager.dto.fatura.FaturaCalculoDTO;
import com.powermanager.powermanager.dto.fatura.FaturaResponseDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.enums.StatusFatura;
import com.powermanager.powermanager.exception.FaturaAlreadyExistsException;
import com.powermanager.powermanager.exception.FaturaAlreadyPaidException;
import com.powermanager.powermanager.exception.FaturaNotFoundException;
import com.powermanager.powermanager.exception.FaturaValidationException;
import com.powermanager.powermanager.exception.MedidorNotFoundException;
import com.powermanager.powermanager.mapper.FaturaMapper;
import com.powermanager.powermanager.repository.FaturaRepo;
import com.powermanager.powermanager.repository.MedidorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FaturaService {

    private final FaturaRepo faturaRepo;
    private final MedidorRepo medidorRepo;
    private final TaxaService taxaService;
    private final FaturaMapper faturaMapper;

    /**
     * Simula o cálculo de uma fatura utilizando a taxa vigente
     * no momento da consulta.
     */
    @Transactional(readOnly = true)
    public BigDecimal calcularFatura(FaturaCalculoDTO dto) {

        if (dto == null) {

            throw new FaturaValidationException("Os dados para cálculo da fatura são obrigatórios.");
        }

        validarUnidades(dto.unidadesConsumidas());

        TaxaResponseDTO taxa = taxaService.obterTaxaVigente();

        return calcularValorTotal(dto.unidadesConsumidas(), taxa);
    }

    /**
     * Gera uma nova fatura para determinado medidor e competência.
     *
     * A taxa utilizada é aquela vigente no primeiro dia
     * da competência informada.
     */
    @Transactional
    public FaturaResponseDTO gerarFatura(FaturaCadastroDTO dto) {

        if (dto == null) {

            throw new FaturaValidationException("Os dados da fatura são obrigatórios.");
        }

        validarDadosFatura(dto.medidorId(), dto.competencia(), dto.unidadesConsumidas());

        Medidor medidor = medidorRepo.findById(dto.medidorId()).orElseThrow(
                () -> new MedidorNotFoundException("Medidor não encontrado: " + dto.medidorId()));

        verificarFaturaExistente(medidor, dto.competencia());

        // Busca a taxa vigente no primeiro dia da competência.
        TaxaResponseDTO taxa = taxaService.obterTaxaNaData(dto.competencia().atStartOfDay());

        BigDecimal valorTotal = calcularValorTotal(dto.unidadesConsumidas(), taxa);

        Fatura fatura = new Fatura();

        fatura.setCompetencia(dto.competencia());
        fatura.setUnidadesConsumidas(dto.unidadesConsumidas());

        /*
         * Snapshot da taxa utilizada no momento da geração.
         * Alterações futuras nas taxas não modificam
         * os valores de faturas já emitidas.
         */
        fatura.setCustoPorUnidadeAplicado(taxa.custoPorUnidade());
        fatura.setAluguelMedidorAplicado(taxa.aluguelMedidor());
        fatura.setTaxaServicoAplicada(taxa.taxaServico());
        fatura.setImpostoServicoAplicado(taxa.impostoServico());
        fatura.setCessAplicado(taxa.cess());
        fatura.setTaxaFixaAplicada(taxa.taxaFixa());

        fatura.setValorTotal(valorTotal);
        fatura.setStatusFatura(StatusFatura.PENDENTE);
        fatura.setMedidor(medidor);

        Fatura faturaSalva = faturaRepo.save(fatura);

        return faturaMapper.paraResponseDTO(faturaSalva);
    }

    /**
     * Lista todas as faturas cadastradas.
     */
    @Transactional(readOnly = true)
    public List<FaturaResponseDTO> listarFaturas() {

        return faturaRepo.findAll()
                .stream()
                .map(faturaMapper::paraResponseDTO)
                .toList();
    }

    /**
     * Lista todas as faturas de determinado medidor.
     */
    @Transactional(readOnly = true)
    public List<FaturaResponseDTO> listarPorMedidor(UUID id) {

        Medidor medidor = buscarMedidor(id);

        return faturaRepo.findByMedidor(medidor)
                .stream()
                .map(faturaMapper::paraResponseDTO)
                .toList();
    }

    /**
     * Busca uma fatura de determinado medidor
     * em uma competência específica.
     */
    @Transactional(readOnly = true)
    public FaturaResponseDTO buscaPorMedidorECompetencia(UUID id, LocalDate competencia) {

        validarCompetencia(competencia);

        Medidor medidor = buscarMedidor(id);

        Fatura fatura = faturaRepo.findByMedidorAndCompetencia(medidor, competencia).orElseThrow(
                () -> new FaturaNotFoundException("Fatura não encontrada para o medidor: "
                                + id + " na competência: " + competencia));

        return faturaMapper.paraResponseDTO(fatura);
    }

    /**
     * Retorna o histórico de faturas de um medidor,
     * ordenado da competência mais recente para a mais antiga.
     */
    @Transactional(readOnly = true)
    public List<FaturaResponseDTO> listarHistoricoPorMedidor(UUID id) {

        Medidor medidor = buscarMedidor(id);

        return faturaRepo.findByMedidorOrderByCompetenciaDesc(medidor)
                .stream()
                .map(faturaMapper::paraResponseDTO)
                .toList();
    }

    /**
     * Busca uma fatura pelo seu identificador.
     */
    @Transactional(readOnly = true)
    public FaturaResponseDTO obterDetalhesDaFatura(UUID id) {

        Fatura fatura = buscarFatura(id);

        return faturaMapper.paraResponseDTO(fatura);
    }

    /**
     * Marca uma fatura como paga.
     */
    @Transactional
    public FaturaResponseDTO pagarFatura(UUID id) {

        Fatura fatura = buscarFatura(id);

        if (fatura.getStatusFatura() == StatusFatura.PAGO) {

            throw new FaturaAlreadyPaidException("Essa fatura já está paga.");
        }

        fatura.setStatusFatura(StatusFatura.PAGO);

        Fatura faturaSalva = faturaRepo.save(fatura);

        return faturaMapper.paraResponseDTO(faturaSalva);
    }

    /**
     * Calcula o valor total da fatura.
     *
     * Fórmula:
     *
     * consumo x custo por unidade
     * + aluguel do medidor
     * + taxa de serviço
     * + imposto de serviço
     * + CESS
     * + taxa fixa
     */
    private BigDecimal calcularValorTotal(BigDecimal unidadesConsumidas, TaxaResponseDTO taxa) {

        BigDecimal consumo = unidadesConsumidas.multiply(taxa.custoPorUnidade());

        return consumo
                .add(taxa.aluguelMedidor())
                .add(taxa.taxaServico())
                .add(taxa.impostoServico())
                .add(taxa.cess())
                .add(taxa.taxaFixa())
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Valida os dados necessários para gerar uma fatura.
     */
    private void validarDadosFatura(UUID medidorId, LocalDate competencia, BigDecimal unidadesConsumidas) {

        if (medidorId == null) {

            throw new FaturaValidationException("O medidor é obrigatório.");
        }

        validarCompetencia(competencia);
        validarUnidades(unidadesConsumidas);
    }

    /**
     * Valida se a competência corresponde ao primeiro dia do mês.
     */
    private void validarCompetencia(LocalDate competencia) {

        if (competencia == null) {

            throw new FaturaValidationException("A competência é obrigatória.");
        }

        if (competencia.getDayOfMonth() != 1) {

            throw new FaturaValidationException("A competência deve corresponder ao primeiro dia do mês.");
        }
    }

    /**
     * Valida as unidades consumidas.
     */
    private void validarUnidades(BigDecimal unidadesConsumidas) {

        if (unidadesConsumidas == null) {

            throw new FaturaValidationException("As unidades consumidas são obrigatórias.");
        }

        if (unidadesConsumidas.compareTo(BigDecimal.ZERO) < 0) {

            throw new FaturaValidationException("As unidades consumidas não podem ser negativas.");
        }
    }

    /**
     * Verifica se já existe uma fatura para o medidor
     * na competência informada.
     */
    private void verificarFaturaExistente(Medidor medidor, LocalDate competencia) {

        if (faturaRepo.findByMedidorAndCompetencia(medidor, competencia).isPresent()) {

            throw new FaturaAlreadyExistsException(
                    "Já existe uma fatura para o medidor: "
                            + medidor.getNumero()
                            + " na competência: "
                            + competencia);
        }
    }

    /**
     * Busca um medidor pelo identificador.
     */
    private Medidor buscarMedidor(UUID id) {

        if (id == null) {

            throw new FaturaValidationException("O identificador do medidor é obrigatório.");
        }

        return medidorRepo.findById(id)
                .orElseThrow(() -> new MedidorNotFoundException("Medidor não encontrado: " + id));
    }

    /**
     * Busca uma fatura pelo identificador.
     */
    private Fatura buscarFatura(UUID id) {

        if (id == null) {

            throw new FaturaValidationException("O identificador da fatura é obrigatório.");
        }

        return faturaRepo.findById(id)
                .orElseThrow(() -> new FaturaNotFoundException("Fatura não encontrada: " + id));
    }
}