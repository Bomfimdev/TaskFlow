# 📂 TicTaref — Sistema de Gerenciamento de Tarefas e Projetos

**TicTaref** é uma aplicação web fullstack para criar, gerenciar e colaborar em tarefas e projetos de forma eficiente. Organize suas atividades com quadros no estilo **Kanban**, colabore com outros usuários e acompanhe o progresso com alertas visuais intuitivos.

---

## 🚀 Funcionalidades

### 🔐 Autenticação de Usuários

* Cadastro e login com validação via **JWT** ✅
* Recuperação de senha *(em planejamento)*

### ✅ Gerenciamento de Tarefas

* Criar, editar, excluir e visualizar tarefas ✅
* Atribuir tarefas a usuários ✅
* Definir prazos e status (To Do, In Progress, Done) ✅
* Arquivar e desarquivar tarefas ✅
* Adicionar e remover tags em tarefas ✅
* Filtrar tarefas por status, tag e data de vencimento ✅

### 👥 Colaboração

* Convidar usuários para projetos e quadros *(em planejamento)*
* Comentar nas tarefas para facilitar a comunicação *(em planejamento)*

### 📊 Dashboard

* Visão geral de tarefas com filtros: pendentes, concluídas, por prazo *(em planejamento)*

### 🔔 Notificações Simples

* Alertas visuais para prazos e novas atribuições *(em planejamento)*

---

## 🧰 Tecnologias Utilizadas

### Frontend *(em planejamento)*

* **ReactJS** + **Tailwind CSS**
* HTML5 / CSS3
* Integração via **Axios** ou **Fetch**

### Backend

* **Java 21** + **Spring Boot**
* **Spring Data JPA** + **Hibernate**
* **Spring Security** + **JWT** para autenticação
* **Jackson** para serialização/desserialização de JSON

### Banco de Dados

* **PostgreSQL** (produção)
* **H2** *(para testes locais, em planejamento)*

### Ferramentas

* **Git** & **GitHub** para controle de versão
* **Maven** para gerenciamento de dependências
* **Postman** para testes manuais
* **Docker** *(em planejamento)*
* **CI/CD** com **Jenkins** ou **GitHub Actions** *(em planejamento)*
* Análise de código com **SonarQube** *(em planejamento)*

---

## 🧱 Estrutura do Projeto

### 📦 Backend

* **Entidades**:

  * `User`: Representa usuários com autenticação.
  * `Task`: Tarefas com título, descrição, status, prazo, etc.
  * `Tag`: Tags associadas a tarefas.
  * `Project` e `Comment` *(em planejamento)*.
* **Endpoints Implementados**:

  * `POST /api/auth/login`: Autenticação via JWT.
  * `POST /api/tasks`: Cria uma nova tarefa.
  * `GET /api/tasks`: Lista todas as tarefas.
  * `PUT /api/tasks/{id}`: Atualiza uma tarefa.
  * `DELETE /api/tasks/{id}`: Deleta uma tarefa.
  * `POST /api/tasks/{id}/archive`: Arquiva uma tarefa.
  * `POST /api/tasks/{id}/unarchive`: Desarquiva uma tarefa.
  * `POST /api/tasks/{id}/tags`: Adiciona uma tag a uma tarefa.
  * `DELETE /api/tasks/{id}/tags/{tagId}`: Remove uma tag de uma tarefa.
  * `GET /api/tasks/filter?status={status}`: Filtra tarefas por status.
  * `GET /api/tasks/filter-by-tag?tag={tagName}`: Filtra tarefas por tag.
  * `GET /api/tasks/filter-by-due-date?startDate={start}&endDate={end}`: Filtra tarefas por data de vencimento.
  * `GET /api/tags`: Lista todas as tags.
* **Segurança**: Spring Security + JWT.
* **Migrations**: Flyway ou Liquibase *(em planejamento)*.

### 🎨 Frontend *(em planejamento)*

* **Páginas**:

  * Login / Cadastro
  * Dashboard
  * Quadro Kanban
* **Componentes**:

  * `TaskCard`, `ProjectBoard`, `CommentSection`

### 💃 Banco de Dados Relacional

* **Tabelas**: `users`, `tasks`, `tags`, `task_tags`, `projects`, `comments`, `project_users` *(algumas em planejamento)*.

---

## 🎯 Objetivos do Projeto

* **Relevância**: Criar uma aplicação prática para gerenciamento de tarefas.
* **Fullstack Real**: Desenvolver backend e frontend integrados.
* **Escalabilidade**: Estrutura pronta para adicionar novas funcionalidades.
* **Boas Práticas**: Código limpo, seguro e testável.
* **Metodologia Ágil**: Aplicar conceitos de Scrum e Kanban.

---

## 🧪 Testes *(em planejamento)*

* **Backend**: JUnit
* **Frontend**: Jest + React Testing Library

---

## 📸 Capturas de Tela

> \[EM CONSTRUÇÃO]

---

## 🛠️ Como Rodar o Projeto Localmente

### Pré-requisitos

* Java 21+
* PostgreSQL (rodando na porta 5433 com banco `task_manager` criado)
* Node.js *(para o frontend, em planejamento)*

### Backend

1. **Configuração**:

   * Atualize as credenciais do banco em `backend/src/main/resources/application.properties`:

     ```properties
     spring.datasource.url=jdbc:postgresql://localhost:5433/task_manager
     spring.datasource.username= //admin
     spring.datasource.password= //admin
     spring.jpa.hibernate.ddl-auto=update
     jwt.secret= //Sua chave secreta aqui
     jwt.expiration=7776000
     ```
2. **Executar:**

```powershell
cd C:\Users\Bomfim\Documents\GitHub\TaskFlow\backend
mvn clean install
mvn spring-boot:run
```

O servidor estará rodando em [http://localhost:8080](http://localhost:8080).

### Frontend *(em planejamento)*

```bash
cd frontend
npm install
npm run dev
```

---

## 🚀 Progresso Atual

**Backend:** Todas as funcionalidades principais de tarefas e tags foram implementadas e testadas:

* Autenticação JWT.
* CRUD de tarefas, arquivamento, gerenciamento de tags e filtros.
* Listagem de tags.

**Problemas Resolvidos:**

* Erro 403 em endpoints protegidos, ajustando o SecurityConfig.
* Serialização cíclica entre Task e Tag usando `@JsonManagedReference` e `@JsonBackReference`.

**Próximos Passos:**

* Reintroduzir verificação de roles no SecurityConfig (em andamento).
* Desenvolver o frontend para integração.
* Implementar testes automatizados.
* Planejar deploy (Railway, Vercel, etc.).

---

## 📬 Contato

Se quiser trocar uma ideia ou dar sugestões:

* 📧 **Email**: [Gbomfimprofissional@gmail.com](mailto:Gbomfimprofissional@gmail.com)
* 💼 **LinkedIn**: \[[Seu perfil aqui](https://www.linkedin.com/in/gabriel-bomfim-oliveira/)]
* 📁 **Portfólio**: \[[Link para seu portfólio](https://github.com/Bomfimdev)]

