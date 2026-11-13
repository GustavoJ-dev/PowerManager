package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.fatura_tributo.FaturaTributoResponseDTO;
import com.powermanager.powermanager.entity.FaturaTributo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FaturaTributoMapper {

    @Mapping(target = "regraTributariaId", source = "regraTributaria.id")
    FaturaTributoResponseDTO paraResponseDTO(FaturaTributo faturaTributo);
}
