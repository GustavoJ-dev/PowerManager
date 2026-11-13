package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaAtualizacaoDTO;
import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaCadastroDTO;
import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaResponseDTO;
import com.powermanager.powermanager.entity.BandeiraTarifaria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BandeiraTarifariaMapper {

    @Mapping(target = "id", ignore = true)
    BandeiraTarifaria paraEntidade(BandeiraTarifariaCadastroDTO dto);

    BandeiraTarifariaResponseDTO paraResponseDTO(BandeiraTarifaria bandeiraTarifaria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoBandeira", ignore = true)
    @Mapping(target = "valorAdicional", ignore = true)
    @Mapping(target = "inicioVigencia", ignore = true)
    void atualizarEntidade(
            BandeiraTarifariaAtualizacaoDTO dto,
            @MappingTarget BandeiraTarifaria bandeiraTarifaria);
}