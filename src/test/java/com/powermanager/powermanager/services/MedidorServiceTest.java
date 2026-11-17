
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.medidor.MedidorAtualizacaoDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.enums.TipoLigacao;
import com.powermanager.powermanager.enums.TipoMedidor;
import com.powermanager.powermanager.exception.medidor.MedidorNotFoundException;
import com.powermanager.powermanager.exception.medidor.MedidorValidationException;
import com.powermanager.powermanager.exception.unidade_consumidora.UnidadeConsumidoraNotFoundException;
import com.powermanager.powermanager.mapper.MedidorMapper;
import com.powermanager.powermanager.repository.MedidorRepo;
import com.powermanager.powermanager.repository.UnidadeConsumidoraRepo;
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
class MedidorServiceTest {

    @Mock
    private MedidorRepo medidorRepo;

    @Mock
    private UnidadeConsumidoraRepo unidadeConsumidoraRepo;

    @Mock
    private MedidorMapper medidorMapper;

    @InjectMocks
    private MedidorService medidorService;

    private MedidorCadastroDTO cadastroDTO(UUID unidadeId) {

        return new MedidorCadastroDTO(
                "MED-001",
                TipoMedidor.ELETRONICO,
                TipoLigacao.MONOFASICO,
                LocalDate.of(2026, 1, 10),
                unidadeId
        );
    }

    private MedidorAtualizacaoDTO atualizacaoDTO() {

        return new MedidorAtualizacaoDTO(
                TipoMedidor.INTELIGENTE,
                TipoLigacao.BIFASICO
        );
    }

    private MedidorResponseDTO responseDTO(UUID id, UUID unidadeId) {

        return new MedidorResponseDTO(
                id,
                "MED-001",
                TipoMedidor.ELETRONICO,
                TipoLigacao.MONOFASICO,
                true,
                LocalDate.of(2026, 1, 10),
                null,
                unidadeId
        );
    }

    @Test
    void deveCadastrarMedidor() {

        UUID unidadeConsumidoraId = UUID.randomUUID();

        MedidorCadastroDTO dto = new MedidorCadastroDTO(
                "MED-001",
                TipoMedidor.ELETRONICO,
                TipoLigacao.MONOFASICO,
                LocalDate.now(),
                unidadeConsumidoraId
        );

        UnidadeConsumidora unidadeConsumidora =
                mock(UnidadeConsumidora.class);

        Medidor medidorSalvo = mock(Medidor.class);

        MedidorResponseDTO responseDTO =
                mock(MedidorResponseDTO.class);

        when(unidadeConsumidoraRepo.findById(unidadeConsumidoraId))
                .thenReturn(Optional.of(unidadeConsumidora));

        when(unidadeConsumidora.getId())
                .thenReturn(unidadeConsumidoraId);

        when(medidorRepo.findByNumero("MED-001"))
                .thenReturn(Optional.empty());

        when(medidorRepo.findByUnidadeConsumidora_IdAndAtivoTrue(
                unidadeConsumidoraId
        )).thenReturn(Optional.empty());

        when(medidorMapper.paraEntidade(dto))
                .thenReturn(medidorSalvo);

        when(medidorRepo.save(medidorSalvo))
                .thenReturn(medidorSalvo);

        when(medidorMapper.paraResponseDTO(medidorSalvo)).thenReturn(responseDTO);

        MedidorResponseDTO resultado = medidorService.cadastrarMedidor(dto);

        assertNotNull(resultado);
        assertEquals(responseDTO, resultado);

        verify(unidadeConsumidoraRepo).findById(unidadeConsumidoraId);
        verify(unidadeConsumidora).getId();
        verify(medidorRepo).findByNumero("MED-001");
        verify(medidorRepo).findByUnidadeConsumidora_IdAndAtivoTrue(unidadeConsumidoraId);
        verify(medidorMapper).paraEntidade(dto);
        verify(medidorRepo).save(medidorSalvo);
        verify(medidorMapper).paraResponseDTO(medidorSalvo);
    }

