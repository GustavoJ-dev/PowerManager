package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.fatura.FaturaResponseDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.entity.enums.StatusFatura;
import com.powermanager.powermanager.exception.*;
import com.powermanager.powermanager.mapper.FaturaMapper;
import com.powermanager.powermanager.mapper.MedidorMapper;
import com.powermanager.powermanager.mapper.TaxaMapper;
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
    private final MedidorService medidorService;
    private final TaxaService taxaService;
    private final FaturaMapper faturaMapper;
    private final TaxaMapper taxaMapper;
    private final MedidorRepo medidorRepo;


    //Calcula uma fatura, utilizando taxa vigente
    @Transactional(readOnly = true)
    public BigDecimal calcularFatura(BigDecimal unidadesConsumidas){

        validarUnidades(unidadesConsumidas);

        TaxaResponseDTO taxaResponse = taxaService.obterTaxaVigente();

        TaxaCadastroDTO taxaCadastroDTO = new TaxaCadastroDTO(
                taxaResponse.custoPorUnidade(),
                taxaResponse.aluguelMedidor(),
                taxaResponse.taxaServico(),
                taxaResponse.impostoServico(),
                taxaResponse.cess(),
                taxaResponse.taxaFixa(),
                taxaResponse.inicioVigencia(),
                taxaResponse.fimVigencia());

        Taxa taxa = taxaMapper.paraEntidade(taxaCadastroDTO);

        return calcularValorTotal(unidadesConsumidas, taxa);
    }

    //gera nova fatura para medidor
    @Transactional
    public FaturaResponseDTO gerarFatura(UUID medidorId, LocalDate competencia, BigDecimal unidadesConsumidas){

        validarDadosFatura(medidorId, competencia, unidadesConsumidas);

        Medidor medidor = medidorRepo.findById(medidorId).orElseThrow(
                ()-> new MedidorNotFoundException("Medidor não encontrado: " + medidorId));

        verificarFaturaExistente(medidor, competencia);

        TaxaResponseDTO taxaResponse = taxaService.obterTaxaVigente();

        TaxaCadastroDTO taxaCadastroDTO = new TaxaCadastroDTO(
                taxaResponse.custoPorUnidade(),
                taxaResponse.aluguelMedidor(),
                taxaResponse.taxaServico(),
                taxaResponse.impostoServico(),
                taxaResponse.cess(),
                taxaResponse.taxaFixa(),
                taxaResponse.inicioVigencia(),
                taxaResponse.fimVigencia());

        Taxa taxa = taxaMapper.paraEntidade(taxaCadastroDTO);

        BigDecimal valorTotal = calcularValorTotal(unidadesConsumidas, taxa);

        Fatura fatura = new Fatura();
        fatura.setCompetencia(competencia);
        fatura.setUnidadesConsumidas(unidadesConsumidas);

        /*
         * Snapshot da taxa utilizada no momento da geração.
         * Alterações futuras na tabela taxa não modificam
         * uma fatura já emitida.
         */

        fatura.setCustoPorUnidadeAplicado(taxa.getCustoPorUnidade());
        fatura.setAluguelMedidorAplicado(taxa.getAluguelMedidor());
        fatura.setTaxaServicoAplicada(taxa.getTaxaServico());
        fatura.setImpostoServicoAplicado(taxa.getImpostoServico());
        fatura.setCessAplicado(taxa.getCess());
        fatura.setTaxaFixaAplicada(taxa.getTaxaFixa());

        fatura.setValorTotal(valorTotal);
        fatura.setStatusFatura(StatusFatura.PENDENTE);
        fatura.setMedidor(medidor);

        Fatura faturaSalva =faturaRepo.save(fatura);

        return faturaMapper.paraResponseDTO(faturaSalva);
    }

    //lista todas as faturas
    @Transactional(readOnly = true)
    public List<FaturaResponseDTO> listarFaturas(){

        return faturaRepo.findAll()
                .stream()
                .map(faturaMapper::paraResponseDTO)
                .toList();
    }

    //lista todas faturas de um medidor
    @Transactional(readOnly = true)
    public List<FaturaResponseDTO> listarPorMedidor(UUID id){

        Medidor medidor = medidorRepo.findById(id).orElseThrow(
                ()-> new MedidorNotFoundException("Medidor não encontrado: " + id));

        return faturaRepo.findByMedidor(medidor)
                .stream()
                .map(faturaMapper::paraResponseDTO)
                .toList();
    }

    //busca determinada fatura de um medidor em uma competência especifíca
    @Transactional(readOnly = true)
    public FaturaResponseDTO buscaPorMedidorECompetencia(UUID id, LocalDate competencia){

        Medidor medidor = medidorRepo.findById(id).orElseThrow(
                ()-> new MedidorNotFoundException("Medidor não encontrado: " + id));

        Fatura fatura = faturaRepo.findByMedidorAndCompetencia(medidor, competencia)
                .orElseThrow(() ->
                        new FaturaNotFoundException("Fatura não encotrada para o medidor: " + id +
                                " na competência: " + competencia));

        return faturaMapper.paraResponseDTO(fatura);

    }

    //retorna o historica de faturas de um medidor pelo mais recente
    @Transactional(readOnly = true)
    public List<FaturaResponseDTO> listarHistoricoPorMedidor(UUID id){

        Medidor medidor = medidorRepo.findById(id).orElseThrow(
                ()-> new MedidorNotFoundException("Medidor não encontrado: " + id));

        return faturaRepo.findByMedidorOrderByCompetenciaDesc(medidor)
                .stream()
                .map(faturaMapper::paraResponseDTO)
                .toList();
    }

    //busca fatura pelo id
    @Transactional(readOnly = true)
    public FaturaResponseDTO obterDetalhesDaFatura(UUID id){

        Fatura fatura = faturaRepo.findById(id).orElseThrow(
                () -> new FaturaNotFoundException("Fatura não encontrada: " + id));

        return faturaMapper.paraResponseDTO(fatura);
    }

    //Marca uma Fatura como paga
    @Transactional
    public FaturaResponseDTO pagarFatura(UUID id){

        Fatura fatura = faturaRepo.findById(id)
                .orElseThrow(
                        ()-> new FaturaNotFoundException("Fatura não encontrada ID: " + id));

        if (fatura.getStatusFatura() == StatusFatura.PAGO){

            throw new FaturaAlreadyPaidException("Essa Fatura já está paga");
        }

        fatura.setStatusFatura(StatusFatura.PAGO);

        Fatura faturaSalva = faturaRepo.save(fatura);

        return faturaMapper.paraResponseDTO(faturaSalva);
    }

    /*
     * Fórmula de cálculo da fatura.
     *
     * unidades × custo por unidade
     * + aluguel do medidor
     * + taxa de serviço
     * + imposto de serviço
     * + CESS
     * + taxa fixa
     */
    private BigDecimal calcularValorTotal(BigDecimal unidadesConsumidas, Taxa taxa){

        BigDecimal consumo = unidadesConsumidas.multiply(taxa.getCustoPorUnidade());

        return consumo
                .add(taxa.getAluguelMedidor())
                .add(taxa.getTaxaServico())
                .add(taxa.getImpostoServico())
                .add(taxa.getCess())
                .add(taxa.getTaxaFixa())
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validarDadosFatura(UUID id, LocalDate competencia, BigDecimal unidadesConsumidas){

        if (id == null){

            throw new FaturaValidationException("O medidor é obrigatório");
        }

        if (competencia == null){

            throw new FaturaValidationException("A competência é obrigatória");
        }

        validarUnidades(unidadesConsumidas);
    }

    private void validarUnidades(BigDecimal unidadesConsumidas){

        if (unidadesConsumidas == null){

            throw new FaturaValidationException("As unidades consumidas são obrigatórias");
        }

        if (unidadesConsumidas.compareTo(BigDecimal.ZERO) < 0){

            throw new FaturaValidationException("As unidades consumidas não podem ser negativas");
        }
    }

    private void verificarFaturaExistente(Medidor medidor, LocalDate competencia){

        if (faturaRepo.findByMedidorAndCompetencia(medidor, competencia).isPresent()){

            throw new FaturaAlreadyExistsException("Já existe uma fatura para o medidor: " + medidor.getNumero() +
                    " na competência: " + competencia);
        }
    }
}
