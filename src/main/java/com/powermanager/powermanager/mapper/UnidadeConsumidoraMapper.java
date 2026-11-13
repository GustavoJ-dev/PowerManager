package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraAtualizacaoDTO;
import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraCadastroDTO;
import com.powermanager.powermanager.dto.unidade_consumidora.UnidadeConsumidoraResponseDTO;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UnidadeConsumidoraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "ativa", ignore = true)
    UnidadeConsumidora paraEntidade(UnidadeConsumidoraCadastroDTO dto);

    @Mapping(target = "clienteId", source = "cliente.id")
    UnidadeConsumidoraResponseDTO paraResponseDTO(UnidadeConsumidora unidadeConsumidora);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "ativa", ignore = true)
    void atualizarEntidade(UnidadeConsumidoraAtualizacaoDTO dto,
                           @MappingTarget UnidadeConsumidora unidadeConsumidora);


}
