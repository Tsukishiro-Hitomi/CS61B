package deque;
import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> myComparator;

    public T max() {
        return max(myComparator);
    }

    public MaxArrayDeque(Comparator<T> c) {
        myComparator = c;
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        T maxItem = get(0);
        for (int i = 1; i < size(); i++) {
            T itemToComp = get(i);
            if (c.compare(itemToComp, maxItem) > 0) {
                maxItem = itemToComp;
            }
        }
        return maxItem;
    }
}
