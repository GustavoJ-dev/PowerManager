
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaAtualizacaoDTO;
import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaCadastroDTO;
import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaResponseDTO;
import com.powermanager.powermanager.entity.RegraTributaria;
import com.powermanager.powermanager.enums.AbrangenciaTributaria;
import com.powermanager.powermanager.enums.TipoCalculoTributario;
import com.powermanager.powermanager.enums.TipoTributo;
import com.powermanager.powermanager.exception.regra_tributaria.RegraTributariaNotFoundException;
import com.powermanager.powermanager.exception.regra_tributaria.RegraTributariaValidationException;
import com.powermanager.powermanager.mapper.RegraTributariaMapper;
import com.powermanager.powermanager.repository.RegraTributariaRepo;
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
class RegraTributariaServiceTest {

    @Mock
    private RegraTributariaRepo regraTributariaRepo;

    @Mock
    private RegraTributariaMapper regraTributariaMapper;

    @InjectMocks
    private RegraTributariaService regraTributariaService;

    private RegraTributaria regra;
    private RegraTributariaResponseDTO responseDTO;

    private UUID id;

    private final LocalDate inicioVigencia = LocalDate.of(2026, 1, 1);
    private final LocalDate fimVigencia = LocalDate.of(2026, 12, 31);

    @BeforeEach
    void setUp() {

        id = UUID.randomUUID();

        regra = new RegraTributaria();
        regra.setId(id);
        regra.setTipoTributo(TipoTributo.PIS);
        regra.setAbrangencia(AbrangenciaTributaria.FEDERAL);
        regra.setTipoCalculo(TipoCalculoTributario.PERCENTUAL);
        regra.setAliquota(new BigDecimal("0.016500"));
        regra.setValorFixo(null);
        regra.setInicioVigencia(inicioVigencia);
        regra.setFimVigencia(null);

        responseDTO = new RegraTributariaResponseDTO(
                id,
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                TipoCalculoTributario.PERCENTUAL,
                new BigDecimal("0.016500"),
                null,
                inicioVigencia,
                null
        );
    }

    @Test
    void deveCadastrarRegraComDadosValidos() {

        RegraTributariaCadastroDTO dto = criarCadastroFederal();

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);
        when(regraTributariaRepo.buscarRegrasComSobreposicao(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of());
        when(regraTributariaRepo.save(regra)).thenReturn(regra);
        when(regraTributariaMapper.paraResponseDTO(regra)).thenReturn(responseDTO);

        RegraTributariaResponseDTO resultado =
                regraTributariaService.cadastrarRegra(dto);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
        assertEquals(TipoTributo.PIS, resultado.tipoTributo());

        verify(regraTributariaMapper).paraEntidade(dto);
        verify(regraTributariaRepo).save(regra);
        verify(regraTributariaMapper).paraResponseDTO(regra);
    }

    @Test
    void naoDeveCadastrarRegraComSobreposicaoDeVigencia() {

        RegraTributariaCadastroDTO dto = criarCadastroFederal();

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);
        when(regraTributariaRepo.buscarRegrasComSobreposicao(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(regra));

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarRegraComTipoTributoIncompativelComAbrangencia() {

        regra.setTipoTributo(TipoTributo.ICMS);
        regra.setAbrangencia(AbrangenciaTributaria.FEDERAL);

        RegraTributariaCadastroDTO dto = criarCadastroFederal();

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }


    @Test
    void naoDeveCadastrarRegraFederalComLocalizacao() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                "BA",
                null,
                null,
                TipoCalculoTributario.PERCENTUAL,
                new BigDecimal("0.650000"),
                null,
                LocalDate.of(2026, 1, 1),
                null
        );

        RegraTributaria regra = new RegraTributaria();

        regra.setTipoTributo(TipoTributo.PIS);
        regra.setAbrangencia(AbrangenciaTributaria.FEDERAL);
        regra.setEstado("BA");
        regra.setMunicipio(null);
        regra.setCodigoIbge(null);
        regra.setTipoCalculo(TipoCalculoTributario.PERCENTUAL);
        regra.setAliquota(new BigDecimal("0.650000"));
        regra.setValorFixo(null);
        regra.setInicioVigencia(LocalDate.of(2026, 1, 1));
        regra.setFimVigencia(null);

