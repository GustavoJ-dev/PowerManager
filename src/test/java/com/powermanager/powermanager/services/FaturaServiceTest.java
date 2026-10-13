package com.powermanager.powermanager.services;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.entity.enums.StatusFatura;
import com.powermanager.powermanager.exception.FaturaAlreadyExistsException;
import com.powermanager.powermanager.exception.FaturaAlreadyPaidException;
import com.powermanager.powermanager.exception.FaturaNotFoundException;
import com.powermanager.powermanager.exception.FaturaValidationException;
import com.powermanager.powermanager.repository.FaturaRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FaturaServiceTest {

    @Mock
    private FaturaRepo faturaRepo;

    @Mock
    private MedidorService medidorService;

    @Mock
    private TaxaService taxaService;

    @InjectMocks
    private FaturaService faturaService;

    private UUID medidorId;
    private UUID faturaId;
    private LocalDate competencia;
    private Medidor medidor;
    private Taxa taxa;

    @BeforeEach
    void setUp() {
        medidorId = UUID.randomUUID();
        faturaId = UUID.randomUUID();
        competencia = LocalDate.of(2026, 1, 1);

        medidor = new Medidor();
        medidor.setId(medidorId);

        taxa = new Taxa();
        taxa.setCustoPorUnidade(new BigDecimal("2.00"));
        taxa.setAluguelMedidor(new BigDecimal("10.00"));
        taxa.setTaxaServico(new BigDecimal("5.00"));
        taxa.setImpostoServico(new BigDecimal("3.00"));
        taxa.setCess(new BigDecimal("2.00"));
        taxa.setTaxaFixa(new BigDecimal("1.00"));
    }


    @Test
    void deveCalcularFaturaComSucesso() {

        when(taxaService.obterTaxaVigente()).thenReturn(taxa);

        BigDecimal resultado = faturaService.calcularFatura(new BigDecimal("10"));

        assertEquals(0, resultado.compareTo(new BigDecimal("41.00")));

        verify(taxaService).obterTaxaVigente();
        verifyNoInteractions(faturaRepo, medidorService);
    }

    @Test
    void deveCalcularFaturaComConsumoZero() {

        when(taxaService.obterTaxaVigente()).thenReturn(taxa);

        BigDecimal resultado = faturaService.calcularFatura(BigDecimal.ZERO);

        assertEquals(0, resultado.compareTo(new BigDecimal("21.00")));

        verify(taxaService).obterTaxaVigente();
        verifyNoInteractions(faturaRepo, medidorService);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesForemNulasNoCalculo() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.calcularFatura(null));

        verifyNoInteractions(faturaRepo, medidorService, taxaService);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesForemNegativasNoCalculo() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.calcularFatura(new BigDecimal("-1")));

        verifyNoInteractions(faturaRepo, medidorService, taxaService);
    }

    @Test
    void deveGerarFaturaComSucesso() {

        when(medidorService.buscarPorId(medidorId)).thenReturn(medidor);

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.empty());

        when(taxaService.obterTaxaVigente()).thenReturn(taxa);

        when(faturaRepo.save(any(Fatura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Fatura resultado =
                faturaService.gerarFatura(medidorId, competencia, new BigDecimal("10"));

        assertNotNull(resultado);

        assertEquals(competencia, resultado.getCompetencia());

        assertEquals(0, resultado.getUnidadesConsumidas()
                .compareTo(new BigDecimal("10")));

        assertEquals(0, resultado.getValorTotal()
                .compareTo(new BigDecimal("41.00")));

        assertEquals(StatusFatura.PENDENTE, resultado.getStatusFatura());

        assertSame(medidor, resultado.getMedidor());

        assertEquals(0, resultado.getCustoPorUnidadeAplicado()
                .compareTo(new BigDecimal("2.00")));

        assertEquals(0, resultado.getAluguelMedidorAplicado()
                .compareTo(new BigDecimal("10.00")));

        assertEquals(0, resultado.getTaxaServicoAplicada()
                .compareTo(new BigDecimal("5.00")));

        assertEquals(0, resultado.getImpostoServicoAplicado()
                .compareTo(new BigDecimal("3.00")));

        assertEquals(0, resultado.getCessAplicado()
                .compareTo(new BigDecimal("2.00")));

        assertEquals(0, resultado.getTaxaFixaAplicada()
                .compareTo(new BigDecimal("1.00")));

        verify(medidorService).buscarPorId(medidorId);
        verify(faturaRepo).findByMedidorAndCompetencia(medidor, competencia);
        verify(taxaService).obterTaxaVigente();
        verify(faturaRepo).save(any(Fatura.class));
    }

    @Test
    void deveLancarExcecaoQuandoMedidorIdForNuloNaGeracao() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.gerarFatura(null, competencia, BigDecimal.ONE));

        verifyNoInteractions(faturaRepo, medidorService, taxaService);
    }

    @Test
    void deveLancarExcecaoQuandoCompetenciaForNulaNaGeracao() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.gerarFatura(
                        medidorId, null, BigDecimal.ONE
                )
        );

        verifyNoInteractions(faturaRepo, medidorService, taxaService);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesForemNulasNaGeracao() {

        assertThrows(
                FaturaValidationException.class,
                () -> faturaService.gerarFatura(medidorId, competencia, null));

        verifyNoInteractions(faturaRepo, medidorService, taxaService);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesForemNegativasNaGeracao() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.gerarFatura(
                        medidorId, competencia, new BigDecimal("-1")));

        verifyNoInteractions(faturaRepo, medidorService, taxaService);
    }

    @Test
    void deveLancarExcecaoQuandoFaturaJaExistir() {

        Fatura faturaExistente = new Fatura();

        when(medidorService.buscarPorId(medidorId)).thenReturn(medidor);

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.of(faturaExistente));

        assertThrows(FaturaAlreadyExistsException.class,
                () -> faturaService.gerarFatura(
                        medidorId, competencia, BigDecimal.ONE));

        verify(medidorService).buscarPorId(medidorId);
        verify(faturaRepo).findByMedidorAndCompetencia(medidor, competencia);
        verify(faturaRepo, never()).save(any(Fatura.class));
        verifyNoInteractions(taxaService);
    }

    @Test
    void devePreservarSnapshotDaTaxaNaFaturaGerada() {

        when(medidorService.buscarPorId(medidorId)).thenReturn(medidor);

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.empty());

        when(taxaService.obterTaxaVigente()).thenReturn(taxa);

        when(faturaRepo.save(any(Fatura.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Fatura resultado = faturaService.gerarFatura(medidorId, competencia, new BigDecimal("5"));

        taxa.setCustoPorUnidade(new BigDecimal("99.00"));

        assertEquals(0, resultado.getCustoPorUnidadeAplicado()
                .compareTo(new BigDecimal("2.00")));

        verify(faturaRepo).save(any(Fatura.class));
    }


    @Test
    void deveListarTodasAsFaturas() {

        List<Fatura> faturas = List.of(new Fatura(), new Fatura());

        when(faturaRepo.findAll()).thenReturn(faturas);

        List<Fatura> resultado = faturaService.listarFaturas();

        assertSame(faturas, resultado);
        assertEquals(2, resultado.size());

        verify(faturaRepo).findAll();
        verifyNoInteractions(medidorService, taxaService);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremFaturas() {

        when(faturaRepo.findAll()).thenReturn(List.of());

        List<Fatura> resultado = faturaService.listarFaturas();

        assertTrue(resultado.isEmpty());
        verify(faturaRepo).findAll();
    }

    @Test
    void deveListarFaturasPorMedidor() {

        List<Fatura> faturas = List.of(new Fatura());

        when(medidorService.buscarPorId(medidorId)).thenReturn(medidor);

        when(faturaRepo.findByMedidor(medidor)).thenReturn(faturas);

        List<Fatura> resultado = faturaService.listarPorMedidor(medidorId);

        assertSame(faturas, resultado);

        verify(medidorService).buscarPorId(medidorId);
        verify(faturaRepo).findByMedidor(medidor);
        verifyNoInteractions(taxaService);
    }

    @Test
    void deveRetornarHistoricoPorMedidorEmOrdemDecrescente() {

        List<Fatura> faturas = List.of(new Fatura(), new Fatura());

        when(medidorService.buscarPorId(medidorId)).thenReturn(medidor);

        when(faturaRepo.findByMedidorOrderByCompetenciaDesc(medidor))
                .thenReturn(faturas);

        List<Fatura> resultado = faturaService.listarHistoricoPorMedidor(medidorId);

        assertSame(faturas, resultado);
        assertEquals(2, resultado.size());

        verify(medidorService).buscarPorId(medidorId);
        verify(faturaRepo).findByMedidorOrderByCompetenciaDesc(medidor);
    }

    @Test
    void deveBuscarFaturaPorMedidorECompetencia() {

        Fatura fatura = new Fatura();

        when(medidorService.buscarPorId(medidorId)).thenReturn(medidor);

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.of(fatura));

        Fatura resultado = faturaService.buscarPorMedidorEMes(medidorId, competencia);

        assertSame(fatura, resultado);

        verify(medidorService).buscarPorId(medidorId);
        verify(faturaRepo).findByMedidorAndCompetencia(medidor, competencia);
    }

    @Test
    void deveLancarExcecaoQuandoFaturaNaoExistirParaMedidorECompetencia() {

        when(medidorService.buscarPorId(medidorId)).thenReturn(medidor);

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.empty());

        assertThrows(FaturaNotFoundException.class,
                () -> faturaService.buscarPorMedidorEMes(medidorId, competencia));

        verify(medidorService).buscarPorId(medidorId);
        verify(faturaRepo).findByMedidorAndCompetencia(medidor, competencia);
    }


    @Test
    void deveObterDetalhesDaFatura() {
        Fatura fatura = new Fatura();

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.of(fatura));

        Fatura resultado = faturaService.obterDetalhesDaFatura(faturaId);

        assertSame(fatura, resultado);
        verify(faturaRepo).findById(faturaId);
        verifyNoInteractions(medidorService, taxaService);
    }

    @Test
    void deveLancarExcecaoQuandoFaturaNaoForEncontradaPorId() {

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.empty());

        assertThrows(FaturaNotFoundException.class,
                () -> faturaService.obterDetalhesDaFatura(faturaId));

        verify(faturaRepo).findById(faturaId);
        verify(faturaRepo, never()).save(any(Fatura.class));
    }

    // =========================
    // PAGAR FATURA
    // =========================

    @Test
    void devePagarFaturaPendente() {

        Fatura fatura = new Fatura();
        fatura.setStatusFatura(StatusFatura.PENDENTE);

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.of(fatura));

        when(faturaRepo.save(fatura)).thenReturn(fatura);

        Fatura resultado = faturaService.pagarFatura(faturaId);

        assertSame(fatura, resultado);
        assertEquals(StatusFatura.PAGO, resultado.getStatusFatura());

        verify(faturaRepo).findById(faturaId);
        verify(faturaRepo).save(fatura);
    }

    @Test
    void deveLancarExcecaoAoPagarFaturaJaPaga() {

        Fatura fatura = new Fatura();
        fatura.setStatusFatura(StatusFatura.PAGO);

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.of(fatura));

        assertThrows(FaturaAlreadyPaidException.class,
                () -> faturaService.pagarFatura(faturaId));

        verify(faturaRepo).findById(faturaId);
        verify(faturaRepo, never()).save(any(Fatura.class));
    }

    @Test
    void deveLancarExcecaoAoPagarFaturaInexistente() {

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.empty());

        assertThrows(FaturaNotFoundException.class,
                () -> faturaService.pagarFatura(faturaId));

        verify(faturaRepo).findById(faturaId);
        verify(faturaRepo, never()).save(any(Fatura.class));
    }
}
