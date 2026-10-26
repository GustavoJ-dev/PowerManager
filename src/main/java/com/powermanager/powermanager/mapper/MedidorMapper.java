package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.medidor.MedidorAtualizacaoDTO;
import com.powermanager.powermanager.dto.medidor.MedidorCadastroDTO;
import com.powermanager.powermanager.dto.medidor.MedidorResponseDTO;
import com.powermanager.powermanager.entity.Medidor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MedidorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    Medidor paraEntidade(MedidorCadastroDTO dto);

    @Mapping(target = "clienteId", source = "cliente.id")
    MedidorResponseDTO paraResponseDTO(Medidor medidor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    void atualizarEntidade(MedidorAtualizacaoDTO dto, @MappingTarget Medidor medidor);
}
