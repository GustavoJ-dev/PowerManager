
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.*;
import com.powermanager.powermanager.enums.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class FaturaTributoRepoTest {

    @Autowired
    private FaturaTributoRepo faturaTributoRepo;

    @Autowired
    private EntityManager entityManager;

    private Cliente criarCliente() {

        Cliente cliente = new Cliente();

        cliente.setNome("João da Silva");
        cliente.setEmail("joao@email.com");
        cliente.setTelefone("71999999999");

        entityManager.persist(cliente);

        return cliente;
    }

    private UnidadeConsumidora criarUnidadeConsumidora() {

        UnidadeConsumidora unidade = new UnidadeConsumidora();

        unidade.setCodigo("UC-001");
        unidade.setEndereco("Rua das Flores, 123");
        unidade.setCidade("Salvador");
        unidade.setEstado("BA");
        unidade.setCep("40000-000");
        unidade.setDistribuidora("Neoenergia Coelba");
        unidade.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        unidade.setSubgrupoTarifario(SubgrupoTarifario.B1);
        unidade.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        unidade.setAtiva(true);
        unidade.setCliente(criarCliente());

        entityManager.persist(unidade);

        return unidade;
    }

    private Fatura criarFatura() {

        Fatura fatura = new Fatura();

        fatura.setUnidadeConsumidora(criarUnidadeConsumidora());
        fatura.setCompetencia(LocalDate.of(2026, 1, 1));
        fatura.setInicioPeriodo(LocalDate.of(2025, 12, 1));
        fatura.setFimPeriodo(LocalDate.of(2025, 12, 31));

        fatura.setConsumoKwh(new BigDecimal("100.000"));
        fatura.setOrigemConsumo(OrigemConsumo.LEITURA);

        fatura.setTarifaEnergiaTE(new BigDecimal("0.300000"));
        fatura.setTarifaUsoDistribuicaoTUSD(new BigDecimal("0.400000"));

        fatura.setValorTE(new BigDecimal("30.00"));
        fatura.setValorTUSD(new BigDecimal("40.00"));
        fatura.setValorBandeira(new BigDecimal("0.00"));

        fatura.setValorSubtotal(new BigDecimal("70.00"));
        fatura.setValorTotal(new BigDecimal("70.00"));

        fatura.setDataEmissao(LocalDate.of(2026, 1, 1));
        fatura.setDataVencimento(LocalDate.of(2026, 1, 15));
        fatura.setStatus(StatusFatura.PENDENTE);

        entityManager.persist(fatura);

        return fatura;
    }

    private RegraTributaria criarRegraTributaria(TipoTributo tipoTributo) {

        RegraTributaria regra = new RegraTributaria();

        regra.setTipoTributo(tipoTributo);
        regra.setAbrangencia(AbrangenciaTributaria.FEDERAL);
        regra.setTipoCalculo(TipoCalculoTributario.PERCENTUAL);
        regra.setAliquota(new BigDecimal("18.000000"));
        regra.setInicioVigencia(LocalDate.of(2026, 1, 1));

        entityManager.persist(regra);

        return regra;
    }

    private FaturaTributo criarFaturaTributo(Fatura fatura, RegraTributaria regra, TipoTributo tipoTributo) {

        FaturaTributo faturaTributo = new FaturaTributo();

        faturaTributo.setFatura(fatura);
        faturaTributo.setRegraTributaria(regra);
        faturaTributo.setTipoTributo(tipoTributo);
        faturaTributo.setTipoCalculo(TipoCalculoTributario.PERCENTUAL);

        faturaTributo.setAliquotaAplicada(new BigDecimal("18.000000"));
        faturaTributo.setBaseCalculo(new BigDecimal("100.00"));
        faturaTributo.setValorTributo(new BigDecimal("18.00"));

        return faturaTributo;
    }

    @Test
    @DisplayName("Deve persistir um registro tributário corretamente")
    void devePersistirFaturaTributo() {

        Fatura fatura = criarFatura();
        RegraTributaria regra = criarRegraTributaria(TipoTributo.ICMS);

        FaturaTributo salvo = faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regra, TipoTributo.ICMS)
        );

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getTipoTributo()).isEqualTo(TipoTributo.ICMS);
        assertThat(salvo.getValorTributo())
                .isEqualByComparingTo("18.00");
    }

    @Test
    @DisplayName("Deve buscar um registro tributário pelo ID")
    void deveBuscarFaturaTributoPorId() {

        Fatura fatura = criarFatura();
        RegraTributaria regra = criarRegraTributaria(TipoTributo.ICMS);

        FaturaTributo salvo = faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regra, TipoTributo.ICMS)
        );

        var encontrado = faturaTributoRepo.findById(salvo.getId());

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getId()).isEqualTo(salvo.getId());
    }

    @Test
    @DisplayName("Deve listar todos os tributos de uma fatura")
    void deveBuscarTributosPorFatura() {

        Fatura fatura = criarFatura();

        RegraTributaria regraIcms = criarRegraTributaria(TipoTributo.ICMS);
        RegraTributaria regraPis = criarRegraTributaria(TipoTributo.PIS);

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regraIcms, TipoTributo.ICMS)
        );

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regraPis, TipoTributo.PIS)
        );

        List<FaturaTributo> encontrados =
                faturaTributoRepo.findByFatura_Id(fatura.getId());

        assertThat(encontrados).hasSize(2);
        assertThat(encontrados)
                .extracting(FaturaTributo::getTipoTributo)
                .containsExactlyInAnyOrder(TipoTributo.ICMS, TipoTributo.PIS);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando a fatura não possuir tributos")
    void deveRetornarListaVaziaQuandoFaturaNaoPossuirTributos() {

        Fatura fatura = criarFatura();

        List<FaturaTributo> encontrados =
                faturaTributoRepo.findByFatura_Id(fatura.getId());

        assertThat(encontrados).isEmpty();
    }

    @Test
    @DisplayName("Deve filtrar tributos por fatura e tipo")
    void deveFiltrarTributosPorFaturaETipo() {

        Fatura fatura = criarFatura();

        RegraTributaria regraIcms = criarRegraTributaria(TipoTributo.ICMS);
        RegraTributaria regraPis = criarRegraTributaria(TipoTributo.PIS);

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regraIcms, TipoTributo.ICMS)
        );

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regraPis, TipoTributo.PIS)
        );

        List<FaturaTributo> encontrados =
                faturaTributoRepo.findByFatura_IdAndTipoTributo(
                        fatura.getId(),
                        TipoTributo.ICMS
                );

        assertThat(encontrados).hasSize(1);
        assertThat(encontrados.get(0).getTipoTributo())
                .isEqualTo(TipoTributo.ICMS);
    }

    @Test
    @DisplayName("Não deve retornar tributos de outro tipo")
    void naoDeveRetornarTributosDeOutroTipo() {

        Fatura fatura = criarFatura();

        RegraTributaria regraPis = criarRegraTributaria(TipoTributo.PIS);

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regraPis, TipoTributo.PIS)
        );

        List<FaturaTributo> encontrados =
                faturaTributoRepo.findByFatura_IdAndTipoTributo(
                        fatura.getId(),
                        TipoTributo.ICMS
                );

        assertThat(encontrados).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar registros pela regra tributária")
    void deveBuscarPorRegraTributaria() {

        Fatura fatura = criarFatura();

        RegraTributaria regra = criarRegraTributaria(TipoTributo.ICMS);

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regra, TipoTributo.ICMS)
        );

        List<FaturaTributo> encontrados =
                faturaTributoRepo.findByRegraTributaria_Id(regra.getId());

        assertThat(encontrados).hasSize(1);
        assertThat(encontrados.get(0).getRegraTributaria().getId())
                .isEqualTo(regra.getId());
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando a regra não possuir aplicações")
    void deveRetornarListaVaziaQuandoRegraNaoPossuirAplicacoes() {

        RegraTributaria regra = criarRegraTributaria(TipoTributo.ICMS);

        List<FaturaTributo> encontrados =
                faturaTributoRepo.findByRegraTributaria_Id(regra.getId());

        assertThat(encontrados).isEmpty();
    }

    @Test
    @DisplayName("Deve excluir um registro tributário")
    void deveExcluirFaturaTributo() {

        Fatura fatura = criarFatura();
        RegraTributaria regra = criarRegraTributaria(TipoTributo.ICMS);

        FaturaTributo salvo = faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regra, TipoTributo.ICMS)
        );

        faturaTributoRepo.delete(salvo);
        faturaTributoRepo.flush();

        assertThat(faturaTributoRepo.findById(salvo.getId())).isEmpty();
    }

    @Test
    @DisplayName("Deve impedir persistência sem fatura associada")
    void naoDevePersistirSemFatura() {

        RegraTributaria regra = criarRegraTributaria(TipoTributo.ICMS);

        FaturaTributo tributo = criarFaturaTributo(
                null,
                regra,
                TipoTributo.ICMS
        );

        assertThatThrownBy(() -> faturaTributoRepo.saveAndFlush(tributo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve impedir persistência sem regra tributária associada")
    void naoDevePersistirSemRegraTributaria() {

        Fatura fatura = criarFatura();

        FaturaTributo tributo = criarFaturaTributo(
                fatura,
                null,
                TipoTributo.ICMS
        );

        assertThatThrownBy(() -> faturaTributoRepo.saveAndFlush(tributo))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve permitir diferentes tributos na mesma fatura")
    void devePermitirDiferentesTributosNaMesmaFatura() {

        Fatura fatura = criarFatura();

        RegraTributaria regraIcms = criarRegraTributaria(TipoTributo.ICMS);
        RegraTributaria regraPis = criarRegraTributaria(TipoTributo.PIS);
        RegraTributaria regraCofins = criarRegraTributaria(TipoTributo.COFINS);

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regraIcms, TipoTributo.ICMS)
        );

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regraPis, TipoTributo.PIS)
        );

        faturaTributoRepo.saveAndFlush(
                criarFaturaTributo(fatura, regraCofins, TipoTributo.COFINS)
        );

        List<FaturaTributo> encontrados =
                faturaTributoRepo.findByFatura_Id(fatura.getId());

        assertThat(encontrados).hasSize(3);
        assertThat(encontrados)
                .extracting(FaturaTributo::getTipoTributo)
                .containsExactlyInAnyOrder(
                        TipoTributo.ICMS,
                        TipoTributo.PIS,
                        TipoTributo.COFINS
                );
    }
}
