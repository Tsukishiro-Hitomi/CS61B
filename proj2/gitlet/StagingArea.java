package gitlet;
import java.io.Serializable;
import java.util.TreeMap;
import java.util.TreeSet;

public class StagingArea implements Serializable {
    private TreeMap<String, String> additions;
    private TreeSet<String> removals;

    
}
