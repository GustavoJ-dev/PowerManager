
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraAtualizacaoDTO;
import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraCadastroDTO;
import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraResponseDTO;
import com.powermanager.powermanager.entity.AcionamentoBandeira;
import com.powermanager.powermanager.enums.TipoBandeira;
import com.powermanager.powermanager.exception.acionamento_bandeira.AcionamentoBandeiraNotFoundException;
import com.powermanager.powermanager.exception.acionamento_bandeira.AcionamentoBandeiraValidationException;
import com.powermanager.powermanager.mapper.AcionamentoBandeiraMapper;
import com.powermanager.powermanager.repository.AcionamentoBandeiraRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcionamentoBandeiraServiceTest {

    @Mock
    private AcionamentoBandeiraRepo acionamentoBandeiraRepo;

    @Mock
    private AcionamentoBandeiraMapper acionamentoBandeiraMapper;

    @InjectMocks
    private AcionamentoBandeiraService acionamentoBandeiraService;

    private UUID id;
    private LocalDate inicio;
    private LocalDate fim;
    private LocalDate divulgacao;
    private AcionamentoBandeiraCadastroDTO cadastroDTO;
    private AcionamentoBandeira acionamento;
    private AcionamentoBandeiraResponseDTO responseDTO;

    @BeforeEach
    void setUp() {

        id = UUID.randomUUID();
        inicio = LocalDate.of(2026, 1, 1);
        fim = LocalDate.of(2026, 12, 31);
        divulgacao = LocalDate.of(2025, 12, 20);

        cadastroDTO = new AcionamentoBandeiraCadastroDTO(
                TipoBandeira.VERDE,
                inicio,
                fim,
                divulgacao,
                "https://www.aneel.gov.br/referencia"
        );

        acionamento = new AcionamentoBandeira();
        acionamento.setId(id);
        acionamento.setTipoBandeira(TipoBandeira.VERDE);
        acionamento.setInicioVigencia(inicio);
        acionamento.setFimVigencia(fim);
        acionamento.setDataDivulgacao(divulgacao);
        acionamento.setReferenciaOficial(
                "https://www.aneel.gov.br/referencia"
        );

        responseDTO = new AcionamentoBandeiraResponseDTO(
                id,
                TipoBandeira.VERDE,
                inicio,
                fim,
                divulgacao,
                "https://www.aneel.gov.br/referencia"
        );
    }

    @Test
    void deveCadastrarAcionamentoComSucesso() {

        when(acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                inicio, fim
        )).thenReturn(List.of());

        when(acionamentoBandeiraMapper.paraEntidade(cadastroDTO))
                .thenReturn(acionamento);

        when(acionamentoBandeiraRepo.save(acionamento))
                .thenReturn(acionamento);

        when(acionamentoBandeiraMapper.paraResponseDTO(acionamento))
                .thenReturn(responseDTO);

        AcionamentoBandeiraResponseDTO resultado =
                acionamentoBandeiraService.cadastrarAcionamento(cadastroDTO);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
        assertEquals(TipoBandeira.VERDE, resultado.tipoBandeira());

        verify(acionamentoBandeiraRepo).save(acionamento);
    }

    @Test
    void deveRejeitarCadastroNulo() {

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(null)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarTipoDeBandeiraNulo() {

        AcionamentoBandeiraCadastroDTO dtoInvalido =
                new AcionamentoBandeiraCadastroDTO(
                        null,
                        inicio,
                        fim,
                        divulgacao,
                        "referencia oficial"
                );

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(dtoInvalido)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarInicioDeVigenciaNulo() {

        AcionamentoBandeiraCadastroDTO dtoInvalido =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        null,
                        fim,
                        divulgacao,
                        "referencia oficial"
                );

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(dtoInvalido)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarDataDeDivulgacaoNula() {

        AcionamentoBandeiraCadastroDTO dtoInvalido =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        inicio,
                        fim,
                        null,
                        "referencia oficial"
                );

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(dtoInvalido)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarReferenciaOficialNula() {

        AcionamentoBandeiraCadastroDTO dtoInvalido =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        inicio,
                        fim,
                        divulgacao,
                        null
                );

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(dtoInvalido)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarReferenciaOficialEmBranco() {

        AcionamentoBandeiraCadastroDTO dtoInvalido =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        inicio,
                        fim,
                        divulgacao,
                        "   "
                );

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(dtoInvalido)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarReferenciaOficialComMaisDe500Caracteres() {

        String referenciaInvalida = "A".repeat(501);

        AcionamentoBandeiraCadastroDTO dtoInvalido =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        inicio,
                        fim,
                        divulgacao,
                        referenciaInvalida
                );

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(dtoInvalido)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void devePermitirReferenciaOficialCom500Caracteres() {

        String referencia = "A".repeat(500);

        AcionamentoBandeiraCadastroDTO dtoValido =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        inicio,
                        fim,
                        divulgacao,
                        referencia
                );

        AcionamentoBandeira acionamentoValido = new AcionamentoBandeira();
        acionamentoValido.setTipoBandeira(TipoBandeira.VERDE);
        acionamentoValido.setInicioVigencia(inicio);
        acionamentoValido.setFimVigencia(fim);
        acionamentoValido.setDataDivulgacao(divulgacao);
        acionamentoValido.setReferenciaOficial(referencia);

        when(acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                inicio, fim
        )).thenReturn(List.of());

        when(acionamentoBandeiraMapper.paraEntidade(dtoValido))
                .thenReturn(acionamentoValido);

        when(acionamentoBandeiraRepo.save(acionamentoValido))
                .thenReturn(acionamentoValido);

        when(acionamentoBandeiraMapper.paraResponseDTO(acionamentoValido))
                .thenReturn(new AcionamentoBandeiraResponseDTO(
                        null,
                        TipoBandeira.VERDE,
                        inicio,
                        fim,
                        divulgacao,
                        referencia
                ));

        AcionamentoBandeiraResponseDTO resultado =
                acionamentoBandeiraService.cadastrarAcionamento(dtoValido);

        assertNotNull(resultado);
        assertEquals(referencia, resultado.referenciaOficial());
    }

    @Test
    void deveRejeitarFimAnteriorAoInicio() {

        AcionamentoBandeiraCadastroDTO dtoInvalido =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        inicio,
                        inicio.minusDays(1),
                        divulgacao,
                        "referencia oficial"
                );

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(dtoInvalido)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarFimIgualAoInicio() {

        AcionamentoBandeiraCadastroDTO dtoInvalido =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.VERDE,
                        inicio,
                        inicio,
                        divulgacao,
                        "referencia oficial"
                );

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(dtoInvalido)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void devePermitirCadastroComVigenciaAberta() {

        AcionamentoBandeiraCadastroDTO dtoAberto =
                new AcionamentoBandeiraCadastroDTO(
                        TipoBandeira.AMARELA,
                        inicio,
                        null,
                        divulgacao,
                        "referencia oficial"
                );

        AcionamentoBandeira acionamentoAberto = new AcionamentoBandeira();
        acionamentoAberto.setTipoBandeira(TipoBandeira.AMARELA);
        acionamentoAberto.setInicioVigencia(inicio);
        acionamentoAberto.setDataDivulgacao(divulgacao);
        acionamentoAberto.setReferenciaOficial("referencia oficial");

        AcionamentoBandeiraResponseDTO responseAberta =
                new AcionamentoBandeiraResponseDTO(
                        UUID.randomUUID(),
                        TipoBandeira.AMARELA,
                        inicio,
                        null,
                        divulgacao,
                        "referencia oficial"
                );

        when(acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                inicio, null
        )).thenReturn(List.of());

        when(acionamentoBandeiraMapper.paraEntidade(dtoAberto))
                .thenReturn(acionamentoAberto);

        when(acionamentoBandeiraRepo.save(acionamentoAberto))
                .thenReturn(acionamentoAberto);

        when(acionamentoBandeiraMapper.paraResponseDTO(acionamentoAberto))
                .thenReturn(responseAberta);

        AcionamentoBandeiraResponseDTO resultado =
                acionamentoBandeiraService.cadastrarAcionamento(dtoAberto);

        assertNotNull(resultado);
        assertNull(resultado.fimVigencia());

        verify(acionamentoBandeiraRepo).save(acionamentoAberto);
    }

    @Test
    void deveRejeitarCadastroComSobreposicao() {

        when(acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                inicio, fim
        )).thenReturn(List.of(acionamento));

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.cadastrarAcionamento(cadastroDTO)
        );

        verify(acionamentoBandeiraRepo, never()).save(any());
        verifyNoInteractions(acionamentoBandeiraMapper);
    }

    @Test
    void deveListarAcionamentosEmOrdemCronologica() {

        AcionamentoBandeira antigo = new AcionamentoBandeira();
        antigo.setInicioVigencia(inicio.minusYears(1));

        AcionamentoBandeira recente = new AcionamentoBandeira();
        recente.setInicioVigencia(inicio);

        AcionamentoBandeiraResponseDTO responseAntigo =
                mock(AcionamentoBandeiraResponseDTO.class);

        AcionamentoBandeiraResponseDTO responseRecente =
                mock(AcionamentoBandeiraResponseDTO.class);

        when(acionamentoBandeiraRepo.findAll())
                .thenReturn(List.of(recente, antigo));

        when(acionamentoBandeiraMapper.paraResponseDTO(antigo))
                .thenReturn(responseAntigo);

        when(acionamentoBandeiraMapper.paraResponseDTO(recente))
                .thenReturn(responseRecente);

        List<AcionamentoBandeiraResponseDTO> resultado =
                acionamentoBandeiraService.listarAcionamentos();

        assertEquals(2, resultado.size());
        assertSame(responseAntigo, resultado.get(0));
        assertSame(responseRecente, resultado.get(1));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremAcionamentos() {

        when(acionamentoBandeiraRepo.findAll()).thenReturn(List.of());

        List<AcionamentoBandeiraResponseDTO> resultado =
                acionamentoBandeiraService.listarAcionamentos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verifyNoInteractions(acionamentoBandeiraMapper);
    }

    @Test
    void deveBuscarAcionamentoPorId() {

        when(acionamentoBandeiraRepo.findById(id))
                .thenReturn(Optional.of(acionamento));

        when(acionamentoBandeiraMapper.paraResponseDTO(acionamento))
                .thenReturn(responseDTO);

        AcionamentoBandeiraResponseDTO resultado =
                acionamentoBandeiraService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoAcionamentoNaoExistirPorId() {

        when(acionamentoBandeiraRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                AcionamentoBandeiraNotFoundException.class,
                () -> acionamentoBandeiraService.buscarPorId(id)
        );

        verifyNoInteractions(acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarIdNuloNaBusca() {

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.buscarPorId(null)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveBuscarAcionamentoVigenteNaDataInformada() {

        LocalDate dataConsulta = LocalDate.of(2026, 6, 15);

        when(acionamentoBandeiraRepo.buscarAcionamentoVigenteEm(dataConsulta))
                .thenReturn(Optional.of(acionamento));

        when(acionamentoBandeiraMapper.paraResponseDTO(acionamento))
                .thenReturn(responseDTO);

        AcionamentoBandeiraResponseDTO resultado =
                acionamentoBandeiraService.buscarAcionamentoVigenteEm(dataConsulta);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverAcionamentoVigente() {

        LocalDate dataConsulta = LocalDate.of(2026, 6, 15);

        when(acionamentoBandeiraRepo.buscarAcionamentoVigenteEm(dataConsulta))
                .thenReturn(Optional.empty());

        assertThrows(
                AcionamentoBandeiraNotFoundException.class,
                () -> acionamentoBandeiraService.buscarAcionamentoVigenteEm(dataConsulta)
        );

        verifyNoInteractions(acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarDataNulaNaBuscaDeVigencia() {

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.buscarAcionamentoVigenteEm(null)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveAtualizarFimVigenciaComSucesso() {

        LocalDate novoFim = LocalDate.of(2026, 8, 31);

        AcionamentoBandeiraAtualizacaoDTO atualizacaoDTO =
                new AcionamentoBandeiraAtualizacaoDTO(novoFim);

        acionamento.setFimVigencia(null);

        AcionamentoBandeiraResponseDTO responseAtualizado =
                new AcionamentoBandeiraResponseDTO(
                        id,
                        TipoBandeira.VERDE,
                        inicio,
                        novoFim,
                        divulgacao,
                        "https://www.aneel.gov.br/referencia"
                );

        when(acionamentoBandeiraRepo.findById(id))
                .thenReturn(Optional.of(acionamento));

        when(acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                inicio, novoFim
        )).thenReturn(List.of(acionamento));

        when(acionamentoBandeiraRepo.save(acionamento))
                .thenReturn(acionamento);

        when(acionamentoBandeiraMapper.paraResponseDTO(acionamento))
                .thenReturn(responseAtualizado);

        AcionamentoBandeiraResponseDTO resultado =
                acionamentoBandeiraService.atualizarFimVigencia(
                        id, atualizacaoDTO
                );

        assertEquals(novoFim, resultado.fimVigencia());

        verify(acionamentoBandeiraMapper)
                .atualizarEntidade(atualizacaoDTO, acionamento);

        verify(acionamentoBandeiraRepo).save(acionamento);
    }

    @Test
    void deveRejeitarAtualizacaoNula() {

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.atualizarFimVigencia(id, null)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarFimNuloNaAtualizacao() {

        AcionamentoBandeiraAtualizacaoDTO dtoInvalido =
                new AcionamentoBandeiraAtualizacaoDTO(null);

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.atualizarFimVigencia(
                        id, dtoInvalido
                )
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarAtualizacaoComIdNulo() {

        AcionamentoBandeiraAtualizacaoDTO dto =
                new AcionamentoBandeiraAtualizacaoDTO(fim);

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.atualizarFimVigencia(null, dto)
        );

        verifyNoInteractions(acionamentoBandeiraRepo, acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarAtualizacaoDeAcionamentoInexistente() {

        AcionamentoBandeiraAtualizacaoDTO dto =
                new AcionamentoBandeiraAtualizacaoDTO(fim);

        when(acionamentoBandeiraRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                AcionamentoBandeiraNotFoundException.class,
                () -> acionamentoBandeiraService.atualizarFimVigencia(id, dto)
        );

        verify(acionamentoBandeiraRepo, never()).save(any());
        verifyNoInteractions(acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarAtualizacaoDeVigenciaJaEncerrada() {

        AcionamentoBandeiraAtualizacaoDTO dto =
                new AcionamentoBandeiraAtualizacaoDTO(
                        LocalDate.of(2027, 1, 1)
                );

        when(acionamentoBandeiraRepo.findById(id))
                .thenReturn(Optional.of(acionamento));

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.atualizarFimVigencia(id, dto)
        );

        verify(acionamentoBandeiraRepo, never())
                .buscarAcionamentosComSobreposicao(any(), any());

        verify(acionamentoBandeiraRepo, never()).save(any());
        verifyNoInteractions(acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarFimAnteriorAoInicioNaAtualizacao() {

        acionamento.setFimVigencia(null);

        AcionamentoBandeiraAtualizacaoDTO dto =
                new AcionamentoBandeiraAtualizacaoDTO(inicio.minusDays(1));

        when(acionamentoBandeiraRepo.findById(id))
                .thenReturn(Optional.of(acionamento));

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.atualizarFimVigencia(id, dto)
        );

        verify(acionamentoBandeiraRepo, never())
                .buscarAcionamentosComSobreposicao(any(), any());

        verify(acionamentoBandeiraRepo, never()).save(any());
        verifyNoInteractions(acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarFimIgualAoInicioNaAtualizacao() {

        acionamento.setFimVigencia(null);

        AcionamentoBandeiraAtualizacaoDTO dto =
                new AcionamentoBandeiraAtualizacaoDTO(inicio);

        when(acionamentoBandeiraRepo.findById(id))
                .thenReturn(Optional.of(acionamento));

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.atualizarFimVigencia(id, dto)
        );

        verify(acionamentoBandeiraRepo, never())
                .buscarAcionamentosComSobreposicao(any(), any());

        verify(acionamentoBandeiraRepo, never()).save(any());
        verifyNoInteractions(acionamentoBandeiraMapper);
    }

    @Test
    void deveRejeitarAtualizacaoQueCausaSobreposicao() {

        LocalDate novoFim = LocalDate.of(2026, 8, 31);

        AcionamentoBandeiraAtualizacaoDTO dto =
                new AcionamentoBandeiraAtualizacaoDTO(novoFim);

        acionamento.setFimVigencia(null);

        AcionamentoBandeira outro = new AcionamentoBandeira();
        outro.setId(UUID.randomUUID());
        outro.setTipoBandeira(TipoBandeira.AMARELA);
        outro.setInicioVigencia(LocalDate.of(2026, 8, 1));
        outro.setFimVigencia(null);

        when(acionamentoBandeiraRepo.findById(id))
                .thenReturn(Optional.of(acionamento));

        when(acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                inicio, novoFim
        )).thenReturn(List.of(acionamento, outro));

        assertThrows(
                AcionamentoBandeiraValidationException.class,
                () -> acionamentoBandeiraService.atualizarFimVigencia(id, dto)
        );

        verify(acionamentoBandeiraRepo, never()).save(any());

        verify(acionamentoBandeiraMapper, never())
                .atualizarEntidade(any(), any());
    }
}
