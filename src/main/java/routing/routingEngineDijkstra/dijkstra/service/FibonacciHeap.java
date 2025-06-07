package routing.routingEngineDijkstra.dijkstra.service;

import java.util.ArrayList;
import java.util.List;

public class FibonacciHeap<T> {
    private Node<T> minNode;
    private int size;

    public static class Node<T> {
        T key;
        double priority;
        Node<T> parent;
        Node<T> child;
        Node<T> left;
        Node<T> right;
        int degree;
        boolean marked;

        public Node(T key, double priority) {
            this.key = key;
            this.priority = priority;
            this.left = this;
            this.right = this;
        }

        public T getKey() { return key; }
        public double getPriority() { return priority; }
    }

    public void insert(T item, double priority) {
        Node<T> node = new Node<>(item, priority);
        if (minNode == null) {
            minNode = node;
        } else {
            mergeLists(minNode, node);
            if (node.priority < minNode.priority) {
                minNode = node;
            }
        }
        size++;
    }

    public T extractMin() {
        Node<T> z = minNode;
        if (z != null) {
            // Add children to root list
            if (z.child != null) {
                Node<T> child = z.child;
                do {
                    child.parent = null;
                    child = child.right;
                } while (child != z.child);
                mergeLists(z, z.child);
            }

            // Remove z from root list
            if (z == z.right) {
                minNode = null;
            } else {
                z.left.right = z.right;
                z.right.left = z.left;
                minNode = z.right;
                consolidate();
            }
            size--;
            return z.key;
        }
        return null;
    }

    private void consolidate() {
        int maxDegree = (int) Math.ceil(Math.log(size) / Math.log(2)) + 1;
        Node<T>[] degreeArray = new Node[maxDegree];

        Node<T> current = minNode;
        List<Node<T>> roots = new ArrayList<>();
        do {
            roots.add(current);
            current = current.right;
        } while (current != minNode);

        for (Node<T> node : roots) {
            int degree = node.degree;
            while (degreeArray[degree] != null) {
                Node<T> other = degreeArray[degree];
                if (node.priority > other.priority) {
                    Node<T> temp = node;
                    node = other;
                    other = temp;
                }
                link(other, node);
                degreeArray[degree] = null;
                degree++;
            }
            degreeArray[degree] = node;
        }

        minNode = null;
        for (Node<T> node : degreeArray) {
            if (node != null) {
                if (minNode == null) {
                    minNode = node;
                    node.left = node;
                    node.right = node;
                } else {
                    mergeLists(minNode, node);
                    if (node.priority < minNode.priority) {
                        minNode = node;
                    }
                }
            }
        }
    }

    private void link(Node<T> child, Node<T> parent) {
        child.left.right = child.right;
        child.right.left = child.left;
        child.parent = parent;

        if (parent.child == null) {
            parent.child = child;
            child.left = child;
            child.right = child;
        } else {
            mergeLists(parent.child, child);
        }
        parent.degree++;
        child.marked = false;
    }

    private void mergeLists(Node<T> a, Node<T> b) {
        Node<T> aRight = a.right;
        Node<T> bLeft = b.left;
        a.right = b;
        b.left = a;
        aRight.left = bLeft;
        bLeft.right = aRight;
    }

    public boolean isEmpty() {
        return minNode == null;
    }

    public int size() {
        return size;
    }
}