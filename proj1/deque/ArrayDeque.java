package deque;
import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int LENGTH = 8;
    private T[] items;
    private int size;
    private int start;
    private int end;

    public ArrayDeque() {
        items = (T[]) new Object[LENGTH];
        size = 0;
        start = 0;
        end = 0;
    }

    private boolean isFull() {
        return size == LENGTH;
    }

    public int size() {
        return size;
    }

    public void addFirst(T item) {
        // empty 的特殊情况：此时 start 和 end 都不做更新
        if (isEmpty()) {
            items[start] = item;
            size += 1;
            return;
        }

        if (!isFull()) {
            int newStart = Math.floorMod(start - 1, LENGTH);
            items[newStart] = item;
            start = newStart;
            size += 1;
        } else {
            int newLength = LENGTH * 2;
            T[] newItems = (T[]) new Object[newLength];
            newItems[0] = item;
            for (int i = 1; i <= size; i++) {
                T itemToMove = items[Math.floorMod(start + i - 1, LENGTH)];
                newItems[i] = itemToMove;
            }
            start = 0;
            end = size;
            size += 1;
            items = newItems;
            LENGTH = newLength;
        }
    }

    public void addLast(T item) {
        // empty 的特殊情况：此时 start 和 end 都不做更新
        if (isEmpty()) {
            items[start] = item;
            size += 1;
            return;
        }
        if (!isFull()) {
            int newEnd = Math.floorMod(end + 1, LENGTH);
            items[newEnd] = item;
            end = newEnd;
            size += 1;
        } else {
            int newLength = LENGTH * 2;
            T[] newItems = (T[]) new Object[newLength];
            for (int i = 0; i < size; i++) {
                T itemToMove = items[Math.floorMod(start + i, LENGTH)];
                newItems[i] = itemToMove;
            }
            start = 0;
            end = size;
            newItems[end] = item;
            size += 1;
            items = newItems;
            LENGTH = newLength;
        }
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        } else {
            T returnItem = items[start];
            start = Math.floorMod(start + 1, LENGTH);
            size -= 1;
            if (LENGTH >= 16 && (float)size / LENGTH < 0.25) {
                T[] newItems = (T[]) new Object[LENGTH / 2];
                for (int i = 0; i < size; i++) {
                    newItems[i] = items[Math.floorMod(start + i, LENGTH)];
                }
                LENGTH /= 2;
                start = 0;
                end = size - 1;
                items = newItems;
            }
            // 重要：如果被删空，则应该将 start 和 end 重置为 0，否则会导致新增元素时出现 bug
            if (isEmpty()) {
                start = 0;
                end = 0;
            }
            return returnItem;
        }
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        } else {
            T returnItem = items[end];
            end = Math.floorMod(end - 1, LENGTH);
            size -= 1;
            if (LENGTH >= 16 && (float)size / LENGTH < 0.25) {
                T[] newItems = (T[]) new Object[LENGTH / 2];
                for (int i = 0; i < size; i++) {
                    newItems[i] = items[Math.floorMod(start + i, LENGTH)];
                }
                LENGTH /= 2;
                start = 0;
                end = size - 1;
                items = newItems;
            }
            // 重要：如果被删空，则应该将 start 和 end 重置为 0，否则会导致新增元素时出现 bug
            if (isEmpty()) {
                start = 0;
                end = 0;
            }
            return returnItem;
        }
    }

    public void printDeque() {
        for (int i = 0; i < size; i++) {
            System.out.print(items[Math.floorMod(start + i, LENGTH)]);
            System.out.print(" ");
        }
        System.out.println();
    }

    public T get(int index) {
        if (index + 1 > size) {
            return null;
        }
        return items[Math.floorMod(start + index, LENGTH)];
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
        return new ADIterator();
    }

    private class ADIterator implements Iterator<T> {
        private int curIndex;
        public ADIterator() { curIndex = 0; }
        
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
