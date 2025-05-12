package com.taskmanager.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TaskDTO {

    private String title;
    private String description;
    private String status;
    private String dueDate;
    private boolean archived;

    // Getters e Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getDueDateAsLocalDateTime() {
        if (dueDate == null || dueDate.trim().isEmpty()) {
            return null;
        }
        try {
            // Tenta interpretar como uma data no formato YYYY-MM-DD
            LocalDate date = LocalDate.parse(dueDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // Converte para LocalDateTime definindo o horário como meia-noite
            return date.atStartOfDay();
        } catch (DateTimeParseException e) {
            // Se falhar, tenta interpretar como LocalDateTime completo
            return LocalDateTime.parse(dueDate);
        }
    }

    @Override
    public String toString() {
        return "TaskDTO{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", archived=" + archived +
                '}';
    }
}