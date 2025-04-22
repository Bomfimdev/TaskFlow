# 🗂️ TaskFlow - Sistema de Gerenciamento de Tarefas e Projetos

**TaskFlow** é uma aplicação web fullstack que permite a criação, gerenciamento e colaboração em tarefas e projetos. Usuários podem organizar suas atividades em quadros no estilo Kanban, interagir com outros membros e acompanhar o progresso das tarefas com notificações simples.

---

## 🚀 Funcionalidades Principais

### 🔐 Autenticação de Usuários
- Cadastro e login com validação via JWT.
- Recuperação de senha (opcional).

### ✅ Gerenciamento de Tarefas
- Criar, editar, excluir e visualizar tarefas.
- Atribuir tarefas a usuários.
- Definir prazos e categorias (To Do, In Progress, Done).

### 👥 Colaboração
- Convidar usuários para um projeto/quadro.
- Comentar em tarefas para facilitar a comunicação.

### 📊 Dashboard
- Visão geral de tarefas com filtros (pendentes, concluídas, por prazo).

### 🔔 Notificações Simples
- Alertas visuais no frontend para prazos e atribuições.

---

## 🧰 Tecnologias Utilizadas

### Frontend
- **ReactJS**: UI dinâmica e responsiva.
- **Tailwind CSS** / CSS3 / HTML5: Estilização moderna.

### Backend
- **Java + Spring Boot**: API RESTful robusta.
- **Spring Data JPA + Hibernate**: ORM e persistência.

### Banco de Dados
- **PostgreSQL**: Banco principal.
- **H2** (opcional): Para testes e dev local.

### DevOps & Ferramentas Complementares
- **Git**: Controle de versão.
- **Maven**: Gerenciamento de dependências.
- **Postman**: Teste de endpoints.
- **Docker**: Containerização do backend e banco de dados.
- **Jenkins** / **GitHub Actions** (opcional): CI/CD.
- **SonarQube** (opcional): Análise de qualidade de código.

---

## 🧱 Estrutura do Projeto

### 📦 Backend
- **Entidades**: `User`, `Task`, `Project`, `Comment`.
- **Endpoints REST**:
  - `/auth/register` e `/auth/login`: Autenticação.
  - `/tasks`: CRUD de tarefas.
  - `/projects`: Gerenciamento de projetos.
  - `/users/invite`: Convite de colaboradores.
- **Segurança**: Spring Security + JWT.
- **Migrations**: Flyway ou Liquibase.

### 🎨 Frontend
- **Páginas**:
  - Login
  - Cadastro
  - Dashboard
  - Quadro de Tarefas (Kanban)
- **Componentes**:
  - `TaskCard`
  - `ProjectBoard`
  - `CommentSection`
- **Integração com API**: Axios ou Fetch.

### 🗃️ Banco de Dados
- Tabelas: `users`, `tasks`, `projects`, `comments`, `project_users` (relacionamento N:N).

---

## 🎯 Por Que Esse Projeto?

- **Relevância**: Sistemas de tarefas são úteis e reconhecíveis.
- **Fullstack**: Abrange frontend, backend e banco de dados.
- **Escalável**: Pode ser expandido com recursos em tempo real, notificações por e-mail, etc.
- **Boas Práticas**: Segue padrões modernos de desenvolvimento, segurança, testes e DevOps.
- **Organização Ágil**: Ideal para aplicar e demonstrar Scrum ou Kanban.

---

## 💼 Dicas para o Portfólio

- **README Detalhado** (este 😉)
- **Instruções de Deploy**:
  - Backend: Railway / Heroku (Docker).
  - Frontend: Vercel / Netlify.
- **Capturas de Tela**: Inclua imagens da interface.
- **Testes**:
  - Backend: JUnit.
  - Frontend: Jest + React Testing Library.
- **Diferencial**:
  - Pipeline CI/CD com Jenkins ou GitHub Actions.
  - SonarQube para análise estática de código.

---

## 🗓️ Estimativa de Tempo

| Etapa                            | Tempo Estimado     |
|----------------------------------|---------------------|
| Planejamento & Design            | 1-2 dias            |
| Setup do Projeto (Backend + Frontend) | 1 dia          |
| Desenvolvimento Backend          | 3-4 dias            |
| Desenvolvimento Frontend         | 3-4 dias            |
| Testes & Ajustes                 | 2 dias              |
| Documentação & Deploy            | 1-2 dias            |
| **Total**                        | **~2-3 semanas**    |

---

## 📂 Como Rodar o Projeto

### Pré-requisitos
- Node.js
- Java 17+
- Docker (opcional)
- PostgreSQL (caso não use Docker)

### Backend
```bash
cd backend
./mvnw spring-boot:run
