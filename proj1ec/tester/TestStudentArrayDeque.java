package tester;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;
import student.StudentArrayDeque;

public class TestStudentArrayDeque {
    @Test
    public void randomizedTest() {
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
        String ops = "";
        for (int i = 0; i < 400; i++) {
            int c = StdRandom.uniform(4);
            if (c == 0) {
                sad.addFirst(i);
                ads.addFirst(i);
                ops += String.format("addFirst(%d)\n", i);
            } else if (c == 1) {
                sad.addLast(i);
                ads.addLast(i);
                ops += String.format("addLast(%d)\n", i);
            } else if (c == 2) {
                if (ads.size() == 0) {
                    continue;
                }
                Integer actual = sad.removeFirst();
                Integer expect = ads.removeFirst();
                ops += String.format("removeFirst()\n");
                assertEquals("the first element of sad and ads should be same: \n" + ops, expect, actual);
            } else {
                if (ads.size() == 0) {
                    continue;
                }
                Integer actual = sad.removeLast();
                Integer expect = ads.removeLast();
                ops += String.format("removeLast()\n");
                assertEquals("the last element of sad and ads should be same: \n" + ops, expect, actual);
            }
            assertEquals("size of sad and ads should be same: \n" + ops, ads.size(), sad.size());
        }
    }
}