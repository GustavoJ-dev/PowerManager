package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.medidor.MedidorAtualizacaoDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.exception.ClienteNotFoundException;
import com.powermanager.powermanager.exception.MedidorNotFoundException;
import com.powermanager.powermanager.exception.MedidorValidationException;
import com.powermanager.powermanager.mapper.MedidorMapper;
import com.powermanager.powermanager.repository.ClienteRepo;
import com.powermanager.powermanager.repository.MedidorRepo;
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
public class MedidorServiceTest {

    @Mock
    private MedidorRepo medidorRepo;

    @Mock
    private ClienteRepo clienteRepo;

    @Mock
    private MedidorMapper medidorMapper;

    @InjectMocks
    private MedidorService medidorService;


    // métodos auxiliares

    private Medidor criarMedidor(){

        Medidor medidor = new Medidor();

        medidor.setId(UUID.randomUUID());
        medidor.setNumero("MED001");
        medidor.setLocalizacao("Sala técnica");
        medidor.setTipo("BIDIRECIONAL");
        medidor.setCodigoFase("TRIFASICO");
        medidor.setTipoFatura("ENERGIA");
        medidor.setDias(30);

        return medidor;
    }

    private Cliente criarCliente() {

        Cliente cliente = new Cliente();

        cliente.setId(UUID.randomUUID());
        cliente.setNome("Cliente Teste");

        return cliente;
    }

    private MedidorCadastroDTO criarCadastroDTO(UUID clienteId) {

        return new MedidorCadastroDTO(
                "MED001",
                "Sala técnica",
                "BIDIRECIONAL",
                "TRIFASICO",
                "ENERGIA",
                30,
                clienteId);
    }

    private MedidorAtualizacaoDTO criarAtualizacaoDTO() {

        return new MedidorAtualizacaoDTO(
                "MED002",
                "Sala técnica",
                "BIDIRECIONAL",
                "TRIFASICO",
                "ENERGIA",
                30);
    }

    private MedidorResponseDTO criarResponseDTO(Medidor medidor) {

        return new MedidorResponseDTO(
                medidor.getId(),
                medidor.getNumero(),
                medidor.getLocalizacao(),
                medidor.getTipo(),
                medidor.getCodigoFase(),
                medidor.getTipoFatura(),
                medidor.getDias(),
                medidor.getCliente() != null ? medidor.getCliente().getId() : null);
    }

    private void preencherMedidor(Medidor destino, MedidorAtualizacaoDTO dto) {

        destino.setNumero(dto.numero());
        destino.setLocalizacao(dto.localizacao());
        destino.setTipo(dto.tipoMedidor());
        destino.setCodigoFase(dto.codigoFase());
        destino.setTipoFatura(dto.classeConsumo());
        destino.setDias(dto.dias());
    }

