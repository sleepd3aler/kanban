package ru.kanban.exceptions;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message) {
        super(message);
    }

  public ManagerSaveException() {
  }
}
