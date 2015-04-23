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

    private int parent(int node) {
        return ((node + 1) >> 1) - 1;
    }

    private int lChild(int node) {
        return ((node + 1) << 1) - 1;
    }

    private int rChild(int node) {
        return ((node + 1) << 1);
    }

    public int size() {
        return heap.size();
    }

    public Entry remove() {
        return remove(0);
    }

    private Entry remove(int position) {
        Entry e = heap.get(position);
        if (position != 0) {
            heap.set(position, null);
            bubbleUp(position);
            position = 0;
        }
        Collections.swap(heap, size() - 1, position);
        heapPosition.put(heap.get(position).getValue(), position);
        if (heap.get(size() - 1) != null)
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
        while ((lChild(position) < size() && heap.get(position).compareTo(heap.get(lChild(position))) > 0)
                || ((rChild(position)) < size() && heap.get(position).compareTo(heap.get(rChild(position))) > 0)) {

            if ((rChild(position)) < size() && heap.get(rChild(position)).compareTo(heap.get(lChild(position))) < 0) {
                Collections.swap(heap, position, rChild(position));
                heapPosition.put(heap.get(position).getValue(), position);
                heapPosition.put(heap.get(rChild(position)).getValue(), rChild(position));
                position = rChild(position);
            } else {
                Collections.swap(heap, position, lChild(position));
                heapPosition.put(heap.get(position).getValue(), position);
                heapPosition.put(heap.get(lChild(position)).getValue(), lChild(position));
                position = lChild(position);
            }
        }

    }

    private void bubbleUp(int position) {
        while (position > 0 && (heap.get(position) == null || heap.get(position).compareTo(heap.get(parent(position))) < 0)) {
            Collections.swap(heap, position, parent(position));
            heapPosition.put(heap.get(position).getValue(), position);
            if (heap.get(parent(position)) != null)
                heapPosition.put(heap.get(parent(position)).getValue(), parent(position));
            position = parent(position);
        }
    }

    @Override
    public String toString() {
        return heap.toString();
//        StringBuilder sb = new StringBuilder().append("\n");
//        int i=0;
//        int power=1;
//        int levels = (int)Math.ceil(Math.log(heap.size())/Math.log(2));
//        int spaces = (int)Math.pow(2, levels)*4;
//        while(power<heap.size()) {
//            for (int j = power-1; j < 2*power-1; j++) {
//                if (j<heap.size())
//                    sb.append(String.format("%-"+Integer.toString(spaces)+"s",heap.get(j)));
//            }
//            sb.append("\n");
//            spaces = spaces/2;
//            power = power*2;
//        }
//        return sb.toString();
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
            return "(K:" + key + ", V: " + value + ")";
        }
    }
}
