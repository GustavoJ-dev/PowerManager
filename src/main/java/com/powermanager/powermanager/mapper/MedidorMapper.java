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
    @Mapping(target = "unidadeConsumidora", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataInstalacao", ignore = true)
    @Mapping(target = "dataRetirada", ignore = true)
    Medidor paraEntidade(MedidorCadastroDTO dto);

    @Mapping(target = "unidadeConsumidoraId", source = "unidadeConsumidora.id")
    MedidorResponseDTO paraResponseDTO(Medidor medidor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "numero", ignore = true)
    @Mapping(target = "unidadeConsumidora", ignore = true)
    @Mapping(target = "ativo", ignore = true)
    @Mapping(target = "dataInstalacao", ignore = true)
    @Mapping(target = "dataRetirada", ignore = true)
    void atualizarEntidade(
            MedidorAtualizacaoDTO dto,
            @MappingTarget Medidor medidor);
}