    @Test
    void deveCadastrarMedidor(){

        Cliente cliente = criarCliente();

        MedidorCadastroDTO dto = criarCadastroDTO(cliente.getId());

        Medidor medidor = criarMedidor();
        medidor.setCliente(cliente);

        MedidorResponseDTO responseDTO = criarResponseDTO(medidor);

        when(medidorRepo.findByNumero(dto.numero())).thenReturn(Optional.empty());
        when(clienteRepo.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(medidorMapper.paraEntidade(dto)).thenReturn(medidor);
        when(medidorRepo.save(medidor)).thenReturn(medidor);
        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(responseDTO);

        MedidorResponseDTO resultado = medidorService.cadastrarMedidor(dto);

        assertEquals(responseDTO, resultado);

        verify(medidorRepo).findByNumero(dto.numero());
        verify(clienteRepo).findById(cliente.getId());
        verify(medidorMapper).paraEntidade(dto);
        verify(medidorRepo).save(medidor);
        verify(medidorMapper).paraResponseDTO(medidor);

        assertEquals(cliente, medidor.getCliente());
    }

    @Test
    void deveLancarExcecaoQuandoCadastroForNulo(){

        assertThrows(MedidorValidationException.class,
                ()-> medidorService.cadastrarMedidor(null));

        verifyNoInteractions(medidorRepo, clienteRepo, medidorMapper);
    }

    @Test
    void deveLancarExcecaoQuandoNumeroJaEstiverCadastrado(){

        MedidorCadastroDTO dto = criarCadastroDTO(UUID.randomUUID());

        when(medidorRepo.findByNumero(dto.numero())).thenReturn(Optional.of(criarMedidor()));

        assertThrows(MedidorValidationException.class,
                ()-> medidorService.cadastrarMedidor(dto));

        verify(medidorRepo).findByNumero(dto.numero());

        verifyNoInteractions(clienteRepo, medidorMapper);
    }

    @Test
    void deveLancarExcecaoQuandoClienteNaoExistir(){

        UUID clienteId = UUID.randomUUID();

        MedidorCadastroDTO dto = criarCadastroDTO(clienteId);

        when(medidorRepo.findByNumero(dto.numero())).thenReturn(Optional.empty());
        when(clienteRepo.findById(clienteId)).thenReturn(Optional.empty());

        assertThrows(ClienteNotFoundException.class,
                ()-> medidorService.cadastrarMedidor(dto));

        verify(medidorRepo).findByNumero(dto.numero());
        verify(clienteRepo).findById(clienteId);

        verifyNoInteractions(medidorMapper);
    }

    @Test
    void devePermitirCadastrarVariosMedidoresParaOMesmoCliente() {

        Cliente cliente = criarCliente();

        MedidorCadastroDTO primeiroDTO = criarCadastroDTO(cliente.getId());

        Medidor primeiroMedidor = criarMedidor();
        primeiroMedidor.setCliente(cliente);

        MedidorResponseDTO primeiroResponse = criarResponseDTO(primeiroMedidor);

        when(medidorRepo.findByNumero(primeiroDTO.numero())).thenReturn(Optional.empty());
        when(clienteRepo.findById(cliente.getId())).thenReturn(Optional.of(cliente));
        when(medidorMapper.paraEntidade(primeiroDTO)).thenReturn(primeiroMedidor);
        when(medidorRepo.save(primeiroMedidor)).thenReturn(primeiroMedidor);
        when(medidorMapper.paraResponseDTO(primeiroMedidor)).thenReturn(primeiroResponse);

        MedidorResponseDTO primeiroResultado = medidorService.cadastrarMedidor(primeiroDTO);

        assertEquals(primeiroResponse, primeiroResultado);

        MedidorCadastroDTO segundoDTO = new MedidorCadastroDTO(
                "MED002",
                "Garagem",
                "BIDIRECIONAL",
                "TRIFASICO",
                "ENERGIA",
                30,
                cliente.getId());

        Medidor segundoMedidor = criarMedidor();
        segundoMedidor.setId(UUID.randomUUID());
        segundoMedidor.setNumero("MED002");
        segundoMedidor.setLocalizacao("Garagem");
        segundoMedidor.setCliente(cliente);

        MedidorResponseDTO segundoResponse = criarResponseDTO(segundoMedidor);

        when(medidorRepo.findByNumero(segundoDTO.numero())).thenReturn(Optional.empty());
        when(medidorMapper.paraEntidade(segundoDTO)).thenReturn(segundoMedidor);
        when(medidorRepo.save(segundoMedidor)).thenReturn(segundoMedidor);
        when(medidorMapper.paraResponseDTO(segundoMedidor)).thenReturn(segundoResponse);
        MedidorResponseDTO segundoResultado = medidorService.cadastrarMedidor(segundoDTO);

        assertEquals(segundoResponse, segundoResultado);
        assertEquals(cliente.getId(), segundoResultado.clienteId());

        verify(medidorRepo, times(2)).save(any(Medidor.class));
        verify(clienteRepo, times(2)).findById(cliente.getId());
    }

    //listar medidores
    @Test
    void deveListarMedidores(){

        Medidor medidor = criarMedidor();

        MedidorResponseDTO responseDTO = criarResponseDTO(medidor);

        when(medidorRepo.findAll()).thenReturn(List.of(medidor));

        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(responseDTO);

        List<MedidorResponseDTO> resultado = medidorService.listarMedidores();

        assertEquals(1, resultado.size());
        assertEquals(responseDTO, resultado.get(0));

        verify(medidorRepo).findAll();
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremMedidores() {

        when(medidorRepo.findAll())
                .thenReturn(List.of());

        List<MedidorResponseDTO> resultado =
                medidorService.listarMedidores();

        assertTrue(resultado.isEmpty());

        verify(medidorRepo).findAll();

        verifyNoInteractions(medidorMapper);
    }


    @Test
    void deveBuscarMedidorPorNumero(){

        String numero = "MED001";

        Medidor medidor = criarMedidor();

        MedidorResponseDTO responseDTO = criarResponseDTO(medidor);

        when(medidorRepo.findByNumero(numero)).thenReturn(Optional.of(medidor));
        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(responseDTO);

        MedidorResponseDTO resultado = medidorService.buscarPorNumero(numero);

        assertEquals(responseDTO, resultado);

        verify(medidorRepo).findByNumero(numero);
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveLancarExcecaoQuandoMedidorNaoForEncontrado(){

        String numero = "MED001";

        when(medidorRepo.findByNumero(numero)).thenReturn(Optional.empty());

        assertThrows(MedidorNotFoundException.class,
                ()-> medidorService.buscarPorNumero(numero));

        verify(medidorRepo).findByNumero(numero);
        verifyNoInteractions(medidorMapper);
    }

    @Test
    void deveBuscarMedidorPorId() {

        UUID id = UUID.randomUUID();

        Medidor medidor = criarMedidor();

        MedidorResponseDTO responseDTO = criarResponseDTO(medidor);

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));
        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(responseDTO);

        MedidorResponseDTO resultado = medidorService.buscarPorId(id);

        assertEquals(responseDTO, resultado);

        verify(medidorRepo).findById(id);
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveLancarExcecaoQuandoMedidorNaoForEncontradoPorId(){

        UUID id = UUID.randomUUID();

        when(medidorRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(MedidorNotFoundException.class,
                () -> medidorService.buscarPorId(id));

        verify(medidorRepo).findById(id);

        verifyNoInteractions(medidorMapper);
    }

    @Test
    void deveAtualizarMedidor(){

        UUID id = UUID.randomUUID();

        Medidor medidor = criarMedidor();

        MedidorAtualizacaoDTO dto = criarAtualizacaoDTO();

        MedidorResponseDTO responseDTO = new MedidorResponseDTO(
                id,
                dto.numero(),
                dto.localizacao(),
                dto.tipoMedidor(),
                dto.codigoFase(),
                dto.classeConsumo(),
                dto.dias(),
                null);

        medidor.setId(id);

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));
        when(medidorRepo.findByNumero(dto.numero())).thenReturn(Optional.empty());

        doAnswer(invocation -> {

            MedidorAtualizacaoDTO dados = invocation.getArgument(0);
            Medidor destino = invocation.getArgument(1);

            preencherMedidor(destino, dados);

            return null;
        }).when(medidorMapper).atualizarEntidade(dto, medidor);

        when(medidorRepo.save(medidor)).thenReturn(medidor);

        when(medidorMapper.paraResponseDTO(medidor)).thenReturn(responseDTO);

        MedidorResponseDTO resultado =
                medidorService.atualizarMedidor(id, dto);

        assertEquals(responseDTO, resultado);
        assertEquals("MED002", medidor.getNumero());
        assertEquals("Sala técnica", medidor.getLocalizacao());
        assertEquals("BIDIRECIONAL", medidor.getTipo());
        assertEquals("TRIFASICO", medidor.getCodigoFase());
        assertEquals("ENERGIA", medidor.getTipoFatura());
        assertEquals(30, medidor.getDias());

        verify(medidorRepo).findById(id);
        verify(medidorRepo).findByNumero(dto.numero());
        verify(medidorMapper).atualizarEntidade(dto, medidor);
        verify(medidorRepo).save(medidor);
        verify(medidorMapper).paraResponseDTO(medidor);
    }

    @Test
    void deveLancarExcecaoQuandoAtualizacaoForNula() {

        assertThrows(MedidorValidationException.class,
                () -> medidorService.atualizarMedidor(UUID.randomUUID(), null));

        verifyNoInteractions(medidorRepo, medidorMapper);
    }

    @Test
    void deveLancarExcecaoAoAtualizarMedidorInexistente() {

        UUID id = UUID.randomUUID();

        MedidorAtualizacaoDTO dto = criarAtualizacaoDTO();

        when(medidorRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(MedidorNotFoundException.class,
                () -> medidorService.atualizarMedidor(id, dto));

        verify(medidorRepo).findById(id);

        verify(medidorRepo, never()).save(any(Medidor.class));
        verifyNoInteractions(medidorMapper);

    }

    @Test
    void deveLancarExcecaoQuandoNumeroPertencerAOutroMedidor() {

        UUID id = UUID.randomUUID();

        Medidor medidorAtual = criarMedidor();
        medidorAtual.setId(id);

        Medidor outroMedidor = criarMedidor();
        outroMedidor.setId(UUID.randomUUID());

        MedidorAtualizacaoDTO dto = criarAtualizacaoDTO();

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidorAtual));

        when(medidorRepo.findByNumero(dto.numero())).thenReturn(Optional.of(outroMedidor));

        assertThrows(MedidorValidationException.class,
                () -> medidorService.atualizarMedidor(id, dto));

        verify(medidorRepo).findById(id);
        verify(medidorRepo).findByNumero(dto.numero());

        verify(medidorRepo, never()).save(any(Medidor.class));
        verifyNoInteractions(medidorMapper);
    }

    // =========================
    // EXCLUSÃO
    // =========================

    @Test
    void deveExcluirMedidor() {

        UUID id = UUID.randomUUID();

        Medidor medidor = criarMedidor();

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));

        doNothing().when(medidorRepo).deleteById(id);

        assertDoesNotThrow(() -> medidorService.excluirMedidor(id));

        verify(medidorRepo).findById(id);
        verify(medidorRepo).deleteById(id);
    }

    @Test
    void deveLancarExcecaoAoExcluirMedidorInexistente() {

        UUID id = UUID.randomUUID();

        when(medidorRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(MedidorNotFoundException.class,
                () -> medidorService.excluirMedidor(id));

        verify(medidorRepo).findById(id);

        verify(medidorRepo, never()).deleteById(any(UUID.class));
    }
}
