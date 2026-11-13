package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.leitura.LeituraCadastroDTO;
import com.powermanager.powermanager.dto.leitura.LeituraResponseDTO;
import com.powermanager.powermanager.entity.Leitura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeituraMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "medidor", ignore = true)
    Leitura paraEntidade(LeituraCadastroDTO dto);

    @Mapping(target = "medidorId", source = "medidor.id")
    LeituraResponseDTO paraResponseDTO(Leitura leitura);
}