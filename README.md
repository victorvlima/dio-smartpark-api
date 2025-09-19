# 🚗 Projeto de Desenvolvimento de API de Estacionamento com Spring Boot

Este projeto visa a construção de uma API RESTful robusta para gerenciar um sistema de estacionamento de veículos.

## 🎯 Objetivos Principais

*   Gerenciar o fluxo de entrada e saída de veículos.
*   Calcular o custo do estacionamento com base no tempo de permanência.
*   Garantir a segurança da API com Spring Security.
*   Assegurar a qualidade do código através de testes unitários e de integração.
*   Disponibilizar a API na nuvem via AWS.

## 🛠️ Tecnologias e Ferramentas Envolvidas

*   **Linguagem de Programação:** Java
*   **Framework:** Spring Boot
*   **Segurança:** Spring Security
*   **Banco de Dados (Produção):** PostgreSQL
*   **Banco de Dados (Testes):** H2 (em memória)
*   **Controle de Versão:** GitHub
*   **Cloud Provider:** AWS (camada gratuita)

## 📈 Fases do Projeto e Requisitos Detalhados

### Fase 1: Configuração Inicial e Estrutura do Projeto

1.  **Criação do Projeto Spring Boot:**
    *   Gerar um projeto Spring Boot com as dependências essenciais: `Spring Web`, `Spring Data JPA`, `PostgreSQL Driver`, `H2 Database`, `Spring Security`.
2.  **Modelagem de Dados (Entidades):**
    *   Identificar as entidades chave:
        *   `Veiculo` (placa, modelo, cor, tipo de veículo, etc.).
        *   `Estacionamento` (registro de entrada e saída, vaga, valor cobrado).
        *   `Usuario` (para Spring Security - username, password, roles).
        *   `Vaga` (número da vaga, status - ocupada/livre).
    *   Definir os relacionamentos entre as entidades.
3.  **Configuração de Banco de Dados:**
    *   Configurar `application.properties` ou `application.yml` para PostgreSQL em ambiente de produção e H2 para testes.
    *   Utilizar `Flyway` ou `Liquibase` (opcional, mas recomendado para gerenciamento de schema) para migrações de banco de dados.

### Fase 2: Desenvolvimento das Funcionalidades Core (CRUD e Regras de Negócio)

1.  **Repositórios:**
    *   Criar interfaces `JpaRepository` para as entidades, permitindo operações CRUD básicas.
2.  **Serviços (Service Layer):**
    *   Implementar a lógica de negócios para:
        *   **Entrada de Veículos:** Registrar a entrada de um veículo, atribuir uma vaga (se disponível), registrar a hora de entrada.
        *   **Saída de Veículos:** Registrar a saída de um veículo, liberar a vaga, calcular o tempo de permanência.
        *   **Cálculo de Pagamento:** Desenvolver a lógica para calcular o valor a ser cobrado, considerando o tempo de permanência (e possíveis regras de tarifação, como primeira hora, horas adicionais, diária, etc.).
        *   Gerenciamento de vagas e veículos.
3.  **Controladores (REST Endpoints):**
    *   Expor endpoints RESTful para as operações CRUD e as funcionalidades de entrada/saída/pagamento.
    *   Exemplos:
        *   `POST /api/v1/estacionamento/entrar`
        *   `PUT /api/v1/estacionamento/sair/{placa}`
        *   `GET /api/v1/estacionamento/calcular/{placa}`
        *   `GET /api/v1/vagas`
        *   `POST /api/v1/veiculos`

### Fase 3: Segurança com Spring Security

1.  **Configuração de Segurança:**
    *   Integrar Spring Security para autenticação e autorização.
    *   Definir estratégias de autenticação (e.g., Basic Auth, JWT - este último sendo mais comum para APIs REST).
    *   Configurar regras de autorização para os endpoints (e.g., quais endpoints exigem autenticação, quais roles têm acesso a quais recursos).
2.  **Gerenciamento de Usuários:**
    *   Implementar um `UserDetailsService` customizado para carregar usuários e suas credenciais do banco de dados.

### Fase 4: Testes e Qualidade de Código

1.  **Testes Unitários:**
    *   Escrever testes unitários para a camada de serviços (Service Layer) e lógica de negócios, utilizando `JUnit` e `Mockito`.
2.  **Testes de Integração:**
    *   Escrever testes de integração para as camadas de repositório e controladores, utilizando `Spring Boot Test` e o banco de dados H2 em memória.
    *   Garantir que os endpoints da API funcionem conforme o esperado.
3.  **Relatórios de Cobertura de Testes:**
    *   Configurar uma ferramenta como `JaCoCo` para gerar relatórios de cobertura de código, visando uma alta porcentagem de cobertura.

### Fase 5: Documentação e Versionamento

1.  **Documentação da API:**
    *   Utilizar `Springdoc OpenAPI` ou `Swagger UI` para gerar automaticamente a documentação interativa da API.
    *   Incluir descrições para endpoints, parâmetros, modelos de requisição/resposta.
2.  **Versionamento com GitHub:**
    *   Criar um repositório no GitHub.
    *   Realizar commits frequentes e significativos, mantendo um histórico claro das mudanças.

### Fase 6: Deploy na AWS

1.  **Preparação para o Deploy:**
    *   Configurar um perfil de deploy Spring Boot (e.g., com configurações de DB para PostgreSQL).
    *   Empacotar a aplicação como um arquivo JAR ou WAR executável.
2.  **Deploy na Camada Gratuita da AWS:**
    *   **Opção 1 (Recomendado para início):** Utilizar AWS Elastic Beanstalk para simplificar o deploy de aplicações Spring Boot.
    *   **Opção 2 (Mais controle):** Provisionar uma instância EC2 (t2.micro) e instalar Java, PostgreSQL (ou usar RDS Free Tier) e a aplicação manualmente ou via Docker.
    *   Configurar Security Groups para liberar as portas necessárias (e.g., 80, 443, 8080).
    *   Configurar variáveis de ambiente para credenciais de banco de dados e outras configurações sensíveis.

## ✅ Critérios de Sucesso

*   Todos os endpoints da API funcionais e acessíveis (pós-deploy).
*   Lógica de segurança funcionando corretamente com autenticação e autorização.
*   Relatórios de testes com boa cobertura de código.
*   Repositório GitHub organizado e com histórico claro.
*   Documentação da API acessível e compreensível.

## 📦 Formato de Entrega

Você deverá entregar:

*   **Link do Repositório GitHub:** Contendo todo o código-fonte.
*   **URL da API em Produção:** Link para a API hospedada na AWS.
*   **Relatórios de Cobertura:** Gerados pela ferramenta de testes.
*   **Documentação da API:** Acessível via Swagger UI ou similar.