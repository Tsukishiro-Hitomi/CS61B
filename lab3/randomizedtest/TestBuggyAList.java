package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
  @Test
  public void testThreeAddThreeRemove() {
    AListNoResizing<Integer> correct = new AListNoResizing<>();
    BuggyAList<Integer> buggy = new BuggyAList<>();
    int N = 3;
    for (int i = 0; i < N; i++) {
      correct.addLast(i);
      buggy.addLast(i);
    }
    for (int i = 0; i < N; i++) {
      assertEquals(correct.getLast(), buggy.getLast());
      assertEquals(correct.removeLast(), buggy.removeLast());
    }
  }

  @Test
  public void randomizedTest() {
      AListNoResizing<Integer> correct = new AListNoResizing<>();
      BuggyAList<Integer> buggy = new BuggyAList<>();

      int N = 5000;
      for (int i = 0; i < N; i += 1) {
          int op = StdRandom.uniform(0, 4);   
          if (op == 0) {                      
              int val = StdRandom.uniform(0, 100);
              correct.addLast(val);
              buggy.addLast(val);
          } else if (op == 1) {                
              assertEquals(correct.size(), buggy.size());
          } else if (op == 2 && correct.size() > 0) {   
              assertEquals(correct.getLast(), buggy.getLast());
          } else if (op == 3 && correct.size() > 0) {   
              assertEquals(correct.removeLast(), buggy.removeLast());
          }
      }
  }
}
