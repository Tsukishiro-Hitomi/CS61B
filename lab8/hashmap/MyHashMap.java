package hashmap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size;
    private double maxLoad;
    private Set<K> keySet;

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
     }

    public MyHashMap(int initialSize) { 
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) { 
        size = 0;
        buckets = createTable(initialSize);
        this.maxLoad = maxLoad;

        /* 注意：这里用 new HashSet<K> 而非 Set<K> */
        keySet = new HashSet<K>();
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */

    /* 使用 protected 修饰，便于重载 */
    protected Collection<Node> createBucket() {
        return new LinkedList<> ();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection <Node>[] table = (Collection<Node>[]) new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }    
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    public void clear() {
        size = 0;
        keySet.clear();
        /* 注意这里不要忘记初始化 buckets ，否则仍然保留有原来的数据 */
        buckets = createTable(16);
    }

    public boolean containsKey(K key) {
        for (K mapKey : keySet()) {
            if (mapKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public V get(K key) {
        int numBuckets = buckets.length;
        /* key.hashCode() 的结果可能是负数，因此需要使用 Math.floorMod() */
        int index = Math.floorMod(key.hashCode(), numBuckets);
        for (Node curNode : buckets[index]) {
            if (curNode.key.equals(key)) {
                return curNode.value;
            }
        }
        return null;
    }

    public int size() {
        return size;
    }

    public void put(K key, V value) {
        int numBuckets = buckets.length;
        int index = Math.floorMod(key.hashCode(), numBuckets);
        for (Node curNode : buckets[index]) {
            if (curNode.key.equals(key)) {
                curNode.value = value;
                return;
            }
        }
        buckets[index].add(createNode(key, value));
        size += 1;
        keySet.add(key);
        if ((float)size / numBuckets > maxLoad) {
            resize(numBuckets * 2);
        }
    }

    private void resize(int newSize) {
        Collection<Node> [] newBuckets = createTable(newSize);
        /* 对原 buckets 的每个节点都需要更新位置 */
        for (Collection<Node> bucket : buckets) {   
            for (Node node : bucket) {              
                int index = Math.floorMod(node.key.hashCode(), newSize);  
                newBuckets[index].add(node);        
            }
        }
        buckets = newBuckets;
    }

    public Set<K> keySet() {
        return keySet;
    }

    /* */
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    public V remove(K key, V value) {
        int index = Math.floorMod(key.hashCode(), buckets.length);
        Node nodeToDelete = null;
        V valueToReturn = null;
        for (Node curNode : buckets[index]) {
            if (curNode.key == key && (value == null || curNode.value == value)) {
                nodeToDelete = curNode;
                valueToReturn = curNode.value;
            }
        }
        if (nodeToDelete == null) {
            return null;
        }
        keySet.remove(key);
        return valueToReturn;
    }

    public V remove(K key) {
        return remove(key, null);
    }

}
