package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraAtualizacaoDTO;
import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraCadastroDTO;
import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraResponseDTO;
import com.powermanager.powermanager.entity.AcionamentoBandeira;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface AcionamentoBandeiraMapper {

    @Mapping(target = "id", ignore = true)
    AcionamentoBandeira paraEntidade(AcionamentoBandeiraCadastroDTO dto);

    AcionamentoBandeiraResponseDTO paraResponseDTO(AcionamentoBandeira acionamentoBandeira);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoBandeira", ignore = true)
    @Mapping(target = "inicioVigencia", ignore = true)
    @Mapping(target = "dataDivulgacao", ignore = true)
    @Mapping(target = "referenciaOficial", ignore = true)
    void atualizarEntidade(
            AcionamentoBandeiraAtualizacaoDTO dto,
            @MappingTarget AcionamentoBandeira acionamentoBandeira);
}