    @Test
    void deveLancarExcecaoAoCadastrarComUnidadeInexistente() {

        UUID unidadeId = UUID.randomUUID();
        MedidorCadastroDTO dto = cadastroDTO(unidadeId);

        when(unidadeConsumidoraRepo.findById(unidadeId))
                .thenReturn(Optional.empty());

        assertThrows(
                UnidadeConsumidoraNotFoundException.class,
                () -> medidorService.cadastrarMedidor(dto)
        );

        verify(unidadeConsumidoraRepo).findById(unidadeId);
        verifyNoInteractions(medidorMapper);
        verify(medidorRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCadastrarNumeroJaExistente() {

        UUID unidadeId = UUID.randomUUID();

        MedidorCadastroDTO dto = cadastroDTO(unidadeId);
        UnidadeConsumidora unidade = mock(UnidadeConsumidora.class);

        when(unidadeConsumidoraRepo.findById(unidadeId))
                .thenReturn(Optional.of(unidade));
        when(medidorRepo.findByNumero("MED-001"))
                .thenReturn(Optional.of(mock(Medidor.class)));

        assertThrows(
                MedidorValidationException.class,
                () -> medidorService.cadastrarMedidor(dto)
        );

        verify(medidorRepo).findByNumero("MED-001");
        verify(medidorRepo, never())
                .findByUnidadeConsumidora_IdAndAtivoTrue(any());
        verifyNoInteractions(medidorMapper);
        verify(medidorRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoUnidadeJaPossuirMedidorAtivo() {

        UUID unidadeConsumidoraId = UUID.randomUUID();

        MedidorCadastroDTO dto = new MedidorCadastroDTO(
                "MED-001",
                TipoMedidor.ELETRONICO,
                TipoLigacao.MONOFASICO,
                LocalDate.now(),
                unidadeConsumidoraId
        );

        UnidadeConsumidora unidadeConsumidora = mock(UnidadeConsumidora.class);

        when(unidadeConsumidoraRepo.findById(unidadeConsumidoraId))
                .thenReturn(Optional.of(unidadeConsumidora));

        when(unidadeConsumidora.getId())
                .thenReturn(unidadeConsumidoraId);

        when(medidorRepo.findByNumero("MED-001"))
                .thenReturn(Optional.empty());

        when(medidorRepo.findByUnidadeConsumidora_IdAndAtivoTrue(unidadeConsumidoraId))
                .thenReturn(Optional.of(mock(Medidor.class)));

        assertThrows(
                MedidorValidationException.class,
                () -> medidorService.cadastrarMedidor(dto)
        );

        verify(medidorRepo, never()).save(any(Medidor.class));
    }

    @Test
    void deveBuscarMedidorPorId() {

        UUID id = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        Medidor medidor = mock(Medidor.class);
        MedidorResponseDTO response = responseDTO(id, unidadeId);

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));
        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(response);

        MedidorResponseDTO resultado = medidorService.buscarPorId(id);

        assertEquals(response, resultado);

        verify(medidorRepo).findById(id);
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveLancarExcecaoAoBuscarMedidorInexistente() {

        UUID id = UUID.randomUUID();

        when(medidorRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(
                MedidorNotFoundException.class,
                () -> medidorService.buscarPorId(id)
        );

        verify(medidorRepo).findById(id);
        verifyNoInteractions(medidorMapper);
    }

    @Test
    void deveBuscarMedidorPorNumero() {

        UUID id = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        Medidor medidor = mock(Medidor.class);
        MedidorResponseDTO response = responseDTO(id, unidadeId);

        when(medidorRepo.findByNumero("MED-001"))
                .thenReturn(Optional.of(medidor));
        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(response);

        MedidorResponseDTO resultado =
                medidorService.buscarPorNumero("MED-001");

        assertEquals(response, resultado);

        verify(medidorRepo).findByNumero("MED-001");
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveLancarExcecaoAoBuscarNumeroInexistente() {

        when(medidorRepo.findByNumero("MED-999"))
                .thenReturn(Optional.empty());

        assertThrows(
                MedidorNotFoundException.class,
                () -> medidorService.buscarPorNumero("MED-999")
        );

        verify(medidorRepo).findByNumero("MED-999");
        verifyNoInteractions(medidorMapper);
    }

    @Test
    void deveLancarExcecaoAoBuscarNumeroEmBranco() {

        assertThrows(
                MedidorValidationException.class,
                () -> medidorService.buscarPorNumero(" ")
        );

        verifyNoInteractions(medidorRepo, medidorMapper);
    }

    @Test
    void deveListarMedidores() {

        UUID unidadeId = UUID.randomUUID();

        Medidor medidor1 = mock(Medidor.class);
        Medidor medidor2 = mock(Medidor.class);

        MedidorResponseDTO response1 =
                responseDTO(UUID.randomUUID(), unidadeId);
        MedidorResponseDTO response2 =
                responseDTO(UUID.randomUUID(), unidadeId);

        when(medidorRepo.findAll())
                .thenReturn(List.of(medidor1, medidor2));
        when(medidorMapper.paraResponseDTO(medidor1))
                .thenReturn(response1);
        when(medidorMapper.paraResponseDTO(medidor2))
                .thenReturn(response2);

        List<MedidorResponseDTO> resultado = medidorService.listarMedidores();

        assertEquals(2, resultado.size());
        assertEquals(response1, resultado.get(0));
        assertEquals(response2, resultado.get(1));

        verify(medidorRepo).findAll();
        verify(medidorMapper).paraResponseDTO(medidor1);
        verify(medidorMapper).paraResponseDTO(medidor2);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremMedidores() {

        when(medidorRepo.findAll()).thenReturn(List.of());

        List<MedidorResponseDTO> resultado = medidorService.listarMedidores();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(medidorRepo).findAll();
        verifyNoInteractions(medidorMapper);
    }

    @Test
    void deveListarMedidoresPorUnidadeConsumidora() {

        UUID unidadeId = UUID.randomUUID();

        Medidor medidor = mock(Medidor.class);
        MedidorResponseDTO response =
                responseDTO(UUID.randomUUID(), unidadeId);

        when(unidadeConsumidoraRepo.existsById(unidadeId)).thenReturn(true);
        when(medidorRepo.findByUnidadeConsumidora_Id(unidadeId))
                .thenReturn(List.of(medidor));
        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(response);

        List<MedidorResponseDTO> resultado =
                medidorService.listarPorUnidadeConsumidora(unidadeId);

        assertEquals(1, resultado.size());
        assertEquals(response, resultado.get(0));

        verify(unidadeConsumidoraRepo).existsById(unidadeId);
        verify(medidorRepo).findByUnidadeConsumidora_Id(unidadeId);
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveLancarExcecaoAoListarMedidoresDeUnidadeInexistente() {

        UUID unidadeId = UUID.randomUUID();

        when(unidadeConsumidoraRepo.existsById(unidadeId)).thenReturn(false);

        assertThrows(
                UnidadeConsumidoraNotFoundException.class,
                () -> medidorService.listarPorUnidadeConsumidora(unidadeId)
        );

        verify(unidadeConsumidoraRepo).existsById(unidadeId);
        verify(medidorRepo, never())
                .findByUnidadeConsumidora_Id(any());
        verifyNoInteractions(medidorMapper);
    }

    @Test
    void deveAtualizarMedidor() {

        UUID id = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        Medidor medidor = mock(Medidor.class);
        MedidorAtualizacaoDTO dto = atualizacaoDTO();
        MedidorResponseDTO response = responseDTO(id, unidadeId);

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));
        when(medidorRepo.save(medidor)).thenReturn(medidor);
        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(response);

        MedidorResponseDTO resultado = medidorService.atualizarMedidor(id, dto);

        assertEquals(response, resultado);

        verify(medidorRepo).findById(id);
        verify(medidorMapper).atualizarEntidade(dto, medidor);
        verify(medidorRepo).save(medidor);
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveRetirarMedidor() {

        UUID id = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();
        LocalDate dataInstalacao = LocalDate.of(2026, 1, 10);
        LocalDate dataRetirada = LocalDate.of(2026, 5, 10);

        Medidor medidor = mock(Medidor.class);

        MedidorResponseDTO response = new MedidorResponseDTO(
                id,
                "MED-001",
                TipoMedidor.ELETRONICO,
                TipoLigacao.MONOFASICO,
                false,
                dataInstalacao,
                dataRetirada,
                unidadeId
        );

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));
        when(medidor.getAtivo()).thenReturn(true);
        when(medidor.getDataRetirada()).thenReturn(null);
        when(medidor.getDataInstalacao()).thenReturn(dataInstalacao);
        when(medidorRepo.save(medidor)).thenReturn(medidor);
        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(response);

        MedidorResponseDTO resultado =
                medidorService.retirarMedidor(id, dataRetirada);

        assertEquals(response, resultado);

        verify(medidor).setAtivo(false);
        verify(medidor).setDataRetirada(dataRetirada);
        verify(medidorRepo).save(medidor);
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveImpedirRetiradaComDataNula() {

        UUID id = UUID.randomUUID();

        assertThrows(
                MedidorValidationException.class,
                () -> medidorService.retirarMedidor(id, null)
        );

        verifyNoInteractions(medidorRepo, medidorMapper);
    }

    @Test
    void deveImpedirRetiradaComDataAnteriorOuIgualAInstalacao() {

        UUID id = UUID.randomUUID();
        LocalDate dataInstalacao = LocalDate.of(2026, 1, 10);

        Medidor medidor = mock(Medidor.class);

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));
        when(medidor.getAtivo()).thenReturn(true);
        when(medidor.getDataRetirada()).thenReturn(null);
        when(medidor.getDataInstalacao()).thenReturn(dataInstalacao);

        assertThrows(
                MedidorValidationException.class,
                () -> medidorService.retirarMedidor(id, dataInstalacao)
        );

        verify(medidorRepo, never()).save(any());
        verifyNoInteractions(medidorMapper);
    }

    @Test
    void deveImpedirRetiradaDeMedidorJaInativo() {

        UUID id = UUID.randomUUID();

        Medidor medidor = mock(Medidor.class);

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));
        when(medidor.getAtivo()).thenReturn(false);

        assertThrows(
                MedidorValidationException.class,
                () -> medidorService.retirarMedidor(
                        id, LocalDate.of(2026, 5, 10)
                )
        );

        verify(medidorRepo, never()).save(any());
        verifyNoInteractions(medidorMapper);
    }
}
