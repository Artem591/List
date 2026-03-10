import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class CustomDoublyLinkedList<E> implements Iterable<E> {

    private static class Node<E> {
        E item;
        Node<E> next;
        Node<E> prev;

        Node(Node<E> prev, E item, Node<E> next) {
            this.prev = prev;
            this.item = item;
            this.next = next;
        }
    }

    private Node<E> head;
    private Node<E> tail;
    private int size;
    private int modCount = 0;

    public CustomDoublyLinkedList() {
        head = null;
        tail = null;
        size = 0;
    }


    public void addFirst(E element) {
        Node<E> newNode = new Node<>(null, element, head);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            head.prev = newNode;
            head = newNode;
        }
        size++;
        modCount++;
    }

    public void addLast(E element) {
        Node<E> newNode = new Node<>(tail, element, null);
        if (tail == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
        modCount++;
    }

    public void add(E element) {
        addLast(element);
    }

    public void add(int index, E element) {
        checkIndexForAdd(index);

        if (index == 0) {
            addFirst(element);
            return;
        }
        if (index == size) {
            addLast(element);
            return;
        }

        Node<E> current = getNode(index);
        Node<E> prev = current.prev;
        Node<E> newNode = new Node<>(prev, element, current);

        current.prev = newNode;
        prev.next = newNode;

        size++;
        modCount++;
    }

    public E set(int index, E element) {
        checkIndex(index);
        Node<E> node = getNode(index);
        E oldVal = node.item;
        node.item = element;
        return oldVal;
    }

    public E removeFirst() {
        if (isEmpty()) throw new IndexOutOfBoundsException("List is empty");
        E element = head.item;
        head = head.next;
        if (head == null) {
            tail = null;
        } else {
            head.prev = null;
        }
        size--;
        modCount++;
        return element;
    }

    public E removeLast() {
        if (isEmpty()) throw new IndexOutOfBoundsException("List is empty");
        E element = tail.item;
        tail = tail.prev;
        if (tail == null) {
            head = null;
        } else {
            tail.next = null;
        }
        size--;
        modCount++;
        return element;
    }

    public E remove(int index) {
        checkIndex(index);
        if (index == 0) return removeFirst();
        if (index == size - 1) return removeLast();

        Node<E> node = getNode(index);
        E element = node.item;
        Node<E> prev = node.prev;
        Node<E> next = node.next;

        prev.next = next;
        next.prev = prev;

        node.item = null;
        node.next = null;
        node.prev = null;

        size--;
        modCount++;
        return element;
    }

    public E get(int index) {
        checkIndex(index);
        return getNode(index).item;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }


    private Node<E> getNode(int index) {
        Node<E> current;
        if (index < size / 2) {
            current = head;
            for (int i = 0; i < index; i++) current = current.next;
        } else {
            current = tail;
            for (int i = size - 1; i > index; i--) current = current.prev;
        }
        return current;
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size)
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<E> current = head;
        while (current != null) {
            sb.append(current.item);
            if (current.next != null) sb.append(", ");
            current = current.next;
        }
        sb.append("]");
        return sb.toString();
    }


    @Override
    public Iterator<E> iterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    public ListIterator<E> listIterator(int index) {
        checkIndexForAdd(index);
        return new ListIteratorImpl(index);
    }

    private class ListIteratorImpl implements ListIterator<E> {
        private Node<E> next;
        private Node<E> lastReturned;
        private int nextIndex;     
        private int expectedModCount = modCount;

        ListIteratorImpl(int index) {
            if (index < size / 2) {
                next = head;
                for (int i = 0; i < index; i++) next = next.next;
            } else {
                next = tail;
                for (int i = size - 1; i > index; i--) next = next.prev;
            }
            nextIndex = index;
        }

        private void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }

        @Override
        public boolean hasNext() {
            return nextIndex < size;
        }

        @Override
        public E next() {
            checkForComodification();
            if (!hasNext()) throw new NoSuchElementException();
            lastReturned = next;
            next = next.next;
            nextIndex++;
            return lastReturned.item;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public E previous() {
            checkForComodification();
            if (!hasPrevious()) throw new NoSuchElementException();

            if (next == null) {
                next = tail;
            } else {
                next = next.prev;
            }
            lastReturned = next;
            nextIndex--;
            return lastReturned.item;
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            checkForComodification();
            if (lastReturned == null) throw new IllegalStateException();

            Node<E> lastNext = lastReturned.next;

            if (lastReturned == head) {
                removeFirst();
            } else if (lastReturned == tail) {
                removeLast();
            } else {
                Node<E> prev = lastReturned.prev;
                prev.next = lastNext;
                lastNext.prev = prev;
                size--;
                modCount++;
            }

            expectedModCount = modCount;

            if (next == lastReturned) {
                next = lastNext;
            } else {
                nextIndex--;
            }
            lastReturned = null;
        }

        @Override
        public void set(E element) {
            checkForComodification();
            if (lastReturned == null) throw new IllegalStateException();
            lastReturned.item = element;
        }

        @Override
        public void add(E element) {
            checkForComodification();

            if (next == null) {
                addLast(element);
            } else if (next == head) {
                addFirst(element);
                next = head;
                nextIndex++;
            } else {
                Node<E> prev = next.prev;
                Node<E> newNode = new Node<>(prev, element, next);
                prev.next = newNode;
                next.prev = newNode;
                size++;
                modCount++;
                expectedModCount = modCount;
                nextIndex++;
            }
            lastReturned = null;
        }
    }

    public static void main(String[] args) {
        CustomDoublyLinkedList<Integer> list = new CustomDoublyLinkedList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        System.out.print("For-each: ");
        for (int val : list) System.out.print(val + " ");
        System.out.println();

        System.out.print("Iterator: ");
        Iterator<Integer> it = list.iterator();
        while (it.hasNext()) System.out.print(it.next() + " ");
        System.out.println();

        System.out.print("ListIterator forward: ");
        ListIterator<Integer> lit = list.listIterator();
        while (lit.hasNext()) System.out.print(lit.next() + " ");

        System.out.print("\nListIterator backward: ");
        while (lit.hasPrevious()) System.out.print(lit.previous() + " ");
        System.out.println();

        lit = list.listIterator(1);
        lit.add(99); // Вставка после 1
        System.out.println("After add(99) at index 1: " + list);

        lit.next();
        lit.set(100);
        System.out.println("After set(100): " + list);

        lit.remove();
        System.out.println("After remove(): " + list);
    }
}