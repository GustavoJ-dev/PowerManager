package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.cliente.ClienteCadastroDTO;
import com.powermanager.powermanager.dto.cliente.ClienteResponseDTO;
import com.powermanager.powermanager.entity.Cliente;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    Cliente paraEntidade(ClienteCadastroDTO dto);

    ClienteResponseDTO paraResponseDTO(Cliente cliente);
}
