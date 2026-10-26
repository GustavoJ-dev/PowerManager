package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.fatura.FaturaResponseDTO;
import com.powermanager.powermanager.entity.Fatura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FaturaMapper {

    @Mapping(target = "medidorId", source = "medidor.id")
    FaturaResponseDTO paraResponseDTO(Fatura fatura);
}
