package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.exception.TaxaNotFoundException;
import com.powermanager.powermanager.exception.TaxaValidationException;
import com.powermanager.powermanager.repository.TaxaRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TaxaServiceTest {

    @Mock
    private TaxaRepo taxaRepo;

    @InjectMocks
    private TaxaService taxaService;

    private Taxa criarTaxaValida(){

        Taxa taxa  = new Taxa();

        taxa.setCustoPorUnidade(new BigDecimal("1.50"));
        taxa.setAluguelMedidor(new BigDecimal("10.00"));
        taxa.setTaxaServico(new BigDecimal("5.00"));
        taxa.setImpostoServico(new BigDecimal("2.00"));
        taxa.setCess(new BigDecimal("1.00"));
        taxa.setTaxaFixa(new BigDecimal("3.00"));
        taxa.setInicioVigencia(LocalDateTime.of(2026, 1, 1, 0, 0));
        taxa.setFimVigencia(null);

        return taxa;
    }

    @Test
    void deveCadastrarNovaTaxa(){

        Taxa taxa = criarTaxaValida();

        when(taxaRepo.save(taxa)).thenReturn(taxa);

        Taxa resultado = taxaService.cadastrarNovaTaxa(taxa);

        assertEquals(taxa, resultado);

        verify(taxaRepo).buscarTaxasComSobreposicao(taxa.getInicioVigencia(), taxa.getFimVigencia());

        verify(taxaRepo).save(taxa);
    }

    @Test
    void deveCadastrarTaxaQuandoFimForPosteriorAoInicio() {

        Taxa taxa = criarTaxaValida();

        taxa.setFimVigencia(taxa.getInicioVigencia().plusMonths(6));

        when(taxaRepo.buscarTaxasComSobreposicao(taxa.getInicioVigencia(), taxa.getFimVigencia()))
                .thenReturn(List.of());

        when(taxaRepo.save(taxa)).thenReturn(taxa);

        Taxa resultado = taxaService.cadastrarNovaTaxa(taxa);

        assertSame(taxa, resultado);

        verify(taxaRepo).buscarTaxasComSobreposicao(taxa.getInicioVigencia(), taxa.getFimVigencia());

        verify(taxaRepo).save(taxa);
    }

    @Test
    void definirInicioVigenciaQuandoForNulo(){

        Taxa taxa = criarTaxaValida();

        taxa.setInicioVigencia(null);

        when(taxaRepo.buscarTaxasComSobreposicao(any(LocalDateTime.class), isNull()))
                .thenReturn(List.of());

        when(taxaRepo.save(taxa)).thenReturn(taxa);

        Taxa resultado = taxaService.cadastrarNovaTaxa(taxa);

        assertNotNull(resultado.getInicioVigencia());

        verify(taxaRepo).save(taxa);
    }

    @Test
    void deveLancarExcecaoQuandoInicioDoPeriodoForNulo() throws Exception {

        var metodo = TaxaService.class.getDeclaredMethod("validarPeriodo",
                LocalDateTime.class, LocalDateTime.class);

        metodo.setAccessible(true);

        var excecao = assertThrows(
                InvocationTargetException.class,
                () -> metodo.invoke(taxaService, null, null));

        assertInstanceOf(TaxaValidationException.class,
                excecao.getCause());

        verifyNoInteractions(taxaRepo);
    }

    @Test
    void deveLancarExcecaoAoCadastrarTaxaNula(){

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.cadastrarNovaTaxa(null));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoCustoPorUnidadeForNulo(){

        Taxa taxa = new Taxa();

        taxa.setCustoPorUnidade(null);

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.cadastrarNovaTaxa(taxa));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoCustoPorUnidadeForNegativo() {
        Taxa taxa = criarTaxaValida();
        taxa.setCustoPorUnidade(new BigDecimal("-1.00"));

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.cadastrarNovaTaxa(taxa));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoValorMonetarioForNegativo(){

        Taxa taxa = new Taxa();

        taxa.setTaxaFixa(new BigDecimal("-1.00"));

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.cadastrarNovaTaxa(taxa));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoFimNaoForPosteriorAoInicio(){

        Taxa taxa = criarTaxaValida();

        taxa.setFimVigencia(taxa.getInicioVigencia());

        assertThrows(TaxaValidationException.class,
                () -> taxaService.cadastrarNovaTaxa(taxa));

        verify(taxaRepo, never()).save(any(Taxa.class));

    }

    @Test
    void deveLancarExcecaoQuandoHouverSobreposicao(){

        Taxa taxa = criarTaxaValida();

        when(taxaRepo.buscarTaxasComSobreposicao(taxa.getInicioVigencia(), taxa.getFimVigencia()))
                .thenReturn(List.of(criarTaxaValida()));

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.cadastrarNovaTaxa(taxa));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveObterTaxaNaData(){

        LocalDateTime data = LocalDateTime.of(2026, 5, 1, 0, 0);

        Taxa taxa = criarTaxaValida();

        when(taxaRepo.buscarTaxaVigenteEm(data)).thenReturn(Optional.of(taxa));

        Taxa resultado = taxaService.obterTaxaNaData(data);

        assertEquals(taxa, resultado);

        verify(taxaRepo).buscarTaxaVigenteEm(data);
    }

    @Test
    void deveLancarExcecaoQuandoDataForNula(){

        assertThrows(TaxaValidationException.class, ()->
                taxaService.obterTaxaNaData(null));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverTaxaVigenteNaData(){


        LocalDateTime data = LocalDateTime.of(2026, 5, 1, 0, 0);

        when(taxaRepo.buscarTaxaVigenteEm(data)).thenReturn(Optional.empty());

        assertThrows(TaxaNotFoundException.class,
                ()-> taxaService.obterTaxaNaData(data));

        verify(taxaRepo).buscarTaxaVigenteEm(data);
    }

    @Test
    void deveListarHistorico(){

        List<Taxa> taxas = List.of(criarTaxaValida(), criarTaxaValida());

        when(taxaRepo.findAll()).thenReturn(taxas);

        List<Taxa> resultado = taxaService.listarHistorico();

        assertEquals(taxas, resultado);

        verify(taxaRepo).findAll();
    }

    @Test
    void deveAtualizarTaxa(){

        LocalDateTime inicioAtual = LocalDateTime.of(2026, 1, 1, 0, 0);

        LocalDateTime inicioNova = LocalDateTime.of(2026, 6, 1, 0, 0);

        Taxa taxaAtual = criarTaxaValida();
        taxaAtual.setInicioVigencia(inicioAtual);

        Taxa novaTaxa = criarTaxaValida();
        novaTaxa.setInicioVigencia(inicioNova);

        when(taxaRepo.buscarTaxaVigenteEm(any(LocalDateTime.class)))
                .thenReturn(Optional.of(taxaAtual));

        when(taxaRepo.save(any(Taxa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Taxa resultado = taxaService.atualizarTaxa(novaTaxa);

        assertEquals(inicioNova, taxaAtual.getFimVigencia());
        assertEquals(inicioNova, resultado.getInicioVigencia());
        assertNull(resultado.getFimVigencia());
        assertNull(resultado.getId());

        verify(taxaRepo, times(2))
                .save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoNovaVigenciaNaoForPosterior(){

        Taxa taxaAtual = criarTaxaValida();
        taxaAtual.setInicioVigencia(LocalDateTime.of(2026, 6, 1, 0, 0));

        Taxa novaTaxa = criarTaxaValida();
        novaTaxa.setInicioVigencia(LocalDateTime.of(2026, 6, 1, 0, 0));

        when(taxaRepo.buscarTaxaVigenteEm(any(LocalDateTime.class)))
                .thenReturn(Optional.of(taxaAtual));

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.atualizarTaxa(novaTaxa));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveAceitarValoresMonetariosIguaisAZero() {

        Taxa taxa = criarTaxaValida();

        taxa.setCustoPorUnidade(BigDecimal.ZERO);
        taxa.setAluguelMedidor(BigDecimal.ZERO);
        taxa.setTaxaServico(BigDecimal.ZERO);
        taxa.setImpostoServico(BigDecimal.ZERO);
        taxa.setCess(BigDecimal.ZERO);
        taxa.setTaxaFixa(BigDecimal.ZERO);

        when(taxaRepo.buscarTaxasComSobreposicao(taxa.getInicioVigencia(), taxa.getFimVigencia()))
                .thenReturn(List.of());

        when(taxaRepo.save(taxa)).thenReturn(taxa);

        Taxa resultado = taxaService.cadastrarNovaTaxa(taxa);

        assertNotNull(resultado);

        verify(taxaRepo).save(taxa);
    }

    @Test
    void deveLancarExcecaoQuandoFimForAnteriorAoInicio() {

        Taxa taxa = criarTaxaValida();

        taxa.setFimVigencia(taxa.getInicioVigencia().minusDays(1));

        assertThrows(TaxaValidationException.class,
                () -> taxaService.cadastrarNovaTaxa(taxa));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveDefinirInicioVigenciaQuandoForNuloNaAtualizacao() {

        Taxa taxaAtual = criarTaxaValida();

        taxaAtual.setInicioVigencia(LocalDateTime.of(2026, 1, 1, 0, 0));

        Taxa novaTaxa = criarTaxaValida();

        novaTaxa.setInicioVigencia(null);

        when(taxaRepo.buscarTaxaVigenteEm(any(LocalDateTime.class)))
                .thenReturn(Optional.of(taxaAtual));

        when(taxaRepo.save(any(Taxa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Taxa resultado = taxaService.atualizarTaxa(novaTaxa);

        assertNotNull(resultado.getInicioVigencia());

        assertTrue(resultado.getInicioVigencia().isAfter(taxaAtual.getInicioVigencia()));

        assertEquals(resultado.getInicioVigencia(), taxaAtual.getFimVigencia());

        verify(taxaRepo, times(2)).save(any(Taxa.class));
    }

}
