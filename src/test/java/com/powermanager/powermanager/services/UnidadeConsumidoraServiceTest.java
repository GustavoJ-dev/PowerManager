
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraAtualizacaoDTO;
import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraCadastroDTO;
import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.exception.cliente.ClienteNotFoundException;
import com.powermanager.powermanager.exception.unidade_consumidora.UnidadeConsumidoraAlreadyExistsException;
import com.powermanager.powermanager.exception.unidade_consumidora.UnidadeConsumidoraNotFoundException;
import com.powermanager.powermanager.mapper.UnidadeConsumidoraMapper;
import com.powermanager.powermanager.repository.ClienteRepo;
import com.powermanager.powermanager.repository.UnidadeConsumidoraRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnidadeConsumidoraServiceTest {

    @Mock
    private UnidadeConsumidoraRepo unidadeConsumidoraRepo;

    @Mock
    private ClienteRepo clienteRepo;

    @Mock
    private UnidadeConsumidoraMapper unidadeConsumidoraMapper;

    @InjectMocks
    private UnidadeConsumidoraService unidadeConsumidoraService;

    private UnidadeConsumidoraCadastroDTO cadastroDTO(UUID clienteId) {

        return new UnidadeConsumidoraCadastroDTO(
                "UC123",
                "Rua A, 100",
                "Paramirim",
                "BA",
                "46190-000",
                "Distribuidora X",
                null,
                null,
                null,
                clienteId
        );
    }

    private UnidadeConsumidoraAtualizacaoDTO atualizacaoDTO() {

        return new UnidadeConsumidoraAtualizacaoDTO(
                "UC123",
                "Rua B, 200",
                "Paramirim",
                "BA",
                "46190-000",
                "Distribuidora X",
                null,
                null,
                null
        );
    }

    private UnidadeConsumidoraResponseDTO responseDTO(UUID id, UUID clienteId) {

        return new UnidadeConsumidoraResponseDTO(
                id,
                "UC123",
                "Rua A, 100",
                "Paramirim",
                "BA",
                "46190-000",
                "Distribuidora X",
                null,
                null,
                null,
                true,
                clienteId
        );
    }

    @Test
    void deveCadastrarUnidadeConsumidora() {

        UUID clienteId = UUID.randomUUID();
        UUID unidadeId = UUID.randomUUID();

        Cliente cliente = mock(Cliente.class);
        UnidadeConsumidora unidade = mock(UnidadeConsumidora.class);

        UnidadeConsumidoraCadastroDTO dto = cadastroDTO(clienteId);
        UnidadeConsumidoraResponseDTO response = responseDTO(unidadeId, clienteId);

        when(clienteRepo.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(unidadeConsumidoraRepo.existsByDistribuidoraAndCodigo(
                "Distribuidora X", "UC123"
        )).thenReturn(false);
        when(unidadeConsumidoraMapper.paraEntidade(dto)).thenReturn(unidade);
        when(unidadeConsumidoraRepo.save(unidade)).thenReturn(unidade);
        when(unidadeConsumidoraMapper.paraResponseDTO(unidade)).thenReturn(response);

        UnidadeConsumidoraResponseDTO resultado =
                unidadeConsumidoraService.cadastrarUnidadeConsumidora(dto);

        assertEquals(response, resultado);

        verify(clienteRepo).findById(clienteId);
        verify(unidadeConsumidoraRepo)
                .existsByDistribuidoraAndCodigo("Distribuidora X", "UC123");
        verify(unidadeConsumidoraMapper).paraEntidade(dto);
        verify(unidade).setCliente(cliente);
        verify(unidadeConsumidoraRepo).save(unidade);
        verify(unidadeConsumidoraMapper).paraResponseDTO(unidade);
    }

    @Test
    void deveLancarExcecaoAoCadastrarComClienteInexistente() {

        UUID clienteId = UUID.randomUUID();

        UnidadeConsumidoraCadastroDTO dto = cadastroDTO(clienteId);

        when(clienteRepo.findById(clienteId)).thenReturn(Optional.empty());

        assertThrows(
                ClienteNotFoundException.class,
                () -> unidadeConsumidoraService.cadastrarUnidadeConsumidora(dto)
        );

        verify(clienteRepo).findById(clienteId);
        verifyNoInteractions(unidadeConsumidoraMapper);
        verify(unidadeConsumidoraRepo, never()).save(any());
    }

    @Test
    void deveLancarExcecaoAoCadastrarUnidadeDuplicada() {

        UUID clienteId = UUID.randomUUID();

        Cliente cliente = mock(Cliente.class);
        UnidadeConsumidoraCadastroDTO dto = cadastroDTO(clienteId);

        when(clienteRepo.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(unidadeConsumidoraRepo.existsByDistribuidoraAndCodigo(
                "Distribuidora X", "UC123"
        )).thenReturn(true);

        assertThrows(
                UnidadeConsumidoraAlreadyExistsException.class,
                () -> unidadeConsumidoraService.cadastrarUnidadeConsumidora(dto)
        );

        verify(unidadeConsumidoraRepo)
                .existsByDistribuidoraAndCodigo("Distribuidora X", "UC123");
        verifyNoInteractions(unidadeConsumidoraMapper);
        verify(unidadeConsumidoraRepo, never()).save(any());
    }

    @Test
    void deveListarUnidadesConsumidoras() {

        UnidadeConsumidora unidade1 = mock(UnidadeConsumidora.class);
        UnidadeConsumidora unidade2 = mock(UnidadeConsumidora.class);

        UUID clienteId = UUID.randomUUID();

        UnidadeConsumidoraResponseDTO response1 =
                responseDTO(UUID.randomUUID(), clienteId);

        UnidadeConsumidoraResponseDTO response2 =
                responseDTO(UUID.randomUUID(), clienteId);

        when(unidadeConsumidoraRepo.findAll())
                .thenReturn(List.of(unidade1, unidade2));

        when(unidadeConsumidoraMapper.paraResponseDTO(unidade1))
                .thenReturn(response1);

        when(unidadeConsumidoraMapper.paraResponseDTO(unidade2))
                .thenReturn(response2);

        List<UnidadeConsumidoraResponseDTO> resultado =
                unidadeConsumidoraService.listarUnidadesConsumidoras();

        assertEquals(2, resultado.size());
        assertEquals(response1, resultado.get(0));
        assertEquals(response2, resultado.get(1));

        verify(unidadeConsumidoraRepo).findAll();
        verify(unidadeConsumidoraMapper).paraResponseDTO(unidade1);
        verify(unidadeConsumidoraMapper).paraResponseDTO(unidade2);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremUnidades() {

        when(unidadeConsumidoraRepo.findAll()).thenReturn(List.of());

        List<UnidadeConsumidoraResponseDTO> resultado =
                unidadeConsumidoraService.listarUnidadesConsumidoras();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());

        verify(unidadeConsumidoraRepo).findAll();
        verifyNoInteractions(unidadeConsumidoraMapper);
    }

    @Test
    void deveBuscarUnidadeConsumidoraPorId() {

        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        UnidadeConsumidora unidade = mock(UnidadeConsumidora.class);
        UnidadeConsumidoraResponseDTO response = responseDTO(id, clienteId);

        when(unidadeConsumidoraRepo.findById(id))
                .thenReturn(Optional.of(unidade));

        when(unidadeConsumidoraMapper.paraResponseDTO(unidade))
                .thenReturn(response);

        UnidadeConsumidoraResponseDTO resultado =
                unidadeConsumidoraService.buscarPorId(id);

        assertEquals(response, resultado);

        verify(unidadeConsumidoraRepo).findById(id);
        verify(unidadeConsumidoraMapper).paraResponseDTO(unidade);
    }

    @Test
    void deveLancarExcecaoQuandoUnidadeNaoForEncontrada() {

        UUID id = UUID.randomUUID();

        when(unidadeConsumidoraRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UnidadeConsumidoraNotFoundException.class,
                () -> unidadeConsumidoraService.buscarPorId(id)
        );

        verify(unidadeConsumidoraRepo).findById(id);
        verifyNoInteractions(unidadeConsumidoraMapper);
    }

    @Test
    void deveListarUnidadesPorCliente() {

        UUID clienteId = UUID.randomUUID();

        UnidadeConsumidora unidade = mock(UnidadeConsumidora.class);
        UnidadeConsumidoraResponseDTO response =
                responseDTO(UUID.randomUUID(), clienteId);

        when(clienteRepo.existsById(clienteId)).thenReturn(true);
        when(unidadeConsumidoraRepo.findByCliente_Id(clienteId))
                .thenReturn(List.of(unidade));

        when(unidadeConsumidoraMapper.paraResponseDTO(unidade))
                .thenReturn(response);

        List<UnidadeConsumidoraResponseDTO> resultado =
                unidadeConsumidoraService.listarPorCliente(clienteId);

        assertEquals(1, resultado.size());
        assertEquals(response, resultado.get(0));

        verify(clienteRepo).existsById(clienteId);
        verify(unidadeConsumidoraRepo).findByCliente_Id(clienteId);
        verify(unidadeConsumidoraMapper).paraResponseDTO(unidade);
    }

    @Test
    void deveLancarExcecaoAoListarUnidadesDeClienteInexistente() {

        UUID clienteId = UUID.randomUUID();

        when(clienteRepo.existsById(clienteId)).thenReturn(false);

        assertThrows(
                ClienteNotFoundException.class,
                () -> unidadeConsumidoraService.listarPorCliente(clienteId)
        );

        verify(clienteRepo).existsById(clienteId);
        verifyNoInteractions(unidadeConsumidoraMapper);
        verify(unidadeConsumidoraRepo, never()).findByCliente_Id(any());
    }

    @Test
    void deveAtualizarUnidadeConsumidora() {

        UUID id = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();

        UnidadeConsumidora unidade = mock(UnidadeConsumidora.class);
        when(unidade.getId()).thenReturn(id);
        UnidadeConsumidoraAtualizacaoDTO dto = atualizacaoDTO();

        UnidadeConsumidoraResponseDTO response =
                responseDTO(id, clienteId);

        when(unidadeConsumidoraRepo.findById(id))
                .thenReturn(Optional.of(unidade));

        when(unidadeConsumidoraRepo.findByDistribuidoraAndCodigo(
                "Distribuidora X", "UC123"
        )).thenReturn(Optional.of(unidade));

        when(unidadeConsumidoraRepo.save(unidade)).thenReturn(unidade);

        when(unidadeConsumidoraMapper.paraResponseDTO(unidade))
                .thenReturn(response);

        UnidadeConsumidoraResponseDTO resultado =
                unidadeConsumidoraService.atualizarUnidadeConsumidora(id, dto);

        assertEquals(response, resultado);

        verify(unidadeConsumidoraRepo).findById(id);
        verify(unidadeConsumidoraRepo)
                .findByDistribuidoraAndCodigo("Distribuidora X", "UC123");
        verify(unidadeConsumidoraMapper).atualizarEntidade(dto, unidade);
        verify(unidadeConsumidoraRepo).save(unidade);
        verify(unidadeConsumidoraMapper).paraResponseDTO(unidade);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComCodigoDeOutraUnidade() {

        UUID id = UUID.randomUUID();

        UnidadeConsumidora unidadeAtual = mock(UnidadeConsumidora.class);
        UnidadeConsumidora outraUnidade = mock(UnidadeConsumidora.class);

        when(unidadeConsumidoraRepo.findById(id))
                .thenReturn(Optional.of(unidadeAtual));

        when(unidadeConsumidoraRepo.findByDistribuidoraAndCodigo(
                "Distribuidora X", "UC123"
        )).thenReturn(Optional.of(outraUnidade));

        when(outraUnidade.getId()).thenReturn(UUID.randomUUID());

        assertThrows(
                UnidadeConsumidoraAlreadyExistsException.class,
                () -> unidadeConsumidoraService.atualizarUnidadeConsumidora(
                        id, atualizacaoDTO()
                )
        );

        verify(unidadeConsumidoraRepo).findById(id);
        verify(unidadeConsumidoraRepo)
                .findByDistribuidoraAndCodigo("Distribuidora X", "UC123");

        verify(unidadeConsumidoraRepo, never()).save(any());
        verifyNoInteractions(unidadeConsumidoraMapper);
    }

    @Test
    void deveLancarExcecaoAoAtualizarUnidadeInexistente() {

        UUID id = UUID.randomUUID();

        when(unidadeConsumidoraRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                UnidadeConsumidoraNotFoundException.class,
                () -> unidadeConsumidoraService.atualizarUnidadeConsumidora(
                        id, atualizacaoDTO()
                )
        );

        verify(unidadeConsumidoraRepo).findById(id);
        verify(unidadeConsumidoraRepo, never())
                .findByDistribuidoraAndCodigo(anyString(), anyString());
        verify(unidadeConsumidoraRepo, never()).save(any());
        verifyNoInteractions(unidadeConsumidoraMapper);
    }
}
