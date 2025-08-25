package ru.kanban.utils;

public class Node<T> {
    public Node<T> prev;
    public T item;
    public Node<T> next;

    public Node(Node<T> prev, T item, Node<T> next) {
        this.prev = prev;
        this.item = item;
        this.next = next;
    }
}
