package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Taxa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaxaMapper {

    @Mapping(target = "id", ignore = true)
    Taxa paraEntidade(TaxaCadastroDTO dto);

    TaxaResponseDTO paraResponseDTO(Taxa taxa);
}
