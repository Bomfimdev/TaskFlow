package com.taskmanager.controller;

import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Tag;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TagRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import com.taskmanager.service.TaskService;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TagRepository tagRepository;

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(defaultValue = "false") boolean includeArchived,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        logger.info("Obtendo todas as tarefas... Include archived: {}, Sort by: {}, Order: {}", includeArchived, sortBy, sortOrder);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<Task> tasks = taskService.getAllTasks(username, includeArchived, sortBy, sortOrder);
        logger.info("Tarefas encontradas: {}", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskDTO taskDTO) {
        logger.info("Recebendo requisição para criar tarefa: {}", taskDTO);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Tentando criar tarefa: {}", taskDTO.getTitle());
        Task createdTask = taskService.createTask(taskDTO, username);
        logger.info("Tarefa criada com sucesso: {}", createdTask.getId());
        return ResponseEntity.ok(createdTask);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) {
        logger.info("Recebendo requisição para atualizar tarefa com ID: {}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        logger.info("Tentando atualizar tarefa: {}", taskDTO.getTitle());
        Task updatedTask = taskService.updateTask(id, taskDTO, username);
        logger.info("Tarefa atualizada com sucesso: {}", updatedTask.getId());
        return ResponseEntity.ok(updatedTask);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Task>> filterTasksByStatus(
            @RequestParam String status,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        try {
            logger.info("Iniciando filtragem de tarefas por status: {}, Include archived: {}, Sort by: {}, Order: {}", status, includeArchived, sortBy, order);

            // Validar o status
            if (status == null || status.trim().isEmpty()) {
                logger.error("Parâmetro 'status' é nulo ou vazio.");
                return ResponseEntity.status(400).build();
            }
            // Validar status válidos
            if (!status.equals("Pendente") && !status.equals("Em Andamento") && !status.equals("Concluída")) {
                logger.error("O status deve ser 'Pendente', 'Em Andamento' ou 'Concluída'.");
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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de buscar as tarefas.");
            taskRepository.flush();

            // Definir a ordenação
            Sort sort = Sort.by(order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);

            // Buscar as tarefas com o status especificado e com base no parâmetro includeArchived
            logger.debug("Buscando tarefas do usuário {} com status: {} e archived: {}", username, status, includeArchived);
            List<Task> tasks = taskRepository.findByUserAndStatusAndArchived(user, status, includeArchived, sort);
            logger.info("Tarefas encontradas com status {}: {}", status, tasks.size());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Erro ao filtrar tarefas por status {}: {}", status, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/filter-by-tag")
    public ResponseEntity<List<Task>> filterTasksByTag(
            @RequestParam String tag,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        try {
            logger.info("Iniciando filtragem de tarefas por tag: {}, Include archived: {}, Sort by: {}, Order: {}", tag, includeArchived, sortBy, order);

            // Validar a tag
            if (tag == null || tag.trim().isEmpty()) {
                logger.error("Parâmetro 'tag' é nulo ou vazio.");
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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de buscar as tarefas.");
            taskRepository.flush();

            // Definir a ordenação
            Sort sort = Sort.by(order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);

            // Buscar as tarefas com a tag especificada e com base no parâmetro includeArchived
            logger.debug("Buscando tarefas do usuário {} com tag: {} e archived: {}", username, tag, includeArchived);
            List<Task> tasks = taskRepository.findByUserAndTagsNameAndArchived(user, tag, includeArchived, sort);
            logger.info("Tarefas encontradas com tag {}: {}", tag, tasks.size());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Erro ao filtrar tarefas por tag {}: {}", tag, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/filter-by-due-date")
    public ResponseEntity<List<Task>> filterTasksByDueDate(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        try {
            logger.info("Iniciando filtragem de tarefas por intervalo de dueDate: startDate={}, endDate={}, Include archived: {}, Sort by: {}, Order: {}", 
                        startDate, endDate, includeArchived, sortBy, order);

            // Validar os parâmetros startDate e endDate
            if (startDate == null || startDate.trim().isEmpty()) {
                logger.error("Parâmetro 'startDate' é nulo ou vazio.");
                return ResponseEntity.status(400).build();
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                logger.error("Parâmetro 'endDate' é nulo ou vazio.");
                return ResponseEntity.status(400).build();
            }

            // Converter os parâmetros para LocalDateTime
            LocalDateTime start;
            LocalDateTime end;
            try {
                start = LocalDateTime.parse(startDate);
                end = LocalDateTime.parse(endDate);
            } catch (Exception e) {
                logger.error("Formato inválido para startDate ou endDate. Use o formato ISO (ex.: 2025-05-15T10:00:00). Erro: {}", e.getMessage());
                return ResponseEntity.status(400).build();
            }

            // Validar que startDate é anterior a endDate
            if (start.isAfter(end)) {
                logger.error("startDate deve ser anterior a endDate.");
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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de buscar as tarefas.");
            taskRepository.flush();

            // Definir a ordenação
            Sort sort = Sort.by(order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);

            // Buscar as tarefas com dueDate no intervalo especificado e com base no parâmetro includeArchived
            logger.debug("Buscando tarefas do usuário {} com dueDate entre {} e {}, e archived: {}", username, startDate, endDate, includeArchived);
            List<Task> tasks = taskRepository.findByUserAndDueDateBetweenAndArchived(user, start, end, includeArchived, sort);
            logger.info("Tarefas encontradas com dueDate entre {} e {}: {}", startDate, endDate, tasks.size());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Erro ao filtrar tarefas por intervalo de dueDate: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Task>> getOverdueTasks(
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived,
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        try {
            logger.info("Iniciando busca de tarefas atrasadas... Include archived: {}, Sort by: {}, Order: {}", includeArchived, sortBy, order);

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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de buscar as tarefas.");
            taskRepository.flush();

            // Definir a ordenação
            Sort sort = Sort.by(order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);

            // Buscar tarefas atrasadas (dueDate antes de agora, status diferente de "Concluída" e com base no parâmetro includeArchived)
            LocalDateTime now = LocalDateTime.now();
            logger.debug("Buscando tarefas do usuário {} com dueDate antes de {} e status diferente de 'Concluída', e archived: {}", username, now, includeArchived);
            List<Task> tasks = taskRepository.findByUserAndDueDateBeforeAndStatusNotAndArchived(user, now, "Concluída", includeArchived, sort);
            logger.info("Tarefas atrasadas encontradas: {}", tasks.size());
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            logger.error("Erro ao buscar tarefas atrasadas: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/overdue/count")
    public ResponseEntity<Long> getOverdueTasksCount(
            @RequestParam(required = false, defaultValue = "false") boolean includeArchived) {
        try {
            logger.info("Iniciando contagem de tarefas atrasadas... Include archived: {}", includeArchived);

            // Obter o usuário autenticado do SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            logger.debug("Autenticação recebida: {}", authentication);
            if (authentication == null) {
                logger.error("Contexto de autenticação é nulo para o endpoint GET /api/tasks/overdue/count.");
                return ResponseEntity.status(401).build();
            }
            if (!authentication.isAuthenticated()) {
                logger.error("Usuário não está autenticado para o endpoint GET /api/tasks/overdue/count.");
                return ResponseEntity.status(401).build();
            }
            String username = authentication.getName();
            logger.debug("Nome do usuário extraído da autenticação: {}", username);
            if (username == null || username.trim().isEmpty()) {
                logger.error("Nome do usuário autenticado é nulo ou vazio para o endpoint GET /api/tasks/overdue/count.");
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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

            // Forçar a sincronização com o banco de dados
            logger.debug("Forçando sincronização com o banco de dados antes de contar as tarefas.");
            taskRepository.flush();

            // Contar tarefas atrasadas (dueDate antes de agora, status diferente de "Concluída" e com base no parâmetro includeArchived)
            LocalDateTime now = LocalDateTime.now();
            logger.debug("Contando tarefas do usuário {} com dueDate antes de {} e status diferente de 'Concluída', e archived: {}", username, now, includeArchived);
            long count = taskRepository.countByUserAndDueDateBeforeAndStatusNotAndArchived(user, now, "Concluída", includeArchived);
            logger.info("Número de tarefas atrasadas encontradas: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("Erro ao contar tarefas atrasadas: {}", e.getMessage(), e);
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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

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
            logger.debug("Tarefa encontrada: ID = {}, Title = {}", task.getId(), task.getTitle());

            // Forçar o carregamento do usuário associado à tarefa
            Hibernate.initialize(task.getUser());
            User taskUser = task.getUser();
            if (taskUser == null) {
                logger.error("Tarefa com ID {} não possui um usuário associado.", id);
                return ResponseEntity.status(400).build();
            }

            logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                        taskUser.getId(), user.getId());
            if (!taskUser.getId().equals(user.getId())) {
                logger.error("Usuário {} (ID: {}) não tem permissão para acessar a tarefa {} (user_id: {})", username, user.getId(), id, taskUser.getId());
                return ResponseEntity.status(403).build();
            }

            logger.info("Tarefa com ID {} retornada com sucesso para o usuário {}", id, username);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            logger.error("Erro ao buscar tarefa com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        logger.info("Recebendo requisição para excluir tarefa com ID: {}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        taskService.deleteTask(id, username);
        logger.info("Tarefa excluída com sucesso: {}", id);
        return ResponseEntity.noContent().build();
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
            // Validar status válidos
            if (!request.getNewStatus().equals("Pendente") && !request.getNewStatus().equals("Em Andamento") && !request.getNewStatus().equals("Concluída")) {
                logger.error("O status deve ser 'Pendente', 'Em Andamento' ou 'Concluída'.");
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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

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
                Hibernate.initialize(task.getUser());
                User taskUser = task.getUser();
                if (taskUser == null) {
                    logger.error("Tarefa com ID {} não possui um usuário associado.", task.getId());
                    return ResponseEntity.status(400).build();
                }
                logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                            taskUser.getId(), user.getId());
                if (!taskUser.getId().equals(user.getId())) {
                    logger.error("Usuário {} (ID: {}) não tem permissão para atualizar a tarefa {} (user_id: {})", username, user.getId(), task.getId(), taskUser.getId());
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

    @PostMapping("/{id}/archive")
    public ResponseEntity<Task> archiveTask(@PathVariable Long id) {
        try {
            logger.info("Iniciando processo de arquivamento para tarefa com ID: {}", id);

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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

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
            logger.debug("Tarefa encontrada: ID = {}, Title = {}", task.getId(), task.getTitle());

            // Forçar o carregamento do usuário associado à tarefa
            Hibernate.initialize(task.getUser());
            User taskUser = task.getUser();
            if (taskUser == null) {
                logger.error("Tarefa com ID {} não possui um usuário associado.", id);
                return ResponseEntity.status(400).build();
            }

            logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                        taskUser.getId(), user.getId());
            if (!taskUser.getId().equals(user.getId())) {
                logger.error("Usuário {} (ID: {}) não tem permissão para arquivar a tarefa {} (user_id: {})", username, user.getId(), id, taskUser.getId());
                return ResponseEntity.status(403).build();
            }

            // Arquivar a tarefa
            task.setArchived(true);
            Task updatedTask = taskRepository.save(task);
            taskRepository.flush();
            logger.info("Tarefa com ID {} arquivada com sucesso.", id);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            logger.error("Erro ao arquivar tarefa com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{id}/unarchive")
    public ResponseEntity<Task> unarchiveTask(@PathVariable Long id) {
        try {
            logger.info("Iniciando processo de desarquivamento para tarefa com ID: {}", id);

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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

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
            logger.debug("Tarefa encontrada: ID = {}, Title = {}", task.getId(), task.getTitle());

            // Forçar o carregamento do usuário associado à tarefa
            Hibernate.initialize(task.getUser());
            User taskUser = task.getUser();
            if (taskUser == null) {
                logger.error("Tarefa com ID {} não possui um usuário associado.", id);
                return ResponseEntity.status(400).build();
            }

            logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                        taskUser.getId(), user.getId());
            if (!taskUser.getId().equals(user.getId())) {
                logger.error("Usuário {} (ID: {}) não tem permissão para desarquivar a tarefa {} (user_id: {})", username, user.getId(), id, taskUser.getId());
                return ResponseEntity.status(403).build();
            }

            // Desarquivar a tarefa
            task.setArchived(false);
            Task updatedTask = taskRepository.save(task);
            taskRepository.flush();
            logger.info("Tarefa com ID {} desarquivada com sucesso.", id);
            return ResponseEntity.ok(updatedTask);
        } catch (Exception e) {
            logger.error("Erro ao desarquivar tarefa com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{id}/tags")
    public ResponseEntity<Task> addTagToTask(@PathVariable Long id, @RequestBody TagRequest tagRequest) {
        try {
            logger.info("Iniciando processo de adição de tag à tarefa com ID: {}", id);

            // Validar o ID
            if (id == null || id <= 0) {
                logger.error("ID da tarefa inválido: {}", id);
                return ResponseEntity.status(400).build();
            }

            // Validar o nome da tag
            if (tagRequest.getTagName() == null || tagRequest.getTagName().trim().isEmpty()) {
                logger.error("Nome da tag é nulo ou vazio.");
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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

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
            logger.debug("Tarefa encontrada: ID = {}, Title = {}", task.getId(), task.getTitle());

            // Forçar o carregamento do usuário associado à tarefa
            Hibernate.initialize(task.getUser());
            User taskUser = task.getUser();
            if (taskUser == null) {
                logger.error("Tarefa com ID {} não possui um usuário associado.", id);
                return ResponseEntity.status(400).build();
            }

            logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                        taskUser.getId(), user.getId());
            if (!taskUser.getId().equals(user.getId())) {
                logger.error("Usuário {} (ID: {}) não tem permissão para adicionar tag à tarefa {} (user_id: {})", username, user.getId(), id, taskUser.getId());
                return ResponseEntity.status(403).build();
            }

            // Buscar ou criar a tag
            logger.debug("Buscando ou criando tag com nome: {}", tagRequest.getTagName());
            Tag tag = tagRepository.findByName(tagRequest.getTagName())
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagRequest.getTagName());
                        return tagRepository.save(newTag);
                    });
            logger.debug("Tag encontrada ou criada: ID = {}, Name = {}", tag.getId(), tag.getName());

            // Adicionar a tag à tarefa
            Hibernate.initialize(task.getTags());
            if (!task.getTags().contains(tag)) {
                task.getTags().add(tag);
                Task updatedTask = taskRepository.save(task);
                taskRepository.flush();
                logger.info("Tag {} adicionada à tarefa com ID {} com sucesso.", tag.getName(), id);
                return ResponseEntity.ok(updatedTask);
            } else {
                logger.info("Tag {} já está associada à tarefa com ID {}.", tag.getName(), id);
                return ResponseEntity.ok(task);
            }
        } catch (Exception e) {
            logger.error("Erro ao adicionar tag à tarefa com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Task> removeTagFromTask(@PathVariable Long id, @PathVariable Long tagId) {
        try {
            logger.info("Iniciando processo de remoção de tag da tarefa com ID: {}, Tag ID: {}", id, tagId);

            // Validar os IDs
            if (id == null || id <= 0) {
                logger.error("ID da tarefa inválido: {}", id);
                return ResponseEntity.status(400).build();
            }
            if (tagId == null || tagId <= 0) {
                logger.error("ID da tag inválido: {}", tagId);
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
            logger.debug("Usuário encontrado: ID = {}, Username = {}", user.getId(), user.getUsername());

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
            logger.debug("Tarefa encontrada: ID = {}, Title = {}", task.getId(), task.getTitle());

            // Forçar o carregamento do usuário associado à tarefa
            Hibernate.initialize(task.getUser());
            User taskUser = task.getUser();
            if (taskUser == null) {
                logger.error("Tarefa com ID {} não possui um usuário associado.", id);
                return ResponseEntity.status(400).build();
            }

            logger.debug("Verificando propriedade da tarefa: user_id da tarefa = {}, id do usuário autenticado = {}", 
                        taskUser.getId(), user.getId());
            if (!taskUser.getId().equals(user.getId())) {
                logger.error("Usuário {} (ID: {}) não tem permissão para remover tag da tarefa {} (user_id: {})", username, user.getId(), id, taskUser.getId());
                return ResponseEntity.status(403).build();
            }

            // Buscar a tag no banco de dados
            logger.debug("Buscando tag com ID: {}", tagId);
            Tag tag = tagRepository.findById(tagId).orElse(null);
            if (tag == null) {
                logger.warn("Tag não encontrada com ID: {}", tagId);
                return ResponseEntity.status(404).build();
            }
            logger.debug("Tag encontrada: ID = {}, Name = {}", tag.getId(), tag.getName());

            // Remover a tag da tarefa
            Hibernate.initialize(task.getTags());
            if (task.getTags().contains(tag)) {
                task.getTags().remove(tag);
                Task updatedTask = taskRepository.save(task);
                taskRepository.flush();
                logger.info("Tag {} removida da tarefa com ID {} com sucesso.", tag.getName(), id);
                return ResponseEntity.ok(updatedTask);
            } else {
                logger.info("Tag {} não está associada à tarefa com ID {}.", tag.getName(), id);
                return ResponseEntity.ok(task);
            }
        } catch (Exception e) {
            logger.error("Erro ao remover tag da tarefa com ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}

class BulkUpdateStatusRequest {
    private List<Long> taskIds;
    private String newStatus;

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

class TagRequest {
    private String tagName;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}