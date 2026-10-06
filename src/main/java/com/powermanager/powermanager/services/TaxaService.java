package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.repository.TaxaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxaService {

    private final TaxaRepo taxaRepo;

    /**
     * Cadastra uma nova taxa.
     */
    @Transactional
    public Taxa cadastrarNovaTaxa(Taxa taxa){

        validarTaxa(taxa);

        if (taxa.getInicioVigencia() == null){

            taxa.setInicioVigencia(LocalDateTime.now());
        }

        validarPeriodo(taxa.getInicioVigencia(), taxa.getFimVigencia());
        validarSobreposicao(taxa.getInicioVigencia(), taxa.getFimVigencia());

        return taxaRepo.save(taxa);
    }

    /**
     * Retorna a taxa vigente neste momento.
     */
    @Transactional(readOnly = true)
    public Taxa obterTaxaVigente(){

        return obterTaxaNaData(LocalDateTime.now());
    }


    /**
     * Retorna a taxa que estava vigente na data informada.
     */
    @Transactional(readOnly = true)
    public Taxa obterTaxaNaData(LocalDateTime data){

        if (data == null){

            throw new IllegalArgumentException("A data para consulta não pode ser nula");
        }

        return taxaRepo.buscarTaxaVigenteEm(data).orElseThrow(() ->
                new RuntimeException("Nenhuma taxa vigente encontrada nessa data"));
    }

    /**
     * Retorna todo o histórico de taxas.
     */
    @Transactional(readOnly = true)
    public List<Taxa> listarHistorico() {

        return taxaRepo.findAll();
    }

    /**
     * Encerra a taxa atual e cria uma nova taxa.
     *
     * A taxa antiga permanece armazenada para preservar o histórico.
     */
    @Transactional
    public Taxa atualizarTaxa(Taxa novaTaxa){

        Taxa taxaAtual = obterTaxaVigente();

        LocalDateTime inicioNovaVigencia = novaTaxa.getInicioVigencia();

        if (inicioNovaVigencia == null){

            inicioNovaVigencia = LocalDateTime.now();
        }

        if (!inicioNovaVigencia.isAfter(taxaAtual.getInicioVigencia())){

            throw new IllegalArgumentException("A nova Taxa deve iniciar apos o inicio da taxa atual");
        }

        taxaAtual.setFimVigencia(inicioNovaVigencia);

        taxaRepo.save(taxaAtual);

        /*
         * Criamos um novo registro para preservar o histórico.
         */
        novaTaxa.setId(null);
        novaTaxa.setInicioVigencia(inicioNovaVigencia);
        novaTaxa.setFimVigencia(null);

        validarTaxa(novaTaxa);

        return taxaRepo.save(novaTaxa);
    }

    /**
     * Valida os valores monetários da taxa.
     */
    private void validarTaxa(Taxa taxa) {

        if (taxa == null) {
            throw new IllegalArgumentException(
                    "A taxa não pode ser nula"
            );
        }

        validarValor(taxa.getCustoPorUnidade(), "Custo por unidade");

        validarValor(taxa.getAluguelMedidor(), "Aluguel do medidor");

        validarValor(taxa.getTaxaServico(), "Taxa de serviço");

        validarValor(taxa.getImpostoServico(), "Imposto de serviço");

        validarValor(taxa.getCess(), "CESS");

        validarValor(taxa.getTaxaFixa(), "Taxa fixa");
    }

    /**
     * Garante que os valores monetários sejam válidos.
     */
    private void validarValor(BigDecimal valor, String campo) {

        if (valor == null) {
            throw new IllegalArgumentException(
                    campo + " não pode ser nulo"
            );
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(campo + " não pode ser negativo");
        }
    }

    /**
     * Valida o intervalo de vigência.
     */
    private void validarPeriodo(LocalDateTime inicio, LocalDateTime fim) {

        if (inicio == null) {

            throw new IllegalArgumentException("O início da vigência não pode ser nulo");
        }

        if (fim != null && !fim.isAfter(inicio)) {

            throw new IllegalArgumentException("O fim da vigência deve ser posterior ao início");
        }
    }

    /**
     * Garante que a nova taxa não sobreponha
     * uma vigência já existente.
     */
    private void validarSobreposicao(LocalDateTime inicio, LocalDateTime fim) {

        List<Taxa> taxasSobrepostas = taxaRepo.buscarTaxasComSobreposicao(inicio, fim);

        if (!taxasSobrepostas.isEmpty()) {

            throw new IllegalArgumentException("O período informado se sobrepõe a uma taxa já existente");
        }
    }
}
