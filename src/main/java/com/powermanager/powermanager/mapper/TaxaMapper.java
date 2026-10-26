package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.taxa.TaxaAtualizacaoDTO;
import com.powermanager.powermanager.dto.taxa.TaxaCadastroDTO;
import com.powermanager.powermanager.dto.taxa.TaxaResponseDTO;
import com.powermanager.powermanager.entity.Taxa;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TaxaMapper {

    Taxa paraEntidade(TaxaCadastroDTO dto);

    TaxaResponseDTO paraResponseDTO(Taxa taxa);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fimVigencia", ignore = true)
    void atualizarEntidade(TaxaAtualizacaoDTO dto, @MappingTarget Taxa taxa);
}
