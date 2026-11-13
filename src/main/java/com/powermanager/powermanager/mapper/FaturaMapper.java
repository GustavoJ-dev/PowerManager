package com.powermanager.powermanager.mapper;

import com.powermanager.powermanager.dto.fatura.FaturaResponseDTO;
import com.powermanager.powermanager.dto.fatura_tributo.FaturaTributoResponseDTO;
import com.powermanager.powermanager.entity.Fatura;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FaturaMapper {

    @Mapping(target = "id", source = "fatura.id")
    @Mapping(target = "unidadeConsumidoraId", source = "fatura.unidadeConsumidora.id")
    @Mapping(target = "competencia", source = "fatura.competencia")
    @Mapping(target = "inicioPeriodo", source = "fatura.inicioPeriodo")
    @Mapping(target = "fimPeriodo", source = "fatura.fimPeriodo")
    @Mapping(target = "consumoKwh", source = "fatura.consumoKwh")
    @Mapping(target = "origemConsumo", source = "fatura.origemConsumo")
    @Mapping(target = "tarifaEnergiaTE", source = "fatura.tarifaEnergiaTE")
    @Mapping(target = "tarifaUsoDistribuicaoTUSD", source = "fatura.tarifaUsoDistribuicaoTUSD")
    @Mapping(target = "valorTE", source = "fatura.valorTE")
    @Mapping(target = "valorTUSD", source = "fatura.valorTUSD")
    @Mapping(target = "valorBandeira", source = "fatura.valorBandeira")
    @Mapping(target = "valorSubtotal", source = "fatura.valorSubtotal")
    @Mapping(target = "valorTotal", source = "fatura.valorTotal")
    @Mapping(target = "dataEmissao", source = "fatura.dataEmissao")
    @Mapping(target = "dataVencimento", source = "fatura.dataVencimento")
    @Mapping(target = "dataPagamento", source = "fatura.dataPagamento")
    @Mapping(target = "tributos", source = "tributos")
    @Mapping(target = "status", source = "fatura.status")
    FaturaResponseDTO paraResponseDTO(Fatura fatura, List<FaturaTributoResponseDTO> tributos);
}