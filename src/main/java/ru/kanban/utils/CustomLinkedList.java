package ru.kanban.utils;

import java.util.ArrayList;
import java.util.Iterator;
import ru.kanban.model.Task;

public class CustomLinkedList<T> implements Iterable<Task> {
    private Node<Task> head;
    private Node<Task> tail;
    private int size;

    public Node<Task> linkLast(Task value) {
        Node<Task> last = tail;
        Node<Task> newNode = new Node<>(last, value, null);
        tail = newNode;
        if (last == null) {
            head = newNode;
        } else {
            last.next = newNode;
        }
        size++;
        return newNode;
    }

    public ArrayList<Task> getTasks() {
        ArrayList<Task> result = new ArrayList<>();
        for (Task task : this) {
            result.add(task);
        }
        return result;
    }

    public void removeNode(Node<Task> nodeToRemove) {
        if (nodeToRemove != null) {
            Node<Task> prev = nodeToRemove.prev;
            Node<Task> next = nodeToRemove.next;
            if (prev == null) {
                head = next;
            } else {
                prev.next = next;
                nodeToRemove.prev = null;
            }
            if (next == null) {
                tail = prev;
            } else {
                next.prev = prev;
                nodeToRemove.next = null;
            }
            nodeToRemove.item = null;
            size--;
        }
    }

    @Override
    public Iterator<Task> iterator() {
        return new Iterator<>() {
            private Node<Task> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Task next() {
                if (hasNext()) {
                    Task result = current.item;
                    current = current.next;
                    return result;
                }
                return null;
            }
        };
    }

    public static class Node<E> {
        private Node<Task> prev;
        private Task item;
        private Node<Task> next;

        public Node(Node<Task> prev, Task item, Node<Task> next) {
            this.prev = prev;
            this.item = item;
            this.next = next;
        }
    }

}
