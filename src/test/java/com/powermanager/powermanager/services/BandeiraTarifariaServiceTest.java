package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaAtualizacaoDTO;
import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaCadastroDTO;
import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaResponseDTO;
import com.powermanager.powermanager.entity.BandeiraTarifaria;
import com.powermanager.powermanager.enums.TipoBandeira;
import com.powermanager.powermanager.exception.bandeira_tarifaria.BandeiraTarifariaNotFoundException;
import com.powermanager.powermanager.exception.bandeira_tarifaria.BandeiraTarifariaValidationException;
import com.powermanager.powermanager.mapper.BandeiraTarifariaMapper;
import com.powermanager.powermanager.repository.BandeiraTarifariaRepo;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BandeiraTarifariaServiceTest {

    @Mock
    private BandeiraTarifariaRepo bandeiraTarifariaRepo;

    @Mock
    private BandeiraTarifariaMapper bandeiraTarifariaMapper;

    @InjectMocks
    private BandeiraTarifariaService bandeiraTarifariaService;

    private UUID id;
    private LocalDate inicio;
    private LocalDate fim;
    private BandeiraTarifariaCadastroDTO cadastroDTO;
    private BandeiraTarifaria bandeira;
    private BandeiraTarifariaResponseDTO responseDTO;

    @BeforeEach
    void setUp() {

        id = UUID.randomUUID();
        inicio = LocalDate.of(2026, 1, 1);
        fim = LocalDate.of(2026, 12, 31);

        cadastroDTO = new BandeiraTarifariaCadastroDTO(
                TipoBandeira.VERDE,
                new BigDecimal("0.000000"),
                inicio,
                fim
        );

        bandeira = new BandeiraTarifaria();
        bandeira.setId(id);
        bandeira.setTipoBandeira(TipoBandeira.VERDE);
        bandeira.setValorAdicional(new BigDecimal("0.000000"));
        bandeira.setInicioVigencia(inicio);
        bandeira.setFimVigencia(fim);

        responseDTO = new BandeiraTarifariaResponseDTO(
                id,
                TipoBandeira.VERDE,
                new BigDecimal("0.000000"),
                inicio,
                fim
        );
    }

    @Test
    void deveCadastrarBandeiraTarifariaComSucesso() {

        when(bandeiraTarifariaMapper.paraEntidade(cadastroDTO))
                .thenReturn(bandeira);
        when(bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                TipoBandeira.VERDE, inicio, fim
        )).thenReturn(List.of());
        when(bandeiraTarifariaRepo.save(bandeira)).thenReturn(bandeira);
        when(bandeiraTarifariaMapper.paraResponseDTO(bandeira))
                .thenReturn(responseDTO);

        BandeiraTarifariaResponseDTO resultado =
                bandeiraTarifariaService.cadastrarBandeiraTarifaria(cadastroDTO);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
        verify(bandeiraTarifariaRepo).save(bandeira);
    }

    @Test
    void deveRejeitarCadastroNulo() {

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.cadastrarBandeiraTarifaria(null)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarTipoDeBandeiraNulo() {

        BandeiraTarifariaCadastroDTO dtoInvalido =
                new BandeiraTarifariaCadastroDTO(
                        null,
                        new BigDecimal("0.100000"),
                        inicio,
                        fim
                );

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.cadastrarBandeiraTarifaria(dtoInvalido)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarValorAdicionalNulo() {

        BandeiraTarifariaCadastroDTO dtoInvalido =
                new BandeiraTarifariaCadastroDTO(
                        TipoBandeira.VERDE,
                        null,
                        inicio,
                        fim
                );

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.cadastrarBandeiraTarifaria(dtoInvalido)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarValorAdicionalNegativo() {

        BandeiraTarifariaCadastroDTO dtoInvalido =
                new BandeiraTarifariaCadastroDTO(
                        TipoBandeira.VERDE,
                        new BigDecimal("-0.010000"),
                        inicio,
                        fim
                );

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.cadastrarBandeiraTarifaria(dtoInvalido)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarInicioDeVigenciaNulo() {

        BandeiraTarifariaCadastroDTO dtoInvalido =
                new BandeiraTarifariaCadastroDTO(
                        TipoBandeira.VERDE,
                        new BigDecimal("0.100000"),
                        null,
                        fim
                );

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.cadastrarBandeiraTarifaria(dtoInvalido)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarFimAnteriorAoInicio() {

        BandeiraTarifariaCadastroDTO dtoInvalido =
                new BandeiraTarifariaCadastroDTO(
                        TipoBandeira.VERDE,
                        new BigDecimal("0.100000"),
                        inicio,
                        inicio.minusDays(1)
                );

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.cadastrarBandeiraTarifaria(dtoInvalido)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarFimIgualAoInicio() {

        BandeiraTarifariaCadastroDTO dtoInvalido =
                new BandeiraTarifariaCadastroDTO(
                        TipoBandeira.VERDE,
                        new BigDecimal("0.100000"),
                        inicio,
                        inicio
                );

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.cadastrarBandeiraTarifaria(dtoInvalido)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void devePermitirCadastroComVigenciaAberta() {

        BandeiraTarifariaCadastroDTO dtoAberto =
                new BandeiraTarifariaCadastroDTO(
                        TipoBandeira.AMARELA,
                        new BigDecimal("0.018850"),
                        inicio,
                        null
                );

        BandeiraTarifaria bandeiraAberta = new BandeiraTarifaria();
        bandeiraAberta.setTipoBandeira(TipoBandeira.AMARELA);
        bandeiraAberta.setValorAdicional(new BigDecimal("0.018850"));
        bandeiraAberta.setInicioVigencia(inicio);

        BandeiraTarifariaResponseDTO responseAberta =
                new BandeiraTarifariaResponseDTO(
                        UUID.randomUUID(),
                        TipoBandeira.AMARELA,
                        new BigDecimal("0.018850"),
                        inicio,
                        null
                );

        when(bandeiraTarifariaMapper.paraEntidade(dtoAberto))
                .thenReturn(bandeiraAberta);
        when(bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                TipoBandeira.AMARELA, inicio, null
        )).thenReturn(List.of());
        when(bandeiraTarifariaRepo.save(bandeiraAberta))
                .thenReturn(bandeiraAberta);
        when(bandeiraTarifariaMapper.paraResponseDTO(bandeiraAberta))
                .thenReturn(responseAberta);

        BandeiraTarifariaResponseDTO resultado =
                bandeiraTarifariaService.cadastrarBandeiraTarifaria(dtoAberto);

        assertNotNull(resultado);
        assertNull(resultado.fimVigencia());
        verify(bandeiraTarifariaRepo).save(bandeiraAberta);
    }

    @Test
    void deveRejeitarCadastroComSobreposicao() {

        when(bandeiraTarifariaMapper.paraEntidade(cadastroDTO))
                .thenReturn(bandeira);
        when(bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                TipoBandeira.VERDE, inicio, fim
        )).thenReturn(List.of(bandeira));

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.cadastrarBandeiraTarifaria(cadastroDTO)
        );

        verify(bandeiraTarifariaRepo, never()).save(any());
    }

    @Test
    void deveListarBandeirasEmOrdemCronologica() {

        BandeiraTarifaria antiga = new BandeiraTarifaria();
        antiga.setInicioVigencia(inicio.minusYears(1));

        BandeiraTarifaria recente = new BandeiraTarifaria();
        recente.setInicioVigencia(inicio);

        BandeiraTarifariaResponseDTO responseAntiga =
                mock(BandeiraTarifariaResponseDTO.class);
        BandeiraTarifariaResponseDTO responseRecente =
                mock(BandeiraTarifariaResponseDTO.class);

        when(bandeiraTarifariaRepo.findAll())
                .thenReturn(List.of(recente, antiga));
        when(bandeiraTarifariaMapper.paraResponseDTO(antiga))
                .thenReturn(responseAntiga);
        when(bandeiraTarifariaMapper.paraResponseDTO(recente))
                .thenReturn(responseRecente);

        List<BandeiraTarifariaResponseDTO> resultado =
                bandeiraTarifariaService.listarBandeirasTarifarias();

        assertEquals(2, resultado.size());
        assertSame(responseAntiga, resultado.get(0));
        assertSame(responseRecente, resultado.get(1));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremBandeiras() {

        when(bandeiraTarifariaRepo.findAll()).thenReturn(List.of());

        List<BandeiraTarifariaResponseDTO> resultado =
                bandeiraTarifariaService.listarBandeirasTarifarias();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(bandeiraTarifariaMapper);
    }

    @Test
    void deveBuscarBandeiraPorId() {

        when(bandeiraTarifariaRepo.findById(id))
                .thenReturn(Optional.of(bandeira));
        when(bandeiraTarifariaMapper.paraResponseDTO(bandeira))
                .thenReturn(responseDTO);

        BandeiraTarifariaResponseDTO resultado =
                bandeiraTarifariaService.buscarPorId(id);

        assertEquals(id, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoBandeiraNaoExistirPorId() {

        when(bandeiraTarifariaRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                BandeiraTarifariaNotFoundException.class,
                () -> bandeiraTarifariaService.buscarPorId(id)
        );
    }

    @Test
    void deveRejeitarIdNuloNaBusca() {

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.buscarPorId(null)
        );

        verifyNoInteractions(bandeiraTarifariaRepo);
    }

    @Test
    void deveBuscarBandeiraVigenteNaDataInformada() {

        LocalDate dataConsulta = LocalDate.of(2026, 6, 15);

        when(bandeiraTarifariaRepo.buscarBandeiraVigenteEm(
                TipoBandeira.VERDE, dataConsulta
        )).thenReturn(Optional.of(bandeira));
        when(bandeiraTarifariaMapper.paraResponseDTO(bandeira))
                .thenReturn(responseDTO);

        BandeiraTarifariaResponseDTO resultado =
                bandeiraTarifariaService.buscarBandeiraVigenteEm(
                        TipoBandeira.VERDE, dataConsulta
                );

        assertEquals(id, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverBandeiraVigente() {

        LocalDate dataConsulta = LocalDate.of(2026, 6, 15);

        when(bandeiraTarifariaRepo.buscarBandeiraVigenteEm(
                TipoBandeira.VERDE, dataConsulta
        )).thenReturn(Optional.empty());

        assertThrows(
                BandeiraTarifariaNotFoundException.class,
                () -> bandeiraTarifariaService.buscarBandeiraVigenteEm(
                        TipoBandeira.VERDE, dataConsulta
                )
        );

        verifyNoInteractions(bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarTipoNuloNaBuscaDeVigencia() {

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.buscarBandeiraVigenteEm(
                        null, inicio
                )
        );

        verifyNoInteractions(bandeiraTarifariaRepo);
    }

    @Test
    void deveRejeitarDataNulaNaBuscaDeVigencia() {

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.buscarBandeiraVigenteEm(
                        TipoBandeira.VERDE, null
                )
        );

        verifyNoInteractions(bandeiraTarifariaRepo);
    }

    @Test
    void deveAtualizarFimVigenciaComSucesso() {

        LocalDate novoFim = LocalDate.of(2026, 8, 31);

        BandeiraTarifariaAtualizacaoDTO atualizacaoDTO =
                new BandeiraTarifariaAtualizacaoDTO(novoFim);

        // A bandeira precisa estar com vigência aberta para ser encerrada.
        bandeira.setFimVigencia(null);

        BandeiraTarifariaResponseDTO responseAtualizado =
                new BandeiraTarifariaResponseDTO(
                        id,
                        TipoBandeira.VERDE,
                        new BigDecimal("0.000000"),
                        inicio,
                        novoFim
                );

        when(bandeiraTarifariaRepo.findById(id))
                .thenReturn(Optional.of(bandeira));

        when(bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                TipoBandeira.VERDE, inicio, novoFim
        )).thenReturn(List.of(bandeira));

        when(bandeiraTarifariaRepo.save(bandeira))
                .thenReturn(bandeira);

        when(bandeiraTarifariaMapper.paraResponseDTO(bandeira))
                .thenReturn(responseAtualizado);

        BandeiraTarifariaResponseDTO resultado =
                bandeiraTarifariaService.atualizarFimVigencia(
                        id, atualizacaoDTO
                );

        assertEquals(novoFim, resultado.fimVigencia());

        verify(bandeiraTarifariaMapper)
                .atualizarEntidade(atualizacaoDTO, bandeira);

        verify(bandeiraTarifariaRepo).save(bandeira);
    }

    @Test
    void deveRejeitarAtualizacaoNula() {

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.atualizarFimVigencia(id, null)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarFimNuloNaAtualizacao() {

        BandeiraTarifariaAtualizacaoDTO dtoInvalido =
                new BandeiraTarifariaAtualizacaoDTO(null);

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.atualizarFimVigencia(
                        id, dtoInvalido
                )
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarAtualizacaoComIdNulo() {

        BandeiraTarifariaAtualizacaoDTO dto =
                new BandeiraTarifariaAtualizacaoDTO(fim);

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.atualizarFimVigencia(null, dto)
        );

        verifyNoInteractions(bandeiraTarifariaRepo, bandeiraTarifariaMapper);
    }

    @Test
    void deveRejeitarAtualizacaoDeBandeiraInexistente() {

        BandeiraTarifariaAtualizacaoDTO dto =
                new BandeiraTarifariaAtualizacaoDTO(fim);

        when(bandeiraTarifariaRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                BandeiraTarifariaNotFoundException.class,
                () -> bandeiraTarifariaService.atualizarFimVigencia(id, dto)
        );

        verify(bandeiraTarifariaRepo, never()).save(any());
    }

    @Test
    void deveRejeitarAtualizacaoDeVigenciaJaEncerrada() {

        BandeiraTarifariaAtualizacaoDTO dto =
                new BandeiraTarifariaAtualizacaoDTO(
                        LocalDate.of(2027, 1, 1)
                );

        when(bandeiraTarifariaRepo.findById(id))
                .thenReturn(Optional.of(bandeira));

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.atualizarFimVigencia(id, dto)
        );

        verify(bandeiraTarifariaRepo, never()).buscarBandeirasComSobreposicao(
                any(), any(), any()
        );
        verify(bandeiraTarifariaRepo, never()).save(any());
    }

    @Test
    void deveRejeitarFimAnteriorAoInicioNaAtualizacao() {

        BandeiraTarifariaAtualizacaoDTO dto =
                new BandeiraTarifariaAtualizacaoDTO(inicio.minusDays(1));

        when(bandeiraTarifariaRepo.findById(id))
                .thenReturn(Optional.of(bandeira));

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.atualizarFimVigencia(id, dto)
        );

        verify(bandeiraTarifariaRepo, never()).buscarBandeirasComSobreposicao(
                any(), any(), any()
        );
        verify(bandeiraTarifariaRepo, never()).save(any());
    }

    @Test
    void deveRejeitarFimIgualAoInicioNaAtualizacao() {

        BandeiraTarifariaAtualizacaoDTO dto =
                new BandeiraTarifariaAtualizacaoDTO(inicio);

        when(bandeiraTarifariaRepo.findById(id))
                .thenReturn(Optional.of(bandeira));

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.atualizarFimVigencia(id, dto)
        );

        verify(bandeiraTarifariaRepo, never()).buscarBandeirasComSobreposicao(
                any(), any(), any()
        );
        verify(bandeiraTarifariaRepo, never()).save(any());
    }

    @Test
    void deveRejeitarAtualizacaoQueCausaSobreposicao() {

        LocalDate novoFim = LocalDate.of(2026, 8, 31);

        BandeiraTarifariaAtualizacaoDTO dto =
                new BandeiraTarifariaAtualizacaoDTO(novoFim);

        bandeira.setFimVigencia(null);

        BandeiraTarifaria outra = new BandeiraTarifaria();
        outra.setId(UUID.randomUUID());
        outra.setTipoBandeira(TipoBandeira.VERDE);
        outra.setValorAdicional(new BigDecimal("0.020000"));
        outra.setInicioVigencia(LocalDate.of(2026, 8, 1));
        outra.setFimVigencia(null);

        when(bandeiraTarifariaRepo.findById(id))
                .thenReturn(Optional.of(bandeira));

        when(bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                TipoBandeira.VERDE, inicio, novoFim
        )).thenReturn(List.of(bandeira, outra));

        assertThrows(
                BandeiraTarifariaValidationException.class,
                () -> bandeiraTarifariaService.atualizarFimVigencia(id, dto)
        );

        verify(bandeiraTarifariaRepo, never()).save(any());

        verify(bandeiraTarifariaMapper, never())
                .atualizarEntidade(any(), any());
    }
}
