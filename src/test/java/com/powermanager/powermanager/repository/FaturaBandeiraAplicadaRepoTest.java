
package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.*;
import com.powermanager.powermanager.enums.*;
import jakarta.persistence.EntityManager;
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
class FaturaBandeiraAplicadaRepoTest {

    @Autowired
    private FaturaBandeiraAplicadaRepo repository;

    @Autowired
    private EntityManager entityManager;

    private Fatura criarFatura() {

        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setEmail("teste" + System.nanoTime() + "@email.com");
        cliente.setTelefone("11999999999");
        entityManager.persist(cliente);

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setCodigo("UC-" + System.nanoTime());
        unidade.setEndereco("Rua Teste, 100");
        unidade.setCidade("São Paulo");
        unidade.setEstado("SP");
        unidade.setCep("01000-000");
        unidade.setDistribuidora("Distribuidora Teste");
        unidade.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        unidade.setSubgrupoTarifario(SubgrupoTarifario.B1);
        unidade.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        unidade.setAtiva(true);
        unidade.setCliente(cliente);
        entityManager.persist(unidade);

        Fatura fatura = new Fatura();
        fatura.setUnidadeConsumidora(unidade);
        fatura.setCompetencia(LocalDate.of(2026, 1, 1));
        fatura.setInicioPeriodo(LocalDate.of(2025, 12, 16));
        fatura.setFimPeriodo(LocalDate.of(2026, 1, 15));
        fatura.setConsumoKwh(new BigDecimal("250.000"));
        fatura.setOrigemConsumo(OrigemConsumo.LEITURA);
        fatura.setTarifaEnergiaTE(new BigDecimal("0.300000"));
        fatura.setTarifaUsoDistribuicaoTUSD(new BigDecimal("0.400000"));
        fatura.setValorTE(new BigDecimal("75.00"));
        fatura.setValorTUSD(new BigDecimal("100.00"));
        fatura.setValorBandeira(new BigDecimal("8.58"));
        fatura.setValorSubtotal(new BigDecimal("183.58"));
        fatura.setValorTotal(new BigDecimal("183.58"));
        fatura.setDataEmissao(LocalDate.of(2026, 1, 16));
        fatura.setDataVencimento(LocalDate.of(2026, 1, 25));
        fatura.setStatus(StatusFatura.PENDENTE);
        entityManager.persist(fatura);

        entityManager.flush();
        return fatura;
    }

    private AcionamentoBandeira criarAcionamento() {

        AcionamentoBandeira acionamento = new AcionamentoBandeira();
        acionamento.setTipoBandeira(TipoBandeira.AMARELA);
        acionamento.setInicioVigencia(LocalDate.of(2026, 1, 1));
        acionamento.setFimVigencia(LocalDate.of(2026, 2, 1));
        acionamento.setDataDivulgacao(LocalDate.of(2025, 12, 20));
        acionamento.setReferenciaOficial("Referência oficial de teste");

        entityManager.persist(acionamento);
        entityManager.flush();

        return acionamento;
    }

    private FaturaBandeiraAplicada criarAplicacao(Fatura fatura, AcionamentoBandeira acionamento,
                                                  LocalDate inicioAplicacao) {

        FaturaBandeiraAplicada aplicada = new FaturaBandeiraAplicada();
        aplicada.setFatura(fatura);
        aplicada.setAcionamentoBandeira(acionamento);
        aplicada.setTipoBandeira(TipoBandeira.AMARELA);
        aplicada.setInicioAplicacao(inicioAplicacao);
        aplicada.setFimAplicacao(inicioAplicacao.plusDays(14));
        aplicada.setValorAdicionalKwh(new BigDecimal("0.018850"));
        aplicada.setConsumoConsideradoKwh(new BigDecimal("100.000"));
        aplicada.setValorAdicional(new BigDecimal("1.89"));

        return aplicada;
    }

    @Test
    void devePersistirFaturaBandeiraAplicada() {
        Fatura fatura = criarFatura();
        AcionamentoBandeira acionamento = criarAcionamento();

        FaturaBandeiraAplicada aplicada =
                criarAplicacao(fatura, acionamento, LocalDate.of(2026, 1, 1));

        FaturaBandeiraAplicada salva = repository.saveAndFlush(aplicada);

        assertThat(salva.getId()).isNotNull();
        assertThat(salva.getTipoBandeira()).isEqualTo(TipoBandeira.AMARELA);
        assertThat(salva.getValorAdicionalKwh())
                .isEqualByComparingTo("0.018850");
        assertThat(salva.getConsumoConsideradoKwh())
                .isEqualByComparingTo("100.000");
        assertThat(salva.getValorAdicional())
                .isEqualByComparingTo("1.89");
    }

