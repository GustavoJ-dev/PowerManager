# ⚡ PowerManager API

> API REST para gerenciamento do ciclo de faturamento de energia elétrica.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📌 Sobre o projeto

O **PowerManager API** é um backend desenvolvido em Java e Spring Boot para organizar o gerenciamento de clientes, medidores de energia, tarifas, faturas e usuários em um sistema de faturamento de energia elétrica.

O projeto está em desenvolvimento. Atualmente, possui entidades JPA, DTOs de entrada e saída, mappers com MapStruct, repositórios Spring Data JPA, services com regras de negócio, exceções específicas e testes automatizados para services, repositories e mappers. **Os controllers e endpoints HTTP ainda não foram implementados.**

## 🎯 Funcionalidades do domínio

- **Clientes:** cadastro, consulta, listagem e exclusão de consumidores.
- **Medidores:** cadastro associado a cliente existente, consulta por identificador ou número, listagem, atualização e exclusão.
- **Tarifas:** cadastro, consulta por vigência, validação de períodos e manutenção de histórico tarifário.
- **Faturas:** cálculo, geração, consulta, histórico por medidor e registro de pagamento.
- **Usuários:** cadastro, consulta, atualização de dados básicos e autenticação provisória; respostas não expõem o campo de senha.
- **DTOs e mapeamento:** objetos específicos para cadastro, atualização e resposta, com conversão por MapStruct.
- **Validação:** DTOs utilizam Bean Validation para validar campos obrigatórios, formatos, limites de tamanho e valores numéricos.
- **Regras de negócio:** prevenção de duplicidades, validações de dados e exceções específicas para situações inválidas.

### Regras de negócio implementadas

- Cada medidor está associado a um cliente, e o relacionamento está mapeado como um-para-um.
- O número do medidor é único e cada cliente pode possuir apenas um medidor associado.
- Um medidor pode ter várias faturas, mas existe uma restrição para impedir mais de uma fatura para a mesma competência.
- O consumo e os componentes monetários da tarifa/fatura não podem ser negativos.
- Uma fatura recém-gerada começa com status `PENDENTE`; o pagamento altera o status para `PAGO`.
- A tarifa possui início e fim de vigência. O serviço verifica sobreposição de períodos e preserva o registro anterior ao atualizar a tarifa.
- Cada usuário possui `username` único. Usuários do tipo `CLIENTE` precisam estar associados a um cliente; usuários `ADMINISTRADOR` não precisam desse vínculo.
- A fatura guarda uma cópia dos componentes tarifários aplicados no momento da geração, para que alterações futuras na tarifa não modifiquem os valores registrados na fatura existente.

### Fluxo de faturamento

```text
Cliente → Medidor → Consumo + Tarifa vigente → Cálculo → Fatura → Pagamento
```

O cálculo utiliza a seguinte fórmula:

```text
(consumo × custo por unidade)
+ aluguel do medidor
+ taxa de serviço
+ imposto de serviço
+ CESS
+ taxa fixa
```

O resultado é arredondado para duas casas decimais usando `RoundingMode.HALF_UP`.

## 🧱 Modelo de domínio: entidades

As entidades ficam no pacote `com.powermanager.powermanager.entity`. Elas representam os dados persistidos e seus relacionamentos; as regras de operação ficam principalmente nos services.

### `Cliente`

Representa o consumidor de energia.

**Campos principais**
- `id`: identificador UUID gerado pela aplicação/persistência.
- `nome`, `endereco`, `cidade`, `estado`: dados de identificação e localização.
- `email`, `telefone`: informações de contato.

**Relacionamentos e restrições**
- Mapeada para a tabela `cliente`.
- Os campos de cadastro são obrigatórios e possuem limites de tamanho definidos nas colunas.
- O vínculo com o medidor é representado pelo campo `cliente` da entidade `Medidor`.

### `Medidor`

Representa o medidor associado a um cliente.

