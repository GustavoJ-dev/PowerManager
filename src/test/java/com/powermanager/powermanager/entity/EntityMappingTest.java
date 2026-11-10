package com.powermanager.powermanager.entity;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.Test;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;


class EntityMappingTest {

    @Test
    void deveInicializarTodasAsEntidadesNoHibernate() {

        StandardServiceRegistry registry =
                new StandardServiceRegistryBuilder()
                        .applySetting(
                                "hibernate.connection.driver_class",
                                "org.h2.Driver"
                        )
                        .applySetting(
                                "hibernate.connection.url",
                                "jdbc:h2:mem:entity_test;DB_CLOSE_DELAY=-1"
                        )
                        .applySetting(
                                "hibernate.connection.username",
                                "sa"
                        )
                        .applySetting(
                                "hibernate.connection.password",
                                ""
                        )
                        .applySetting(
                                "hibernate.hbm2ddl.auto",
                                "create-drop"
                        )
                        .applySetting(
                                "hibernate.show_sql",
                                "true"
                        )
                        .build();

        try {
            MetadataSources metadata = new MetadataSources(registry)

                    .addAnnotatedClass(Cliente.class)
                    .addAnnotatedClass(UnidadeConsumidora.class)
                    .addAnnotatedClass(Medidor.class)
                    .addAnnotatedClass(Leitura.class)
                    .addAnnotatedClass(Taxa.class)
                    .addAnnotatedClass(BandeiraTarifaria.class)
                    .addAnnotatedClass(AcionamentoBandeira.class)
                    .addAnnotatedClass(Fatura.class)
                    .addAnnotatedClass(FaturaBandeiraAplicada.class)
                    .addAnnotatedClass(RegraTributaria.class)
                    .addAnnotatedClass(FaturaTributo.class)
                    .addAnnotatedClass(Usuario.class);

            try (SessionFactory sessionFactory =
                         metadata.buildMetadata().buildSessionFactory()) {

                assertNotNull(sessionFactory);
            }

        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
