package ru.kanban.utils;

import java.util.ArrayList;
import java.util.Iterator;

public class CustomLinkedList<T> implements Iterable<T> {
    private Node<T> head;
    private Node<T> tail;
    private int size;

    public Node<T> linkLast(T value) {
        Node<T> last = tail;
        Node<T> newNode = new Node<>(last, value, null);
        tail = newNode;
        if (last == null) {
            head = newNode;
        } else {
            last.next = newNode;
        }
        size++;
        return newNode;
    }

    public ArrayList<T> getTasks() {
        ArrayList<T> result = new ArrayList<>();
        for (T task : this) {
            result.add(task);
        }
        return result;
    }

    public void removeNode(Node<T> nodeToRemove) {
        if (nodeToRemove != null) {
            Node<T> prev = nodeToRemove.prev;
            Node<T> next = nodeToRemove.next;
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

    public Node<T> getHead() {
        return head;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                if (hasNext()) {
                    T result = current.item;
                    current = current.next;
                    return result;
                }
                return null;
            }
        };
    }

}