**Campos principais**
- `id`: identificador UUID.
- `numero`: número único do medidor.
- `localizacao`, `tipo`, `codigoFase`, `tipoFatura`: características e configuração do medidor.
- `dias`: quantidade de dias considerada para o período de faturamento.
- `cliente`: cliente associado.

**Relacionamentos e restrições**
- O relacionamento com `Cliente` é `@OneToOne`.
- A coluna `cliente_id` é obrigatória e única, assim como o número do medidor.

### `Taxa`

Representa os componentes tarifários aplicáveis ao faturamento durante um intervalo de vigência.

**Componentes monetários**
- `custoPorUnidade`
- `aluguelMedidor`
- `taxaServico`
- `impostoServico`
- `cess`
- `taxaFixa`

**Vigência e restrições**
- `inicioVigencia`: início obrigatório da vigência.
- `fimVigencia`: fim da vigência; quando nulo, a taxa pode continuar vigente.
- Os valores monetários são obrigatórios e possuem restrições no banco para impedir valores negativos.
- A validação de intervalos e de sobreposição é realizada também por `TaxaService`.

### `Fatura`

Representa a cobrança de um medidor para determinada competência.

**Campos principais**
- `id`: identificador UUID.
- `competencia`: data que identifica o período faturado.
- `unidadesConsumidas`: consumo registrado.
- `valorTotal`: total calculado.
- `statusFatura`: estado da fatura, representado pelo enum `StatusFatura`.
- `medidor`: medidor ao qual a cobrança pertence.

**Snapshot tarifário**
A fatura armazena os componentes aplicados na emissão: `custoPorUnidadeAplicado`, `aluguelMedidorAplicado`, `taxaServicoAplicada`, `impostoServicoAplicado`, `cessAplicado` e `taxaFixaAplicada`. Isso preserva os valores utilizados no cálculo mesmo que a tarifa seja alterada depois.

**Relacionamentos e restrições**
- O relacionamento com `Medidor` é `@ManyToOne`.
- A combinação `medidor_id + competencia` é única.
- O banco restringe o status aos valores `PENDENTE` e `PAGO` e impede consumo, total ou componentes monetários negativos.

### `Usuario`

Representa uma conta de acesso ao sistema.

**Campos principais**
- `id`: identificador UUID.
- `username`: nome de usuário único.
- `nome`: nome de exibição.
- `passwordHash`: campo destinado ao hash da senha.
- `tipoUsuario`: enum `TipoUsuario`, com os tipos `ADMINISTRADOR` e `CLIENTE`.
- `cliente`: associação opcional com `Cliente`, carregada de forma lazy.

**Regras de associação**
- O `username` é único.
- Usuários `CLIENTE` precisam estar vinculados a um cliente existente.
- Usuários `ADMINISTRADOR` são armazenados sem vínculo com cliente.

> **Nota de segurança:** o método de autenticação atual compara o valor recebido com o campo armazenado e ainda não utiliza `PasswordEncoder` ou Spring Security. A autenticação é provisória e precisa ser aprimorada antes de qualquer uso em produção.

## 📦 DTOs e mapeamento

Os DTOs ficam em `com.powermanager.powermanager.dto` e definem os dados aceitos e devolvidos pelos services, evitando expor diretamente as entidades JPA como contrato de entrada e saída. São implementados como `record`; os DTOs de entrada utilizam anotações do Bean Validation.

| Domínio | DTOs | Responsabilidade |
| --- | --- | --- |
| Cliente | `ClienteCadastroDTO`, `ClienteResponseDTO` | Receber dados cadastrais e devolver os dados do cliente com identificador. |
| Medidor | `MedidorCadastroDTO`, `MedidorAtualizacaoDTO`, `MedidorResponseDTO` | Separar cadastro, atualização e resposta; o cadastro recebe `clienteId`. |
| Taxa | `TaxaCadastroDTO`, `TaxaAtualizacaoDTO`, `TaxaResponseDTO` | Representar componentes monetários e período de vigência. |
| Fatura | `FaturaResponseDTO` | Expor cobrança, snapshot tarifário, status e `medidorId`. |
| Usuário | `UsuarioCadastroDTO`, `UsuarioAtualizacaoDTO`, `UsuarioResponseDTO` | Separar entrada e resposta; o DTO de resposta não contém senha ou hash. |

