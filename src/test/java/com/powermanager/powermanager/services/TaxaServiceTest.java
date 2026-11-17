package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Taxa;
import com.powermanager.powermanager.enums.ClasseConsumo;
import com.powermanager.powermanager.enums.ModalidadeTarifaria;
import com.powermanager.powermanager.enums.SubgrupoTarifario;
import com.powermanager.powermanager.exception.taxa.TaxaNotFoundException;
import com.powermanager.powermanager.exception.taxa.TaxaValidationException;
import com.powermanager.powermanager.mapper.TaxaMapper;
import com.powermanager.powermanager.repository.TaxaRepo;
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
class TaxaServiceTest {

    @Mock
    private TaxaRepo taxaRepo;

    @Mock
    private TaxaMapper taxaMapper;

    @InjectMocks
    private TaxaService taxaService;

    private TaxaCadastroDTO cadastroDTO;
    private Taxa taxa;
    private TaxaResponseDTO responseDTO;

    private UUID id;
    private LocalDate inicio;
    private LocalDate fim;

    @BeforeEach
    void setUp() {

        id = UUID.randomUUID();
        inicio = LocalDate.of(2026, 1, 1);
        fim = LocalDate.of(2026, 12, 31);

        cadastroDTO = new TaxaCadastroDTO(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("0.350000"),
                new BigDecimal("0.250000"),
                inicio,
                fim
        );

        taxa = new Taxa();
        taxa.setId(id);
        taxa.setDistribuidora("Distribuidora Teste");
        taxa.setSubgrupoTarifario(SubgrupoTarifario.B1);
        taxa.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        taxa.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        taxa.setTarifaEnergia(new BigDecimal("0.350000"));
        taxa.setTarifaUsoDistribuicao(new BigDecimal("0.250000"));
        taxa.setInicioVigencia(inicio);
        taxa.setFimVigencia(fim);

        responseDTO = new TaxaResponseDTO(
                id,
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("0.350000"),
                new BigDecimal("0.250000"),
                inicio,
                fim
        );
    }

    @Test
    void deveCadastrarTaxaComSucesso() {

        when(taxaMapper.paraEntidade(cadastroDTO)).thenReturn(taxa);
        when(taxaRepo.buscarTaxasComSobreposicao(
                anyString(), any(), any(), any(), any(), any()
        )).thenReturn(List.of());
        when(taxaRepo.save(taxa)).thenReturn(taxa);
        when(taxaMapper.paraResponseDTO(taxa)).thenReturn(responseDTO);

        TaxaResponseDTO resultado = taxaService.cadastrarTaxa(cadastroDTO);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());

