package com.powermanager.powermanager.services;
import com.powermanager.powermanager.dto.fatura.FaturaResponseDTO;
import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Fatura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.entity.enums.StatusFatura;
import com.powermanager.powermanager.exception.FaturaAlreadyExistsException;
import com.powermanager.powermanager.exception.FaturaAlreadyPaidException;
import com.powermanager.powermanager.exception.FaturaNotFoundException;
import com.powermanager.powermanager.exception.FaturaValidationException;
import com.powermanager.powermanager.mapper.FaturaMapper;
import com.powermanager.powermanager.mapper.TaxaMapper;
import com.powermanager.powermanager.repository.FaturaRepo;
import com.powermanager.powermanager.repository.MedidorRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private TaxaMapper taxaMapper;

    @Mock
    private TaxaService taxaService;

    @Mock
    private MedidorRepo medidorRepo;

    @Mock
    private FaturaMapper faturaMapper;

    @InjectMocks
    private FaturaService faturaService;

    private UUID medidorId;
    private UUID faturaId;
    private LocalDate competencia;
    private Medidor medidor;
    private Taxa taxa;
    private TaxaResponseDTO taxaResponseDTO;


    @BeforeEach
    void setUp() {
        medidorId = UUID.randomUUID();
        faturaId = UUID.randomUUID();
        competencia = LocalDate.of(2026, 1, 1);

        medidor = new Medidor();
        medidor.setId(medidorId);

        taxaResponseDTO = new TaxaResponseDTO(
                UUID.randomUUID(),
                new BigDecimal("2.00"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("3.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                LocalDateTime.now(),
                null);

        taxa = new Taxa();
        taxa.setCustoPorUnidade(new BigDecimal("2.00"));
        taxa.setAluguelMedidor(new BigDecimal("10.00"));
        taxa.setTaxaServico(new BigDecimal("5.00"));
        taxa.setImpostoServico(new BigDecimal("3.00"));
        taxa.setCess(new BigDecimal("2.00"));
        taxa.setTaxaFixa(new BigDecimal("1.00"));
    }

    private FaturaResponseDTO criarResposta(Fatura fatura){

        return new FaturaResponseDTO(
                fatura.getId(),
                fatura.getCompetencia(),
                fatura.getUnidadesConsumidas(),
                fatura.getCustoPorUnidadeAplicado(),
                fatura.getAluguelMedidorAplicado(),
                fatura.getTaxaServicoAplicada(),
                fatura.getImpostoServicoAplicado(),
                fatura.getCessAplicado(),
                fatura.getTaxaFixaAplicada(),
                fatura.getValorTotal(),
                fatura.getStatusFatura(),
                fatura.getMedidor() == null ? null : fatura.getMedidor().getId());
    }

    private Fatura criarFaturaCompleta() {

        Fatura fatura = new Fatura();
        fatura.setId(faturaId);
        fatura.setCompetencia(competencia);
        fatura.setUnidadesConsumidas(new BigDecimal("10.00"));
        fatura.setCustoPorUnidadeAplicado(new BigDecimal("2.00"));
        fatura.setAluguelMedidorAplicado(new BigDecimal("10.00"));
        fatura.setTaxaServicoAplicada(new BigDecimal("5.00"));
        fatura.setImpostoServicoAplicado(new BigDecimal("3.00"));
        fatura.setCessAplicado(new BigDecimal("2.00"));
        fatura.setTaxaFixaAplicada(new BigDecimal("1.00"));
        fatura.setValorTotal(new BigDecimal("41.00"));
        fatura.setStatusFatura(StatusFatura.PENDENTE);
        fatura.setMedidor(medidor);
        return fatura;
    }


    @Test
    void deveCalcularFaturaComSucesso() {

        when(taxaService.obterTaxaVigente()).thenReturn(taxaResponseDTO);
        when(taxaMapper.paraEntidade(any(TaxaCadastroDTO.class))).thenReturn(taxa);

        BigDecimal resultado = faturaService.calcularFatura(new BigDecimal("10"));

        assertEquals(0, resultado.compareTo(new BigDecimal("41.00")));

        verify(taxaService).obterTaxaVigente();
        verifyNoInteractions(faturaRepo, medidorService, faturaMapper, medidorRepo);
    }

    @Test
    void deveCalcularFaturaComConsumoZero() {

        when(taxaService.obterTaxaVigente()).thenReturn(taxaResponseDTO);
        when(taxaMapper.paraEntidade(any(TaxaCadastroDTO.class))).thenReturn(taxa);

        BigDecimal resultado = faturaService.calcularFatura(BigDecimal.ZERO);

        assertEquals(0, resultado.compareTo(new BigDecimal("21.00")));

        verify(taxaService).obterTaxaVigente();
        verifyNoInteractions(faturaRepo, medidorService, faturaMapper, medidorRepo);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesForemNulasNoCalculo() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.calcularFatura(null));

        verifyNoInteractions(faturaRepo, medidorService, taxaService, faturaMapper);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesForemNegativasNoCalculo() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.calcularFatura(new BigDecimal("-1")));

        verifyNoInteractions(faturaRepo, medidorService, taxaService, faturaMapper);
    }

    @Test
    void deveGerarFaturaComSucesso() {

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.empty());

        when(taxaService.obterTaxaVigente()).thenReturn(taxaResponseDTO);

        when(faturaRepo.save(any(Fatura.class)))
                .thenAnswer(invocation -> {
                    Fatura salva = invocation.getArgument(0);
                    salva.setId(faturaId);
                    return salva;
                });

        when(faturaMapper.paraResponseDTO(any(Fatura.class)))
                .thenAnswer(invocation -> criarResposta(invocation.getArgument(0)));

        when(taxaMapper.paraEntidade(any(TaxaCadastroDTO.class)))
                .thenReturn(taxa);

        FaturaResponseDTO resultado =
                faturaService.gerarFatura(medidorId, competencia, new BigDecimal("10"));

        assertNotNull(resultado);
        assertEquals(faturaId, resultado.id());
        assertEquals(competencia, resultado.competencia());
        assertEquals(medidorId, resultado.medidorId());

        assertEquals(0, resultado.unidadesConsumidas().compareTo(new BigDecimal("10")));

        assertEquals(0, resultado.valorTotal().compareTo(new BigDecimal("41.00")));

        assertEquals(StatusFatura.PENDENTE, resultado.statusFatura());

        assertEquals(0, resultado.custoPorUnidadeAplicado().compareTo(new BigDecimal("2.00")));

        assertEquals(0, resultado.aluguelMedidorAplicado().compareTo(new BigDecimal("10.00")));

        assertEquals(0, resultado.taxaServicoAplicada().compareTo(new BigDecimal("5.00")));

        assertEquals(0, resultado.impostoServicoAplicado().compareTo(new BigDecimal("3.00")));

        assertEquals(0, resultado.cessAplicado().compareTo(new BigDecimal("2.00")));

        assertEquals(0, resultado.taxaFixaAplicada().compareTo(new BigDecimal("1.00")));

        verify(medidorRepo).findById(medidorId);
        verify(faturaRepo).findByMedidorAndCompetencia(medidor, competencia);
        verify(taxaService).obterTaxaVigente();
        verify(faturaRepo).save(any(Fatura.class));
        verify(faturaMapper).paraResponseDTO(any(Fatura.class));
    }

    @Test
    void deveLancarExcecaoQuandoMedidorIdForNuloNaGeracao() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.gerarFatura(null, competencia, BigDecimal.ONE));

        verifyNoInteractions(faturaRepo, medidorService, taxaService, faturaMapper);
    }

    @Test
    void deveLancarExcecaoQuandoCompetenciaForNulaNaGeracao() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.gerarFatura(
                        medidorId, null, BigDecimal.ONE));

        verifyNoInteractions(faturaRepo, medidorService, taxaService, faturaMapper);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesForemNulasNaGeracao() {

        assertThrows(
                FaturaValidationException.class,
                () -> faturaService.gerarFatura(medidorId, competencia, null));

        verifyNoInteractions(faturaRepo, medidorService, taxaService, faturaMapper);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadesForemNegativasNaGeracao() {

        assertThrows(FaturaValidationException.class,
                () -> faturaService.gerarFatura(
                        medidorId, competencia, new BigDecimal("-1")));

        verifyNoInteractions(faturaRepo, medidorService, taxaService, faturaMapper);
    }

    @Test
    void deveLancarExcecaoQuandoFaturaJaExistir() {

        Fatura faturaExistente = criarFaturaCompleta();

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.of(faturaExistente));

        assertThrows(FaturaAlreadyExistsException.class,
                () -> faturaService.gerarFatura(
                        medidorId, competencia, BigDecimal.ONE));

        verify(medidorRepo).findById(medidorId);
        verify(faturaRepo).findByMedidorAndCompetencia(medidor, competencia);
        verify(faturaRepo, never()).save(any(Fatura.class));
        verifyNoInteractions(taxaService, faturaMapper);
    }

    @Test
    void devePreservarSnapshotDaTaxaNaFaturaGerada() {

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.empty());

        when(taxaMapper.paraEntidade(any(TaxaCadastroDTO.class))).thenReturn(taxa);

        when(taxaService.obterTaxaVigente()).thenReturn(taxaResponseDTO);

        when(faturaRepo.save(any(Fatura.class)))
                .thenAnswer(invocation -> {
                    Fatura salva = invocation.getArgument(0);
                    salva.setId(faturaId);
                    return salva;
                });

        when(faturaMapper.paraResponseDTO(any(Fatura.class)))
                .thenAnswer(invocation -> criarResposta(invocation.getArgument(0)));

        FaturaResponseDTO resultado = faturaService.gerarFatura(medidorId, competencia,
                new BigDecimal("5"));

        taxa.setCustoPorUnidade(new BigDecimal("99.00"));

        assertEquals(0, resultado.custoPorUnidadeAplicado()
                .compareTo(new BigDecimal("2.00")));

        assertEquals(0, resultado.valorTotal()
                .compareTo(new BigDecimal("31.00")));

        verify(medidorRepo).findById(medidorId);
        verify(faturaRepo).save(any(Fatura.class));
    }


    @Test
    void deveListarTodasAsFaturas() {

        Fatura fatura1 = criarFaturaCompleta();
        Fatura fatura2 = criarFaturaCompleta();

        List<Fatura> faturas = List.of( fatura1, fatura2);

        FaturaResponseDTO resposta1 = criarResposta(fatura1);
        FaturaResponseDTO resposta2 = criarResposta(fatura2);

        when(faturaRepo.findAll()).thenReturn(faturas);
        when(faturaMapper.paraResponseDTO(fatura1)).thenReturn(resposta1);
        when(faturaMapper.paraResponseDTO(fatura2)).thenReturn(resposta2);

        List<FaturaResponseDTO> resultado = faturaService.listarFaturas();

        assertEquals(2, resultado.size());
        assertEquals(resposta1, resultado.get(0));
        assertEquals(resposta2, resultado.get(1));

        verify(faturaRepo).findAll();
        verify(faturaMapper).paraResponseDTO(fatura1);
        verify(faturaMapper).paraResponseDTO(fatura2);
        verifyNoInteractions(medidorService, taxaService);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremFaturas() {

        when(faturaRepo.findAll()).thenReturn(List.of());

        List<FaturaResponseDTO> resultado = faturaService.listarFaturas();

        assertTrue(resultado.isEmpty());
        verify(faturaRepo).findAll();
        verifyNoInteractions(faturaMapper);
    }

    @Test
    void deveListarFaturasPorMedidor() {

        Fatura fatura = criarFaturaCompleta();
        FaturaResponseDTO resposta = criarResposta(fatura);

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));
        when(faturaRepo.findByMedidor(medidor)).thenReturn(List.of(fatura));
        when(faturaMapper.paraResponseDTO(fatura)).thenReturn(resposta);

        List<FaturaResponseDTO> resultado = faturaService.listarPorMedidor(medidorId);

        assertEquals(List.of(resposta), resultado);

        verify(medidorRepo).findById(medidorId);
        verify(faturaRepo).findByMedidor(medidor);
        verify(faturaMapper).paraResponseDTO(fatura);
        verifyNoInteractions(taxaService);
    }

    @Test
    void deveRetornarHistoricoPorMedidorEmOrdemDecrescente() {

        Fatura fatura1 = criarFaturaCompleta();
        Fatura fatura2 = criarFaturaCompleta();

        List<Fatura> faturas = List.of(fatura1, fatura2);

        FaturaResponseDTO resposta1 = criarResposta(fatura1);
        FaturaResponseDTO resposta2 = criarResposta(fatura2);

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));

        when(faturaRepo.findByMedidorOrderByCompetenciaDesc(medidor))
                .thenReturn(faturas);

        when(faturaMapper.paraResponseDTO(fatura1)).thenReturn(resposta1);
        when(faturaMapper.paraResponseDTO(fatura2)).thenReturn(resposta2);

        List<FaturaResponseDTO> resultado = faturaService.listarHistoricoPorMedidor(medidorId);

        assertEquals(List.of(resposta1, resposta2), resultado);

        verify(medidorRepo).findById(medidorId);
        verify(faturaRepo).findByMedidorOrderByCompetenciaDesc(medidor);
        verify(faturaMapper).paraResponseDTO(fatura1);
        verify(faturaMapper).paraResponseDTO(fatura2);
        verify(faturaMapper).paraResponseDTO(fatura2);
    }

    @Test
    void deveBuscarFaturaPorMedidorECompetencia() {

        Fatura fatura = criarFaturaCompleta();
        FaturaResponseDTO resposta = criarResposta(fatura);

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.of(fatura));
        when(faturaMapper.paraResponseDTO(fatura)).thenReturn(resposta);

        FaturaResponseDTO resultado = faturaService.buscaPorMedidorECompetencia(medidorId, competencia);

        assertEquals(resposta, resultado);

        verify(medidorRepo).findById(medidorId);
        verify(faturaRepo).findByMedidorAndCompetencia(medidor, competencia);
        verify(faturaMapper).paraResponseDTO(fatura);
    }

    @Test
    void deveLancarExcecaoQuandoFaturaNaoExistirParaMedidorECompetencia() {

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));

        when(faturaRepo.findByMedidorAndCompetencia(medidor, competencia))
                .thenReturn(Optional.empty());

        assertThrows(FaturaNotFoundException.class,
                () -> faturaService.buscaPorMedidorECompetencia(medidorId, competencia));

        verify(medidorRepo).findById(medidorId);
        verify(faturaRepo).findByMedidorAndCompetencia(medidor, competencia);
        verifyNoInteractions(faturaMapper);
    }


    @Test
    void deveObterDetalhesDaFatura() {

        Fatura fatura = criarFaturaCompleta();
        FaturaResponseDTO resposta = criarResposta(fatura);

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.of(fatura));
        when(faturaMapper.paraResponseDTO(fatura)).thenReturn(resposta);

        FaturaResponseDTO resultado = faturaService.obterDetalhesDaFatura(faturaId);

        assertEquals(resposta, resultado);

        verify(faturaRepo).findById(faturaId);
        verify(faturaMapper).paraResponseDTO(fatura);
        verifyNoMoreInteractions(medidorService, taxaService);
    }

    @Test
    void deveLancarExcecaoQuandoFaturaNaoForEncontradaPorId() {

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.empty());

        assertThrows(FaturaNotFoundException.class,
                () -> faturaService.obterDetalhesDaFatura(faturaId));

        verify(faturaRepo).findById(faturaId);
        verify(faturaRepo, never()).save(any(Fatura.class));
        verifyNoInteractions(faturaMapper);
    }

    // =========================
    // PAGAR FATURA
    // =========================

    @Test
    void devePagarFaturaPendente() {

        Fatura fatura = criarFaturaCompleta();
        fatura.setStatusFatura(StatusFatura.PENDENTE);

        FaturaResponseDTO resposta = criarResposta(fatura);

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.of(fatura));

        when(faturaRepo.save(any(Fatura.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        when(faturaMapper.paraResponseDTO(any(Fatura.class))).thenAnswer(
                invocation -> criarResposta(invocation.getArgument(0)));

        FaturaResponseDTO resultado = faturaService.pagarFatura(faturaId);

        assertEquals(StatusFatura.PAGO, fatura.getStatusFatura());
        assertEquals(StatusFatura.PAGO, resultado.statusFatura());

        verify(faturaRepo).findById(faturaId);
        verify(faturaRepo).save(fatura);
        verify(faturaMapper).paraResponseDTO(fatura);
    }

    @Test
    void deveLancarExcecaoAoPagarFaturaJaPaga() {

        Fatura fatura = criarFaturaCompleta();
        fatura.setStatusFatura(StatusFatura.PAGO);

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.of(fatura));

        assertThrows(FaturaAlreadyPaidException.class,
                ()-> faturaService.pagarFatura(faturaId));

        verify(faturaRepo).findById(faturaId);
        verify(faturaRepo, never()).save(any(Fatura.class));
        verifyNoInteractions(faturaMapper);
    }

    @Test
    void deveLancarExcecaoAoPagarFaturaInexistente() {

        when(faturaRepo.findById(faturaId)).thenReturn(Optional.empty());

        assertThrows(FaturaNotFoundException.class,
                () -> faturaService.pagarFatura(faturaId));

        verify(faturaRepo).findById(faturaId);
        verify(faturaRepo, never()).save(any(Fatura.class));
        verifyNoInteractions(faturaMapper);
    }
}