### Validações declarativas

- Campos textuais obrigatórios utilizam `@NotBlank` e limites de tamanho com `@Size`.
- E-mail de cliente utiliza `@Email`.
- Campos obrigatórios e identificadores utilizam `@NotNull`.
- Dias do medidor utilizam `@Positive`.
- Componentes monetários da taxa utilizam `@DecimalMin` e `@Digits`, impedindo valores negativos e limitando a precisão a 8 dígitos inteiros e 2 decimais.

Os DTOs definem validações declarativas; os services continuam responsáveis por regras de negócio, como existência de relacionamentos, unicidade e consistência de períodos.

### Mappers

Os mappers ficam em `com.powermanager.powermanager.mapper` e são interfaces anotadas com `@Mapper(componentModel = "spring")`, permitindo injeção nos services.

| Mapper | Responsabilidade |
| --- | --- |
| `ClienteMapper` | Cadastro DTO para entidade e entidade para resposta. |
| `MedidorMapper` | Cadastro, resposta e atualização de entidade existente; converte `cliente.id` em `clienteId`. |
| `TaxaMapper` | Cadastro, resposta e atualização dos campos tarifários, preservando identificador e fim da vigência. |
| `FaturaMapper` | Entidade para resposta, convertendo `medidor.id` em `medidorId`. |
| `UsuarioMapper` | Cadastro, resposta e atualização dos campos permitidos, sem expor senha/hash. |

Os mappers ignoram campos controlados pelo sistema ou protegidos durante a conversão, como identificadores gerados, relacionamentos resolvidos pelo service, senha/hash e tipo de usuário na atualização. A senha é atribuída pelo fluxo de cadastro do service e não é incluída no DTO de resposta.

## 🗄️ Repositórios

Os repositórios ficam em `com.powermanager.powermanager.repository` e estendem `JpaRepository`. Isso disponibiliza operações padrão como `save`, `findAll`, `findById` e `deleteById`, além dos métodos derivados ou consultas customizadas listados abaixo.

| Repositório | Métodos específicos | Finalidade |
| --- | --- | --- |
| `ClienteRepo` | Nenhum método adicional | Utiliza as operações padrão de `JpaRepository<Cliente, UUID>`. |
| `MedidorRepo` | `findByNumero(String numero)` | Busca um medidor pelo número e retorna `Optional<Medidor>`. |
| `MedidorRepo` | `existsByClienteId(UUID clienteId)` | Verifica se o cliente já possui medidor associado. |
| `TaxaRepo` | `buscarTaxaVigenteEm(LocalDateTime data)` | Consulta a taxa cuja vigência inclui a data informada. |
| `TaxaRepo` | `buscarTaxasComSobreposicao(LocalDateTime inicio, LocalDateTime fim)` | Lista taxas com períodos que se sobrepõem ao intervalo informado. |
| `FaturaRepo` | `findByMedidor(Medidor medidor)` | Lista faturas associadas a um medidor. |
| `FaturaRepo` | `findByMedidorOrderByCompetenciaDesc(Medidor medidor)` | Lista as faturas de um medidor por competência decrescente. |
| `FaturaRepo` | `findByMedidorAndCompetencia(Medidor medidor, LocalDate competencia)` | Busca a fatura de um medidor em uma competência específica. |
| `UsuarioRepo` | `findByUsername(String username)` | Busca um usuário pelo username e retorna `Optional<Usuario>`. |

### Detalhes das consultas de tarifa

