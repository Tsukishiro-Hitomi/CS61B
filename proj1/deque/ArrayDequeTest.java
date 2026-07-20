package deque;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Random;

/** 针对 ArrayDeque 的全面测试。
 *  覆盖维度：空队列 / 增删顺序 / 触发扩容 / 触发缩容 / 环绕 / 清空后重填 /
 *           equals（含跨实现、非 Deque、size 不同）/ iterator / 随机对照。 */
public class ArrayDequeTest {

    /* ---------- 基础：空队列与 size ---------- */

    @Test
    public void newDequeIsEmpty() {
        ArrayDeque<String> ad = new ArrayDeque<>();
        assertTrue("刚建的 deque 应为空", ad.isEmpty());
        assertEquals(0, ad.size());
    }

    @Test
    public void removeFromEmptyReturnsNull() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        assertNull("空队列 removeFirst 应返回 null", ad.removeFirst());
        assertNull("空队列 removeLast 应返回 null", ad.removeLast());
        assertEquals("多次从空队列删除后 size 仍为 0", 0, ad.size());
    }

    @Test
    public void getOutOfRangeReturnsNull() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addLast(10);
        assertNull("越界索引应返回 null", ad.get(5));
        assertNull("空队列 get(0) 应返回 null", new ArrayDeque<Integer>().get(0));
    }

    /* ---------- 增删顺序 ---------- */

    @Test
    public void addFirstAddLastOrder() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addLast(1);    // [1]
        ad.addLast(2);    // [1, 2]
        ad.addFirst(0);   // [0, 1, 2]
        assertEquals(3, ad.size());
        assertEquals((Integer) 0, ad.get(0));
        assertEquals((Integer) 1, ad.get(1));
        assertEquals((Integer) 2, ad.get(2));
    }

    @Test
    public void removeOrderMatchesInsertion() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addLast(1);
        ad.addLast(2);
        ad.addLast(3);          // [1, 2, 3]
        assertEquals((Integer) 1, ad.removeFirst());  // 队首出
        assertEquals((Integer) 3, ad.removeLast());   // 队尾出
        assertEquals((Integer) 2, ad.removeFirst());
        assertTrue(ad.isEmpty());
    }

    /* ---------- 触发扩容：加入远超初始容量 8 的元素 ---------- */

    @Test
    public void resizeUpKeepsOrder() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            ad.addLast(i);
        }
        assertEquals(n, ad.size());
        // 扩容多次后，逐个校验值是否仍然正确、顺序不乱
        for (int i = 0; i < n; i++) {
            assertEquals("扩容后 get(" + i + ") 值应正确", (Integer) i, ad.get(i));
        }
        // 再用 removeFirst 逐个取出，顺序应为 0,1,2,...
        for (int i = 0; i < n; i++) {
            assertEquals((Integer) i, ad.removeFirst());
        }
        assertTrue(ad.isEmpty());
    }

    @Test
    public void resizeUpWithAddFirst() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            ad.addFirst(i);   // 每次插到最前，最终顺序应为 n-1, ..., 1, 0
        }
        for (int i = 0; i < n; i++) {
            assertEquals((Integer) (n - 1 - i), ad.get(i));
        }
    }

    /* ---------- 触发缩容：先撑大，再删到使用率很低 ---------- */

    @Test
    public void resizeDownKeepsOrder() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        int n = 1000;
        for (int i = 0; i < n; i++) {
            ad.addLast(i);
        }
        // 删掉前 990 个，触发多次缩容
        for (int i = 0; i < 990; i++) {
            assertEquals((Integer) i, ad.removeFirst());
        }
        // 剩下应为 990..999
        assertEquals(10, ad.size());
        for (int i = 0; i < 10; i++) {
            assertEquals("缩容后剩余元素值应正确", (Integer) (990 + i), ad.get(i));
        }
    }

    /* ---------- 环绕：反复在两端增删，让指针越过数组边界 ---------- */

    @Test
    public void wraparoundStress() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        // 先放几个占位
        for (int i = 0; i < 5; i++) {
            ad.addLast(i);
        }
        // 交替 addFirst / removeLast，迫使 start 反复越过 0 边界回绕
        for (int i = 0; i < 100; i++) {
            ad.addFirst(-i);
            ad.removeLast();
        }
        // 交替 addLast / removeFirst，迫使 end 反复越界
        for (int i = 0; i < 100; i++) {
            ad.addLast(1000 + i);
            ad.removeFirst();
        }
        assertEquals("反复两端增删后 size 应保持不变", 5, ad.size());
    }

    /* ---------- 清空后重填（此前发现的 bug 场景） ---------- */

    @Test
    public void emptyThenRefill() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        ad.addLast(1);
        ad.removeLast();       // 空
        ad.addLast(2);
        ad.addLast(3);         // 期望 [2, 3]
        assertEquals(2, ad.size());
        assertEquals((Integer) 2, ad.get(0));
        assertEquals((Integer) 3, ad.get(1));

        // 换 removeFirst 清空再重填，另一条路径也要对
        ArrayDeque<Integer> ad2 = new ArrayDeque<>();
        ad2.addFirst(1);
        ad2.removeFirst();     // 空
        ad2.addFirst(2);
        ad2.addFirst(3);       // 期望 [3, 2]
        assertEquals((Integer) 3, ad2.get(0));
        assertEquals((Integer) 2, ad2.get(1));
    }

    /* ---------- equals ---------- */

    @Test
    public void equalsSameContents() {
        ArrayDeque<Integer> a = new ArrayDeque<>();
        ArrayDeque<Integer> b = new ArrayDeque<>();
        for (int i = 0; i < 10; i++) {
            a.addLast(i);
            b.addLast(i);
        }
        assertTrue("内容相同应相等", a.equals(b));
        assertTrue("空 vs 空应相等", new ArrayDeque<Integer>().equals(new ArrayDeque<Integer>()));
    }

    @Test
    public void equalsDifferences() {
        ArrayDeque<Integer> a = new ArrayDeque<>();
        ArrayDeque<Integer> b = new ArrayDeque<>();
        a.addLast(1); a.addLast(2);
        b.addLast(1);
        assertFalse("size 不同应不相等", a.equals(b));
        b.addLast(9);                 // 同 size 不同值
        assertFalse("同长度不同值应不相等", a.equals(b));
        assertFalse("与非 Deque 对象比应不相等", a.equals("not a deque"));
        assertFalse("与 null 比应不相等", a.equals(null));
    }

    @Test
    public void equalsAcrossImplementations() {
        // spec: 只要对方是 Deque 且内容相同即可相等，跨实现也应成立
        ArrayDeque<Integer> a = new ArrayDeque<>();
        LinkedListDeque<Integer> l = new LinkedListDeque<>();
        for (int i = 0; i < 20; i++) {
            a.addLast(i);
            l.addLast(i);
        }
        assertTrue("ArrayDeque 应等于内容相同的 LinkedListDeque", a.equals(l));
    }

    /* ---------- iterator ---------- */

    @Test
    public void iteratorVisitsAllInOrder() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        for (int i = 1; i <= 5; i++) {
            ad.addLast(i);        // [1,2,3,4,5]
        }
        int sum = 0;
        int expectIndex = 0;
        for (int x : ad) {
            assertEquals("迭代顺序应与 get 一致", ad.get(expectIndex), (Integer) x);
            expectIndex += 1;
            sum += x;
        }
        assertEquals("应访问到全部 5 个元素", 5, expectIndex);
        assertEquals(15, sum);
    }

    /* ---------- 随机对照测试：以已验证的 LinkedListDeque 为“标准答案” ---------- */

    @Test
    public void randomizedAgainstLinkedList() {
        ArrayDeque<Integer> ad = new ArrayDeque<>();
        LinkedListDeque<Integer> lld = new LinkedListDeque<>();
        Random r = new Random(2021);   // 固定种子，失败可复现
        int ops = 20000;

        for (int k = 0; k < ops; k++) {
            int choice = r.nextInt(4);
            switch (choice) {
                case 0: {
                    int x = r.nextInt(1000);
                    ad.addFirst(x);
                    lld.addFirst(x);
                    break;
                }
                case 1: {
                    int x = r.nextInt(1000);
                    ad.addLast(x);
                    lld.addLast(x);
                    break;
                }
                case 2:
                    // 两边做相同操作，返回值必须一致（都空则都为 null）
                    assertEquals("removeFirst 返回值应一致", lld.removeFirst(), ad.removeFirst());
                    break;
                default:
                    assertEquals("removeLast 返回值应一致", lld.removeLast(), ad.removeLast());
                    break;
            }
            // 每步都比对 size 和全部元素
            assertEquals("size 应一致", lld.size(), ad.size());
            assertEquals("isEmpty 应一致", lld.isEmpty(), ad.isEmpty());
            for (int i = 0; i < lld.size(); i++) {
                assertEquals("第 " + k + " 步 get(" + i + ") 应一致", lld.get(i), ad.get(i));
            }
        }
    }
}