    @Test
    void deveBuscarPorId() {
        Fatura fatura = criarFatura();
        AcionamentoBandeira acionamento = criarAcionamento();

        FaturaBandeiraAplicada salva = repository.saveAndFlush(
                criarAplicacao(fatura, acionamento, LocalDate.of(2026, 1, 1))
        );

        assertThat(repository.findById(salva.getId())).isPresent();
    }

    @Test
    void deveBuscarAplicacoesPorFaturaEmOrdemCronologica() {
        Fatura fatura = criarFatura();
        AcionamentoBandeira acionamento = criarAcionamento();

        FaturaBandeiraAplicada segunda =
                criarAplicacao(fatura, acionamento, LocalDate.of(2026, 1, 16));

        FaturaBandeiraAplicada primeira =
                criarAplicacao(fatura, acionamento, LocalDate.of(2026, 1, 1));

        repository.save(segunda);
        repository.save(primeira);
        repository.flush();

        List<FaturaBandeiraAplicada> resultado =
                repository.findByFatura_IdOrderByInicioAplicacaoAsc(fatura.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getInicioAplicacao())
                .isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(resultado.get(1).getInicioAplicacao())
                .isEqualTo(LocalDate.of(2026, 1, 16));
    }

    @Test
    void deveRetornarListaVaziaQuandoFaturaNaoPossuirAplicacoes() {
        Fatura fatura = criarFatura();

        assertThat(repository.findByFatura_IdOrderByInicioAplicacaoAsc(fatura.getId()))
                .isEmpty();
    }

    @Test
    void deveBuscarAplicacoesPorAcionamentoBandeira() {
        Fatura fatura = criarFatura();
        AcionamentoBandeira acionamento = criarAcionamento();

        repository.saveAndFlush(
                criarAplicacao(fatura, acionamento, LocalDate.of(2026, 1, 1))
        );

        List<FaturaBandeiraAplicada> resultado =
                repository.findByAcionamentoBandeira_Id(acionamento.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getAcionamentoBandeira().getId())
                .isEqualTo(acionamento.getId());
    }

    @Test
    void deveRetornarListaVaziaQuandoAcionamentoNaoPossuirAplicacoes() {
        AcionamentoBandeira acionamento = criarAcionamento();

        assertThat(repository.findByAcionamentoBandeira_Id(acionamento.getId()))
                .isEmpty();
    }

    @Test
    void devePreservarSnapshotsDeAplicacoesDiferentes() {
        Fatura fatura = criarFatura();
        AcionamentoBandeira acionamento = criarAcionamento();

        FaturaBandeiraAplicada primeira =
                criarAplicacao(fatura, acionamento, LocalDate.of(2026, 1, 1));

        FaturaBandeiraAplicada segunda =
                criarAplicacao(fatura, acionamento, LocalDate.of(2026, 1, 16));

        segunda.setTipoBandeira(TipoBandeira.VERMELHA_PATAMAR_1);
        segunda.setValorAdicionalKwh(new BigDecimal("0.044630"));
        segunda.setConsumoConsideradoKwh(new BigDecimal("150.000"));
        segunda.setValorAdicional(new BigDecimal("6.69"));

        repository.save(primeira);
        repository.save(segunda);
        repository.flush();

        List<FaturaBandeiraAplicada> resultado =
                repository.findByFatura_IdOrderByInicioAplicacaoAsc(fatura.getId());

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getTipoBandeira())
                .isEqualTo(TipoBandeira.AMARELA);
        assertThat(resultado.get(1).getTipoBandeira())
                .isEqualTo(TipoBandeira.VERMELHA_PATAMAR_1);
        assertThat(resultado.get(1).getValorAdicional())
                .isEqualByComparingTo("6.69");
    }

    @Test
    void deveExcluirAplicacao() {
        Fatura fatura = criarFatura();
        AcionamentoBandeira acionamento = criarAcionamento();

        FaturaBandeiraAplicada salva = repository.saveAndFlush(
                criarAplicacao(fatura, acionamento, LocalDate.of(2026, 1, 1))
        );

        repository.delete(salva);
        repository.flush();

        assertThat(repository.findById(salva.getId())).isEmpty();
    }

    @Test
    void naoDevePersistirAplicacaoSemFatura() {
        AcionamentoBandeira acionamento = criarAcionamento();

        FaturaBandeiraAplicada aplicada =
                criarAplicacao(null, acionamento, LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> repository.saveAndFlush(aplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void naoDevePersistirAplicacaoSemAcionamentoBandeira() {
        Fatura fatura = criarFatura();

        FaturaBandeiraAplicada aplicada =
                criarAplicacao(fatura, null, LocalDate.of(2026, 1, 1));

        assertThatThrownBy(() -> repository.saveAndFlush(aplicada))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

