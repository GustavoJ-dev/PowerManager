package com.powermanager.powermanager.services;

import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.repository.MedidorRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MedidorService {

    private final MedidorRepo medidorRepo;

    public Medidor cadastrarMedidor(Medidor medidor){

        return medidorRepo.save(medidor);
    }

    public Medidor buscarPorNumero(String numero){

        return medidorRepo.findByNumero(numero).orElseThrow(() ->
                new RuntimeException("Medidor não encontrado"));
    }

    public Medidor atualizarMedidor(UUID id, Medidor dadosAtualizados) {
        Medidor medidor = medidorRepo.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Medidor não encontrado: " + id));

        medidor.setNumero(dadosAtualizados.getNumero());
        medidor.setLocalizacao(dadosAtualizados.getLocalizacao());
        medidor.setTipo(dadosAtualizados.getTipo());
        medidor.setCodigoFase(dadosAtualizados.getCodigoFase());
        medidor.setTipoFatura(dadosAtualizados.getTipoFatura());
        medidor.setDias(dadosAtualizados.getDias());

        return medidorRepo.save(medidor);
    }
}