- `buscarTaxaVigenteEm`: considera `inicioVigencia <= data` e `fimVigencia > data`; se `fimVigencia` for nulo, a taxa permanece vigente.
- `buscarTaxasComSobreposicao`: encontra registros cujo período se cruza com o intervalo informado. O service utiliza esse resultado para rejeitar vigências sobrepostas.

## ⚙️ Services e métodos

Os services ficam em `com.powermanager.powermanager.services`. Eles coordenam os repositórios, aplicam regras de negócio e lançam exceções específicas quando uma operação não pode ser concluída.

### `ClienteService`

Gerencia operações de cadastro e consulta de clientes.

| Método | Descrição |
| --- | --- |
| `cadastrarCliente(Cliente cliente)` | Persiste um novo cliente. |
| `listarClientes()` | Retorna todos os clientes cadastrados. |
| `buscarPorId(UUID id)` | Busca um cliente pelo UUID; lança `ClienteNotFoundException` se não existir. |
| `excluir(UUID id)` | Verifica a existência do cliente e solicita sua exclusão; lança `ClienteNotFoundException` quando não encontrado. |

### `MedidorService`

Gerencia o cadastro, a consulta e a atualização dos dados de um medidor.

| Método | Descrição |
| --- | --- |
| `cadastrarMedidor(Medidor medidor)` | Persiste um medidor. |
| `buscarPorNumero(String numero)` | Busca pelo número único; lança `MedidorNotFoundException` se não encontrar. |
| `buscarPorId(UUID id)` | Busca pelo UUID; lança `MedidorNotFoundException` se não encontrar. |
| `listarMedidores()` | Lista todos os medidores como DTOs de resposta. |
| `atualizarMedidor(UUID id, MedidorAtualizacaoDTO dados)` | Atualiza os campos permitidos, verifica conflito de número e retorna o DTO atualizado. |
| `excluirMedidor(UUID medidorId)` | Verifica a existência do medidor e solicita sua exclusão. |

### `TaxaService`

Centraliza a validação de valores e períodos tarifários e mantém o histórico das taxas.

| Método | Descrição |
| --- | --- |
| `cadastrarNovaTaxa(TaxaCadastroDTO taxa)` | Valida os componentes monetários e a vigência. Se o início não for informado, utiliza a data/hora atual; rejeita períodos inválidos ou sobrepostos. |
| `obterTaxaVigente()` | Retorna a taxa vigente no momento da chamada. |
| `obterTaxaNaData(LocalDateTime data)` | Retorna a taxa vigente na data indicada; valida data nula e lança `TaxaNotFoundException` se não houver taxa vigente. |
| `listarHistorico()` | Retorna os registros de taxa armazenados. |
| `atualizarTaxa(TaxaAtualizacaoDTO novaTaxa)` | Encerra a vigência da taxa atual e cria um novo registro, preservando o histórico; exige que a nova vigência comece depois do início da taxa atual. |

O service também contém validações internas para garantir que a taxa e os valores monetários não sejam nulos ou negativos, que o fim seja posterior ao início e que os períodos não se sobreponham. As operações de escrita são transacionais.

### `FaturaService`

Coordena o cálculo, a emissão, a consulta e a mudança de status das faturas.

| Método | Descrição |
| --- | --- |
| `calcularFatura(BigDecimal unidadesConsumidas)` | Calcula o valor com a taxa vigente, sem persistir uma fatura; rejeita consumo nulo ou negativo. |
| `gerarFatura(UUID medidorId, LocalDate competencia, BigDecimal unidadesConsumidas)` | Valida os dados, localiza o medidor, impede duplicidade por medidor/competência, obtém a taxa vigente, calcula o total e salva a fatura como `PENDENTE` com snapshot tarifário. |
| `listarFaturas()` | Retorna todas as faturas. |
| `listarPorMedidor(UUID id)` | Retorna as faturas de um medidor. |
| `buscaPorMedidorECompetencia(UUID id, LocalDate competencia)` | Busca a fatura de um medidor para a competência informada e retorna `FaturaResponseDTO`; lança `FaturaNotFoundException` se não existir. |
| `listarHistoricoPorMedidor(UUID id)` | Retorna as faturas do medidor por competência decrescente. |
| `obterDetalhesDaFatura(UUID id)` | Busca uma fatura pelo UUID; lança `FaturaNotFoundException` se não existir. |
| `pagarFatura(UUID id)` | Altera o status para `PAGO`; lança `FaturaAlreadyPaidException` se a fatura já estiver paga. |

