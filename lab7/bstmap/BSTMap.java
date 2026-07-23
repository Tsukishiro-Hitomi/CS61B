package bstmap;
import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
public class BSTMap <K extends Comparable<K>, V> implements Map61B<K, V> {
    private int size;

    private Node root;

    private class Node {
        K key;
        V value;
        Node left;
        Node right;

        Node(K k, V v) {
            key = k;
            value = v;
            left = null;
            right = null;
        }
    }

    public BSTMap() {
        root = null;
        size = 0;
    }

    public void put(K key, V value) {
        if (root == null) {
            root = new Node(key, value);
            size += 1;
            return;
        }
        Node curNode = root;
        while (curNode != null) {
            if (curNode.key.compareTo(key) > 0) {
                if (curNode.left == null) {
                    curNode.left = new Node(key, value);
                    size += 1;
                    return;
                }
                curNode = curNode.left;
            } else if (curNode.key.compareTo(key) < 0) {
                if (curNode.right == null) {
                    curNode.right = new Node(key, value);
                    size += 1;
                    return;
                }
                curNode = curNode.right;
            } else{
                curNode.value = value;
                return;
            }
        }
    }

    private Node traversal(K key) {
        Node curNode = root;
        while (curNode != null) {
            if (curNode.key.compareTo(key) > 0) {
                curNode = curNode.left;
            } else if (curNode.key.compareTo(key) < 0) {
                curNode = curNode.right;
            } else {
                return curNode;
            }
        }
        return null;       
    }

    public V get(K key) {
        Node nodeToGet = traversal(key);
        if (nodeToGet != null) {
            return nodeToGet.value;
        }
        return null;
    }

    // 注意：有可能节点的值就是 null，但对应的 key 存在，因此需要比较的是节点本身是否为 null
    public boolean containsKey(K key) {
        return traversal(key) != null;
    }

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
        root = null;
    }

    public void printInOrder() {
        printHelper(root);
    }

    private void printHelper(Node curNode) {
        if (curNode == null) {
            return;
        }
        if (curNode.left != null) {
            printHelper(curNode.left);
        }
        System.out.println(String.format("(%s, %s)", curNode.key, curNode.value));
        if (curNode.right != null) {
            printHelper(curNode.right);
        }
    }

    public Set<K> keySet() {
        Set<K> result = new HashSet<> ();
        keySetHelper(result, root);
        return result;
    }

    private void keySetHelper(Set<K> result, Node root) {
        if (root == null) {
            return;
        }
        keySetHelper(result, root.left);
        result.add(root.key);
        keySetHelper(result, root.right);
    }

    public V remove(K key) {
        Node parent = root;
        Node child = root;
        boolean isLeftChild = false;   // 用于标记 child 是 parent 的 left/right child
        while (child != null && child.key.compareTo(key) != 0) {
            if (child.key.compareTo(key) < 0) {
                parent = child;
                child = child.right;
                isLeftChild = false;
            } else if (child.key.compareTo(key) > 0) {
                parent = child;
                child = child.left;
                isLeftChild = true;
            }
        }

        // 未找到
        if (child == null) {
            return null;
        }

        if (child.left != null && child.right == null) {
            if (child == root) {
                root = child.left;
            } else if (isLeftChild) {
                parent.left = child.left;
            } else {
                parent.right = child.left;
            }
        }

        if (child.left == null && child.right != null) {
            if (child == root) {
                root = child.right;
            } else if (isLeftChild) {
                parent.left = child.right;
            } else {
                parent.right = child.right;
            }
        }

        if (child.left != null && child.right != null) {
            Node curNode = child.left;
            while (curNode.right != null) {
                curNode = curNode.right;
            }
            curNode.right = child.right;
            if (child == root) {
                root = parent.left;
            } else if (isLeftChild) {
                parent.left = child.left;
            } else {
                parent.right = child.left;
            }
        }

        if (child.left == null && child.right == null) {
            if (child == root) {
                root = null;
            } else if (isLeftChild) {
                parent.left = null;
            } else {
                parent.right = null;
            }
        }
        size -= 1;
        return child.value;
    }

    public V remove(K key, V value) {
        Node parent = root;
        Node child = root;
        boolean isLeftChild = false;   // 用于标记 child 是 parent 的 left/right child
        while (child != null && child.key.compareTo(key) != 0) {
            if (child.key.compareTo(key) < 0) {
                parent = child;
                child = child.right;
                isLeftChild = false;
            } else if (child.key.compareTo(key) > 0) {
                parent = child;
                child = child.left;
                isLeftChild = true;
            }
        }

        // 未找到
        if (child == null) {
            return null;
        }

        if (!child.value.equals(value)) {
            return null;
        }

        if (child.left != null && child.right == null) {
            if (child == root) {
                root = child.left;
            } else if (isLeftChild) {
                parent.left = child.left;
            } else {
                parent.right = child.left;
            }
        }

        if (child.left == null && child.right != null) {
            if (child == root) {
                root = child.right;
            } else if (isLeftChild) {
                parent.left = child.right;
            } else {
                parent.right = child.right;
            }
        }

        if (child.left != null && child.right != null) {
            Node curNode = child.left;
            while (curNode.right != null) {
                curNode = curNode.right;
            }
            curNode.right = child.right;
            if (child == root) {
                root = parent.left;
            } else if (isLeftChild) {
                parent.left = child.left;
            } else {
                parent.right = child.left;
            }
        }

        if (child.left == null && child.right == null) {
            if (child == root) {
                root = null;
            } else if (isLeftChild) {
                parent.left = null;
            } else {
                parent.right = null;
            }
        }
        size -= 1;
        return child.value;
    }

    public Iterator<K> iterator() {
        return keySet().iterator();
    }
}
