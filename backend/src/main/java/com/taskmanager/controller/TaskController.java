package com.taskmanager.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        try {
            logger.info("Tentando criar tarefa: {}", task.getTitle());

            // Obter o usuário autenticado do SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("Nenhum usuário autenticado encontrado para criar a tarefa.");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            logger.debug("Usuário autenticado: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));
            logger.debug("Usuário encontrado: {}", user.getUsername());

            task.setUser(user);
            task.setCreatedAt(LocalDateTime.now());

            Task savedTask = taskRepository.save(task);
            logger.info("Tarefa criada com sucesso: {}", savedTask.getId());
            return ResponseEntity.ok(savedTask);
        } catch (Exception e) {
            logger.error("Erro ao criar tarefa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Task>> getTasks() {
        try {
            logger.info("Obtendo todas as tarefas...");

            // Obter o usuário autenticado do SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("Nenhum usuário autenticado encontrado para obter as tarefas.");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            logger.debug("Usuário autenticado: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));
            logger.debug("Usuário encontrado: {}", user.getUsername());

            List<Task> tasks = taskRepository.findByUser(user);
            logger.info("Tarefas encontradas: {}", tasks.size());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Erro ao obter tarefas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
        try {
            logger.info("Tentando atualizar tarefa com ID: {}", id);

            // Obter o usuário autenticado do SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                logger.error("Nenhum usuário autenticado encontrado para atualizar a tarefa.");
                return ResponseEntity.status(401).build();
            }

            String username = authentication.getName();
            logger.debug("Usuário autenticado: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));
            logger.debug("Usuário encontrado: {}", user.getUsername());

            Task task = taskRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + id));
            logger.debug("Tarefa encontrada: {}", task.getTitle());

            // Verificar se a tarefa pertence ao usuário autenticado
            if (!task.getUser().getId().equals(user.getId())) {
                logger.error("Usuário {} não tem permissão para atualizar a tarefa {}", username, id);
                return ResponseEntity.status(403).build();
            }

            // Atualizar os campos da tarefa
            task.setTitle(taskDetails.getTitle());
            task.setDescription(taskDetails.getDescription());
            task.setStatus(taskDetails.getStatus());

            Task updatedTask = taskRepository.save(task);
            logger.info("Tarefa atualizada com sucesso: {}", updatedTask.getId());
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            logger.error("Erro ao atualizar tarefa: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        try {
            logger.info("Iniciando processo de deleção para tarefa com ID: {}", id);

            // Validar o ID
            if (id == null || id <= 0) {
                logger.error("ID da tarefa inválido: {}", id);
                return ResponseEntity.status(400).build();
            }

            // Obter o usuário autenticado do SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                logger.error("Contexto de autenticação é nulo.");
                return ResponseEntity.status(401).build();
            }
            if (!authentication.isAuthenticated()) {
                logger.error("Usuário não está autenticado.");
                return ResponseEntity.status(401).build();
            }
            String username = authentication.getName();
            if (username == null || username.trim().isEmpty()) {
                logger.error("Nome do usuário autenticado é nulo ou vazio.");
                return ResponseEntity.status(401).build();
            }
            logger.debug("Usuário autenticado: {}", username);

            // Buscar o usuário no banco de dados
            logger.debug("Buscando usuário com username: {}", username);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                logger.error("Usuário não encontrado no banco de dados: {}", username);
                return ResponseEntity.status(404).build();
            }
            logger.debug("Usuário encontrado: {}", user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de buscar a tarefa.");
            taskRepository.flush(); // Garante que qualquer alteração pendente seja sincronizada

            // Buscar a tarefa no banco de dados
            logger.debug("Buscando tarefa com ID: {}", id);
            Task task = taskRepository.findById(id).orElse(null);
            if (task == null) {
                logger.warn("Tarefa não encontrada com ID: {}", id);
                return ResponseEntity.status(404).build();
            }
            logger.debug("Tarefa encontrada: {}", task.getTitle());

            // Verificar se a tarefa pertence ao usuário autenticado
            User taskUser = task.getUser();
            if (taskUser == null) {
                logger.error("Tarefa com ID {} não possui um usuário associado.", id);
                return ResponseEntity.status(400).build();
            }

            logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                        taskUser.getId(), user.getId());
            if (!taskUser.getId().equals(user.getId())) {
                logger.error("Usuário {} não tem permissão para deletar a tarefa {}", username, id);
                return ResponseEntity.status(403).build();
            }

            // Deletar a tarefa
            logger.debug("Deletando tarefa com ID: {}", id);
            taskRepository.delete(task);
            taskRepository.flush(); // Garante que a deleção seja refletida no banco de dados
            logger.info("Tarefa deletada com sucesso: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Erro ao deletar tarefa com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        try {
            logger.info("Iniciando busca de tarefa com ID: {}", id);

            // Validar o ID
            if (id == null || id <= 0) {
                logger.error("ID da tarefa inválido: {}", id);
                return ResponseEntity.status(400).build();
            }

            // Obter o usuário autenticado do SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                logger.error("Contexto de autenticação é nulo.");
                return ResponseEntity.status(401).build();
            }
            if (!authentication.isAuthenticated()) {
                logger.error("Usuário não está autenticado.");
                return ResponseEntity.status(401).build();
            }
            String username = authentication.getName();
            if (username == null || username.trim().isEmpty()) {
                logger.error("Nome do usuário autenticado é nulo ou vazio.");
                return ResponseEntity.status(401).build();
            }
            logger.debug("Usuário autenticado: {}", username);

            // Buscar o usuário no banco de dados
            logger.debug("Buscando usuário com username: {}", username);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                logger.error("Usuário não encontrado no banco de dados: {}", username);
                return ResponseEntity.status(404).build();
            }
            logger.debug("Usuário encontrado: {}", user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de buscar a tarefa.");
            taskRepository.flush();

            // Buscar a tarefa no banco de dados
            logger.debug("Buscando tarefa com ID: {}", id);
            Task task = taskRepository.findById(id).orElse(null);
            if (task == null) {
                logger.warn("Tarefa não encontrada com ID: {}", id);
                return ResponseEntity.status(404).build();
            }
            logger.debug("Tarefa encontrada: {}", task.getTitle());

            // Verificar se a tarefa pertence ao usuário autenticado
            User taskUser = task.getUser();
            if (taskUser == null) {
                logger.error("Tarefa com ID {} não possui um usuário associado.", id);
                return ResponseEntity.status(400).build();
            }

            logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                        taskUser.getId(), user.getId());
            if (!taskUser.getId().equals(user.getId())) {
                logger.error("Usuário {} não tem permissão para acessar a tarefa {}", username, id);
                return ResponseEntity.status(403).build();
            }

            logger.info("Tarefa com ID {} retornada com sucesso para o usuário {}", id, username);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            logger.error("Erro ao buscar tarefa com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Task>> filterTasksByStatus(@RequestParam String status) {
        try {
            logger.info("Iniciando filtragem de tarefas por status: {}", status);

            // Validar o status
            if (status == null || status.trim().isEmpty()) {
                logger.error("Parâmetro 'status' é nulo ou vazio.");
                return ResponseEntity.status(400).build();
            }

            // Obter o usuário autenticado do SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                logger.error("Contexto de autenticação é nulo.");
                return ResponseEntity.status(401).build();
            }
            if (!authentication.isAuthenticated()) {
                logger.error("Usuário não está autenticado.");
                return ResponseEntity.status(401).build();
            }
            String username = authentication.getName();
            if (username == null || username.trim().isEmpty()) {
                logger.error("Nome do usuário autenticado é nulo ou vazio.");
                return ResponseEntity.status(401).build();
            }
            logger.debug("Usuário autenticado: {}", username);

            // Buscar o usuário no banco de dados
            logger.debug("Buscando usuário com username: {}", username);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                logger.error("Usuário não encontrado no banco de dados: {}", username);
                return ResponseEntity.status(404).build();
            }
            logger.debug("Usuário encontrado: {}", user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de buscar as tarefas.");
            taskRepository.flush();

            // Buscar as tarefas com o status especificado
            logger.debug("Buscando tarefas do usuário {} com status: {}", username, status);
            List<Task> tasks = taskRepository.findByUserAndStatus(user, status);
            logger.info("Tarefas encontradas com status {}: {}", status, tasks.size());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Erro ao filtrar tarefas por status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/bulk-update-status")
    public ResponseEntity<List<Task>> bulkUpdateStatus(@RequestBody BulkUpdateStatusRequest request) {
        try {
            logger.info("Iniciando atualização em massa de status para tarefas: IDs = {}, Novo Status = {}", request.getTaskIds(), request.getNewStatus());

            // Validar a requisição
            if (request.getTaskIds() == null || request.getTaskIds().isEmpty()) {
                logger.error("Lista de IDs de tarefas é nula ou vazia.");
                return ResponseEntity.status(400).build();
            }
            if (request.getNewStatus() == null || request.getNewStatus().trim().isEmpty()) {
                logger.error("Novo status é nulo ou vazio.");
                return ResponseEntity.status(400).build();
            }

            // Obter o usuário autenticado do SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                logger.error("Contexto de autenticação é nulo.");
                return ResponseEntity.status(401).build();
            }
            if (!authentication.isAuthenticated()) {
                logger.error("Usuário não está autenticado.");
                return ResponseEntity.status(401).build();
            }
            String username = authentication.getName();
            if (username == null || username.trim().isEmpty()) {
                logger.error("Nome do usuário autenticado é nulo ou vazio.");
                return ResponseEntity.status(401).build();
            }
            logger.debug("Usuário autenticado: {}", username);

            // Buscar o usuário no banco de dados
            logger.debug("Buscando usuário com username: {}", username);
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                logger.error("Usuário não encontrado no banco de dados: {}", username);
                return ResponseEntity.status(404).build();
            }
            logger.debug("Usuário encontrado: {}", user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de buscar as tarefas.");
            taskRepository.flush();

            // Buscar as tarefas pelos IDs fornecidos
            logger.debug("Buscando tarefas com IDs: {}", request.getTaskIds());
            List<Task> tasks = taskRepository.findAllById(request.getTaskIds());
            if (tasks.isEmpty()) {
                logger.warn("Nenhuma tarefa encontrada para os IDs fornecidos: {}", request.getTaskIds());
                return ResponseEntity.status(404).build();
            }

            // Verificar se todas as tarefas pertencem ao usuário autenticado e atualizá-las
            for (Task task : tasks) {
                User taskUser = task.getUser();
                if (taskUser == null) {
                    logger.error("Tarefa com ID {} não possui um usuário associado.", task.getId());
                    return ResponseEntity.status(400).build();
                }
                logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                            taskUser.getId(), user.getId());
                if (!taskUser.getId().equals(user.getId())) {
                    logger.error("Usuário {} não tem permissão para atualizar a tarefa {}", username, task.getId());
                    return ResponseEntity.status(403).build();
                }
                task.setStatus(request.getNewStatus());
            }

            // Salvar as tarefas atualizadas
            logger.debug("Salvando tarefas atualizadas: {}", tasks.size());
            List<Task> updatedTasks = taskRepository.saveAll(tasks);
            taskRepository.flush();
            logger.info("Tarefas atualizadas com sucesso: {}", updatedTasks.size());
            return ResponseEntity.ok(updatedTasks);
        } catch (Exception e) {
            logger.error("Erro ao atualizar status em massa para tarefas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}

// Classe auxiliar para representar a requisição de atualização em massa
class BulkUpdateStatusRequest {
    private List<Long> taskIds;
    private String newStatus;

    // Getters e Setters
    public List<Long> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}