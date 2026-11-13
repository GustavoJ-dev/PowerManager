package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.cliente.ClienteAtualizacaoDTO;
import com.powermanager.powermanager.dto.cliente.ClienteCadastroDTO;
import com.powermanager.powermanager.dto.cliente.ClienteResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "unidadeConsumidoras", ignore = true)
    Cliente paraEntidade(ClienteCadastroDTO dto);

    ClienteResponseDTO paraResponseDTO(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "unidadeConsumidoras", ignore = true)
    void atualizarEntidade(ClienteAtualizacaoDTO dto,
                           @MappingTarget Cliente cliente);
}
