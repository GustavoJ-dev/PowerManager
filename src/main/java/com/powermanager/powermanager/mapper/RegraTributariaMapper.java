package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaAtualizacaoDTO;
import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaCadastroDTO;
import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaResponseDTO;
import com.powermanager.powermanager.entity.RegraTributaria;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RegraTributariaMapper {

    @Mapping(target = "id", ignore = true)
    RegraTributaria paraEntidade(RegraTributariaCadastroDTO dto);

    RegraTributariaResponseDTO paraResponseDTO(RegraTributaria regraTributaria);

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "fimVigencia", source = "fimVigencia")
    void atualizarEntidade(
            RegraTributariaAtualizacaoDTO dto,
            @MappingTarget RegraTributaria regraTributaria);
}