        when(regraTributariaMapper.paraEntidade(dto))
                .thenReturn(regra);

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaMapper).paraEntidade(dto);
        verify(regraTributariaRepo, never()).save(any(RegraTributaria.class));
    }


    @Test
    void naoDeveCadastrarRegraEstadualSemEstado() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.ICMS,
                AbrangenciaTributaria.ESTADUAL,
                null,
                null,
                null,
                TipoCalculoTributario.PERCENTUAL,
                new BigDecimal("0.180000"),
                null,
                inicioVigencia,
                null
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);

        regra.setTipoTributo(TipoTributo.ICMS);
        regra.setAbrangencia(AbrangenciaTributaria.ESTADUAL);
        regra.setEstado(null);

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarRegraEstadualComMunicipio() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.ICMS,
                AbrangenciaTributaria.ESTADUAL,
                "BA",
                "Salvador",
                null,
                TipoCalculoTributario.PERCENTUAL,
                new BigDecimal("0.180000"),
                null,
                inicioVigencia,
                null
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);

        regra.setTipoTributo(TipoTributo.ICMS);
        regra.setAbrangencia(AbrangenciaTributaria.ESTADUAL);
        regra.setEstado("BA");
        regra.setMunicipio("Salvador");

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarRegraMunicipalSemLocalizacaoCompleta() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.CIP_COSIP,
                AbrangenciaTributaria.MUNICIPAL,
                "BA",
                null,
                null,
                TipoCalculoTributario.VALOR_FIXO,
                null,
                new BigDecimal("15.00"),
                inicioVigencia,
                null
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);

        regra.setTipoTributo(TipoTributo.CIP_COSIP);
        regra.setAbrangencia(AbrangenciaTributaria.MUNICIPAL);
        regra.setEstado("BA");
        regra.setMunicipio(null);
        regra.setCodigoIbge(null);

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarRegraMunicipalComCodigoIbgeInvalido() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.CIP_COSIP,
                AbrangenciaTributaria.MUNICIPAL,
                "BA",
                "Salvador",
                "123",
                TipoCalculoTributario.VALOR_FIXO,
                null,
                new BigDecimal("15.00"),
                inicioVigencia,
                null
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);

        regra.setTipoTributo(TipoTributo.CIP_COSIP);
        regra.setAbrangencia(AbrangenciaTributaria.MUNICIPAL);
        regra.setEstado("BA");
        regra.setMunicipio("Salvador");
        regra.setCodigoIbge("123");

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarCalculoPercentualSemAliquota() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                TipoCalculoTributario.PERCENTUAL,
                null,
                null,
                inicioVigencia,
                null
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);
        regra.setAliquota(null);

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarCalculoPercentualComValorFixo() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                TipoCalculoTributario.PERCENTUAL,
                new BigDecimal("0.016500"),
                new BigDecimal("10.00"),
                inicioVigencia,
                null
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);
        regra.setValorFixo(new BigDecimal("10.00"));

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarValorFixoSemValorInformado() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.CIP_COSIP,
                AbrangenciaTributaria.MUNICIPAL,
                "BA",
                "Salvador",
                "2927408",
                TipoCalculoTributario.VALOR_FIXO,
                null,
                null,
                inicioVigencia,
                null
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);

        regra.setTipoTributo(TipoTributo.CIP_COSIP);
        regra.setAbrangencia(AbrangenciaTributaria.MUNICIPAL);
        regra.setTipoCalculo(TipoCalculoTributario.VALOR_FIXO);
        regra.setEstado("BA");
        regra.setMunicipio("Salvador");
        regra.setCodigoIbge("2927408");
        regra.setValorFixo(null);

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarValorFixoComAliquota() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.CIP_COSIP,
                AbrangenciaTributaria.MUNICIPAL,
                "BA",
                "Salvador",
                "2927408",
                TipoCalculoTributario.VALOR_FIXO,
                new BigDecimal("0.100000"),
                new BigDecimal("15.00"),
                inicioVigencia,
                null
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);

        regra.setTipoTributo(TipoTributo.CIP_COSIP);
        regra.setAbrangencia(AbrangenciaTributaria.MUNICIPAL);
        regra.setTipoCalculo(TipoCalculoTributario.VALOR_FIXO);
        regra.setEstado("BA");
        regra.setMunicipio("Salvador");
        regra.setCodigoIbge("2927408");
        regra.setAliquota(new BigDecimal("0.100000"));
        regra.setValorFixo(new BigDecimal("15.00"));

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveCadastrarRegraComPeriodoInvalido() {

        RegraTributariaCadastroDTO dto = new RegraTributariaCadastroDTO(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                TipoCalculoTributario.PERCENTUAL,
                new BigDecimal("0.016500"),
                null,
                inicioVigencia,
                inicioVigencia
        );

        when(regraTributariaMapper.paraEntidade(dto)).thenReturn(regra);
        regra.setFimVigencia(inicioVigencia);

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.cadastrarRegra(dto)
        );

        verify(regraTributariaRepo, never()).save(any());
    }


    @Test
    void deveListarTodasAsRegras() {

        RegraTributaria segundaRegra = new RegraTributaria();

        when(regraTributariaRepo.findAll())
                .thenReturn(List.of(regra, segundaRegra));

        when(regraTributariaMapper.paraResponseDTO(regra))
                .thenReturn(responseDTO);

        when(regraTributariaMapper.paraResponseDTO(segundaRegra))
                .thenReturn(responseDTO);

        List<RegraTributariaResponseDTO> resultado =
                regraTributariaService.listarRegras();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());

        verify(regraTributariaRepo).findAll();
        verify(regraTributariaMapper, times(2)).paraResponseDTO(any());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremRegras() {

        when(regraTributariaRepo.findAll()).thenReturn(List.of());

        List<RegraTributariaResponseDTO> resultado =
                regraTributariaService.listarRegras();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(regraTributariaRepo).findAll();
        verifyNoInteractions(regraTributariaMapper);
    }


    @Test
    void deveBuscarRegraPorId() {

        when(regraTributariaRepo.findById(id))
                .thenReturn(Optional.of(regra));

        when(regraTributariaMapper.paraResponseDTO(regra))
                .thenReturn(responseDTO);

        RegraTributariaResponseDTO resultado =
                regraTributariaService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());

        verify(regraTributariaRepo).findById(id);
        verify(regraTributariaMapper).paraResponseDTO(regra);
    }

    @Test
    void naoDeveBuscarRegraInexistentePorId() {

        when(regraTributariaRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                RegraTributariaNotFoundException.class,
                () -> regraTributariaService.buscarPorId(id)
        );

        verify(regraTributariaRepo).findById(id);
        verifyNoInteractions(regraTributariaMapper);
    }


    @Test
    void deveBuscarRegraVigenteEmDataInformada() {

        LocalDate data = LocalDate.of(2026, 6, 15);

        when(regraTributariaRepo.buscarRegraVigenteEm(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                data
        )).thenReturn(Optional.of(regra));

        when(regraTributariaMapper.paraResponseDTO(regra))
                .thenReturn(responseDTO);

        RegraTributariaResponseDTO resultado =
                regraTributariaService.buscarRegraVigenteEm(
                        TipoTributo.PIS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        data
                );

        assertNotNull(resultado);
        assertEquals(TipoTributo.PIS, resultado.tipoTributo());

        verify(regraTributariaRepo).buscarRegraVigenteEm(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                data
        );
    }

    @Test
    void naoDeveBuscarRegraVigenteComTipoTributoIncompativel() {

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.buscarRegraVigenteEm(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        inicioVigencia
                )
        );

        verify(regraTributariaRepo, never()).buscarRegraVigenteEm(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void naoDeveBuscarRegraEstadualSemEstado() {

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.buscarRegraVigenteEm(
                        TipoTributo.ICMS,
                        AbrangenciaTributaria.ESTADUAL,
                        null,
                        null,
                        null,
                        inicioVigencia
                )
        );

        verify(regraTributariaRepo, never()).buscarRegraVigenteEm(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void naoDeveBuscarRegraMunicipalComCodigoIbgeInvalido() {

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.buscarRegraVigenteEm(
                        TipoTributo.CIP_COSIP,
                        AbrangenciaTributaria.MUNICIPAL,
                        "BA",
                        "Salvador",
                        "123",
                        inicioVigencia
                )
        );

        verify(regraTributariaRepo, never()).buscarRegraVigenteEm(
                any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    void deveLancarExcecaoQuandoNaoExistirRegraVigente() {

        LocalDate data = LocalDate.of(2026, 6, 15);

        when(regraTributariaRepo.buscarRegraVigenteEm(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                data
        )).thenReturn(Optional.empty());

        assertThrows(
                RegraTributariaNotFoundException.class,
                () -> regraTributariaService.buscarRegraVigenteEm(
                        TipoTributo.PIS,
                        AbrangenciaTributaria.FEDERAL,
                        null,
                        null,
                        null,
                        data
                )
        );

        verify(regraTributariaRepo).buscarRegraVigenteEm(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                data
        );
    }

    @Test
    void deveAtualizarFimVigenciaDeRegraAberta() {

        RegraTributariaAtualizacaoDTO dto =
                new RegraTributariaAtualizacaoDTO(fimVigencia);

        when(regraTributariaRepo.findById(id))
                .thenReturn(Optional.of(regra));

        when(regraTributariaRepo.buscarRegrasComSobreposicao(
                any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(regra));

        when(regraTributariaRepo.save(regra))
                .thenReturn(regra);

        when(regraTributariaMapper.paraResponseDTO(regra))
                .thenReturn(responseDTO);

        RegraTributariaResponseDTO resultado =
                regraTributariaService.atualizarFimVigencia(id, dto);

        assertNotNull(resultado);

        verify(regraTributariaRepo).findById(id);
        verify(regraTributariaMapper).atualizarEntidade(dto, regra);
        verify(regraTributariaRepo).save(regra);
    }

    @Test
    void naoDeveAtualizarRegraInexistente() {

        RegraTributariaAtualizacaoDTO dto =
                new RegraTributariaAtualizacaoDTO(fimVigencia);

        when(regraTributariaRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                RegraTributariaNotFoundException.class,
                () -> regraTributariaService.atualizarFimVigencia(id, dto)
        );

        verify(regraTributariaRepo).findById(id);
        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveAtualizarRegraQueJaPossuiFimVigencia() {

        regra.setFimVigencia(LocalDate.of(2026, 6, 30));

        RegraTributariaAtualizacaoDTO dto =
                new RegraTributariaAtualizacaoDTO(fimVigencia);

        when(regraTributariaRepo.findById(id))
                .thenReturn(Optional.of(regra));

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.atualizarFimVigencia(id, dto)
        );

        verify(regraTributariaMapper, never())
                .atualizarEntidade(any(), any());

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveAtualizarFimVigenciaAnteriorAoInicio() {

        RegraTributariaAtualizacaoDTO dto =
                new RegraTributariaAtualizacaoDTO(
                        LocalDate.of(2025, 12, 31)
                );

        when(regraTributariaRepo.findById(id))
                .thenReturn(Optional.of(regra));

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.atualizarFimVigencia(id, dto)
        );

        verify(regraTributariaMapper, never())
                .atualizarEntidade(any(), any());

        verify(regraTributariaRepo, never()).save(any());
    }

    @Test
    void naoDeveAtualizarFimVigenciaIgualAoInicio() {

        RegraTributariaAtualizacaoDTO dto =
                new RegraTributariaAtualizacaoDTO(inicioVigencia);

        when(regraTributariaRepo.findById(id))
                .thenReturn(Optional.of(regra));

        assertThrows(
                RegraTributariaValidationException.class,
                () -> regraTributariaService.atualizarFimVigencia(id, dto)
        );

        verify(regraTributariaMapper, never())
                .atualizarEntidade(any(), any());

        verify(regraTributariaRepo, never()).save(any());
    }


    private RegraTributariaCadastroDTO criarCadastroFederal() {

        return new RegraTributariaCadastroDTO(
                TipoTributo.PIS,
                AbrangenciaTributaria.FEDERAL,
                null,
                null,
                null,
                TipoCalculoTributario.PERCENTUAL,
                new BigDecimal("0.016500"),
                null,
                inicioVigencia,
                null
        );
    }
}

