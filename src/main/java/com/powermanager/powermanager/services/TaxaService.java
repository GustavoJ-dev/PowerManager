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
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaxaService {

    private final TaxaRepo taxaRepo;
    private final TaxaMapper taxaMapper;

    /**
     * Cadastra uma nova taxa.
     */
    @Transactional
    public TaxaResponseDTO cadastrarNovaTaxa(TaxaCadastroDTO taxaCadastroDTO){

        if (taxaCadastroDTO == null){

            throw new TaxaValidationException("a Taxa não pode ser nula");
        }

        Taxa taxa = taxaMapper.paraEntidade(taxaCadastroDTO);

        validarTaxa(taxa);

        if (taxa.getInicioVigencia() == null){

            taxa.setInicioVigencia(LocalDateTime.now());
        }

        validarPeriodo(taxa.getInicioVigencia(), taxa.getFimVigencia());
        validarSobreposicao(taxa.getInicioVigencia(), taxa.getFimVigencia());

        Taxa taxaSalva = taxaRepo.save(taxa);

        return taxaMapper.paraResponseDTO(taxaSalva);
    }

    /**
     * Retorna a taxa vigente neste momento.
     */
    @Transactional(readOnly = true)
    public TaxaResponseDTO obterTaxaVigente(){

        return obterTaxaNaData(LocalDateTime.now());

    }


    /**
     * Retorna a taxa que estava vigente na data informada.
     */
    @Transactional(readOnly = true)
    public TaxaResponseDTO obterTaxaNaData(LocalDateTime data){

        if (data == null){

            throw new TaxaValidationException("A data para consulta não pode ser nula");
        }

        Taxa taxa = taxaRepo.buscarTaxaVigenteEm(data).orElseThrow(
                () -> new TaxaNotFoundException("Nenhuma taxa vigente encontrada nessa data"));

        return taxaMapper.paraResponseDTO(taxa);
    }

    /**
     * Retorna todo o histórico de taxas.
     */
    @Transactional(readOnly = true)
    public List<TaxaResponseDTO> listarHistorico() {

        return taxaRepo.findAll()
                .stream()
                .map(taxaMapper::paraResponseDTO)
                .toList();
    }

    /**
     * Encerra a taxa atual e cria uma nova taxa.
     *
     * A taxa antiga permanece armazenada para preservar o histórico.
     */
    @Transactional
    public TaxaResponseDTO atualizarTaxa(TaxaAtualizacaoDTO novaTaxaDTO){

        if (novaTaxaDTO == null){

            throw new TaxaValidationException("A taxa não pode ser nula");
        }

        LocalDateTime agora = LocalDateTime.now();

        //precisa da entidade para encerrar a vigência
        Taxa taxaAtual = buscarTaxaVigenteEntidade(agora);

        LocalDateTime inicioNovaVigencia = novaTaxaDTO.inicioVigencia();

        if (inicioNovaVigencia == null){

            inicioNovaVigencia = agora;
        }

        if (!inicioNovaVigencia.isAfter(taxaAtual.getInicioVigencia())){

            throw new TaxaValidationException("A nova Taxa deve iniciar apos o inicio da taxa atual");
        }

        //criamos outra nova entidade para preservar histórico.

        Taxa novaTaxa = new Taxa();

        taxaMapper.atualizarEntidade(novaTaxaDTO, novaTaxa);

        novaTaxa.setInicioVigencia(inicioNovaVigencia);
        novaTaxa.setFimVigencia(null);

        validarTaxa(novaTaxa);
        validarPeriodo(novaTaxa.getInicioVigencia(), novaTaxa.getFimVigencia());

        //encerra taxa anterior sem alterar valores monetários
        taxaAtual.setFimVigencia(inicioNovaVigencia);
        taxaRepo.save(taxaAtual);

        Taxa taxaSalva = taxaRepo.save(novaTaxa);

        return taxaMapper.paraResponseDTO(taxaSalva);
    }

    private Taxa buscarTaxaVigenteEntidade(LocalDateTime dateTime){

        return taxaRepo.buscarTaxaVigenteEm(dateTime)
                .orElseThrow(
                        ()-> new TaxaNotFoundException("Nenhuma Taxa vigente encontrada nessa data:" +
                                dateTime));
    }

    /**
     * Valida os valores monetários da taxa.
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
     * Garante que os valores monetários sejam válidos.
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
     * Garante que a nova taxa não sobreponha
     * uma vigência já existente.
     */
    private void validarSobreposicao(LocalDateTime inicio, LocalDateTime fim) {

        List<Taxa> taxasSobrepostas = taxaRepo.buscarTaxasComSobreposicao(inicio, fim);

        if (!taxasSobrepostas.isEmpty()) {

            throw new TaxaValidationException("O período informado se sobrepõe a uma taxa já existente");
        }
    }
}
