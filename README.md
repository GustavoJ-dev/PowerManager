# ⚡ PowerManager API

> API REST para gerenciamento do ciclo de faturamento de energia elétrica.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📌 Sobre o projeto

O **PowerManager API** é um projeto backend desenvolvido em Java com Spring Boot para organizar o gerenciamento de clientes, medidores, tarifas, faturas e usuários em um sistema de faturamento de energia elétrica.

A aplicação está em desenvolvimento. Nesta etapa, o projeto possui o domínio modelado, persistência com Spring Data JPA, repositórios, camada de serviços, exceções de domínio e testes unitários dos services. A camada de endpoints REST ainda será desenvolvida.

## 🎯 Funcionalidades do domínio

- **Clientes:** cadastro e consulta dos consumidores.
- **Medidores:** identificação e configuração dos medidores associados aos clientes.
- **Tarifas:** armazenamento dos componentes usados no cálculo do faturamento.
- **Faturas:** registro das cobranças, competência, consumo, valor e status.
- **Usuários:** gerenciamento de usuários dos tipos `ADMIN` e `CUSTOMER`.
- **Regras de negócio:** validações e tratamento de situações inválidas nas camadas de serviço.

### Regras de negócio principais

- Cada cliente possui um único medidor associado.
- O número do medidor deve ser único.
- Um medidor pode possuir várias faturas, mas não mais de uma fatura por competência.
- O consumo e os valores monetários não podem ser negativos.
- Os status de fatura previstos são `PENDING` e `PAID`.
- Cada usuário possui um nome de usuário único.
- Usuários do tipo `CUSTOMER` podem ser vinculados a um cliente; usuários `ADMIN` podem existir sem esse vínculo.

### Fluxo de faturamento

```text
Cliente → Medidor → Consumo → Tarifas → Cálculo → Fatura → Pagamento
```

O valor da fatura considera o consumo e os componentes tarifários cadastrados, como custo por unidade, aluguel do medidor, taxas e impostos.

## 🧰 Tecnologias

| Tecnologia | Utilização |
| --- | --- |
| Java 21 | Linguagem da aplicação |
| Spring Boot 4.1.1 | Configuração e execução da aplicação |
| Spring Data JPA | Persistência e acesso a dados |
| Hibernate | Mapeamento objeto-relacional |
| Bean Validation | Validação de dados |
| Maven Wrapper | Build e execução do projeto |
| H2 | Banco em memória para desenvolvimento |
| MySQL 8.4 | Banco configurado para o ambiente de produção |
| Docker Compose | Inicialização do MySQL local |
| JUnit / Spring Boot Test | Testes automatizados |
| JaCoCo | Relatório de cobertura de testes |
| Lombok | Redução de código repetitivo |

## 🏗️ Arquitetura

O código está organizado por responsabilidade dentro do pacote `com.powermanager.powermanager`:

- `entity/`: entidades JPA e enums do domínio.
- `repository/`: interfaces de persistência com Spring Data JPA.
- `services/`: regras de negócio e operações das entidades.
- `exception/`: exceções específicas do domínio.
- `src/test/`: testes automatizados, incluindo testes unitários dos services.

A camada de controllers e os endpoints HTTP ainda não estão implementados. Por isso, esta versão do README não lista rotas REST que ainda não existem no código.

## 🚀 Pré-requisitos

Para executar a aplicação no ambiente de desenvolvimento:

- JDK 21;
- Git.

O projeto inclui Maven Wrapper, portanto não é necessário instalar o Maven separadamente. Para usar o profile de produção localmente, também será necessário Docker com Docker Compose e as configurações do MySQL descritas abaixo.

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

O profile `dev` utiliza o banco H2 em memória. A configuração atual recria as tabelas ao iniciar a aplicação; os dados não são persistidos depois que ela é encerrada.

### Console H2

Com a aplicação em execução, acesse:

- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:powermanager`
- Usuário: `sa`
- Senha: deixe em branco.

Use esses dados apenas no ambiente local de desenvolvimento, conforme a configuração atual.

## 🐬 Executando com MySQL

O profile `prod` está configurado para MySQL. Para executá-lo localmente, primeiro configure as variáveis de ambiente usadas pelo `docker-compose.yml` e pelo `application-prod.yml`.

Crie um arquivo `.env` na raiz do projeto, sem enviá-lo ao Git, com os valores do seu ambiente:

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

O volume `powermanager-mysql-data` mantém os dados do MySQL entre reinicializações do container. Para removê-lo junto com os dados, seria necessário executar explicitamente `docker compose down -v`.

## 🧪 Testes

Execute a suíte de testes com o Maven Wrapper:

**Linux/macOS:**

```bash
./mvnw test
```

**Windows:**

```powershell
.\mvnw.cmd test
```

Para executar também a etapa de verificação e gerar o relatório de cobertura do JaCoCo:

```bash
./mvnw verify
```

No Windows, use `.\mvnw.cmd verify`.

Após a execução bem-sucedida, o relatório HTML do JaCoCo fica em:

```text
target/site/jacoco/index.html
```

Os testes dos services cobrem cenários de sucesso e de validação/regras de negócio. A cobertura deve ser consultada no relatório gerado; este README não declara um percentual fixo.

## 📍 Estado atual e próximos passos

O projeto continua em desenvolvimento.

### Implementado

- [x] Modelagem das entidades e relacionamentos.
- [x] Configuração dos profiles de desenvolvimento (H2) e produção (MySQL).
- [x] Repositórios com Spring Data JPA.
- [x] Camada de services e regras de negócio iniciais.
- [x] Exceções específicas do domínio.
- [x] Testes unitários dos services.

### Próximas etapas

- [ ] Implementar controllers e endpoints REST.
- [ ] Padronizar respostas HTTP e tratamento global de erros.
- [ ] Completar validações de entrada e integração com a camada web.
- [ ] Implementar autenticação e autorização.
- [ ] Documentar os endpoints com exemplos de requisição e resposta.
- [ ] Expandir testes de integração e revisar a cobertura.

## 📄 Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

## 👤 Autor

**Gustavo de Jesus Silva**  
GitHub: [@GustavoJ-dev](https://github.com/GustavoJ-dev)
