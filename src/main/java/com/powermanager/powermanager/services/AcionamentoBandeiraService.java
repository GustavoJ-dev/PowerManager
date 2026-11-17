
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraAtualizacaoDTO;
import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraCadastroDTO;
import com.powermanager.powermanager.dto.acionamento_bandeira.AcionamentoBandeiraResponseDTO;
import com.powermanager.powermanager.entity.AcionamentoBandeira;
import com.powermanager.powermanager.exception.acionamento_bandeira.AcionamentoBandeiraNotFoundException;
import com.powermanager.powermanager.exception.acionamento_bandeira.AcionamentoBandeiraValidationException;
import com.powermanager.powermanager.mapper.AcionamentoBandeiraMapper;
import com.powermanager.powermanager.repository.AcionamentoBandeiraRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AcionamentoBandeiraService {

    private final AcionamentoBandeiraRepo acionamentoBandeiraRepo;
    private final AcionamentoBandeiraMapper acionamentoBandeiraMapper;



    @Transactional
    public AcionamentoBandeiraResponseDTO cadastrarAcionamento(AcionamentoBandeiraCadastroDTO dto) {

        validarCadastro(dto);

        List<AcionamentoBandeira> sobreposicoes =
                acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                        dto.inicioVigencia(),
                        dto.fimVigencia()
                );

        if (!sobreposicoes.isEmpty()) {

            throw new AcionamentoBandeiraValidationException(
                    "Já existe um acionamento de bandeira com vigência sobreposta.");
        }

        AcionamentoBandeira acionamento =
                acionamentoBandeiraMapper.paraEntidade(dto);

        AcionamentoBandeira salvo =
                acionamentoBandeiraRepo.save(acionamento);

        return acionamentoBandeiraMapper.paraResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<AcionamentoBandeiraResponseDTO> listarAcionamentos() {

        return acionamentoBandeiraRepo.findAll()
                .stream()
                .sorted((a, b) ->
                        a.getInicioVigencia().compareTo(b.getInicioVigencia()))
                .map(acionamentoBandeiraMapper::paraResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AcionamentoBandeiraResponseDTO buscarPorId(UUID id) {

        validarId(id);

        AcionamentoBandeira acionamento = buscarEntidadePorId(id);

        return acionamentoBandeiraMapper.paraResponseDTO(acionamento);
    }

    @Transactional(readOnly = true)
    public AcionamentoBandeiraResponseDTO buscarAcionamentoVigenteEm(LocalDate data) {

        if (data == null) {

            throw new AcionamentoBandeiraValidationException("A data de consulta é obrigatória.");
        }

        AcionamentoBandeira acionamento =
                acionamentoBandeiraRepo.buscarAcionamentoVigenteEm(data)
                        .orElseThrow(() ->
                                new AcionamentoBandeiraNotFoundException(
                                        "Nenhum acionamento de bandeira encontrado para a data informada."
                                )
                        );

        return acionamentoBandeiraMapper.paraResponseDTO(acionamento);
    }

    @Transactional
    public AcionamentoBandeiraResponseDTO atualizarFimVigencia(UUID id, AcionamentoBandeiraAtualizacaoDTO dto) {

        validarId(id);
        validarAtualizacao(dto);

        AcionamentoBandeira acionamento = buscarEntidadePorId(id);

        if (acionamento.getFimVigencia() != null) {

            throw new AcionamentoBandeiraValidationException("A vigência deste acionamento já foi encerrada.");
        }

        validarPeriodo(acionamento.getInicioVigencia(), dto.fimVigencia());

        List<AcionamentoBandeira> sobreposicoes =
                acionamentoBandeiraRepo.buscarAcionamentosComSobreposicao(
                        acionamento.getInicioVigencia(),
                        dto.fimVigencia()
                );

        boolean existeOutraSobreposicao = sobreposicoes.stream()
                .anyMatch(outro -> !outro.getId().equals(acionamento.getId()));

        if (existeOutraSobreposicao) {

            throw new AcionamentoBandeiraValidationException(
                    "O encerramento informado causa sobreposição com outro acionamento.");
        }

        acionamentoBandeiraMapper.atualizarEntidade(dto, acionamento);

        AcionamentoBandeira atualizado =
                acionamentoBandeiraRepo.save(acionamento);

        return acionamentoBandeiraMapper.paraResponseDTO(atualizado);
    }

    private AcionamentoBandeira buscarEntidadePorId(UUID id) {

        return acionamentoBandeiraRepo.findById(id)
                .orElseThrow(() ->
                        new AcionamentoBandeiraNotFoundException(
                                "Acionamento de bandeira não encontrado."
                        )
                );
    }

    private void validarCadastro(AcionamentoBandeiraCadastroDTO dto) {

        if (dto == null) {

            throw new AcionamentoBandeiraValidationException("Os dados do acionamento são obrigatórios.");
        }

        if (dto.tipoBandeira() == null) {

            throw new AcionamentoBandeiraValidationException("O tipo de bandeira é obrigatório.");
        }

        if (dto.inicioVigencia() == null) {

            throw new AcionamentoBandeiraValidationException("O início da vigência é obrigatório.");
        }

        if (dto.dataDivulgacao() == null) {

            throw new AcionamentoBandeiraValidationException("A data de divulgação é obrigatória.");
        }

        if (dto.referenciaOficial() == null || dto.referenciaOficial().isBlank()) {

            throw new AcionamentoBandeiraValidationException("A referência oficial é obrigatória.");
        }

        if (dto.referenciaOficial().length() > 500) {

            throw new AcionamentoBandeiraValidationException(
                    "A referência oficial deve possuir no máximo 500 caracteres.");
        }

        validarPeriodo(dto.inicioVigencia(), dto.fimVigencia());
    }

    private void validarAtualizacao(AcionamentoBandeiraAtualizacaoDTO dto) {

        if (dto == null || dto.fimVigencia() == null) {

            throw new AcionamentoBandeiraValidationException("A data final da vigência é obrigatória.");
        }
    }

    private void validarPeriodo(LocalDate inicio, LocalDate fim) {

        if (inicio == null) {

            throw new AcionamentoBandeiraValidationException("O início da vigência é obrigatório.");
        }

        if (fim != null && !fim.isAfter(inicio)) {

            throw new AcionamentoBandeiraValidationException("O fim da vigência deve ser posterior ao início.");
        }
    }

    private void validarId(UUID id) {

        if (id == null) {

            throw new AcionamentoBandeiraValidationException("O identificador do acionamento é obrigatório.");
        }
    }
}
