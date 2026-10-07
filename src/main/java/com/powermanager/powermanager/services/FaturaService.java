package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.entity.enums.StatusFatura;
import com.powermanager.powermanager.repository.FaturaRepo;
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


    //Calcula uma fatura, utilizando taxa vigente
    @Transactional(readOnly = true)
    public BigDecimal calcularFatura(BigDecimal unidadesConsumidas){

        validarUnidades(unidadesConsumidas);

        Taxa taxa= taxaService.obterTaxaVigente();

        return calcularValorTotal(unidadesConsumidas, taxa);
    }

    //gera nova fatura para medidor
    @Transactional
    public Fatura gerarFatura(UUID medidorId, LocalDate competencia, BigDecimal unidadesConsumidas){

        validarDadosFatura(medidorId, competencia, unidadesConsumidas);

        Medidor medidor = medidorService.buscarPorId(medidorId);

        verificarFaturaExistente(medidor, competencia);

        Taxa taxa = taxaService.obterTaxaVigente();

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

        return faturaRepo.save(fatura);
    }

    //lista todas as faturas
    @Transactional(readOnly = true)
    public List<Fatura> listarFaturas(){

        return faturaRepo.findAll();
    }

    //lista todas faturas de um medidor
    @Transactional(readOnly = true)
    public List<Fatura> listarPorMedidor(UUID id){

        Medidor medidor = medidorService.buscarPorId(id);

        return faturaRepo.findByMedidor(medidor);
    }

    //busca determinada fatura de um medidor em uma competência especifíca
    @Transactional(readOnly = true)
    public Fatura buscarPorMedidorEMs(UUID id, LocalDate competencia){

        Medidor medidor = medidorService.buscarPorId(id);

        return faturaRepo.findByMedidorAndCompetencia(medidor, competencia)
                .orElseThrow(() ->
                        new RuntimeException("Fatura não encotrada para o medidor: " + id +
                                " na competência: " + competencia));

    }

    //retorna o historica de faturas de um medidor pelo mais recente
    @Transactional(readOnly = true)
    public List<Fatura> listarHistoricoPorMedidor(UUID id){

        Medidor medidor = medidorService.buscarPorId(id);

        return faturaRepo.findByMedidorOrderByCompetenciaDesc(medidor);
    }

    //busca fatura pelo id
    @Transactional(readOnly = true)
    public Fatura obterDetalhesDaFatura(UUID id){

        return faturaRepo.findById(id).orElseThrow(() ->
                new RuntimeException("Fatura não encontrada: " + id));
    }

    //Marca uma Fatura como paga
    @Transactional
    public Fatura pagarFatura(UUID id){

        Fatura fatura = obterDetalhesDaFatura(id);

        if (fatura.getStatusFatura() == StatusFatura.PAGO){

            throw new RuntimeException("Essa fatura já está paga.");
        }

        fatura.setStatusFatura(StatusFatura.PAGO);

        return faturaRepo.save(fatura);
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

            throw new IllegalArgumentException("O medidor é obrigatório");
        }

        if (competencia == null){

            throw new IllegalArgumentException("A competência é obrigatória");
        }

        validarUnidades(unidadesConsumidas);
    }

    private void validarUnidades(BigDecimal unidadesConsumidas){

        if (unidadesConsumidas == null){

            throw new IllegalArgumentException("As unidades consumidas são obrigatórias");
        }

        if (unidadesConsumidas.compareTo(BigDecimal.ZERO) < 0){

            throw new IllegalArgumentException("As unidades consumidas não podem ser negativas");
        }
    }

    private void verificarFaturaExistente(Medidor medidor, LocalDate competencia){

        if (faturaRepo.findByMedidorAndCompetencia(medidor, competencia).isPresent()){

            throw new RuntimeException("Já existe uma fatura para o medidor: " + medidor.getNumero() +
                    "na competência: " + competencia);
        }
    }
}
