package deque;
import java.util.Comparator;
import org.junit.Test;
import static org.junit.Assert.*;

public class MaxArrayDequeTest {
    /* 建两个用于测试的 Comparator 小类 */
    private static class IntComparator implements Comparator<Integer> {
        public int compare(Integer a, Integer b) {
            if (a > b) {
                return 1;
            } else if (a < b) {
                return -1;
            } else {
                return 0;
            }
        }
    }

    private static class ReverseIntComparator implements Comparator<Integer> {
        public int compare(Integer a, Integer b) {
            if (a > b) {
                return -1;
            } else if (a < b) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Test
    public void testIntegersComparator() {
        IntComparator myComparator = new IntComparator();
        MaxArrayDeque<Integer> ad = new MaxArrayDeque<Integer> (myComparator);
        for (int i = 0; i < 20; i++) {
            ad.addFirst(i);
        }
        int maxItem = ad.max();
        System.out.println(maxItem);
        assertEquals("Max value in ad should be 19: ", 19, maxItem);
    }

    /* 传入一个不同的 Comparator 实例 */
    @Test
    public void testReverseIntegersComparator() {
        IntComparator myComparator = new IntComparator();
        ReverseIntComparator myReverseComparator = new ReverseIntComparator();
        MaxArrayDeque<Integer> ad = new MaxArrayDeque<Integer> (myComparator);
        for (int i = 0; i < 20; i++) {
            ad.addFirst(i);
        }
        int maxItem = ad.max(myReverseComparator);
        System.out.println(maxItem);
        assertEquals("Max value in ad should be 0: ", 0, maxItem);
    }

    /* 测试是否空 deque 返回 null */
    @Test
    public void testNullReturn() {
        IntComparator myComparator = new IntComparator();
        MaxArrayDeque<Integer> ad = new MaxArrayDeque<Integer> (myComparator);
        assertNull("Max value in ad should be null: ", ad.max());
    }
}
