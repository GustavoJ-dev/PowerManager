
package com.powermanager.powermanager.services;

import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaAtualizacaoDTO;
import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaCadastroDTO;
import com.powermanager.powermanager.dto.regra_tributaria.RegraTributariaResponseDTO;
import com.powermanager.powermanager.entity.RegraTributaria;
import com.powermanager.powermanager.enums.AbrangenciaTributaria;
import com.powermanager.powermanager.enums.TipoTributo;
import com.powermanager.powermanager.exception.regra_tributaria.RegraTributariaNotFoundException;
import com.powermanager.powermanager.exception.regra_tributaria.RegraTributariaValidationException;
import com.powermanager.powermanager.mapper.RegraTributariaMapper;
import com.powermanager.powermanager.repository.RegraTributariaRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegraTributariaService {

    private final RegraTributariaRepo regraTributariaRepo;
    private final RegraTributariaMapper regraTributariaMapper;


    /**
     * Cadastra uma nova regra tributária.
     */
    @Transactional
    public RegraTributariaResponseDTO cadastrarRegra(RegraTributariaCadastroDTO dto) {

        if (dto == null) {

            throw new RegraTributariaValidationException("Os dados da regra tributária não podem ser nulos");
        }

        RegraTributaria regra = regraTributariaMapper.paraEntidade(dto);

        if (regra == null) {

            throw new RegraTributariaValidationException("Não foi possível converter os dados da regra tributária");
        }

        normalizarLocalizacao(regra);
        validarRegra(regra);
        validarPeriodo(regra.getInicioVigencia(), regra.getFimVigencia());
        validarSobreposicao(regra, null);

        RegraTributaria regraSalva = regraTributariaRepo.save(regra);

        return regraTributariaMapper.paraResponseDTO(regraSalva);
    }

    /**
     * Lista todas as regras tributárias em ordem cronológica.
     */
    @Transactional
    public List<RegraTributariaResponseDTO> listarRegras() {

        return regraTributariaRepo.findAll()
                .stream()
                .sorted(
                        Comparator.comparing(
                                RegraTributaria::getInicioVigencia,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        ).thenComparing(
                                RegraTributaria::getId,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                )
                .map(regraTributariaMapper::paraResponseDTO)
                .toList();
    }

    /**
     * Busca uma regra tributária pelo identificador.
     */
    @Transactional
    public RegraTributariaResponseDTO buscarPorId(UUID id) {

        if (id == null) {

            throw new RegraTributariaValidationException("O identificador da regra tributária não pode ser nulo");
        }

        RegraTributaria regra = buscarEntidadePorId(id);

        return regraTributariaMapper.paraResponseDTO(regra);
    }

    /**
     * Busca a regra tributária vigente para determinada combinação
     * de tributo, abrangência, localização e data.
     */

    @Transactional
    public RegraTributariaResponseDTO buscarRegraVigenteEm(TipoTributo tipoTributo, AbrangenciaTributaria abrangencia,
                                                           String estado, String municipio, String codigoIbge,
                                                           LocalDate data) {

        if (tipoTributo == null || abrangencia == null || data == null) {

            throw new RegraTributariaValidationException("Tributo, abrangência e data são obrigatórios para a consulta");
        }

        // Valida a compatibilidade antes de consultar o banco.
        validarCompatibilidadeTributaria(tipoTributo, abrangencia);

        String estadoNormalizado = normalizarTexto(estado);
        String municipioNormalizado = normalizarTexto(municipio);
        String codigoNormalizado = normalizarTexto(codigoIbge);

        if (estadoNormalizado != null) {

            estadoNormalizado = estadoNormalizado.toUpperCase(Locale.ROOT);
        }

        validarLocalizacao(abrangencia, estadoNormalizado, municipioNormalizado, codigoNormalizado);

        RegraTributaria regra = regraTributariaRepo.buscarRegraVigenteEm(
                tipoTributo,
                abrangencia,
                estadoNormalizado,
                municipioNormalizado,
                codigoNormalizado,
                data
        ).orElseThrow(
                () -> new RegraTributariaNotFoundException(
                        "Nenhuma regra tributária vigente encontrada para a data informada")
        );

        return regraTributariaMapper.paraResponseDTO(regra);
    }


    /**
     * Encerra a vigência de uma regra tributária.
     * Os parâmetros tributários e territoriais permanecem imutáveis.
     */
    @Transactional
    public RegraTributariaResponseDTO atualizarFimVigencia(UUID id, RegraTributariaAtualizacaoDTO dto) {

        if (id == null || dto == null) {

            throw new RegraTributariaValidationException("Identificador e dados de atualização são obrigatórios");
        }

        RegraTributaria regra = buscarEntidadePorId(id);

        if (regra.getFimVigencia() != null) {

            throw new RegraTributariaValidationException("Não é permitido alterar uma regra tributária já encerrada");
        }

        validarPeriodo(regra.getInicioVigencia(), dto.fimVigencia());

        validarSobreposicao(regra, id, dto.fimVigencia());

        regraTributariaMapper.atualizarEntidade(dto, regra);

        RegraTributaria regraAtualizada = regraTributariaRepo.save(regra);

        return regraTributariaMapper.paraResponseDTO(regraAtualizada);
    }

    /**
     * Busca a entidade ou lança exceção.
     */
    private RegraTributaria buscarEntidadePorId(UUID id) {

        return regraTributariaRepo.findById(id)
                .orElseThrow(
                        () -> new RegraTributariaNotFoundException(
                                "Regra tributária não encontrada: " + id)
                );
    }

    /**
     * Normaliza os dados territoriais antes da validação e persistência.
     */
    private void normalizarLocalizacao(RegraTributaria regra) {

        regra.setEstado(normalizarTexto(regra.getEstado()));
        regra.setMunicipio(normalizarTexto(regra.getMunicipio()));
        regra.setCodigoIbge(normalizarTexto(regra.getCodigoIbge()));

        if (regra.getEstado() != null) {

            regra.setEstado(regra.getEstado().toUpperCase(Locale.ROOT));
        }
    }

    private String normalizarTexto(String valor) {

        if (valor == null || valor.isBlank()) {

            return null;
        }

        return valor.trim();
    }

    /**
     * Valida a coerência entre tributo e abrangência.
     */
    private void validarCompatibilidadeTributaria(RegraTributaria regra) {

        boolean compativel = switch (regra.getTipoTributo()) {

            case ICMS ->
                    regra.getAbrangencia() == AbrangenciaTributaria.ESTADUAL;

            case PIS, COFINS ->
                    regra.getAbrangencia() == AbrangenciaTributaria.FEDERAL;

            case CIP_COSIP ->
                    regra.getAbrangencia() == AbrangenciaTributaria.MUNICIPAL;
        };

        if (!compativel) {

            throw new RegraTributariaValidationException("A abrangência informada não é compatível com o tributo");
        }
    }

    /**
     * Valida os dados territoriais conforme a abrangência.
     */
    private void validarLocalizacao(AbrangenciaTributaria abrangencia, String estado, String municipio,
                                    String codigoIbge) {

        switch (abrangencia) {

            case FEDERAL -> {

                if (estado != null || municipio != null || codigoIbge != null) {

                    throw new RegraTributariaValidationException(
                            "Regras federais não devem possuir localização territorial");
                }
            }

            case ESTADUAL -> {

                if (estado == null) {

                    throw new RegraTributariaValidationException("Regras estaduais exigem a informação da UF");
                }

                if (municipio != null || codigoIbge != null) {

                    throw new RegraTributariaValidationException(
                            "Regras estaduais não devem possuir município ou código IBGE");
                }

                if (!estado.matches("[A-Z]{2}")) {

                    throw new RegraTributariaValidationException("A UF deve conter exatamente duas letras");
                }
            }

            case MUNICIPAL -> {

                if (estado == null || municipio == null || codigoIbge == null) {

                    throw new RegraTributariaValidationException(
                            "Regras municipais exigem UF, município e código IBGE");
                }

                if (!estado.matches("[A-Z]{2}")) {

                    throw new RegraTributariaValidationException("A UF deve conter exatamente duas letras");
                }

                if (!codigoIbge.matches("\\d{7}")) {

                    throw new RegraTributariaValidationException("O código IBGE deve conter exatamente sete dígitos");
                }
            }
        }
    }

    private void validarLocalizacao(RegraTributaria regra) {

        validarLocalizacao(regra.getAbrangencia(), regra.getEstado(), regra.getMunicipio(), regra.getCodigoIbge());
    }

    /**
     * Valida os parâmetros conforme o tipo de cálculo.
     */
    private void validarTipoCalculo(RegraTributaria regra) {

        if (regra.getTipoCalculo() == null) {

            throw new RegraTributariaValidationException("O tipo de cálculo é obrigatório");
        }

        switch (regra.getTipoCalculo()) {

            case PERCENTUAL -> {

                validarValorNaoNegativo(regra.getAliquota(), "Alíquota");

                if (regra.getValorFixo() != null) {

                    throw new RegraTributariaValidationException("Regras percentuais não devem possuir valor fixo");
                }
            }

            case VALOR_FIXO -> {

                validarValorNaoNegativo(regra.getValorFixo(), "Valor fixo");

                if (regra.getAliquota() != null) {

                    throw new RegraTributariaValidationException("Regras de valor fixo não devem possuir alíquota");
                }
            }
        }
    }

    /**
     * Valida todos os dados de negócio da regra.
     */
    private void validarRegra(RegraTributaria regra) {

        if (regra.getTipoTributo() == null || regra.getAbrangencia() == null) {

            throw new RegraTributariaValidationException("Tributo e abrangência são obrigatórios");
        }

        validarCompatibilidadeTributaria(regra);
        validarLocalizacao(regra);
        validarTipoCalculo(regra);
    }

    private void validarValorNaoNegativo(BigDecimal valor, String campo) {

        if (valor == null) {

            throw new RegraTributariaValidationException(campo + " é obrigatório");
        }

        if (valor.compareTo(BigDecimal.ZERO) < 0) {

            throw new RegraTributariaValidationException(campo + " não pode ser negativo");
        }
    }

    /**
     * Valida o intervalo de vigência [início, fim).
     */
    private void validarPeriodo(LocalDate inicio, LocalDate fim) {

        if (inicio == null) {

            throw new RegraTributariaValidationException("O início da vigência não pode ser nulo");
        }

        if (fim != null && !fim.isAfter(inicio)) {

            throw new RegraTributariaValidationException("O fim da vigência deve ser posterior ao início");
        }
    }

    private void validarCompatibilidadeTributaria(TipoTributo tipoTributo, AbrangenciaTributaria abrangencia) {

        boolean compativel = switch (tipoTributo) {

            case ICMS ->
                    abrangencia == AbrangenciaTributaria.ESTADUAL;

            case PIS, COFINS ->
                    abrangencia == AbrangenciaTributaria.FEDERAL;

            case CIP_COSIP ->
                    abrangencia == AbrangenciaTributaria.MUNICIPAL;
        };

        if (!compativel) {

            throw new RegraTributariaValidationException("A abrangência informada não é compatível com o tributo");
        }
    }


    /**
     * Impede sobreposição para a mesma combinação tributária e territorial.
     */
    private void validarSobreposicao(RegraTributaria regra, UUID idIgnorado) {


        validarSobreposicao(regra, idIgnorado, regra.getFimVigencia());
    }

    /**
     * Permite validar a sobreposição usando o fim solicitado
     * durante o encerramento da vigência.
     */
    private void validarSobreposicao(RegraTributaria regra,UUID idIgnorado, LocalDate fimVigencia) {

        List<RegraTributaria> regrasSobrepostas =
                regraTributariaRepo.buscarRegrasComSobreposicao(
                        regra.getTipoTributo(),
                        regra.getAbrangencia(),
                        regra.getEstado(),
                        regra.getMunicipio(),
                        regra.getCodigoIbge(),
                        regra.getInicioVigencia(),
                        fimVigencia
                );

        boolean existeConflito = regrasSobrepostas.stream()
                .anyMatch(regraExistente ->
                        !java.util.Objects.equals(
                                regraExistente.getId(),
                                idIgnorado
                        )
                );

        if (existeConflito) {

            throw new RegraTributariaValidationException(
                    "Já existe uma regra tributária com vigência sobreposta para essa combinação");
        }
    }
}
