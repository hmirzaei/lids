package org.uci.lids.utils;


import java.util.*;

/**
 * MinHeap
 */
public class MinHeap<K extends Comparable<? super K>, V> {

    private List<Entry> heap = new ArrayList<Entry>();
    private Map<V, Integer> heapPosition = new HashMap<V, Integer>();

    public void put(K key, V value) {
        heap.add(new Entry(key, value));
        heapPosition.put(value, size() - 1);
        bubbleUp(heap.size() - 1);
    }

    public int size() {
        return heap.size();
    }

    public Entry remove() {
        return remove(0);
    }

    private Entry remove(int position) {
        Entry e = heap.get(position);
        Collections.swap(heap, size() - 1, position);
        heapPosition.put(heap.get(position).getValue(), position);
        heapPosition.remove(heap.get(size() - 1).getValue());
        heap.remove(size() - 1);
        bubbleDown(position);
        return e;
    }

    public K remove(V value) {
        return remove(heapPosition.get(value)).getKey();
    }

    public boolean contains(V value) {
        return heapPosition.containsKey(value);
    }

    private void bubbleDown(int position) {
        while ((position << 1 < size() && heap.get(position).compareTo(heap.get(position << 1)) > 0)
                || ((position << 1 | 1) < size() && heap.get(position).compareTo(heap.get(position << 1 | 1)) > 0)) {
            if ((position << 1 | 1) < size() && heap.get(position << 1 | 1).compareTo(heap.get(position << 1)) < 0) {
                Collections.swap(heap, position, position << 1 | 1);
                heapPosition.put(heap.get(position).getValue(), position);
                heapPosition.put(heap.get(position << 1 | 1).getValue(), position << 1 | 1);
                position = position << 1 | 1;
            } else {
                Collections.swap(heap, position, position << 1);
                heapPosition.put(heap.get(position).getValue(), position);
                heapPosition.put(heap.get(position << 1).getValue(), position << 1);
                position = position << 1;
            }
        }
    }

    private void bubbleUp(int position) {
        while (heap.get(position).compareTo(heap.get(position >> 1)) < 0) {
            Collections.swap(heap, position, position >> 1);
            heapPosition.put(heap.get(position).getValue(), position);
            heapPosition.put(heap.get(position >> 1).getValue(), position >> 1);
            position >>= 1;
        }
    }

    public class Entry implements Comparable<Entry> {
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

        public int compareTo(Entry o) {
            return this.key.compareTo(o.key);
        }

        @Override
        public String toString() {
            return "K:" + key + ", V: " + value;
        }
    }
}
