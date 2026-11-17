package com.powermanager.powermanager.repository;

import com.powermanager.powermanager.entity.Cliente;
import com.powermanager.powermanager.entity.Leitura;
import com.powermanager.powermanager.entity.Medidor;
import com.powermanager.powermanager.entity.UnidadeConsumidora;
import com.powermanager.powermanager.enums.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class LeituraRepoTest {

    @Autowired
    private LeituraRepo leituraRepo;

    @Autowired
    private ClienteRepo clienteRepo;

    @Autowired
    private UnidadeConsumidoraRepo unidadeConsumidoraRepo;

    @Autowired
    private MedidorRepo medidorRepo;

    @Autowired
    private EntityManager entityManager;

    private Medidor criarMedidor(String email, String codigo, String numero) {

        Cliente cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setEmail(email);
        cliente.setTelefone("71999999999");

        cliente = clienteRepo.saveAndFlush(cliente);

        UnidadeConsumidora unidade = new UnidadeConsumidora();
        unidade.setCodigo(codigo);
        unidade.setEndereco("Rua das Flores, 123");
        unidade.setCidade("Salvador");
        unidade.setEstado("BA");
        unidade.setCep("40000000");
        unidade.setDistribuidora("Neoenergia Coelba");
        unidade.setClasseConsumo(ClasseConsumo.RESIDENCIAL);
        unidade.setSubgrupoTarifario(SubgrupoTarifario.B1);
        unidade.setModalidadeTarifaria(ModalidadeTarifaria.CONVENCIONAL);
        unidade.setAtiva(true);
        unidade.setCliente(cliente);

        unidade = unidadeConsumidoraRepo.saveAndFlush(unidade);

        Medidor medidor = new Medidor();
        medidor.setNumero(numero);
        medidor.setTipoMedidor(TipoMedidor.ELETRONICO);
        medidor.setTipoLigacao(TipoLigacao.MONOFASICO);
        medidor.setAtivo(true);
        medidor.setDataInstalacao(LocalDate.of(2026, 1, 1));
        medidor.setUnidadeConsumidora(unidade);

        return medidorRepo.saveAndFlush(medidor);
    }

    private Leitura criarLeitura(Medidor medidor, LocalDateTime dataLeitura, String valor, TipoLeitura tipo) {

        Leitura leitura = new Leitura();

        leitura.setMedidor(medidor);
        leitura.setDataLeitura(dataLeitura);
        leitura.setValorLeitura(new BigDecimal(valor));
        leitura.setTipoLeitura(tipo);

        return leitura;
    }

    @Test
    @DisplayName("Deve persistir uma leitura corretamente")
    void devePersistirLeitura() {

        Medidor medidor = criarMedidor(
                "cliente1@email.com", "UC-001", "MED-001"
        );

        Leitura salva = leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 2, 1, 10, 0),
                        "150.500",
                        TipoLeitura.REAL
                )
        );

        assertThat(salva.getId()).isNotNull();

        entityManager.clear();

        Leitura encontrada = leituraRepo.findById(salva.getId())
                .orElseThrow();

        assertThat(encontrada.getValorLeitura())
                .isEqualByComparingTo("150.500");

        assertThat(encontrada.getTipoLeitura())
                .isEqualTo(TipoLeitura.REAL);

        assertThat(encontrada.getMedidor().getId())
                .isEqualTo(medidor.getId());
    }

    @Test
    @DisplayName("Deve buscar leitura pelo ID")
    void deveBuscarLeituraPorId() {

        Medidor medidor = criarMedidor(
                "cliente2@email.com", "UC-002", "MED-002"
        );

        Leitura salva = leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 2, 1, 10, 0),
                        "200.000",
                        TipoLeitura.REAL
                )
        );

        entityManager.clear();

        Optional<Leitura> resultado = leituraRepo.findById(salva.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(salva.getId());
    }

    @Test
    @DisplayName("Não deve encontrar leitura com ID inexistente")
    void naoDeveEncontrarLeituraPorIdInexistente() {

        assertThat(leituraRepo.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Deve listar todas as leituras")
    void deveListarLeituras() {

        Medidor medidor = criarMedidor(
                "cliente3@email.com", "UC-003", "MED-003"
        );

        leituraRepo.saveAllAndFlush(List.of(
                criarLeitura(medidor,
                        LocalDateTime.of(2026, 2, 1, 10, 0),
                        "100.000", TipoLeitura.REAL),
                criarLeitura(medidor,
                        LocalDateTime.of(2026, 3, 1, 10, 0),
                        "150.000", TipoLeitura.REAL)
        ));

        entityManager.clear();

        assertThat(leituraRepo.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("Deve listar leituras de um medidor em ordem crescente")
    void deveListarLeiturasEmOrdemCrescente() {

        Medidor medidor = criarMedidor(
                "cliente4@email.com", "UC-004", "MED-004"
        );

        leituraRepo.saveAllAndFlush(List.of(
                criarLeitura(medidor,
                        LocalDateTime.of(2026, 3, 1, 10, 0),
                        "300.000", TipoLeitura.REAL),
                criarLeitura(medidor,
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        "100.000", TipoLeitura.REAL),
                criarLeitura(medidor,
                        LocalDateTime.of(2026, 2, 1, 10, 0),
                        "200.000", TipoLeitura.ESTIMADA)
        ));

        entityManager.clear();

        List<Leitura> resultado =
                leituraRepo.findByMedidor_IdOrderByDataLeituraAsc(medidor.getId());

        assertThat(resultado)
                .extracting(Leitura::getValorLeitura)
                .containsExactly(
                        new BigDecimal("100.000"),
                        new BigDecimal("200.000"),
                        new BigDecimal("300.000")
                );
    }

    @Test
    @DisplayName("Deve listar leituras de um medidor em ordem decrescente")
    void deveListarLeiturasEmOrdemDecrescente() {

        Medidor medidor = criarMedidor(
                "cliente5@email.com", "UC-005", "MED-005"
        );

        leituraRepo.saveAllAndFlush(List.of(
                criarLeitura(medidor,
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        "100.000", TipoLeitura.REAL),
                criarLeitura(medidor,
                        LocalDateTime.of(2026, 3, 1, 10, 0),
                        "300.000", TipoLeitura.REAL),
                criarLeitura(medidor,
                        LocalDateTime.of(2026, 2, 1, 10, 0),
                        "200.000", TipoLeitura.REAL)
        ));

        entityManager.clear();

        List<Leitura> resultado =
                leituraRepo.findByMedidor_IdOrderByDataLeituraDesc(medidor.getId());

        assertThat(resultado)
                .extracting(Leitura::getValorLeitura)
                .containsExactly(
                        new BigDecimal("300.000"),
                        new BigDecimal("200.000"),
                        new BigDecimal("100.000")
                );
    }

    @Test
    @DisplayName("Deve buscar a última leitura do medidor")
    void deveBuscarUltimaLeitura() {

        Medidor medidor = criarMedidor(
                "cliente6@email.com", "UC-006", "MED-006"
        );

        Leitura ultima = leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 3, 1, 10, 0),
                        "300.000",
                        TipoLeitura.REAL
                )
        );

        leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 1, 1, 10, 0),
                        "100.000",
                        TipoLeitura.REAL
                )
        );

        entityManager.clear();

        Optional<Leitura> resultado =
                leituraRepo.findTopByMedidor_IdOrderByDataLeituraDesc(medidor.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(ultima.getId());
    }

    @Test
    @DisplayName("Deve buscar a leitura imediatamente anterior ao instante informado")
    void deveBuscarLeituraAnterior() {

        Medidor medidor = criarMedidor(
                "cliente7@email.com", "UC-007", "MED-007"
        );

        Leitura anterior = leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 2, 1, 10, 0),
                        "200.000",
                        TipoLeitura.REAL
                )
        );

        leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 3, 1, 10, 0),
                        "300.000",
                        TipoLeitura.REAL
                )
        );

        entityManager.clear();

        Optional<Leitura> resultado =
                leituraRepo.findTopByMedidor_IdAndDataLeituraBeforeOrderByDataLeituraDesc(
                        medidor.getId(),
                        LocalDateTime.of(2026, 2, 15, 10, 0)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(anterior.getId());
    }

    @Test
    @DisplayName("Deve buscar a próxima leitura posterior ao instante informado")
    void deveBuscarProximaLeitura() {

        Medidor medidor = criarMedidor(
                "cliente8@email.com", "UC-008", "MED-008"
        );

        Leitura proxima = leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 3, 1, 10, 0),
                        "300.000",
                        TipoLeitura.REAL
                )
        );

        leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 4, 1, 10, 0),
                        "400.000",
                        TipoLeitura.REAL
                )
        );

        entityManager.clear();

        Optional<Leitura> resultado =
                leituraRepo.findTopByMedidor_IdAndDataLeituraAfterOrderByDataLeituraAsc(
                        medidor.getId(),
                        LocalDateTime.of(2026, 2, 15, 10, 0)
                );

        assertThat(resultado).isPresent();
        assertThat(resultado.orElseThrow().getId()).isEqualTo(proxima.getId());
    }

    @Test
    @DisplayName("Deve verificar se já existe leitura no mesmo instante")
    void deveVerificarExistenciaPorMedidorEData() {

        Medidor medidor = criarMedidor(
                "cliente9@email.com", "UC-009", "MED-009"
        );

        LocalDateTime data = LocalDateTime.of(2026, 2, 1, 10, 0);

        leituraRepo.saveAndFlush(
                criarLeitura(medidor, data, "100.000", TipoLeitura.REAL)
        );

        assertThat(leituraRepo.existsByMedidor_IdAndDataLeitura(
                medidor.getId(), data
        )).isTrue();

        assertThat(leituraRepo.existsByMedidor_IdAndDataLeitura(
                medidor.getId(), data.plusDays(1)
        )).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir duas leituras do mesmo medidor no mesmo instante")
    void naoDevePermitirLeiturasDuplicadas() {

        Medidor medidor = criarMedidor(
                "cliente10@email.com", "UC-010", "MED-010"
        );

        LocalDateTime data = LocalDateTime.of(2026, 2, 1, 10, 0);

        leituraRepo.saveAndFlush(
                criarLeitura(medidor, data, "100.000", TipoLeitura.REAL)
        );

        assertThatThrownBy(() ->
                leituraRepo.saveAndFlush(
                        criarLeitura(medidor, data, "150.000", TipoLeitura.REAL)
                )
        ).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Deve excluir leitura pelo ID")
    void deveExcluirLeitura() {

        Medidor medidor = criarMedidor(
                "cliente11@email.com", "UC-011", "MED-011"
        );

        Leitura salva = leituraRepo.saveAndFlush(
                criarLeitura(
                        medidor,
                        LocalDateTime.of(2026, 2, 1, 10, 0),
                        "100.000",
                        TipoLeitura.REAL
                )
        );

        UUID id = salva.getId();

        leituraRepo.deleteById(id);
        leituraRepo.flush();

        assertThat(leituraRepo.findById(id)).isEmpty();
    }
}