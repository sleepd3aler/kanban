package ru.kanban.exceptions;

public class EpicNotFoundException extends Exception {
    public EpicNotFoundException(String message) {
        super(message);
    }
}