A geração valida a presença do identificador do medidor e da competência. A prevenção de duplicidade também lança `FaturaAlreadyExistsException`; dados inválidos geram `FaturaValidationException`.

### `UsuarioService`

Gerencia contas de usuário, valida unicidade do username e aplica as regras de associação com clientes.

| Método | Descrição |
| --- | --- |
| `cadastrarUsuario(UsuarioCadastroDTO usuario)` | Valida os campos obrigatórios, verifica a disponibilidade do username e aplica a regra de associação conforme o tipo de usuário. |
| `listarUsuarios()` | Retorna todos os usuários cadastrados. |
| `buscarPorId(UUID id)` | Busca pelo UUID; lança `UsuarioNotFoundException` se não existir. |
| `buscaPorUsername(String username)` | Busca pelo username; lança `UsuarioNotFoundException` se não existir. |
| `atualizarUsuario(UUID id, UsuarioAtualizacaoDTO dados)` | Atualiza username e nome, verificando se o novo username já pertence a outro usuário. |
| `autenticar(String username, String passwordHash)` | Procura o usuário e compara o valor recebido com o campo armazenado; retorna erro genérico quando usuário/senha não correspondem. A implementação é provisória, conforme a nota de segurança acima. |

O cadastro exige username, nome, senha/hash e tipo. Se o username já estiver em uso, o service lança `UsernameAlreadyExistsException`. Para usuários do tipo `CLIENTE`, a associação é validada e resolvida para um cliente existente; para `ADMINISTRADOR`, o vínculo com cliente é removido.

## 🧰 Tecnologias

| Tecnologia | Utilização |
| --- | --- |
| Java 21 | Linguagem da aplicação |
| Spring Boot 4.1.1 | Configuração e execução |
| Spring Data JPA | Persistência e acesso a dados |
| Hibernate | Mapeamento objeto-relacional |
| Bean Validation | Validação declarativa dos campos dos DTOs |
| Maven Wrapper | Build e execução |
| H2 | Banco em memória no ambiente de desenvolvimento |
| MySQL 8.4 | Banco configurado para o profile de produção |
| Docker Compose | Inicialização local do MySQL |
| JUnit 5 e Mockito | Testes unitários |
| JaCoCo 0.8.14 | Relatório de cobertura |
| MapStruct 1.6.3 | Geração de mapeamentos entre DTOs e entidades |
| Lombok | Redução de código repetitivo |

## 🏗️ Arquitetura

O código está organizado por responsabilidade dentro de `com.powermanager.powermanager`:

- `entity/`: entidades JPA e enums do domínio.
- `repository/`: interfaces de acesso a dados com Spring Data JPA.
- `dto/`: records de entrada, atualização e resposta, com validações declarativas.
- `mapper/`: interfaces MapStruct para conversão entre DTOs e entidades.
- `services/`: regras de negócio do domínio, recebendo e retornando DTOs.
- `exception/`: exceções específicas do domínio.
- `src/test/`: testes automatizados, incluindo testes unitários dos services.

A camada de controllers e os endpoints HTTP ainda não foram implementados; por isso, não são listadas rotas REST nesta documentação.

## 🚀 Pré-requisitos

- JDK 21;
- Git.

