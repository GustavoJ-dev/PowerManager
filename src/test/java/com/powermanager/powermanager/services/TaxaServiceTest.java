package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.taxa.TaxaAtualizacaoDTO;
import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.exception.TaxaNotFoundException;
import com.powermanager.powermanager.exception.TaxaValidationException;
import com.powermanager.powermanager.mapper.TaxaMapper;
import com.powermanager.powermanager.repository.TaxaRepo;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private TaxaMapper taxaMapper;

    @InjectMocks
    private TaxaService taxaService;

    @BeforeEach
    void configurarMapper(){

        //simula a conversão de DTO de cadastro para entidade.
        lenient().when(taxaMapper.paraEntidade(any(TaxaCadastroDTO.class)))
                .thenAnswer(invocation ->
                        converterCadastro(invocation.getArgument(0)));

        //simula a conversão de entidade para DTO de resposta
        lenient().when(taxaMapper.paraResponseDTO(any(Taxa.class)))
                .thenAnswer(invocation -> criarResposta(invocation.getArgument(0)));

        //simula o preenchimento da nova entidade durante a atualização
        lenient().doAnswer(invocation -> {

            TaxaAtualizacaoDTO atualizacaoDTO = invocation.getArgument(0);
            Taxa taxa = invocation.getArgument(1);

            preencherTaxa(
                    taxa,
                    atualizacaoDTO.custoPorUnidade(),
                    atualizacaoDTO.aluguelMedidor(),
                    atualizacaoDTO.taxaServico(),
                    atualizacaoDTO.impostoServico(),
                    atualizacaoDTO.cess(),
                    atualizacaoDTO.taxaFixa());

            return null;
        }).when(taxaMapper).atualizarEntidade(any(TaxaAtualizacaoDTO.class),
                any(Taxa.class));

    }
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

    private void preencherTaxa(Taxa taxa, BigDecimal custoPorUnidade, BigDecimal aluguelMedidor,
                               BigDecimal taxaServico, BigDecimal impostoServico, BigDecimal cess,
                               BigDecimal taxaFixa){

        taxa.setCustoPorUnidade(custoPorUnidade);
        taxa.setAluguelMedidor(aluguelMedidor);
        taxa.setTaxaServico(taxaServico);
        taxa.setImpostoServico(impostoServico);
        taxa.setCess(cess);
        taxa.setTaxaFixa(taxaFixa);
    }
    private Taxa converterCadastro(TaxaCadastroDTO taxaCadastroDTO){

        Taxa taxa = new Taxa();

        preencherTaxa(
                taxa,
                taxaCadastroDTO.custoPorUnidade(),
                taxaCadastroDTO.aluguelMedidor(),
                taxaCadastroDTO.taxaServico(),
                taxaCadastroDTO.impostoServico(),
                taxaCadastroDTO.cess(),
                taxaCadastroDTO.taxaFixa());

        taxa.setInicioVigencia(taxaCadastroDTO.inicioVigencia());
        taxa.setFimVigencia(taxaCadastroDTO.fimVigencia());

        return taxa;
    }

    private TaxaCadastroDTO criarCadastroValido(){

        return new TaxaCadastroDTO(
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("3.00"),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null);
    }

    private TaxaAtualizacaoDTO criarAtualizacaoValida(LocalDateTime inicio){

        return new TaxaAtualizacaoDTO(
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("3.00"),
                inicio);
    }

    private TaxaResponseDTO criarResposta(Taxa taxa) {

        return new TaxaResponseDTO(
                taxa.getId(),
                taxa.getCustoPorUnidade(),
                taxa.getAluguelMedidor(),
                taxa.getTaxaServico(),
                taxa.getImpostoServico(),
                taxa.getCess(),
                taxa.getTaxaFixa(),
                taxa.getInicioVigencia(),
                taxa.getFimVigencia());
    }

    @Test
    void deveCadastrarNovaTaxa(){

        TaxaCadastroDTO dto = criarCadastroValido();

        when(taxaRepo.buscarTaxasComSobreposicao(dto.inicioVigencia(), dto.fimVigencia()))
                .thenReturn(List.of());

        when(taxaRepo.save(any(Taxa.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        TaxaResponseDTO resultado = taxaService.cadastrarNovaTaxa(dto);

        assertEquals(dto.custoPorUnidade(), resultado.custoPorUnidade());
        assertEquals(dto.inicioVigencia(), resultado.inicioVigencia());

        verify(taxaMapper).paraEntidade(dto);
        verify(taxaRepo).buscarTaxasComSobreposicao(dto.inicioVigencia(), dto.fimVigencia());

        verify(taxaRepo).save(any(Taxa.class));
    }

    @Test
    void deveCadastrarTaxaQuandoFimForPosteriorAoInicio() {

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime fim = inicio.plusMonths(6L);

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("3.00"),
                inicio,
                fim);

        when(taxaRepo.buscarTaxasComSobreposicao(inicio, fim)).thenReturn(List.of());

        when(taxaRepo.save(any(Taxa.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        TaxaResponseDTO resultado = taxaService.cadastrarNovaTaxa(dto);

        assertEquals(inicio, resultado.inicioVigencia());
        assertEquals(fim, resultado.fimVigencia());

        verify(taxaRepo).save(any(Taxa.class));
    }

    @Test
    void deveDefinirInicioVigenciaQuandoForNulo(){

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("3.00"),
                null,
                null);

        when(taxaRepo.buscarTaxasComSobreposicao(any(LocalDateTime.class), isNull()))
                .thenReturn(List.of());

        when(taxaRepo.save(any(Taxa.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        TaxaResponseDTO resultado = taxaService.cadastrarNovaTaxa(dto);

        assertNotNull(resultado.inicioVigencia());

        verify(taxaRepo).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoInicioDoPeriodoForNulo() throws Exception {

        var metodo = TaxaService.class.getDeclaredMethod(
                "validarPeriodo", LocalDateTime.class, LocalDateTime.class);

        metodo.setAccessible(true);

        var excecao = assertThrows(InvocationTargetException.class,
                ()->metodo.invoke(taxaService,null, null));

        assertInstanceOf(TaxaValidationException.class, excecao.getCause());

        verifyNoInteractions(taxaRepo);
    }

    @Test
    void deveLancarExcecaoAoCadastrarTaxaNula(){

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.cadastrarNovaTaxa(null));

        verifyNoInteractions(taxaRepo, taxaMapper);
    }

    @Test
    void deveLancarExcecaoQuandoCustoPorUnidadeForNulo(){

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                null,
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("3.00"),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null);

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.cadastrarNovaTaxa(dto));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoCustoPorUnidadeForNegativo() {

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                new BigDecimal("-1.00"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("3.00"),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null);

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.cadastrarNovaTaxa(dto));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoValorMonetarioForNegativo(){

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("-1.00"),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null);

        assertThrows(TaxaValidationException.class,
                () -> taxaService.cadastrarNovaTaxa(dto));

        verify(taxaRepo, never()).save(any(Taxa.class));

    }

    @Test
    void deveLancarExcecaoQuandoFimNaoForPosteriorAoInicio(){

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("3.00"),
                inicio,
                inicio);

        assertThrows(TaxaValidationException.class,
                () -> taxaService.cadastrarNovaTaxa(dto));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoHouverSobreposicao(){

        TaxaCadastroDTO dto = criarCadastroValido();

        when(taxaRepo.buscarTaxasComSobreposicao(dto.inicioVigencia(), dto.fimVigencia()))
                .thenReturn(List.of(criarTaxaValida()));

        assertThrows(TaxaValidationException.class,
                ()-> taxaService.cadastrarNovaTaxa(dto));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveObterTaxaNaData(){

        LocalDateTime data = LocalDateTime.of(2026, 5, 1, 0, 0);

        Taxa taxa = criarTaxaValida();

        when(taxaRepo.buscarTaxaVigenteEm(data)).thenReturn(Optional.of(taxa));

        TaxaResponseDTO resultado = taxaService.obterTaxaNaData(data);

        assertEquals(taxa.getCustoPorUnidade(), resultado.custoPorUnidade());
        assertEquals(taxa.getInicioVigencia(), resultado.inicioVigencia());

        verify(taxaRepo).buscarTaxaVigenteEm(data);

        verify(taxaRepo).buscarTaxaVigenteEm(data);
        verify(taxaMapper).paraResponseDTO(taxa);
    }

    @Test
    void deveLancarExcecaoQuandoDataForNula(){

        assertThrows(TaxaValidationException.class, ()->
                taxaService.obterTaxaNaData(null));

        verifyNoInteractions(taxaRepo);
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverTaxaVigenteNaData(){


        LocalDateTime data = LocalDateTime.of(2026, 5, 1, 0, 0);

        when(taxaRepo.buscarTaxaVigenteEm(data)).thenReturn(Optional.empty());

        assertThrows(TaxaNotFoundException.class,
                ()-> taxaService.obterTaxaNaData(data));

        verify(taxaRepo).buscarTaxaVigenteEm(data);
        verify(taxaMapper, never()).paraResponseDTO(any(Taxa.class));
    }

    @Test
    void deveListarHistorico(){

        List<Taxa> taxas = List.of(criarTaxaValida(), criarTaxaValida());

        when(taxaRepo.findAll()).thenReturn(taxas);

        List<TaxaResponseDTO> resultado = taxaService.listarHistorico();

        assertEquals(2, resultado.size());
        assertEquals(taxas.get(0).getCustoPorUnidade(), resultado.get(0).custoPorUnidade());

        verify(taxaRepo).findAll();
        verify(taxaMapper, times(2)).paraResponseDTO(any(Taxa.class));
    }

    @Test
    void deveAtualizarTaxa(){

        LocalDateTime inicioAtual = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime inicioNova = LocalDateTime.of(2026, 6, 1, 0, 0);

        Taxa taxaAtual = criarTaxaValida();
        taxaAtual.setInicioVigencia(inicioAtual);

        BigDecimal custoHistorico = taxaAtual.getCustoPorUnidade();

        TaxaAtualizacaoDTO dto = criarAtualizacaoValida(inicioNova);

        when(taxaRepo.buscarTaxaVigenteEm(any(LocalDateTime.class)))
                .thenReturn(Optional.of(taxaAtual));

        when(taxaRepo.save(any(Taxa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaxaResponseDTO resultado = taxaService.atualizarTaxa(dto);

        assertEquals(inicioNova, taxaAtual.getFimVigencia());
        assertEquals(custoHistorico, taxaAtual.getCustoPorUnidade());

        assertEquals(inicioNova, resultado.inicioVigencia());
        assertNull(resultado.fimVigencia());
        assertNull(resultado.id());

        verify(taxaRepo, times(2))
                .save(any(Taxa.class));

        verify(taxaMapper).atualizarEntidade(eq(dto), any(Taxa.class));
    }

    @Test
    void deveLancarExcecaoQuandoNovaVigenciaNaoForPosterior(){

        LocalDateTime inicio = LocalDateTime.of(2026, 6, 1, 0, 0);

        Taxa taxaAtual = criarTaxaValida();
        taxaAtual.setInicioVigencia(inicio);

        TaxaAtualizacaoDTO dto = criarAtualizacaoValida(inicio);

        when(taxaRepo.buscarTaxaVigenteEm(any(LocalDateTime.class)))
                .thenReturn(Optional.of(taxaAtual));

        assertThrows(TaxaValidationException.class,
                () -> taxaService.atualizarTaxa(dto));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveAceitarValoresMonetariosIguaisAZero() {

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDateTime.of(2026, 1, 1, 0, 0),
                null);

        when(taxaRepo.buscarTaxasComSobreposicao(dto.inicioVigencia(), dto.fimVigencia()))
                .thenReturn(List.of());

        when(taxaRepo.save(any(Taxa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaxaResponseDTO resultado = taxaService.cadastrarNovaTaxa(dto);

        assertNotNull(resultado);
        assertEquals(BigDecimal.ZERO, resultado.custoPorUnidade());

        verify(taxaRepo).save(any(Taxa.class));

    }

    @Test
    void deveLancarExcecaoQuandoFimForAnteriorAoInicio() {

        LocalDateTime inicio = LocalDateTime.of(2026, 1, 1, 0, 0);

        TaxaCadastroDTO dto = new TaxaCadastroDTO(
                new BigDecimal("1.50"),
                new BigDecimal("10.00"),
                new BigDecimal("5.00"),
                new BigDecimal("2.00"),
                new BigDecimal("1.00"),
                new BigDecimal("3.00"),
                inicio,
                inicio.minusDays(1));

        assertThrows(TaxaValidationException.class,
                () -> taxaService.cadastrarNovaTaxa(dto));

        verify(taxaRepo, never()).save(any(Taxa.class));
    }

    @Test
    void deveDefinirInicioVigenciaQuandoForNuloNaAtualizacao() {

        Taxa taxaAtual = criarTaxaValida();
        taxaAtual.setInicioVigencia(LocalDateTime.of(2026, 1, 1, 0, 0));

        TaxaAtualizacaoDTO dto = criarAtualizacaoValida(null);

        when(taxaRepo.buscarTaxaVigenteEm(any(LocalDateTime.class)))
                .thenReturn(Optional.of(taxaAtual));

        when(taxaRepo.save(any(Taxa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaxaResponseDTO resultado = taxaService.atualizarTaxa(dto);

        assertNotNull(resultado.inicioVigencia());

        assertTrue(resultado.inicioVigencia()
                        .isAfter(LocalDateTime.of(2026, 1, 1, 0, 0)));

        assertEquals(resultado.inicioVigencia(), taxaAtual.getFimVigencia());

        verify(taxaRepo, times(2)).save(any(Taxa.class));
    }
}
