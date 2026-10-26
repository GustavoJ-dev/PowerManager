package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.usuario.UsuarioAtualizacaoDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioCadastroDTO;
import com.powermanager.powermanager.dto.usuario.UsuarioResponseDTO;
import com.powermanager.powermanager.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    Usuario paraEntidade(UsuarioCadastroDTO dto);

    @Mapping(target = "clienteId", source = "cliente.id")
    UsuarioResponseDTO paraResponseDTO(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "tipoUsuario", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    void atualizarEntidade(UsuarioAtualizacaoDTO dto, @MappingTarget Usuario usuario);
}
