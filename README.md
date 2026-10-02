# ⚡ PowerManager API

> API REST para gerenciamento de clientes, medidores, tarifas e faturamento de energia elétrica.

---

## Sobre o projeto

O **PowerManager API** é uma API REST desenvolvida para centralizar o gerenciamento do processo de faturamento de energia elétrica.

A aplicação permite administrar clientes, medidores, tarifas, faturas e usuários, além de realizar o cálculo e o controle das cobranças geradas a partir do consumo de energia.

O projeto é baseado em um sistema de faturamento de energia elétrica já existente, mantendo suas principais funcionalidades e regras de negócio, adaptadas para uma API REST.

---

## Funcionalidades

### 👤 Clientes

Gerenciamento dos consumidores cadastrados no sistema.

Cada cliente possui:

- Nome;
- Endereço;
- Cidade;
- Estado;
- E-mail;
- Telefone.

---

### ⚡ Medidores

Gerenciamento dos medidores associados aos clientes.

Cada medidor possui:

- Número do medidor;
- Localização;
- Tipo;
- Código de fase;
- Tipo de faturamento;
- Quantidade de dias.

Cada cliente possui um único medidor associado.

---

### 💰 Tarifas

Gerenciamento dos valores utilizados no cálculo das faturas.

O sistema contempla:

- Custo por unidade consumida;
- Aluguel do medidor;
- Taxa de serviço;
- Imposto de serviço;
- Cess;
- Taxa fixa.

Esses valores são utilizados na composição do valor final da fatura.

---

### 🧾 Faturas

Gerenciamento das cobranças de energia elétrica.

Cada fatura possui:

- Competência;
- Unidades consumidas;
- Valor total;
- Status;
- Medidor relacionado.

Uma mesma competência não pode possuir mais de uma fatura para o mesmo medidor.

#### Status

| Status | Descrição |
| --- | --- |
| `PENDING` | Fatura pendente |
| `PAID` | Fatura paga |

---

### 🧮 Cálculo do faturamento

O valor da fatura é determinado a partir das unidades consumidas e dos componentes tarifários cadastrados.

O processo considera:

**Unidades consumidas → Custo por unidade → Componentes tarifários → Valor total da fatura**

Os componentes tarifários considerados são:

- Custo por unidade;
- Aluguel do medidor;
- Taxa de serviço;
- Imposto de serviço;
- Cess;
- Taxa fixa.

---

### 💳 Controle de pagamento

As faturas possuem controle de situação para identificar cobranças pendentes e pagas.

O fluxo de pagamento é:

**PENDING → PAID**

---

### 🔐 Usuários

Gerenciamento dos usuários do sistema.

Existem dois tipos de usuário:

| Tipo | Descrição |
| --- | --- |
| `ADMIN` | Usuário administrativo |
| `CUSTOMER` | Usuário vinculado a um cliente |

Usuários `CUSTOMER` podem estar associados a um cliente cadastrado.

Cada usuário possui um nome de usuário único.

---

## Fluxo do sistema

O processo principal da aplicação segue o fluxo:

**Cliente → Medidor → Consumo → Tarifas → Cálculo → Fatura → Pagamento**

O cliente possui um medidor associado, o consumo é utilizado no cálculo da cobrança e as tarifas determinam os componentes que formam o valor final da fatura.

Após sua geração, a fatura permanece com seu status controlado pelo sistema, permitindo identificar se está pendente ou paga.

---

## Regras de negócio

### Clientes e medidores

- Cada cliente possui um único medidor;
- Cada medidor pertence a um cliente;
- O número do medidor deve ser único;
- Um cliente não pode possuir mais de um medidor associado.

### Faturas

- Cada fatura pertence a um medidor;
- Um medidor pode possuir várias faturas;
- Um medidor não pode possuir duas faturas para a mesma competência;
- As unidades consumidas não podem ser negativas;
- O valor total da fatura não pode ser negativo;
- O status da fatura deve ser válido.

### Tarifas

- Os valores das tarifas não podem ser negativos;
- As tarifas são utilizadas na composição do valor da fatura.

### Usuários

- Cada usuário possui um nome de usuário único;
- O usuário deve possuir um tipo válido;
- Usuários `CUSTOMER` podem estar vinculados a um cliente;
- Usuários `ADMIN` podem existir sem vínculo com um cliente.

---

## Modelo do domínio

| Entidade | Responsabilidade |
| --- | --- |
| `Cliente` | Cadastro dos consumidores |
| `Medidor` | Identificação e configuração do medidor |
| `Fatura` | Registro e controle das cobranças |
| `Taxa` | Valores utilizados no cálculo do faturamento |
| `Usuario` | Usuários e seus respectivos tipos de acesso |

### Relacionamentos

- **Cliente 1 : 1 Medidor**
- **Medidor 1 : N Fatura**
- **Cliente 1 : N Usuario**
- **Taxa** participa do processo de cálculo das faturas.

---

## Estado do projeto

**Em desenvolvimento.**

### Concluído

- [x] Definição do domínio;
- [x] Modelagem das entidades;
- [x] Definição dos relacionamentos;
- [x] Definição das regras de negócio;
- [x] Estrutura inicial do banco de dados;
- [x] Configuração do ambiente de desenvolvimento;
- [x] Configuração da persistência;
- [x] Definição do processo de faturamento.

### Em desenvolvimento

- [ ] Implementação das entidades;
- [ ] Implementação da persistência;
- [ ] Implementação das regras de negócio;
- [ ] Implementação dos recursos REST;
- [ ] Implementação das validações;
- [ ] Implementação do tratamento de erros;
- [ ] Implementação da autenticação e autorização;
- [ ] Implementação dos testes;
- [ ] Documentação da API.

---

## Roadmap

A evolução da PowerManager API seguirá a implementação dos recursos definidos para o domínio:

- [ ] Gerenciamento completo de clientes;
- [ ] Gerenciamento completo de medidores;
- [ ] Gerenciamento de tarifas;
- [ ] Geração de faturas;
- [ ] Cálculo das faturas;
- [ ] Controle de pagamentos;
- [ ] Gerenciamento de usuários;
- [ ] Autenticação;
- [ ] Autorização;
- [ ] Consultas e filtros;
- [ ] Paginação;
- [ ] Documentação dos endpoints.

---

## Visão geral

A PowerManager API centraliza o ciclo de faturamento de energia elétrica, conectando o gerenciamento de clientes, medidores, consumo, tarifas, faturas e usuários em um único sistema.

O fluxo principal pode ser resumido como:

**Cliente → Medidor → Consumo → Faturamento → Fatura → Pagamento**

---

---

## Autor

**Gustavo de Jesus Silva**