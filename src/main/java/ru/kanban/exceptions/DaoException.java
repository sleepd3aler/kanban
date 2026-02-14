package ru.kanban.exceptions;

public class DaoException extends RuntimeException {
    public DaoException(String message, Exception e) {
        super(message);
    }
}
