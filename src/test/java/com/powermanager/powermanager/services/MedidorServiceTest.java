package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.exception.MedidorNotFoundException;
import com.powermanager.powermanager.repository.MedidorRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MedidorServiceTest {

    @Mock
    private MedidorRepo medidorRepo;

    @InjectMocks
    private MedidorService medidorService;

    @Test
    void deveCadastrarMedidor(){

        Medidor medidor = new Medidor();

        when(medidorRepo.save(medidor)).thenReturn(medidor);

        Medidor resultado = medidorService.cadastrarMedidor(medidor);

        assertEquals(medidor, resultado);

        verify(medidorRepo).save(medidor);
    }

    @Test
    void deveBuscarMedidorPorNumero(){

        String numero = "MED001";

        Medidor medidor = new Medidor();

        when(medidorRepo.findByNumero(numero)).thenReturn(Optional.of(medidor));

        Medidor resultado = medidorService.buscarPorNumero(numero);

        assertEquals(medidor, resultado);

        verify(medidorRepo).findByNumero(numero);
    }

    @Test
    void deveLancarExcecaoQuandoMedidorNaoForEncontrado(){

        String numero = "MED001";

        when(medidorRepo.findByNumero(numero)).thenReturn(Optional.empty());

        assertThrows(MedidorNotFoundException.class,
                ()-> medidorService.buscarPorNumero(numero));

        verify(medidorRepo).findByNumero(numero);
    }

    @Test
    void deveBuscarMedidorPorId() {

        UUID id = UUID.randomUUID();

        Medidor medidor = new Medidor();

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidor));

        Medidor resultado = medidorService.buscarPorId(id);

        assertEquals(medidor, resultado);

        verify(medidorRepo).findById(id);
    }

    @Test
    void deveLancarExcecaoQuandoMedidorNaoForEncontradoPorId(){

        UUID id = UUID.randomUUID();

        when(medidorRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(MedidorNotFoundException.class,
                () -> medidorService.buscarPorId(id));

        verify(medidorRepo).findById(id);
    }

    @Test
    void deveAtualizarMedidor(){

        UUID id = UUID.randomUUID();

        Medidor medidorExistente = new Medidor();
        Medidor dadosAtualizados = new Medidor();

        dadosAtualizados.setNumero("MED002");
        dadosAtualizados.setLocalizacao("Sala técnica");
        dadosAtualizados.setTipo("BIDIRECIONAL");
        dadosAtualizados.setCodigoFase("TRIFASICO");
        dadosAtualizados.setTipoFatura("ENERGIA");
        dadosAtualizados.setDias(30);

        when(medidorRepo.findById(id)).thenReturn(Optional.of(medidorExistente));

        when(medidorRepo.save(medidorExistente)).thenReturn(medidorExistente);

        Medidor resultado = medidorService.atualizarMedidor(id, dadosAtualizados);

        assertEquals("MED002", resultado.getNumero());
        assertEquals("Sala técnica", resultado.getLocalizacao());
        assertEquals("BIDIRECIONAL", resultado.getTipo());
        assertEquals("TRIFASICO", resultado.getCodigoFase());
        assertEquals("ENERGIA", resultado.getTipoFatura());
        assertEquals(30, resultado.getDias());

        verify(medidorRepo).findById(id);
        verify(medidorRepo).save(medidorExistente);
    }

    @Test
    void deveLancarExcecaoAoAtualizarMedidorInexistente() {

        UUID id = UUID.randomUUID();
        Medidor dadosAtualizados = new Medidor();

        when(medidorRepo.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(MedidorNotFoundException.class,
                () -> medidorService.atualizarMedidor(id, dadosAtualizados));

        verify(medidorRepo).findById(id);
        verify(medidorRepo, never()).save(any(Medidor.class));


    }
}
