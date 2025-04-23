# 🗂️ TaskFlow — Sistema de Gerenciamento de Tarefas e Projetos

**TaskFlow** é uma aplicação web fullstack que permite criar, gerenciar e colaborar em tarefas e projetos de forma simples e eficiente. Organize suas atividades com quadros no estilo **Kanban**, colabore com outros usuários e acompanhe o progresso com alertas visuais intuitivos.

---

## 🚀 Funcionalidades Principais

### 🔐 Autenticação de Usuários
- Cadastro e login com validação via **JWT**
- Recuperação de senha *(opcional)*

### ✅ Gerenciamento de Tarefas
- Criar, editar, excluir e visualizar tarefas
- Atribuir tarefas a usuários
- Definir prazos e status (To Do, In Progress, Done)

### 👥 Colaboração
- Convidar usuários para projetos e quadros
- Comentar nas tarefas para facilitar a comunicação

### 📊 Dashboard
- Visão geral de tarefas com filtros: pendentes, concluídas, por prazo

### 🔔 Notificações Simples
- Alertas visuais para prazos e novas atribuições

---

## 🧰 Tecnologias Utilizadas

### Frontend
- **ReactJS** + **Tailwind CSS**
- HTML5 / CSS3

### Backend
- **Java + Spring Boot**
- **Spring Data JPA** + **Hibernate**

### Banco de Dados
- **PostgreSQL**
- **H2** *(para testes locais)*

### DevOps & Ferramentas
- Git & GitHub
- Maven
- Docker
- Postman
- CI/CD com **Jenkins** ou **GitHub Actions** *(opcional)*
- Análise de código com **SonarQube** *(opcional)*

---

## 🧱 Estrutura do Projeto

### 📦 Backend
- **Entidades**: `User`, `Task`, `Project`, `Comment`
- **Endpoints**:
  - `/auth/register` | `/auth/login`
  - `/tasks` → CRUD de tarefas
  - `/projects` → Gerenciamento de projetos
  - `/users/invite` → Convite de colaboradores
- **Segurança**: Spring Security + JWT
- **Migrations**: Flyway ou Liquibase

### 🎨 Frontend
- **Páginas**:
  - Login / Cadastro
  - Dashboard
  - Quadro Kanban
- **Componentes**:
  - `TaskCard`, `ProjectBoard`, `CommentSection`
- Integração via **Axios** ou **Fetch**

### 🗃️ Banco de Dados Relacional
- Tabelas: `users`, `tasks`, `projects`, `comments`, `project_users`

---

## 🎯 Por Que Esse Projeto?

- 📌 **Relevância**: Apps de tarefas são úteis e fáceis de entender
- 🔁 **Fullstack real**: Frontend + Backend + Banco + CI/CD
- 📦 **Escalável**: Ideal para adicionar novas features
- 🧪 **Boas práticas**: Código limpo, seguro e testável
- 🛠️ **Agilidade**: Aplica conceitos de Scrum e Kanban

---

## 🧪 Testes

- Backend: **JUnit**
- Frontend: **Jest** + **React Testing Library**

---

## 📸 Capturas de Tela

> [EM CONSTRUÇÃO!]

---

## 🛠️ Instruções de Deploy

- Backend: [Railway](https://railway.app) / Heroku (com Docker)
- Frontend: [Vercel](https://vercel.com) / Netlify

---

## 🗓️ Estimativa de Tempo

| Etapa                            | Tempo Estimado     |
|----------------------------------|---------------------|
| Planejamento & Design            | 1–2 dias            |
| Setup do Projeto                 | 1 dia               |
| Desenvolvimento Backend          | 3–4 dias            |
| Desenvolvimento Frontend         | 3–4 dias            |
| Testes & Ajustes                 | 2 dias              |
| Documentação & Deploy            | 1–2 dias            |
| **Total**                        | **~2–3 semanas**    |

---

## 📂 Como Rodar o Projeto Localmente

### Pré-requisitos
- Node.js
- Java 17+
- PostgreSQL
- Docker (opcional)

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

---

## 📬 Contato

Se quiser trocar uma ideia ou dar sugestões:

📧 Gbomfimprofissional@gmail.com
💼 [LinkedIn](https://www.linkedin.com/in/gabriel-bomfim-oliveira/)  
📁 [Portfólio](https://github.com/Bomfimdev)
