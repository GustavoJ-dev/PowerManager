# ⚡ PowerManager API

> API REST para gerenciamento do ciclo de medição e faturamento de energia elétrica.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

> **Status:** projeto em refatoração de domínio. As regras de negócio e a nova modelagem foram consolidadas; as camadas de aplicação estão sendo adequadas gradualmente a esse modelo. As funcionalidades descritas como planejadas ou pendentes não devem ser interpretadas como endpoints já disponíveis.

## 📚 Navegação

- [Sobre o projeto](#-sobre-o-projeto)
- [Funcionalidades e regras de negócio](#-funcionalidades-e-regras-de-negócio)
- [Fluxo de faturamento](#-fluxo-de-faturamento)
- [Modelo de domínio](#-modelo-de-domínio)
- [DTOs e mapeamento](#-dtos-e-mapeamento)
- [Repositórios](#-repositórios)
- [Services e regras de aplicação](#-services-e-regras-de-aplicação)
- [Tecnologias e arquitetura](#-tecnologias-e-arquitetura)
- [Execução](#-execução)
- [Testes e cobertura](#-testes-e-cobertura)
- [Estado atual e roadmap](#-estado-atual-e-roadmap)
- [Documentação](#-documentação)
- [Licença e autor](#-licença-e-autor)

---

## 📌 Sobre o projeto

O **PowerManager API** é um backend desenvolvido em Java e Spring Boot para representar o gerenciamento de clientes, unidades consumidoras, medidores, leituras, tarifas e faturas de energia elétrica no Brasil.

O escopo inicial considera consumidores do **Grupo B**. A proposta é modelar o faturamento de maneira mais próxima da estrutura real do setor elétrico, sem resumir a cobrança a um único preço por kWh. A composição considera separadamente TE, TUSD, bandeiras tarifárias e tributos aplicáveis.

Um princípio central do projeto é a **preservação histórica**: alterações futuras em tarifas, bandeiras ou regras tributárias não podem modificar os parâmetros utilizados em faturas já emitidas.

## 🎯 Funcionalidades e regras de negócio

O domínio contempla as seguintes capacidades:

- **Clientes:** cadastro de pessoas físicas ou jurídicas.
- **Unidades consumidoras:** cadastro de várias instalações por cliente, cada uma com endereço e características tarifárias próprias.
- **Medidores:** associação à unidade consumidora, com instalação, retirada e histórico de substituições.
- **Leituras:** registro de leituras reais e estimadas, com data/hora e valor acumulado.
- **Tarifas:** administração de TE e TUSD por distribuidora, subgrupo, classe, modalidade e período de vigência.
- **Bandeiras tarifárias:** cadastro dos valores adicionais e dos acionamentos oficiais por período.
- **Tributos:** configuração separada de regras federais, estaduais e municipais.
- **Faturas:** geração por unidade consumidora, consulta de histórico e registro manual de pagamento.
- **Usuários:** contas com perfis `ADMINISTRADOR` e `CLIENTE`, respeitando a associação com o cliente.
- **Rastreabilidade:** preservação dos parâmetros utilizados em cada faturamento.

Na primeira etapa, tarifas, bandeiras e regras tributárias serão cadastradas manualmente por usuários administrativos. A integração automática com fontes oficiais é uma evolução futura.

### Regras gerais consolidadas

- Um cliente pode possuir várias unidades consumidoras.
- Uma unidade consumidora representa a instalação elétrica e concentra endereço, distribuidora e enquadramento tarifário.
- Uma unidade pode possuir vários medidores ao longo do tempo, preservando o histórico; a regra operacional prevê normalmente apenas um medidor ativo por unidade.
- Leituras pertencem a medidores e registram valores acumulados.
- O consumo de um intervalo é obtido pela diferença entre leituras válidas, respeitando situações especiais como troca, reinicialização ou rollover do medidor.
- Tarifas não podem ter períodos de vigência sobrepostos para a mesma combinação de distribuidora, subgrupo, classe e modalidade.
- Bandeira tarifária cadastrada e acionamento oficial são conceitos distintos.
- Tributos não são tratados como uma taxa universal: possuem abrangência, localização, parâmetros e vigências próprias.
- Uma fatura pertence à unidade consumidora, não diretamente ao medidor.
- A fatura preserva snapshots dos valores e parâmetros utilizados na emissão.
- Clientes e unidades com histórico não devem ser apagados fisicamente como operação comum; deve-se priorizar desativação lógica.
- Regras regulatórias ainda não validadas não devem ser transformadas em fórmulas definitivas por suposição.

## 🔄 Fluxo de faturamento

Visão conceitual do domínio:

```text
Cliente
   │
   ▼
Unidade Consumidora
   │
   ├── Medidor(es) ── Leituras ──► Consumo do período
   │
   └── Enquadramento tarifário
             │
             ▼
       Tarifa TE + TUSD
             │
             ├── Bandeiras aplicáveis ao ciclo
             │
             └── Regras tributárias aplicáveis
                       │
                       ▼
                  Fatura
                       │
                       ▼
                    Pagamento
```

### Tarifa de energia: TE e TUSD

A tarifa é composta por dois componentes independentes:

- **TE:** Tarifa de Energia.
- **TUSD:** Tarifa de Uso do Sistema de Distribuição.

Cálculo tarifário básico:

```text
ValorTE   = Consumo (kWh) × TE
ValorTUSD = Consumo (kWh) × TUSD

Composição tarifária básica = ValorTE + ValorTUSD
```

Essa composição não representa necessariamente o total final da fatura. A tarifa deve corresponder à distribuidora, subgrupo, classe e modalidade da unidade consumidora, e estar vigente no início da competência. Reajustes encerram a vigência anterior e criam um novo registro, sem apagar o histórico.

### Bandeiras tarifárias

O cadastro do valor adicional (`BandeiraTarifaria`) é separado do registro de acionamento oficial (`AcionamentoBandeira`).

Um ciclo de faturamento pode atravessar mais de um período de bandeira. A aplicação deve considerar os períodos efetivamente abrangidos pelo ciclo. A distribuição do consumo entre esses períodos deve seguir a regra regulatória aplicável; não se deve presumir rateio proporcional sem validação.

### Tributos

Os tributos são modelados individualmente:

- **ICMS:** abrangência estadual.
- **PIS e COFINS:** abrangência federal.
- **CIP/COSIP:** abrangência municipal.

Bases de cálculo, hipóteses de incidência, fórmulas e arredondamentos dependem das normas aplicáveis. A implementação desses cálculos exige validação regulatória e tributária; não se presume que todos os tributos incidam sobre a mesma base.

### Fatura e pagamento

- A fatura pertence à unidade consumidora.
- A competência é representada pelo primeiro dia do mês.
- Deve existir no máximo uma fatura por unidade consumidora e competência.
- A origem do consumo é identificada como `LEITURA` ou `MANUAL`.
- O status inicial é `PENDENTE`; após o registro do pagamento, passa a `PAGO`.
- O pagamento será manual na primeira versão, sem integração com gateway.
- Não deve ser permitido registrar pagamento duplicado.
- Os snapshots da fatura preservam os valores usados no momento da emissão.

## 🧱 Modelo de domínio

### Relacionamentos principais

```text
Cliente 1 ───────── N UnidadeConsumidora
UnidadeConsumidora 1 ───────── N Medidor
Medidor 1 ───────── N Leitura
UnidadeConsumidora 1 ───────── N Fatura

Fatura 1 ───────── N FaturaBandeiraAplicada
Fatura 1 ───────── N FaturaTributo

FaturaBandeiraAplicada N ───── 1 AcionamentoBandeira
FaturaTributo N ────────────── 1 RegraTributaria

Usuario 0..1 ───────────────── 1 Cliente
```

### Entidades

| Entidade | Responsabilidade e principais conceitos |
| --- | --- |
| `Cliente` | Representa a pessoa física ou jurídica titular de unidades consumidoras; concentra dados pessoais e contato. |
| `UnidadeConsumidora` | Representa a instalação elétrica; concentra endereço, distribuidora, classe, subgrupo e modalidade tarifária. |
| `Medidor` | Representa o equipamento instalado na unidade, com número, tipo, ligação, situação ativa e datas de instalação/retirada. |
| `Leitura` | Registra valor acumulado, data/hora, tipo real ou estimado e medidor correspondente. |
| `Taxa` | Armazena TE e TUSD por distribuidora, subgrupo, classe, modalidade e vigência. |
| `BandeiraTarifaria` | Armazena o valor adicional por kWh de cada tipo de bandeira e sua vigência. |
| `AcionamentoBandeira` | Registra a bandeira oficialmente acionada e o período correspondente. |
| `Fatura` | Representa a cobrança da unidade consumidora, competência, consumo, snapshots tarifários, valores e status. |
| `FaturaBandeiraAplicada` | Registra os períodos e valores de bandeira considerados em uma fatura. |
| `RegraTributaria` | Define parâmetros de tributos por abrangência, localização, tipo de cálculo e vigência. |
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

## 📦 DTOs e mapeamento

Os DTOs definem os contratos de entrada, atualização e resposta da API, evitando expor diretamente entidades JPA. O projeto utiliza MapStruct para conversão entre DTOs e entidades.

Com a remodelagem do domínio, os contratos antigos precisam ser revistos para refletir os novos relacionamentos e conceitos:

| Domínio | Diretriz para os DTOs |
| --- | --- |
| Cliente | Dados cadastrais e de contato; sem endereço da instalação. |
| UnidadeConsumidora | Dados da instalação, endereço, distribuidora e enquadramento tarifário; recebe a referência do cliente. |
| Medidor | Dados do equipamento e vínculo com unidade consumidora; datas de instalação e retirada. |
| Leitura | Valor, data/hora, tipo de leitura e referência ao medidor. |
| Taxa | TE, TUSD, combinação tarifária e vigência. |
| Bandeiras | Cadastro de valores e registro dos acionamentos em contratos próprios. |
| Fatura | Competência, período, consumo, origem, valores e status; sem expor entidades relacionadas. |
| Tributos | Regras e valores aplicados representados separadamente. |
| Usuário | Dados de acesso e perfil; nunca devolver senha ou hash no DTO de resposta. |

Validações declarativas com Bean Validation complementam, mas não substituem, as validações de negócio nos services. Quando os controllers forem implementados, os parâmetros de entrada deverão acionar a validação com `@Valid`.

### Mappers

Os mappers devem permanecer responsáveis apenas pela conversão de dados. Resolução de relacionamentos, autorização, validações de existência, vigência e regras de cálculo pertencem à camada de aplicação/domínio. Campos controlados pelo sistema — como identificadores gerados, snapshots calculados e hash de senha — não devem ser aceitos indiscriminadamente por mapeamento automático.

## 🗄️ Repositórios

Os repositórios utilizam Spring Data JPA e devem oferecer consultas coerentes com o novo modelo. A remodelagem altera as chaves de pesquisa que existiam na estrutura anterior: por exemplo, faturas passam a ser consultadas por unidade consumidora e competência, e medidores por unidade e situação.

Consultas e verificações importantes previstas:

- cliente e suas unidades consumidoras;
- unidade por distribuidora e código;
- medidores de uma unidade e medidor ativo;
- leituras de um medidor ordenadas cronologicamente;
- tarifa vigente para distribuidora, subgrupo, classe, modalidade e data;
- detecção de sobreposição de vigências tarifárias;
- acionamentos de bandeira que cruzam determinado intervalo;
- regras tributárias vigentes conforme tipo, abrangência e localização;
- fatura por unidade consumidora e competência;
- faturas por unidade consumidora em ordem cronológica;
- usuário por username e associação única com cliente.

Os métodos concretos devem ser conferidos e atualizados durante a etapa de repositories; esta documentação não afirma que todas essas consultas já estejam implementadas.

## ⚙️ Services e regras de aplicação

Os services concentram validações, coordenação dos repositórios, transações e regras de negócio. A nova modelagem exige que os fluxos antigos sejam remodelados, e não apenas renomeados.

| Service/domínio | Responsabilidades esperadas |
| --- | --- |
| Cliente | Cadastro, consulta e desativação lógica, preservando vínculos históricos. |
| UnidadeConsumidora | Cadastro e atualização da instalação; validação do cliente e da combinação distribuidora/código. |
| Medidor | Instalação, consulta, atualização permitida e retirada/substituição, preservando histórico. |
| Leitura | Registro e consulta cronológica; validação de duplicidade, sequência e consistência dos valores. |
| Taxa | Cadastro, consulta por vigência, prevenção de sobreposição e criação de nova vigência sem apagar histórico. |
| BandeiraTarifaria / AcionamentoBandeira | Manutenção dos valores oficiais e dos períodos de acionamento, sem confundir cadastro com aplicação. |
| RegraTributaria | Manutenção de parâmetros por tributo, abrangência, localização e vigência. |
| Fatura | Obtenção do consumo, seleção de tarifa e regras aplicáveis, composição dos componentes, snapshots, geração e pagamento. |
| Usuário | Cadastro, unicidade de username, associação conforme perfil e operações de autenticação/autorização. |

### Pontos críticos de implementação

- Operações de geração de fatura devem ser transacionais e impedir duplicidade por unidade/competência.
- Regras de vigência devem considerar limites de período de maneira consistente.
- Substituição de medidor não pode apagar leituras ou faturas históricas.
- Leituras estimadas e situações de troca/reinicialização exigem tratamento explícito.
- Cálculos devem utilizar `BigDecimal` e snapshots persistidos.
- A autorização deve garantir que um usuário cliente só consulte recursos das próprias unidades.
- A senha deve ser armazenada com hash seguro via `PasswordEncoder` (por exemplo, BCrypt); comparação em texto puro não é aceitável.
- Cálculos tributários e rateio de bandeiras dependem de validação normativa antes de serem considerados definitivos.

## 🧰 Tecnologias e arquitetura

### Tecnologias

| Tecnologia | Utilização |
| --- | --- |
| Java 21 | Linguagem da aplicação |
| Spring Boot 4.1.1 | Configuração e execução |
| Spring Data JPA / Hibernate | Persistência e ORM |
| Maven | Build e gerenciamento de dependências |
| H2 | Banco em memória para desenvolvimento e testes |
| MySQL 8.4 | Banco previsto para produção |
| MapStruct 1.6.3 | Mapeamento entre DTOs e entidades |
| Lombok | Redução de código repetitivo |
| Bean Validation | Validação declarativa |
| JUnit 5, Mockito e AssertJ | Testes automatizados |
| JaCoCo 0.8.14 | Relatório de cobertura |
| Flyway | Estratégia definida para versionamento do schema |

### Organização lógica

```text
com.powermanager.powermanager
├── controller/   # Endpoints REST
├── dto/          # Contratos de entrada e saída
├── entity/       # Entidades JPA
├── enums/        # Enumerações do domínio
├── exception/    # Exceções e tratamento de erros
├── mapper/       # Conversão com MapStruct
├── repository/  # Consultas e persistência
└── services/    # Regras de negócio e transações
```

A estrutura acima representa a separação lógica de responsabilidades. Os pacotes e classes concretos devem ser alinhados ao estado atual do código durante a refatoração.

### Fluxo entre camadas

```text
Requisição HTTP
      │
      ▼
  Controller
      │
      ▼
    DTO ───────► Mapper
                   │
                   ▼
                Service ◄──── Repository
                   │               │
                   ▼               ▼
              DTO de resposta   Banco de dados
```

Controllers não devem conter regras de negócio, repositories não devem calcular faturas e entidades JPA não devem ser expostas diretamente como contratos HTTP.

## 🚀 Execução

### Pré-requisitos

- JDK 21;
- Git;
- Maven Wrapper incluído no projeto;
- Docker com Docker Compose para executar o MySQL localmente.

### Desenvolvimento com H2

O profile `dev` utiliza H2 em memória para desenvolvimento e testes. Os dados desse banco são temporários e podem ser recriados ao reiniciar a aplicação, conforme a configuração vigente.

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Se o console H2 estiver habilitado na configuração local, os parâmetros de acesso devem ser consultados no `application-dev.yml`.

### Banco MySQL

O profile `prod` está previsto para utilizar MySQL. Configure as variáveis de ambiente exigidas pelo `docker-compose.yml` e pelo arquivo `application-prod.yml`; não versione credenciais reais.

```bash
docker compose up -d
```

A execução da aplicação com o profile de produção depende de o schema estar previamente compatível com as entidades. A estratégia de migrations com Flyway deve ser concluída antes de considerar esse fluxo pronto para produção.

## 🧪 Testes e cobertura

O projeto utiliza JUnit 5, Mockito e AssertJ para testes automatizados, além de JaCoCo para relatório de cobertura.

A refatoração deve ampliar a cobertura para além dos testes unitários de services e mappers, incluindo:

- inicialização do contexto JPA;
- persistência e recuperação de entidades relacionadas em H2;
- constraints, índices e unicidades;
- regras de vigência tarifária e tributária;
- leituras cronológicas e troca de medidor;
- geração de faturas e preservação dos snapshots;
- aplicação de bandeiras em ciclos que cruzam períodos;
- autorização e isolamento dos dados por cliente.

Comando usual de verificação Maven:

```bash
./mvnw clean verify
```

No Windows:

```powershell
.\mvnw.cmd clean verify
```

Os percentuais de cobertura devem ser obtidos da execução atual do JaCoCo; não são publicados números sem uma execução correspondente.

## 📍 Estado atual e roadmap

O projeto está em uma refatoração estrutural do domínio. A lista abaixo separa decisões/modelagem consolidadas das etapas que ainda precisam ser implementadas e validadas.

### Diretrizes consolidadas

- [x] Separar Cliente de UnidadeConsumidora.
- [x] Centralizar endereço e enquadramento tarifário na UnidadeConsumidora.
- [x] Relacionar Medidor à UnidadeConsumidora e preservar seu histórico.
- [x] Modelar leituras reais e estimadas.
- [x] Separar TE e TUSD.
- [x] Separar cadastro de bandeira e acionamento oficial.
- [x] Modelar regras tributárias por abrangência e localização.
- [x] Relacionar Fatura à UnidadeConsumidora.
- [x] Preservar snapshots tarifários, de bandeira e tributários.
- [x] Definir perfis de usuário e regras de associação com Cliente.
- [x] Definir uso de BigDecimal e precisões decimais.

### Etapas de implementação

1. [ ] Finalizar validação de entidades e enums, incluindo persistência real no H2.
2. [ ] Remodelar DTOs.
3. [ ] Atualizar mappers.
4. [ ] Remodelar repositories e consultas.
5. [ ] Implementar services e regras transacionais.
6. [ ] Implementar controllers e endpoints REST.
7. [ ] Implementar Spring Security, hash de senhas e autorização por recurso.
8. [ ] Configurar Flyway e migrations versionadas.
9. [ ] Expandir testes unitários, integração e persistência.
10. [ ] Validar cálculos tarifários, bandeiras e tributos com as normas aplicáveis.
11. [ ] Realizar validação final de integridade, histórico e documentação da API.

## 📄 Documentação

- [Especificação consolidada de regras de negócio e arquitetura](docs/PowerManager-Especificacao-Consolidada.pdf) — documento de referência para as decisões do domínio, relacionamentos, restrições, pendências e roteiro da refatoração.

## 📜 Licença e autor

Este projeto está licenciado sob a [MIT License](LICENSE).

**Gustavo de Jesus Silva**  
GitHub: [@GustavoJ-dev](https://github.com/GustavoJ-dev)
