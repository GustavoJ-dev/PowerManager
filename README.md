# ⚡ PowerManager API

API backend para gerenciamento de unidades consumidoras, medidores, leituras, tarifas e faturamento de energia elétrica no Brasil.

> **Estado do projeto:** em refatoração de domínio. A modelagem das entidades e dos enums foi remodelada para refletir as regras consolidadas. As camadas de aplicação ainda precisam ser alinhadas a essa nova modelagem.

## Índice

- [Sobre o projeto](#sobre-o-projeto)
- [Escopo funcional](#escopo-funcional)
- [Modelo de domínio](#modelo-de-domínio)
- [Regras centrais de faturamento](#regras-centrais-de-faturamento)
- [Arquitetura](#arquitetura)
- [Tecnologias](#tecnologias)
- [Estado da implementação](#estado-da-implementação)
- [Próximas etapas](#próximas-etapas)
- [Documentação](#documentação)

## Sobre o projeto

O PowerManager é uma API destinada a representar o ciclo de gerenciamento e faturamento de energia elétrica, inicialmente voltado aos consumidores do **Grupo B no Brasil**.

O sistema não deve reduzir o faturamento a uma multiplicação por um preço único. A composição da cobrança considera, de forma separada, os componentes tarifários TE e TUSD, bandeiras tarifárias, tributos aplicáveis e demais componentes que venham a ser definidos com regras próprias.

A arquitetura deve preservar os valores históricos utilizados em cada fatura, mesmo quando tarifas, bandeiras ou regras tributárias forem alteradas posteriormente.

## Escopo funcional

O domínio contempla:

- cadastro de clientes pessoas físicas ou jurídicas;
- cadastro de uma ou mais unidades consumidoras por cliente;
- gerenciamento e histórico de medidores vinculados às unidades;
- registro de leituras reais e estimadas;
- administração de tarifas TE e TUSD por distribuidora, subgrupo, classe, modalidade e vigência;
- cadastro dos valores oficiais das bandeiras e dos períodos em que foram acionadas;
- configuração de regras tributárias federais, estaduais e municipais;
- geração, consulta e pagamento de faturas;
- gerenciamento de usuários com perfis `ADMINISTRADOR` e `CLIENTE`;
- preservação de histórico e rastreabilidade das alterações relevantes.

A integração automática com fontes oficiais, como dados tarifários e de bandeiras divulgados por órgãos competentes, é uma evolução futura. Na primeira etapa, os dados serão cadastrados manualmente por usuários administrativos.

## Modelo de domínio

### Relacionamentos principais

```text
Cliente 1 ───── N UnidadeConsumidora
UnidadeConsumidora 1 ───── N Medidor
Medidor 1 ───── N Leitura
UnidadeConsumidora 1 ───── N Fatura

Fatura 1 ───── N FaturaBandeiraAplicada
Fatura 1 ───── N FaturaTributo

FaturaBandeiraAplicada N ───── 1 AcionamentoBandeira
FaturaTributo N ───── 1 RegraTributaria

Usuario 0..1 ───── 1 Cliente
```

### Entidades

| Entidade | Responsabilidade |
| --- | --- |
| `Cliente` | Representa a pessoa física ou jurídica titular de unidades consumidoras. |
| `UnidadeConsumidora` | Representa a instalação elétrica; concentra endereço e características tarifárias. |
| `Medidor` | Representa o equipamento instalado em uma unidade consumidora, preservando seu ciclo de instalação e retirada. |
| `Leitura` | Registra o valor acumulado do medidor, data/hora e natureza real ou estimada. |
| `Taxa` | Armazena TE e TUSD por distribuidora, subgrupo, classe, modalidade e vigência. |
| `BandeiraTarifaria` | Armazena o valor adicional oficial por kWh de cada tipo de bandeira e sua vigência. |
| `AcionamentoBandeira` | Registra qual bandeira foi oficialmente acionada em determinado período. |
| `RegraTributaria` | Define parâmetros de uma regra tributária conforme tributo, abrangência, localização, tipo de cálculo e vigência. |
| `Fatura` | Registra a cobrança da unidade consumidora, competência, consumo, snapshots tarifários, valores e status. |
| `FaturaBandeiraAplicada` | Registra os períodos e valores de bandeira considerados em uma fatura. |
| `FaturaTributo` | Registra o tributo calculado e os parâmetros aplicados como snapshot da fatura. |
| `Usuario` | Representa a conta de acesso, seu perfil e eventual associação a um cliente. |

### Enums do domínio

- `AbrangenciaTributaria`: `FEDERAL`, `ESTADUAL`, `MUNICIPAL`.
- `ClasseConsumo`: `RESIDENCIAL`, `INDUSTRIAL`, `COMERCIAL_SERVICOS`, `RURAL`, `PODER_PUBLICO`, `ILUMINACAO_PUBLICA`, `SERVICO_PUBLICO`, `CONSUMO_PROPRIO`.
- `ModalidadeTarifaria`: `CONVENCIONAL`, `BRANCA`.
- `OrigemConsumo`: `LEITURA`, `MANUAL`.
- `StatusFatura`: `PENDENTE`, `PAGO`.
- `SubgrupoTarifario`: `B1`, `B2`, `B3`, `B4`.
- `TipoBandeira`: `VERDE`, `AMARELA`, `VERMELHA_PATAMAR_1`, `VERMELHA_PATAMAR_2`.
- `TipoCalculoTributario`: `PERCENTUAL`, `VALOR_FIXO`.
- `TipoLeitura`: `REAL`, `ESTIMADA`.
- `TipoLigacao`: `MONOFASICO`, `BIFASICO`, `TRIFASICO`.
- `TipoMedidor`: `ELETROMECANICO`, `ELETRONICO`, `INTELIGENTE`.
- `TipoTributo`: `ICMS`, `PIS`, `COFINS`, `CIP_COSIP`.
- `TipoUsuario`: `ADMINISTRADOR`, `CLIENTE`.

## Regras centrais de faturamento

### Tarifa de energia

A tarifa é composta por dois componentes independentes:

- **TE:** Tarifa de Energia.
- **TUSD:** Tarifa de Uso do Sistema de Distribuição.

Fórmula tarifária básica:

```text
ValorTE + ValorTUSD = Consumo (kWh) × (TE + TUSD)
```

Esse resultado é a composição tarifária básica, não necessariamente o total da fatura. A tarifa aplicável deve corresponder à combinação da distribuidora, subgrupo, classe e modalidade da unidade, e estar vigente no início da competência.

Os períodos de vigência não podem se sobrepor para a mesma combinação tarifária. Um reajuste encerra o registro anterior e cria um novo, sem apagar o histórico.

### Bandeiras tarifárias

O cadastro do valor adicional da bandeira (`BandeiraTarifaria`) é separado do registro do acionamento oficial (`AcionamentoBandeira`).

Um ciclo de faturamento pode atravessar mais de um período de bandeira. Por isso, a aplicação deve considerar os períodos efetivamente abrangidos pelo ciclo, sem presumir que uma única bandeira vale para toda a fatura.

`FaturaBandeiraAplicada` preserva os dados utilizados no cálculo. A distribuição do consumo entre períodos deve seguir a regra regulatória aplicável; não se deve assumir rateio proporcional sem validação.

### Tributos

Os tributos são modelados separadamente:

- ICMS: abrangência estadual;
- PIS e COFINS: abrangência federal;
- CIP/COSIP: abrangência municipal.

Cada regra possui localização, tipo de cálculo, parâmetros e vigência próprios. A fatura armazena snapshots dos parâmetros e do valor efetivamente aplicado. Fórmulas, bases de cálculo, hipóteses de incidência e arredondamentos específicos precisam ser validados antes da implementação do cálculo tributário.

### Fatura

- A fatura pertence à `UnidadeConsumidora`, não diretamente ao medidor.
- A competência é representada pelo primeiro dia do mês.
- Não pode haver mais de uma fatura para a mesma unidade e competência.
- A origem do consumo é identificada como `LEITURA` ou `MANUAL`.
- A fatura preserva os valores aplicados no momento da geração.
- O status inicial é `PENDENTE`; após o registro do pagamento, passa a `PAGO`.
- O pagamento é manual nesta primeira versão e não admite registro duplicado.

### Precisão numérica

- Utilizar `BigDecimal` para consumo, tarifas, alíquotas e valores monetários.
- Não utilizar `float` ou `double` em cálculos financeiros.
- TE e TUSD: precisão definida no modelo como `DECIMAL(12,6)`.
- Consumo e leituras: `DECIMAL(12,3)`.
- Valores monetários consolidados: `DECIMAL(14,2)`.
- O arredondamento geral inicial é `HALF_UP`; a regra específica por componente depende de validação regulatória.

## Arquitetura

A organização lógica prevista separa responsabilidades:

| Camada | Responsabilidade |
| --- | --- |
| `entity` | Entidades JPA e relacionamentos persistentes. |
| `enums` | Valores enumerados do domínio. |
| `dto` | Contratos de entrada, atualização e resposta. |
| `mapper` | Conversão entre entidades e DTOs com MapStruct. |
| `repository` | Persistência e consultas com Spring Data JPA. |
| `services` | Regras de negócio, validações e transações. |
| `controller` | Exposição dos endpoints REST. |
| `exception` | Exceções e tratamento consistente de erros. |

Os controllers, autenticação/autorização e as camadas de aplicação remodeladas devem ser implementados ou alinhados às novas entidades antes de considerar a API pronta para uso externo.

## Tecnologias

A configuração do repositório utiliza:

- Java 21;
- Spring Boot 4.1.1;
- Spring Data JPA / Hibernate;
- Maven;
- MySQL 8.4 para o ambiente de produção;
- H2 em memória para desenvolvimento;
- MapStruct 1.6.3;
- Lombok;
- Bean Validation;
- JUnit 5, Mockito e AssertJ nos testes existentes;
- JaCoCo 0.8.14 para relatório de cobertura.

O profile `dev` utiliza H2 em memória. O profile `prod` utiliza MySQL e `ddl-auto: validate`.

> **Banco de dados e migrations:** Flyway foi definido como estratégia de versionamento do schema na especificação consolidada. A adoção efetiva deve ser confirmada na configuração/dependências e nas migrations do repositório antes de ser considerada concluída. O `create-drop` do profile de desenvolvimento também deverá ser revisto para não competir com o Flyway.

## Estado da implementação

O estado abaixo distingue o que foi verificado no código disponibilizado do que ainda é decisão ou trabalho futuro.

### Verificado nas entidades e enums

- [x] `Cliente` separado de endereço e instalação.
- [x] `UnidadeConsumidora` associada a `Cliente`.
- [x] `Medidor` associado a `UnidadeConsumidora`, com `ativo`, `dataInstalacao` e `dataRetirada`.
- [x] `Leitura` associada a `Medidor`, com `BigDecimal`, `LocalDateTime` e `TipoLeitura`.
- [x] `Taxa` remodelada para TE/TUSD e vigência.
- [x] `BandeiraTarifaria` separada de `AcionamentoBandeira`.
- [x] `FaturaBandeiraAplicada` com snapshots por período.
- [x] `RegraTributaria` e `FaturaTributo`.
- [x] `Fatura` associada à unidade consumidora e com snapshots de TE/TUSD e valores consolidados.
- [x] `Usuario` com associação opcional e única a `Cliente`.
- [x] Enums do domínio remodelado.
- [x] Índices e constraints JPA presentes em parte das entidades.

### Ainda não comprovado como concluído

- [ ] Serviços de negócio alinhados integralmente ao novo modelo de unidade, leituras, tarifas, bandeiras e tributação.
- [ ] DTOs e mappers atualizados para todos os novos relacionamentos e entidades.
- [ ] Repositórios e consultas do novo modelo.
- [ ] Controllers e endpoints REST.
- [ ] Spring Security, hash de senhas com `PasswordEncoder` e autorização por propriedade do recurso.
- [ ] Flyway e migrations versionadas.
- [ ] Testes de persistência e integração cobrindo os relacionamentos remodelados.
- [ ] Validação regulatória das fórmulas e bases tributárias.
- [ ] Auditoria das alterações administrativas e tarifárias.

A existência de uma entidade ou constraint JPA não significa que o fluxo de negócio correspondente já esteja implementado ou testado.

## Próximas etapas

1. Finalizar e validar entidades e enums, incluindo testes de persistência no H2.
2. Remodelar DTOs.
3. Atualizar mappers.
4. Remodelar repositories.
5. Implementar services e regras transacionais.
6. Implementar controllers e endpoints.
7. Implementar Spring Security e autorização.
8. Configurar Flyway e migrations.
9. Expandir testes unitários, de integração e de persistência.
10. Fazer validação final do fluxo de faturamento e da integridade histórica.

## Documentação

- [`ESPECIFICACAO_CONSOLIDADA.md`](ESPECIFICACAO_CONSOLIDADA.md): regras de negócio, modelo de domínio, decisões posteriores ao PDF, pendências e critérios de implementação.