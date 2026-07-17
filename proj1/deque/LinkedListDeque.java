package deque; 
import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private static class Node<T> {
        T item;
        Node<T> prev;
        Node<T> next;

        Node() {}

        Node(T item, Node<T> prev, Node<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next; 
        }
    }

    private int size;
    private Node<T> sentinel;

    public int size() {
        return size;
    }

    public void addFirst(T item) {
        Node<T> newNode = new Node<T> (item, sentinel, sentinel.next);
        sentinel.next = newNode;
        newNode.prev = sentinel;
        newNode.next.prev = newNode;
        size += 1;
    }

    public void addLast(T item) {
        Node<T> newNode = new Node<T> (item, sentinel.prev, sentinel);
        sentinel.prev = newNode;
        newNode.prev.next = newNode;
        size += 1;
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        Node<T> nodeToRemove = sentinel.next;
        sentinel.next = nodeToRemove.next;
        sentinel.next.prev = sentinel;
        size -= 1;
        return nodeToRemove.item;
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        Node<T> nodeToRemove = sentinel.prev;
        sentinel.prev = nodeToRemove.prev;
        sentinel.prev.next = sentinel;
        size -= 1;
        return nodeToRemove.item;
    }

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node<T> ();
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
    }

    public T get(int index) {
        if (index >= size()) {
            return null;
        }
        Node<T> nodeToGet = sentinel;
        for (int i = -1; i < index; i++) {
            nodeToGet = nodeToGet.next;
        }
        return nodeToGet.item;
    }

    public T getRecursive(int index) {
        if (index >= size) {
            return null;
        }
        return getRecursiveHelper(index, sentinel.next);
    }

    private T getRecursiveHelper(int index, Node<T> current) {
        if (index == 0) {
            return current.item;
        }
        return getRecursiveHelper(index - 1, current.next);
    }

    public void printDeque() {
        Node<T> curNode = sentinel.next;
        while (curNode != sentinel) {
            System.out.print(curNode.item);
            System.out.print(" ");
            curNode = curNode.next;
        }
        System.out.println();
    }


    public boolean equals(Object o) {
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<?> other = (Deque<?>) o;
        if (size() != other.size()) {
            return false; 
        }
        for (int i = 0; i < size(); i++) {
            if (!get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new LLDIterator();
    }

    private class LLDIterator implements Iterator<T> {
        private int curIndex;
        public LLDIterator() { curIndex = 0; }
        
        public boolean hasNext() {
            return curIndex < size();
        }

        public T next() {
            T elem = get(curIndex);
            curIndex += 1;
            return elem;
        }
    }
}
