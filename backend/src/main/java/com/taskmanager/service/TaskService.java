package com.taskmanager.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.taskmanager.dto.TaskDTO;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;

@Service
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Task> getAllTasks(String username, boolean includeArchived, String sortBy, String sortOrder) {
        logger.debug("Buscando tarefas para o usuário: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        return taskRepository.findByUserAndArchived(user, includeArchived, sort);
    }

    public Task createTask(TaskDTO taskDTO, String username) {
        logger.debug("Criando tarefa para o usuário: {}", username);

        // Validar os campos title e status
        if (taskDTO.getTitle() == null || taskDTO.getTitle().trim().isEmpty()) {
            logger.error("O título da tarefa não pode ser nulo ou vazio.");
            throw new IllegalArgumentException("O título da tarefa não pode ser nulo ou vazio.");
        }
        if (taskDTO.getStatus() == null || taskDTO.getStatus().trim().isEmpty()) {
            logger.error("O status da tarefa não pode ser nulo ou vazio.");
            throw new IllegalArgumentException("O status da tarefa não pode ser nulo ou vazio.");
        }
        // Validar status válidos
        if (!taskDTO.getStatus().equals("Pendente") && !taskDTO.getStatus().equals("Em Andamento") && !taskDTO.getStatus().equals("Concluída")) {
            logger.error("O status deve ser 'Pendente', 'Em Andamento' ou 'Concluída'.");
            throw new IllegalArgumentException("O status deve ser 'Pendente', 'Em Andamento' ou 'Concluída'.");
        }
        // Validar dueDate (se fornecido, deve ser no mesmo dia ou futura)
        LocalDateTime dueDateTime = taskDTO.getDueDateAsLocalDateTime();
        if (dueDateTime != null) {
            LocalDate dueDate = dueDateTime.toLocalDate();
            LocalDate today = LocalDate.now();
            logger.debug("Validando dueDate: dueDateTime={}, dueDate={}, today={}", dueDateTime, dueDate, today);
            if (dueDate.isBefore(today)) {
                logger.error("A data de vencimento (dueDate) deve ser no mesmo dia ou uma data futura.");
                throw new IllegalArgumentException("A data de vencimento (dueDate) deve ser no mesmo dia ou uma data futura.");
            }
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus());
        task.setDueDate(dueDateTime);
        task.setUser(user);
        task.setCreatedAt(LocalDateTime.now());
        task.setArchived(taskDTO.isArchived());

        Task savedTask = taskRepository.save(task);
        logger.debug("Tarefa criada com sucesso: {}", savedTask.getId());
        return savedTask;
    }

    public Task updateTask(Long id, TaskDTO taskDTO, String username) {
        logger.debug("Atualizando tarefa com ID: {} para o usuário: {}", id, username);

        // Validar os campos title e status
        if (taskDTO.getTitle() == null || taskDTO.getTitle().trim().isEmpty()) {
            logger.error("O título da tarefa não pode ser nulo ou vazio.");
            throw new IllegalArgumentException("O título da tarefa não pode ser nulo ou vazio.");
        }
        if (taskDTO.getStatus() == null || taskDTO.getStatus().trim().isEmpty()) {
            logger.error("O status da tarefa não pode ser nulo ou vazio.");
            throw new IllegalArgumentException("O status da tarefa não pode ser nulo ou vazio.");
        }
        // Validar status válidos
        if (!taskDTO.getStatus().equals("Pendente") && !taskDTO.getStatus().equals("Em Andamento") && !taskDTO.getStatus().equals("Concluída")) {
            logger.error("O status deve ser 'Pendente', 'Em Andamento' ou 'Concluída'.");
            throw new IllegalArgumentException("O status deve ser 'Pendente', 'Em Andamento' ou 'Concluída'.");
        }
        // Validar dueDate (se fornecido, deve ser no mesmo dia ou futura)
        LocalDateTime dueDateTime = taskDTO.getDueDateAsLocalDateTime();
        if (dueDateTime != null) {
            LocalDate dueDate = dueDateTime.toLocalDate();
            LocalDate today = LocalDate.now();
            logger.debug("Validando dueDate: dueDateTime={}, dueDate={}, today={}", dueDateTime, dueDate, today);
            if (dueDate.isBefore(today)) {
                logger.error("A data de vencimento (dueDate) deve ser no mesmo dia ou uma data futura.");
                throw new IllegalArgumentException("A data de vencimento (dueDate) deve ser no mesmo dia ou uma data futura.");
            }
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + id));

        // Verificar se a tarefa pertence ao usuário autenticado
        if (!task.getUser().getId().equals(user.getId())) {
            logger.error("Usuário {} (ID: {}) não tem permissão para atualizar a tarefa {} (user_id: {})", username, user.getId(), id, task.getUser().getId());
            throw new RuntimeException("Usuário não tem permissão para atualizar esta tarefa.");
        }

        // Atualizar os campos da tarefa
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus());
        task.setDueDate(dueDateTime);
        task.setArchived(taskDTO.isArchived());

        Task updatedTask = taskRepository.save(task);
        logger.debug("Tarefa atualizada com sucesso: {}", updatedTask.getId());
        return updatedTask;
    }

    public void deleteTask(Long id, String username) {
        logger.debug("Excluindo tarefa com ID: {} para o usuário: {}", id, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada: " + id));

        // Verificar se a tarefa pertence ao usuário autenticado
        if (!task.getUser().getId().equals(user.getId())) {
            logger.error("Usuário {} (ID: {}) não tem permissão para excluir a tarefa {} (user_id: {})", username, user.getId(), id, task.getUser().getId());
            throw new RuntimeException("Usuário não tem permissão para excluir esta tarefa.");
        }

        taskRepository.delete(task);
        logger.debug("Tarefa excluída com sucesso: {}", id);
    }
}