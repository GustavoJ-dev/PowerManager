
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaAtualizacaoDTO;
import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaCadastroDTO;
import com.powermanager.powermanager.dto.bandeira_tarifaria.BandeiraTarifariaResponseDTO;
import com.powermanager.powermanager.entity.BandeiraTarifaria;
import com.powermanager.powermanager.enums.TipoBandeira;
import com.powermanager.powermanager.exception.bandeira_tarifaria.BandeiraTarifariaNotFoundException;
import com.powermanager.powermanager.exception.bandeira_tarifaria.BandeiraTarifariaValidationException;
import com.powermanager.powermanager.mapper.BandeiraTarifariaMapper;
import com.powermanager.powermanager.repository.BandeiraTarifariaRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BandeiraTarifariaService {

    private final BandeiraTarifariaRepo bandeiraTarifariaRepo;
    private final BandeiraTarifariaMapper bandeiraTarifariaMapper;

    @Transactional
    public BandeiraTarifariaResponseDTO cadastrarBandeiraTarifaria(BandeiraTarifariaCadastroDTO dto) {

        validarCadastro(dto);

        BandeiraTarifaria novaBandeira =
                bandeiraTarifariaMapper.paraEntidade(dto);

        validarSobreposicao(dto.tipoBandeira(), dto.inicioVigencia(), dto.fimVigencia());

        BandeiraTarifaria salva =
                bandeiraTarifariaRepo.save(novaBandeira);

        return bandeiraTarifariaMapper.paraResponseDTO(salva);
    }

    public List<BandeiraTarifariaResponseDTO> listarBandeirasTarifarias() {

        return bandeiraTarifariaRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        BandeiraTarifaria::getInicioVigencia
                ))
                .map(bandeiraTarifariaMapper::paraResponseDTO)
                .toList();
    }

    public BandeiraTarifariaResponseDTO buscarPorId(UUID id) {

        validarId(id);

        BandeiraTarifaria bandeira = buscarEntidadePorId(id);

        return bandeiraTarifariaMapper.paraResponseDTO(bandeira);
    }

    public BandeiraTarifariaResponseDTO buscarBandeiraVigenteEm(TipoBandeira tipoBandeira, LocalDate data) {

        validarParametrosBusca(tipoBandeira, data);

        BandeiraTarifaria bandeira = bandeiraTarifariaRepo
                .buscarBandeiraVigenteEm(tipoBandeira, data)
                .orElseThrow(() -> new BandeiraTarifariaNotFoundException(
                        "Não existe valor tarifário vigente para a bandeira "
                                + tipoBandeira + " na data " + data + "."
                ));

        return bandeiraTarifariaMapper.paraResponseDTO(bandeira);
    }

    @Transactional
    public BandeiraTarifariaResponseDTO atualizarFimVigencia(UUID id, BandeiraTarifariaAtualizacaoDTO dto) {

        validarId(id);
        validarAtualizacao(dto);

        BandeiraTarifaria bandeira = buscarEntidadePorId(id);

        if (bandeira.getFimVigencia() != null) {

            throw new BandeiraTarifariaValidationException("A vigência desta bandeira já foi encerrada.");
        }

        validarPeriodo(bandeira.getInicioVigencia(), dto.fimVigencia());

        validarSobreposicaoNaAtualizacao(bandeira, dto.fimVigencia());

        bandeiraTarifariaMapper.atualizarEntidade(dto, bandeira);

        BandeiraTarifaria atualizada =
                bandeiraTarifariaRepo.save(bandeira);

        return bandeiraTarifariaMapper.paraResponseDTO(atualizada);
    }

    private BandeiraTarifaria buscarEntidadePorId(UUID id) {
        return bandeiraTarifariaRepo.findById(id)
                .orElseThrow(() -> new BandeiraTarifariaNotFoundException(
                        "Bandeira tarifária não encontrada: " + id
                ));
    }

    private void validarCadastro(BandeiraTarifariaCadastroDTO dto) {

        if (dto == null) {

            throw new BandeiraTarifariaValidationException(
                    "Os dados de cadastro da bandeira tarifária são obrigatórios.");
        }

        if (dto.tipoBandeira() == null) {

            throw new BandeiraTarifariaValidationException("O tipo da bandeira é obrigatório.");
        }

        if (dto.valorAdicional() == null
                || dto.valorAdicional().compareTo(BigDecimal.ZERO) < 0) {

            throw new BandeiraTarifariaValidationException("O valor adicional deve ser maior ou igual a zero.");
        }

        if (dto.inicioVigencia() == null) {

            throw new BandeiraTarifariaValidationException("A data de início da vigência é obrigatória.");
        }

        validarPeriodo(dto.inicioVigencia(), dto.fimVigencia());
    }

    private void validarAtualizacao(BandeiraTarifariaAtualizacaoDTO dto) {

        if (dto == null || dto.fimVigencia() == null) {

            throw new BandeiraTarifariaValidationException("A data de fim da vigência é obrigatória.");
        }
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {

        if (inicio == null) {

            throw new BandeiraTarifariaValidationException("A data de início da vigência é obrigatória.");
        }

        if (fim != null && !fim.isAfter(inicio)) {

            throw new BandeiraTarifariaValidationException(
                    "A data de fim da vigência deve ser posterior à data de início.");
        }
    }

    private void validarSobreposicao(TipoBandeira tipoBandeira, LocalDate inicio, LocalDate fim) {

        List<BandeiraTarifaria> sobreposicoes =
                bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                        tipoBandeira,
                        inicio,
                        fim
                );

        if (!sobreposicoes.isEmpty()) {

            throw new BandeiraTarifariaValidationException(
                    "Já existe uma vigência cadastrada para este tipo de bandeira "
                            + "que conflita com o período informado.");
        }
    }

    private void validarSobreposicaoNaAtualizacao(BandeiraTarifaria bandeiraAtual, LocalDate novoFim) {

        List<BandeiraTarifaria> sobreposicoes =
                bandeiraTarifariaRepo.buscarBandeirasComSobreposicao(
                        bandeiraAtual.getTipoBandeira(),
                        bandeiraAtual.getInicioVigencia(),
                        novoFim
                );

        boolean existeOutraSobreposicao = sobreposicoes.stream()
                .anyMatch(bandeira -> !Objects.equals(
                        bandeira.getId(),
                        bandeiraAtual.getId()
                ));

        if (existeOutraSobreposicao) {

            throw new BandeiraTarifariaValidationException("O encerramento informado causaria sobreposição "
                            + "com outra vigência do mesmo tipo de bandeira.");
        }
    }

    private void validarId(UUID id) {

        if (id == null) {

            throw new BandeiraTarifariaValidationException("O identificador da bandeira tarifária é obrigatório.");
        }
    }

    private void validarParametrosBusca(TipoBandeira tipoBandeira, LocalDate data) {

        if (tipoBandeira == null) {

            throw new BandeiraTarifariaValidationException("O tipo da bandeira é obrigatório para a consulta.");
        }

        if (data == null) {

            throw new BandeiraTarifariaValidationException("A data de consulta é obrigatória.");
        }
    }
}