        verify(taxaRepo).save(taxa);
    }

    @Test
    void deveRejeitarCadastroNulo() {

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.cadastrarTaxa(null)
        );

        verifyNoInteractions(taxaMapper, taxaRepo);
    }

    @Test
    void deveRejeitarTaxaComValorNegativo() {

        TaxaCadastroDTO dtoInvalido = new TaxaCadastroDTO(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("-0.100000"),
                new BigDecimal("0.250000"),
                inicio,
                fim
        );

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.cadastrarTaxa(dtoInvalido)
        );

        verifyNoInteractions(taxaMapper, taxaRepo);
    }

    @Test
    void deveRejeitarTaxaComValorNulo() {

        TaxaCadastroDTO dtoInvalido = new TaxaCadastroDTO(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                null,
                new BigDecimal("0.250000"),
                inicio,
                fim
        );

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.cadastrarTaxa(dtoInvalido)
        );

        verifyNoInteractions(taxaMapper, taxaRepo);
    }

    @Test
    void deveRejeitarPeriodoComFimAnteriorAoInicio() {

        TaxaCadastroDTO dtoInvalido = new TaxaCadastroDTO(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("0.350000"),
                new BigDecimal("0.250000"),
                inicio,
                inicio.minusDays(1)
        );

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.cadastrarTaxa(dtoInvalido)
        );

        verifyNoInteractions(taxaMapper, taxaRepo);
    }

    @Test
    void deveRejeitarCadastroComSobreposicaoDeVigencia() {

        when(taxaMapper.paraEntidade(cadastroDTO)).thenReturn(taxa);
        when(taxaRepo.buscarTaxasComSobreposicao(
                anyString(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(taxa));

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.cadastrarTaxa(cadastroDTO)
        );

        verify(taxaRepo, never()).save(any());
    }

    @Test
    void deveListarTaxasEmOrdemCronologica() {

        Taxa taxaAntiga = new Taxa();
        taxaAntiga.setId(UUID.randomUUID());
        taxaAntiga.setInicioVigencia(inicio.minusYears(1));

        Taxa taxaRecente = new Taxa();
        taxaRecente.setId(UUID.randomUUID());
        taxaRecente.setInicioVigencia(inicio);

        TaxaResponseDTO responseAntiga = mock(TaxaResponseDTO.class);
        TaxaResponseDTO responseRecente = mock(TaxaResponseDTO.class);

        when(taxaRepo.findAll()).thenReturn(List.of(taxaRecente, taxaAntiga));
        when(taxaMapper.paraResponseDTO(taxaAntiga)).thenReturn(responseAntiga);
        when(taxaMapper.paraResponseDTO(taxaRecente)).thenReturn(responseRecente);

        List<TaxaResponseDTO> resultado = taxaService.listarTaxas();

        assertEquals(2, resultado.size());
        assertSame(responseAntiga, resultado.get(0));
        assertSame(responseRecente, resultado.get(1));
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremTaxas() {

        when(taxaRepo.findAll()).thenReturn(List.of());

        List<TaxaResponseDTO> resultado = taxaService.listarTaxas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verifyNoInteractions(taxaMapper);
    }

    @Test
    void deveBuscarTaxaPorId() {

        when(taxaRepo.findById(id)).thenReturn(Optional.of(taxa));
        when(taxaMapper.paraResponseDTO(taxa)).thenReturn(responseDTO);

        TaxaResponseDTO resultado = taxaService.buscarPorId(id);

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoTaxaNaoExistirPorId() {

        when(taxaRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                TaxaNotFoundException.class,
                () -> taxaService.buscarPorId(id)
        );
    }

    @Test
    void deveRejeitarIdNuloNaBusca() {

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.buscarPorId(null)
        );

        verifyNoInteractions(taxaRepo);
    }

    @Test
    void deveBuscarTaxaVigenteNaDataInformada() {

        LocalDate dataConsulta = LocalDate.of(2026, 6, 15);

        when(taxaRepo.buscarTaxaVigenteEm(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                dataConsulta
        )).thenReturn(Optional.of(taxa));

        when(taxaMapper.paraResponseDTO(taxa)).thenReturn(responseDTO);

        TaxaResponseDTO resultado = taxaService.buscarTaxaVigenteEm(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                dataConsulta
        );

        assertNotNull(resultado);
        assertEquals(id, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoNaoHouverTaxaVigente() {

        LocalDate dataConsulta = LocalDate.of(2026, 6, 15);

        when(taxaRepo.buscarTaxaVigenteEm(
                anyString(), any(), any(), any(), any()
        )).thenReturn(Optional.empty());

        assertThrows(
                TaxaNotFoundException.class,
                () -> taxaService.buscarTaxaVigenteEm(
                        "Distribuidora Teste",
                        SubgrupoTarifario.B1,
                        ClasseConsumo.RESIDENCIAL,
                        ModalidadeTarifaria.CONVENCIONAL,
                        dataConsulta
                )
        );
    }

    @Test
    void deveRejeitarDataNulaNaBuscaDeVigencia() {

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.buscarTaxaVigenteEm(
                        "Distribuidora Teste",
                        SubgrupoTarifario.B1,
                        ClasseConsumo.RESIDENCIAL,
                        ModalidadeTarifaria.CONVENCIONAL,
                        null
                )
        );

        verifyNoInteractions(taxaRepo);
    }

    @Test
    void deveReajustarTaxaPreservandoAnterior() {

        LocalDate inicioNovo = fim.plusDays(1);

        TaxaCadastroDTO novoDTO = new TaxaCadastroDTO(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("0.400000"),
                new BigDecimal("0.300000"),
                inicioNovo,
                null
        );

        Taxa novaTaxa = new Taxa();
        novaTaxa.setDistribuidora("Distribuidora Teste");
        novaTaxa.setSubgrupoTarifario(SubgrupoTarifario.B1);
        novaTaxa.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        novaTaxa.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        novaTaxa.setTarifaEnergia(new BigDecimal("0.400000"));
        novaTaxa.setTarifaUsoDistribuicao(new BigDecimal("0.300000"));
        novaTaxa.setInicioVigencia(inicioNovo);

        TaxaResponseDTO novaResponse = new TaxaResponseDTO(
                UUID.randomUUID(),
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("0.400000"),
                new BigDecimal("0.300000"),
                inicioNovo,
                null
        );

        when(taxaRepo.buscarTaxaVigenteEm(
                anyString(), any(), any(), any(), eq(inicioNovo)
        )).thenReturn(Optional.of(taxa));

        when(taxaMapper.paraEntidade(novoDTO)).thenReturn(novaTaxa);

        when(taxaRepo.buscarTaxasComSobreposicao(
                anyString(), any(), any(), any(), eq(inicioNovo), isNull()
        )).thenReturn(List.of(taxa));

        when(taxaRepo.save(novaTaxa)).thenReturn(novaTaxa);
        when(taxaMapper.paraResponseDTO(novaTaxa)).thenReturn(novaResponse);

        TaxaResponseDTO resultado = taxaService.reajustarTaxa(novoDTO);

        assertNotNull(resultado);
        assertEquals(inicioNovo, taxa.getFimVigencia());
        assertEquals(inicioNovo, novaTaxa.getInicioVigencia());

        verify(taxaRepo).save(novaTaxa);
    }

    @Test
    void deveRejeitarReajusteComInicioIgualAoDaTaxaAtual() {

        when(taxaRepo.buscarTaxaVigenteEm(
                anyString(), any(), any(), any(), eq(inicio)
        )).thenReturn(Optional.of(taxa));

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.reajustarTaxa(cadastroDTO)
        );

        verify(taxaRepo, never()).save(any());
    }

    @Test
    void deveRejeitarReajusteQuandoExistirOutraTaxaSobreposta() {

        LocalDate inicioNovo = fim.plusDays(1);

        TaxaCadastroDTO novoDTO = new TaxaCadastroDTO(
                "Distribuidora Teste",
                SubgrupoTarifario.B1,
                ClasseConsumo.RESIDENCIAL,
                ModalidadeTarifaria.CONVENCIONAL,
                new BigDecimal("0.400000"),
                new BigDecimal("0.300000"),
                inicioNovo,
                null
        );

        Taxa novaTaxa = new Taxa();
        novaTaxa.setDistribuidora("Distribuidora Teste");
        novaTaxa.setSubgrupoTarifario(SubgrupoTarifario.B1);
        novaTaxa.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        novaTaxa.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        novaTaxa.setTarifaEnergia(new BigDecimal("0.400000"));
        novaTaxa.setTarifaUsoDistribuicao(new BigDecimal("0.300000"));
        novaTaxa.setInicioVigencia(inicioNovo);

        Taxa outraTaxa = new Taxa();
        outraTaxa.setId(UUID.randomUUID());
        outraTaxa.setDistribuidora("Distribuidora Teste");
        outraTaxa.setSubgrupoTarifario(SubgrupoTarifario.B1);
        outraTaxa.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        outraTaxa.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        outraTaxa.setInicioVigencia(inicioNovo);
        outraTaxa.setTarifaEnergia(new BigDecimal("0.450000"));
        outraTaxa.setTarifaUsoDistribuicao(new BigDecimal("0.350000"));

        when(taxaRepo.buscarTaxaVigenteEm(
                anyString(), any(), any(), any(), eq(inicioNovo)
        )).thenReturn(Optional.of(taxa));

        when(taxaMapper.paraEntidade(novoDTO)).thenReturn(novaTaxa);

        when(taxaRepo.buscarTaxasComSobreposicao(
                anyString(), any(), any(), any(), eq(inicioNovo), isNull()
        )).thenReturn(List.of(taxa, outraTaxa));

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.reajustarTaxa(novoDTO)
        );

        verify(taxaRepo, never()).save(any());
    }

    @Test
    void deveRejeitarReajusteNulo() {

        assertThrows(
                TaxaValidationException.class,
                () -> taxaService.reajustarTaxa(null)
        );

        verifyNoInteractions(taxaRepo, taxaMapper);
    }
}