O projeto inclui Maven Wrapper, então não é necessário instalar o Maven separadamente. Para executar o profile de produção localmente, também será necessário Docker com Docker Compose e as configurações do MySQL.

## ▶️ Executando em desenvolvimento

1. Clone o repositório:

   ```bash
   git clone https://github.com/GustavoJ-dev/PowerManager.git
   cd PowerManager
   ```

2. Execute a aplicação com o profile `dev`.

   **Linux/macOS:**

   ```bash
   ./mvnw spring-boot:run
   ```

   **Windows (PowerShell ou CMD):**

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

O profile `dev` utiliza o banco H2 em memória. A configuração atual recria as tabelas ao iniciar a aplicação; os dados não são mantidos depois que ela é encerrada.

### Console H2

Com a aplicação em execução, acesse:

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:powermanager`
- Usuário: `sa`
- Senha: deixe em branco.

Use esses dados apenas no ambiente local de desenvolvimento.

## 🐬 Executando com MySQL

O profile `prod` está configurado para MySQL. Configure as variáveis utilizadas pelo `docker-compose.yml` e pelo `application-prod.yml`.

Crie um arquivo `.env` na raiz do projeto, sem enviá-lo ao Git:

```dotenv
MYSQL_DATABASE=powermanager
MYSQL_USER=powermanager
MYSQL_PASSWORD=defina_uma_senha_local
MYSQL_PORT=3306
```

Inicie o banco:

```bash
docker compose up -d
```

Depois, inicie a aplicação com o profile `prod`.

**Linux/macOS:**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

**Windows:**

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

O profile de produção usa `ddl-auto: validate`, portanto espera que o esquema do banco já esteja compatível com as entidades. Não utilize credenciais de exemplo em ambientes reais.

Para parar o container do banco:

```bash
docker compose down
```

O volume `powermanager-mysql-data` mantém os dados entre reinicializações do container. Para removê-lo junto com os dados, execute explicitamente `docker compose down -v`.

## 🧪 Testes e cobertura de código

Há testes automatizados para os cinco services, repositories e mappers, utilizando JUnit 5, Mockito e AssertJ. Os testes dos services foram adaptados para simular os mappers e validar os fluxos de conversão entre DTOs e entidades.

### Relatório JaCoCo

O JaCoCo está configurado para gerar o relatório durante a fase `verify`. Como os services e os testes foram refatorados para utilizar DTOs e MapStruct, consulte o relatório da execução atual para obter os percentuais atualizados. Os números de execuções anteriores não devem ser interpretados como cobertura atual ou como cobertura global do projeto.

Execute os testes:

**Linux/macOS:**

```bash
./mvnw clean verify
```

**Windows:**

```powershell
.\mvnw.cmd clean verify
```

O relatório HTML do JaCoCo fica em:

```text
target/site/jacoco/index.html
```

## 📍 Estado atual e próximos passos

O projeto continua em desenvolvimento.

### Implementado

- [x] Modelagem das entidades e relacionamentos.
- [x] Configuração dos profiles de desenvolvimento (H2) e produção (MySQL).
- [x] Repositórios com Spring Data JPA.
- [x] Camada de services e regras de negócio.
- [x] DTOs de cadastro, atualização e resposta com Bean Validation.
- [x] Mappers MapStruct para conversão entre DTOs e entidades.
- [x] Exceções específicas do domínio.
- [x] Testes automatizados de services, repositories e mappers.

### Próximas etapas

- [ ] Implementar controllers e endpoints REST.
- [ ] Padronizar respostas HTTP e tratamento global de erros.
- [ ] Completar validações de entrada e integração com a camada web.
- [ ] Implementar autenticação e autorização seguras.
- [ ] Documentar endpoints com exemplos de requisição e resposta.
- [ ] Expandir testes de integração e revisar a cobertura global.

## 📄 Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

## 👤 Autor

**Gustavo de Jesus Silva**  
GitHub: [@GustavoJ-dev](https://github.com/GustavoJ-dev)
