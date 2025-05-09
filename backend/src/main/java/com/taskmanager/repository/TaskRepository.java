package com.taskmanager.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserAndArchived(User user, boolean archived, Sort sort);

    List<Task> findByUserAndStatusAndArchived(User user, String status, boolean archived, Sort sort);

    List<Task> findByUserAndTagsNameAndArchived(User user, String tagName, boolean archived, Sort sort);

    List<Task> findByUserAndDueDateBetweenAndArchived(User user, LocalDateTime startDate, LocalDateTime endDate, boolean archived, Sort sort);

    List<Task> findByUserAndDueDateBeforeAndStatusNotAndArchived(User user, LocalDateTime dueDate, String status, boolean archived, Sort sort);

    long countByUserAndDueDateBeforeAndStatusNotAndArchived(User user, LocalDateTime dueDate, String status, boolean archived);
}