
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.leitura.LeituraCadastroDTO;
import com.powermanager.powermanager.dto.leitura.LeituraResponseDTO;
import com.powermanager.powermanager.entity.Leitura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.enums.TipoLeitura;
import com.powermanager.powermanager.exception.leitura.LeituraNotFoundException;
import com.powermanager.powermanager.exception.leitura.LeituraValidationException;
import com.powermanager.powermanager.exception.medidor.MedidorNotFoundException;
import com.powermanager.powermanager.mapper.LeituraMapper;
import com.powermanager.powermanager.repository.LeituraRepo;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeituraServiceTest {

    @Mock
    private LeituraRepo leituraRepo;

    @Mock
    private MedidorRepo medidorRepo;

    @Mock
    private LeituraMapper leituraMapper;

    @InjectMocks
    private LeituraService leituraService;

    private UUID medidorId;
    private UUID leituraId;
    private Medidor medidor;
    private LeituraCadastroDTO cadastroDTO;
    private Leitura leitura;
    private LeituraResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        medidorId = UUID.randomUUID();
        leituraId = UUID.randomUUID();

        medidor = new Medidor();
        medidor.setId(medidorId);
        medidor.setNumero("MED-001");
        medidor.setAtivo(true);
        medidor.setDataInstalacao(LocalDate.of(2025, 1, 1));
        medidor.setDataRetirada(null);

        cadastroDTO = new LeituraCadastroDTO(
                new BigDecimal("150.000"),
                LocalDateTime.of(2025, 2, 10, 10, 0),
                TipoLeitura.REAL,
                medidorId
        );

        leitura = new Leitura();
        leitura.setId(leituraId);
        leitura.setValorLeitura(cadastroDTO.valorLeitura());
        leitura.setDataLeitura(cadastroDTO.dataLeitura());
        leitura.setTipoLeitura(cadastroDTO.tipoLeitura());
        leitura.setMedidor(medidor);

        responseDTO = new LeituraResponseDTO(
                leituraId,
                cadastroDTO.valorLeitura(),
                cadastroDTO.dataLeitura(),
                cadastroDTO.tipoLeitura(),
                medidorId
        );
    }

    @Test
    void deveRegistrarLeituraComSucesso() {

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));
        when(leituraRepo.existsByMedidor_IdAndDataLeitura(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(false);

        when(leituraRepo.findTopByMedidor_IdAndDataLeituraBeforeOrderByDataLeituraDesc(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(Optional.empty());

        when(leituraRepo.findTopByMedidor_IdAndDataLeituraAfterOrderByDataLeituraAsc(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(Optional.empty());

        when(leituraMapper.paraEntidade(cadastroDTO)).thenReturn(leitura);
        when(leituraRepo.save(leitura)).thenReturn(leitura);
        when(leituraMapper.paraResponseDTO(leitura)).thenReturn(responseDTO);

        LeituraResponseDTO resultado = leituraService.registrarLeitura(cadastroDTO);

        assertNotNull(resultado);
        assertEquals(leituraId, resultado.id());
        assertEquals(new BigDecimal("150.000"), resultado.valorLeitura());
        assertEquals(medidorId, resultado.medidorId());

        verify(leituraRepo).save(leitura);
        verify(leituraMapper).paraEntidade(cadastroDTO);
        verify(leituraMapper).paraResponseDTO(leitura);
    }

    @Test
    void deveLancarExcecaoQuandoMedidorNaoExistir() {

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.empty());

        assertThrows(
                MedidorNotFoundException.class,
                () -> leituraService.registrarLeitura(cadastroDTO)
        );

        verify(leituraRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoLeituraEstiverDuplicada() {

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));
        when(leituraRepo.existsByMedidor_IdAndDataLeitura(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(true);

        assertThrows(
                LeituraValidationException.class,
                () -> leituraService.registrarLeitura(cadastroDTO)
        );

        verify(leituraRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoLeituraForAnteriorAInstalacao() {

        cadastroDTO = new LeituraCadastroDTO(
                new BigDecimal("100.000"),
                LocalDateTime.of(2024, 12, 31, 23, 59),
                TipoLeitura.REAL,
                medidorId
        );

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));

        assertThrows(
                LeituraValidationException.class,
                () -> leituraService.registrarLeitura(cadastroDTO)
        );

        verify(leituraRepo, never()).save(any());
    }

    @Test
    void devePermitirLeituraAtrasadaDentroDoPeriodoDeOperacaoDoMedidorRetirado() {

        medidor.setAtivo(false);
        medidor.setDataRetirada(LocalDate.of(2025, 3, 1));

        cadastroDTO = new LeituraCadastroDTO(
                new BigDecimal("200.000"),
                LocalDateTime.of(2025, 2, 20, 10, 0),
                TipoLeitura.REAL,
                medidorId
        );

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));
        when(leituraRepo.existsByMedidor_IdAndDataLeitura(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(false);

        when(leituraRepo.findTopByMedidor_IdAndDataLeituraBeforeOrderByDataLeituraDesc(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(Optional.empty());

        when(leituraRepo.findTopByMedidor_IdAndDataLeituraAfterOrderByDataLeituraAsc(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(Optional.empty());

        when(leituraMapper.paraEntidade(cadastroDTO)).thenReturn(leitura);
        when(leituraRepo.save(leitura)).thenReturn(leitura);
        when(leituraMapper.paraResponseDTO(leitura)).thenReturn(responseDTO);

        assertDoesNotThrow(() -> leituraService.registrarLeitura(cadastroDTO));

        verify(leituraRepo).save(leitura);
    }

    @Test
    void deveLancarExcecaoQuandoLeituraForPosteriorADataDeRetirada() {

        medidor.setAtivo(false);
        medidor.setDataRetirada(LocalDate.of(2025, 2, 1));

        cadastroDTO = new LeituraCadastroDTO(
                new BigDecimal("200.000"),
                LocalDateTime.of(2025, 2, 2, 10, 0),
                TipoLeitura.REAL,
                medidorId
        );

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));

        assertThrows(
                LeituraValidationException.class,
                () -> leituraService.registrarLeitura(cadastroDTO)
        );

        verify(leituraRepo, never()).save(any());
    }

    @Test
    void deveRejeitarLeituraComValorMenorQueLeituraAnterior() {

        Leitura anterior = new Leitura();
        anterior.setValorLeitura(new BigDecimal("200.000"));
        anterior.setDataLeitura(LocalDateTime.of(2025, 2, 1, 10, 0));
        anterior.setMedidor(medidor);

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));
        when(leituraRepo.existsByMedidor_IdAndDataLeitura(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(false);

        when(leituraRepo.findTopByMedidor_IdAndDataLeituraBeforeOrderByDataLeituraDesc(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(Optional.of(anterior));

        assertThrows(
                LeituraValidationException.class,
                () -> leituraService.registrarLeitura(cadastroDTO)
        );

        verify(leituraRepo, never()).save(any());
    }

    @Test
    void deveRejeitarLeituraComValorMaiorQueLeituraPosterior() {

        Leitura posterior = new Leitura();
        posterior.setValorLeitura(new BigDecimal("100.000"));
        posterior.setDataLeitura(LocalDateTime.of(2025, 2, 20, 10, 0));
        posterior.setMedidor(medidor);

        when(medidorRepo.findById(medidorId)).thenReturn(Optional.of(medidor));
        when(leituraRepo.existsByMedidor_IdAndDataLeitura(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(false);

        when(leituraRepo.findTopByMedidor_IdAndDataLeituraBeforeOrderByDataLeituraDesc(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(Optional.empty());

        when(leituraRepo.findTopByMedidor_IdAndDataLeituraAfterOrderByDataLeituraAsc(
                medidorId, cadastroDTO.dataLeitura()
        )).thenReturn(Optional.of(posterior));

        assertThrows(
                LeituraValidationException.class,
                () -> leituraService.registrarLeitura(cadastroDTO)
        );

        verify(leituraRepo, never()).save(any());
    }

    @Test
    void deveListarTodasAsLeituras() {

        when(leituraRepo.findAll()).thenReturn(List.of(leitura));

        List<LeituraResponseDTO> resultado = leituraService.listarLeituras();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        verify(leituraMapper).paraResponseDTO(leitura);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremLeituras() {

        when(leituraRepo.findAll()).thenReturn(List.of());

        List<LeituraResponseDTO> resultado = leituraService.listarLeituras();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarLeituraPorId() {

        when(leituraRepo.findById(leituraId)).thenReturn(Optional.of(leitura));
        when(leituraMapper.paraResponseDTO(leitura)).thenReturn(responseDTO);

        LeituraResponseDTO resultado = leituraService.buscarPorId(leituraId);

        assertNotNull(resultado);
        assertEquals(leituraId, resultado.id());
        verify(leituraMapper).paraResponseDTO(leitura);
    }

    @Test
    void deveLancarExcecaoQuandoLeituraNaoExistir() {

        when(leituraRepo.findById(leituraId)).thenReturn(Optional.empty());

        assertThrows(
                LeituraNotFoundException.class,
                () -> leituraService.buscarPorId(leituraId)
        );
    }

    @Test
    void deveListarLeiturasPorMedidor() {

        when(medidorRepo.existsById(medidorId)).thenReturn(true);
        when(leituraRepo.findByMedidor_IdOrderByDataLeituraAsc(medidorId))
                .thenReturn(List.of(leitura));
        when(leituraMapper.paraResponseDTO(leitura)).thenReturn(responseDTO);

        List<LeituraResponseDTO> resultado =
                leituraService.listarPorMedidor(medidorId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(leituraId, resultado.get(0).id());
    }

    @Test
    void deveLancarExcecaoAoListarLeiturasDeMedidorInexistente() {

        when(medidorRepo.existsById(medidorId)).thenReturn(false);

        assertThrows(
                MedidorNotFoundException.class,
                () -> leituraService.listarPorMedidor(medidorId)
        );

        verify(leituraRepo, never())
                .findByMedidor_IdOrderByDataLeituraAsc(any());
    }
}
