package org.uci.lids.utils;

/** Source code example for "A Practical Introduction to Data
 Structures and Algorithm Analysis, 3rd Edition (Java)"
 by Clifford A. Shaffer
 Copyright 2008-2011 by Clifford A. Shaffer
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Min-heap implementation
 */
public class MinHeap<K extends Comparable<? super K>, V> {
    private K[] heap;   // Pointer to the heap array
    private V[] values;
    private Map<V, Integer> heapPosition;
    private int size;   // Maximum size of the heap
    private int n;      // Number of things in heap

    public MinHeap(K[] h, V[] v) {
        heap = h;
        values = v;
        heapPosition = new HashMap<V, Integer>();
        for (int i = 0; i < values.length; i++) {
            heapPosition.put(values[i], i);
        }
        n = h.length;
        size = h.length;
        buildHeap();
    }

    private static <E> void swap(E[] A, int p1, int p2) {
        E temp = A[p1];
        A[p1] = A[p2];
        A[p2] = temp;
    }

    /**
     * Return current size of the heap
     */
    public int heapSize() {
        return n;
    }

    /**
     * Is pos a leaf position?
     */
    private boolean isLeaf(int pos) {
        return (pos >= n / 2) && (pos < n);
    }

    /**
     * Return position for left child of pos
     */
    private int leftChild(int pos) {
        assert pos < n / 2 : "Position has no left child";
        return 2 * pos + 1;
    }

    /**
     * Return position for parent
     */
    private int parent(int pos) {
        assert pos > 0 : "Position has no parent";
        return (pos - 1) / 2;
    }

    /**
     * Heapify contents of heap
     */
    private void buildHeap() {
        for (int i = n / 2 - 1; i >= 0; i--)
            siftDown(i);
    }

    /**
     * Insert into heap
     */
    public void insert(K key, V value) {
        assert n < size : "Heap is full";
        int curr = n++;
        heap[curr] = key;                 // Start at end of heap
        heapPosition.put(value, curr);
        // Now sift up until curr's parent's key > curr's key
        while ((curr != 0) &&
                (heap[curr].compareTo(heap[parent(curr)]) < 0)) {
            MinHeap.swap(heap, curr, parent(curr));
            MinHeap.swap(values, curr, parent(curr));
            heapPosition.put(values[curr], curr);
            heapPosition.put(values[parent(curr)], parent(curr));

            curr = parent(curr);
        }
    }

    /**
     * Put element in its correct place
     */
    private void siftDown(int pos) {
        assert pos >= 0 && pos < n : "Illegal heap position";
        while (!isLeaf(pos)) {
            int j = leftChild(pos);
            if ((j < (n - 1)) && (heap[j].compareTo(heap[j + 1]) > 0))
                j++; // j is now index of child with greater value
            if (heap[pos].compareTo(heap[j]) <= 0)
                return;
            MinHeap.swap(heap, pos, j);
            MinHeap.swap(values, pos, j);
            heapPosition.put(values[pos], pos);
            heapPosition.put(values[j], j);

            pos = j;  // Move down
        }
    }

    public Entry removeMin() {     // Remove minimum value
        assert n > 0 : "Removing from empty heap";
        MinHeap.swap(heap, 0, --n); // Swap minimum with last value
        MinHeap.swap(values, 0, n); // Swap with last value
        heapPosition.put(values[0], 0);
        heapPosition.remove(values[n]);


        if (n != 0)      // Not on last element
            siftDown(0);   // Put new heap root val in correct place
        return new Entry(heap[n], values[n]);
    }

    public boolean contains(V value) {
        return this.heapPosition.containsKey(value);
    }

    public K remove(V value) {
        return this.remove(heapPosition.get(value));
    }

    /**
     * Remove element at specified position
     */
    public K remove(int pos) {
        assert (pos >= 0) && (pos < n) : "Illegal heap position";
        if (pos == (n - 1)) n--; // Last element, no work to be done
        else {
            MinHeap.swap(heap, pos, --n); // Swap with last value
            MinHeap.swap(values, pos, n); // Swap with last value
            heapPosition.put(values[pos], pos);
            heapPosition.remove(values[n]);


            // If we just swapped in a small value, push it up
            while ((pos > 0) && (heap[pos].compareTo(heap[parent(pos)]) < 0)) {
                MinHeap.swap(heap, pos, parent(pos));
                MinHeap.swap(values, pos, parent(pos));
                heapPosition.put(values[pos], pos);
                heapPosition.put(values[parent(pos)], parent(pos));


                pos = parent(pos);
            }
            if (n != 0) siftDown(pos);   // If it is big, push down
        }
        return heap[n];
    }

    public class Entry {
        private K key;
        private V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public V getValue() {
            return value;
        }

        public K getKey() {
            return key;
        }
    }

}
