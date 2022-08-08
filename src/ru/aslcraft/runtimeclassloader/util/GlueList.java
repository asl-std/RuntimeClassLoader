package ru.aslcraft.runtimeclassloader.util;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 *
 * <p>
 * GlueList is a brand-new List implementation which is way faster than
 * ArrayList and LinkedList. This implementation inspired from ArrayList and
 * LinkedList working mechanism.
 * </p>
 * <p>
 * Nodes holding data in arrays, in the beginning the world just like ArrayList
 * ,inserts data into array one by one when there is no space for insertion to
 * array new Node will be created and linked with the last Node.
 * </p>
 * <p>
 * The array which belongs to newly created node has half of the size of list ,
 * just like ArrayList. In ArrayList when there is no space for it it creates
 * new array with double of old size and inserts old data into new one. Unlike
 * ArrayList GlueList does it dynamically way with creating new node so old data
 * does NOT have to be moved to another array. You can think that GlueList is
 * dynamic version of ArrayList.
 * </p>
 * <p>
 * Adding and removing operations much faster than ArrayList and LinkedList.
 * Searching operations nearly same with ArrayList and way better than
 * LinkedList.
 * </p>
 *
 * @see Collection
 * @see List
 * @see LinkedList
 * @see ArrayList
 * @param <T> The exact type of elements held in this collection
 */
@SuppressWarnings("serial")
public class GlueList<T> extends AbstractList<T> implements List<T>, Cloneable, Serializable {

	transient Node<T> first;
	transient Node<T> last;

	int size;

	int initialCapacity;

	private static final int DEFAULT_CAPACITY = 10;

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

	public GlueList() {

		final Node<T> initNode = new Node<>(null, null, 0, DEFAULT_CAPACITY);

		first = initNode;
		last = initNode;
	}

	public GlueList(int initialCapacity) {
		this.initialCapacity = Math.min(initialCapacity, MAX_ARRAY_SIZE);
		final Node<T> initNode = new Node<>(null, null, 0, initialCapacity);
		first = initNode;
		last = initNode;
	}

	public GlueList(Collection<? extends T> c) {
		final Object[] arr = c.toArray();
		final int len = arr.length;
		if (len != 0) {
			final Node<T> initNode = new Node<>(null, null, 0, len);
			first = initNode;
			last = initNode;
			System.arraycopy(arr, 0, last.elementData, 0, len);
			last.elementDataPointer += len;
		} else {
			final Node<T> initNode = new Node<>(null, null, 0, DEFAULT_CAPACITY);
			first = initNode;
			last = initNode;
		}
		modCount++;
		size += len;
	}

	@Override
	public boolean add(T element) {

		final Node<T> l = last;

		if (l.isAddable()) {
			l.add(element);
		} else {
			final Node<T> newNode = new Node<>(l, null, size);
			newNode.add(element);
			last = newNode;
			l.next = last;
		}

		modCount++;
		size++;

		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void add(int index, T element) {
		rangeCheckForAdd(index);

		Node<T> node = getNodeForAdd(index);

		if (node == null) {
			final Node<T> l = last;
			final Node<T> newNode = new Node<>(l, null, size);

			last = newNode;
			l.next = last;
			node = newNode;
		}

		// if it is last and has extra space for element...
		if (node == last && node.elementData.length - node.elementDataPointer > 0) {

			final int nodeArrIndex = index - node.startingIndex;

			System.arraycopy(node.elementData, nodeArrIndex, node.elementData, nodeArrIndex + 1,
					node.elementDataPointer - nodeArrIndex);

			node.elementData[nodeArrIndex] = element;

			if (nodeArrIndex > 0) {
				System.arraycopy(node.elementData, 0, node.elementData, 0, nodeArrIndex);
			}

			node.elementDataPointer++;
		} else {

			final int newLen = node.elementData.length + 1;
			final T[] newElementData = (T[]) new Object[newLen];

			final int nodeArrIndex = index - node.startingIndex;

			System.arraycopy(node.elementData, nodeArrIndex, newElementData, nodeArrIndex + 1,
					node.elementDataPointer - nodeArrIndex);

			newElementData[nodeArrIndex] = element;

			if (nodeArrIndex > 0) {
				System.arraycopy(node.elementData, 0, newElementData, 0, nodeArrIndex);
			}

			node.elementData = newElementData;
			node.endingIndex++;
			node.elementDataPointer++;
		}

		updateNodesAfterAdd(node);

		modCount++;
		size++;
	}

	private void rangeCheckForAdd(int index) {
		if (index > size || index < 0)
			throw new ArrayIndexOutOfBoundsException(index);
	}

	private void updateNodesAfterAdd(Node<T> nodeFrom) {
		for (Node<T> node = nodeFrom.next; node != null; node = node.next) {

			node.startingIndex++;
			node.endingIndex++;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(Collection<? extends T> c) {

		Objects.requireNonNull(c);

		final Object[] collection = c.toArray();

		final int len = collection.length;

		if (len == 0) {
			return false;
		}

		if (size == 0) {
			if (initialCapacity >= len)
				System.arraycopy(collection, 0, last.elementData, 0, len);
			else {
				last.elementData = Arrays.copyOf((T[]) collection, len);
				last.endingIndex = len - 1;
			}

			last.elementDataPointer += len;

			modCount++;
			size += len;

			return true;
		}

		final int elementDataLen = last.elementData.length;
		final int elementSize = last.elementDataPointer;

		final int remainedStorage = elementDataLen - elementSize;

		if (remainedStorage == 0) {

			final Node<T> l = last;

			final int newLen = (size >>> 1);
			final int initialLen = Math.max(len, newLen);

			final Node<T> newNode = new Node<>(l, null, size, initialLen);

			System.arraycopy(collection, 0, newNode.elementData, 0, len);

			newNode.elementDataPointer += len;

			last = newNode;
			l.next = last;

			modCount++;
			size += len;

			return true;
		}

		if (len <= remainedStorage) {

			System.arraycopy(collection, 0, last.elementData, elementSize, len);

			last.elementDataPointer += len;

			modCount++;
			size += len;

			return true;
		}

		if (len > remainedStorage) {

			System.arraycopy(collection, 0, last.elementData, elementSize, remainedStorage);

			last.elementDataPointer += remainedStorage;
			size += remainedStorage;

			final int newLen = (size >>> 1);
			final int remainedDataLen = len - remainedStorage;

			final int initialLen = Math.max(newLen, remainedDataLen);

			final Node<T> l = last;

			final Node<T> newNode = new Node<>(l, null, size, initialLen);

			System.arraycopy(collection, remainedStorage, newNode.elementData, 0, remainedDataLen);

			newNode.elementDataPointer += remainedDataLen;

			last = newNode;
			l.next = last;

			modCount++;
			size += remainedDataLen;

			return true;
		}

		return false;
	}

	@Override
	public T set(int index, T element) {

		rangeCheck(index);

		final Node<T> node = getNode(index);

		final int nodeArrIndex = index - node.startingIndex;

		final T oldValue = node.elementData[nodeArrIndex];

		node.elementData[nodeArrIndex] = element;

		return oldValue;
	}

	@Override
	public T get(int index) {

		rangeCheck(index);

		final Node<T> node = getNode(index);

		return node.elementData[index - node.startingIndex];
	}

	@Override
	public int indexOf(Object o) {

		int index = 0;

		if (o == null) {

			for (Node<T> node = first; node != null; node = node.next) {
				for (int i = 0; i < node.elementDataPointer; i++) {
					if (node.elementData[i] == null) {
						return index;
					}
					index++;
				}
			}
		} else {

			for (Node<T> node = first; node != null; node = node.next) {
				for (int i = 0; i < node.elementDataPointer; i++) {
					if (o.equals(node.elementData[i])) {
						return index;
					}
					index++;
				}
			}
		}

		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {

		int index = size - 1;

		if (o == null) {
			for (Node<T> node = last; node != null; node = node.pre) {
				for (int i = node.elementDataPointer - 1; i >= 0; i--) {
					if (node.elementData[i] == null) {
						return index;
					}
					index--;
				}
			}
		} else {

			for (Node<T> node = last; node != null; node = node.pre) {
				for (int i = node.elementDataPointer - 1; i >= 0; i--) {
					if (o.equals(node.elementData[i])) {
						return index;
					}
					index--;
				}
			}
		}

		return -1;
	}

	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	@Override
	public T remove(int index) {

		rangeCheck(index);

		Node<T> node;

		if (size == 2 && first != last) {

			final Node<T> newNode = new Node<>(null, null, 0, 2);
			newNode.add(first.elementData[0]);
			newNode.add(last.elementData[0]);

			node = first = last = newNode;
		} else {
			node = getNode(index);
		}

		final T[] elementData = node.elementData;

		final int elementSize = node.elementDataPointer;

		final int nodeArrIndex = index - node.startingIndex;

		final T oldValue = elementData[nodeArrIndex];

		final int numMoved = elementSize - nodeArrIndex - 1;

		if (numMoved > 0) {
			System.arraycopy(node.elementData, nodeArrIndex + 1, node.elementData, nodeArrIndex, numMoved);
		}

		if (first == last || node == last) {
			node.elementData[elementSize - 1] = null;
		} else {
			node.elementData = Arrays.copyOf(node.elementData, elementSize - 1);
			node.endingIndex = (--node.endingIndex < 0) ? 0 : node.endingIndex;
		}

		node.elementDataPointer--;

		updateNodesAfterRemove(node);

		if (node.elementDataPointer == 0 && first != last) {

			final Node<T> next = node.next;
			final Node<T> prev = node.pre;

			if (prev == null) {
				first = next;
			} else {
				prev.next = next;
				node.pre = null;
			}

			if (next == null) {
				last = prev;
			} else {
				next.pre = prev;
				node.next = null;
			}

			node.elementData = null;
		}

		size--;
		modCount++;

		return oldValue;
	}

	@Override
	public boolean removeAll(Collection<?> c) {

		Objects.requireNonNull(c);

		final Object[] arr = c.toArray();
		if (arr.length == 0) {
			return false;
		}

		boolean isModified = false;

		for (final Object o : arr) {
			isModified |= remove(o);
		}

		return isModified;
	}

	@Override
	public boolean retainAll(Collection<?> c) {

		Objects.requireNonNull(c);

		final Object[] arr = c.toArray();
		if (arr.length == 0) {
			return false;
		}

		boolean isModified = false;

		final Object[] elements = toArray();

		for (final Object element : elements) {

			if (!c.contains(element)) {
				isModified |= remove(element);
			}
		}

		return isModified;
	}

	@Override
	public boolean remove(Object o) {

		final int index = indexOf(o);

		if (index != -1) {
			remove(index);
			return true;
		} else {
			return false;
		}
	}

	private void updateNodesAfterRemove(Node<T> fromNode) {

		for (Node<T> node = fromNode.next; node != null; node = node.next) {

			node.startingIndex = (--node.startingIndex < 0) ? 0 : node.startingIndex;
			node.endingIndex = (--node.endingIndex < 0) ? 0 : node.endingIndex;
		}
	}

	private Node<T> getNode(int index) {

		final int firstStartingIndex = first.startingIndex;
		final int firstEndingIndex = first.endingIndex;

		final int firstMinDistance = min(abs(index - firstStartingIndex), abs(index - firstEndingIndex));

		final int lastStartingIndex = last.startingIndex;
		final int lastEndingIndex = last.endingIndex;

		final int lastMinDistance = min(abs(index - lastStartingIndex), abs(index - lastEndingIndex));

		if (firstMinDistance <= lastMinDistance) {

			Node<T> node = first;
			do {

				if (node.startingIndex <= index && index <= node.endingIndex) {
					return node;
				}

				node = node.next;
			} while (true);
		} else {

			Node<T> node = last;
			do {

				if (node.startingIndex <= index && index <= node.endingIndex) {
					return node;
				}

				node = node.pre;
			} while (true);
		}
	}

	private Node<T> getNodeForAdd(int index) {

		if (index == size && ((last.startingIndex > index) || (index > last.endingIndex))) {
			return null;
		}

		return getNode(index);
	}

	private void rangeCheck(int index) {

		if (index >= size || index < 0) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}

	@Override
	public void clear() {

		for (Node<T> node = first; node != null;) {

			final Node<T> next = node.next;

			node.next = null;
			node.pre = null;
			node.elementData = null;

			node = next;
		}

		first = last = null;

		final int capacity = min(MAX_ARRAY_SIZE, max(size, max(initialCapacity, DEFAULT_CAPACITY)));

		final Node<T> initNode = new Node<>(null, null, 0, capacity);

		initialCapacity = capacity;

		first = initNode;
		last = initNode;

		modCount++;
		size = 0;
	}

	public void trimToSize() {

		final int pointer = last.elementDataPointer;
		final int arrLen = last.elementData.length;

		if (pointer < arrLen && arrLen > 2) {

			if (pointer < 2) {
				last.elementData = Arrays.copyOf(last.elementData, 2);
				last.endingIndex -= arrLen - 2;
			} else {
				last.elementData = Arrays.copyOf(last.elementData, pointer);
				last.endingIndex -= arrLen - pointer;
			}
		}
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return super.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {

		final Object[] objects = new Object[size];

		int i = 0;
		for (Node<T> node = first; node != null; node = node.next) {

			final int len = node.elementDataPointer;

			if (len > 0) {
				System.arraycopy(node.elementData, 0, objects, i, len);
			}

			i += len;
		}

		return objects;
	}

	@SuppressWarnings({ "unchecked", "hiding" })
	@Override
	public <T> T[] toArray(T[] a) {
		return (T[]) Arrays.copyOf(toArray(), size, a.getClass());
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Iterator<T> iterator() {
		return new Itr();
	}

	private class Itr implements Iterator<T> {

		Node<T> node = first;

		int i = 0;// inner-array index
		int j = 0;// total index -> cursor

		int lastReturn = -1;

		int expectedModCount = modCount;
		int elementDataPointer = node.elementDataPointer;

		@Override
		public boolean hasNext() {
			return j != size;
		}

		@Override
		public T next() {

			checkForComodification();

			if (j >= size) {
				throw new NoSuchElementException();
			}

			if (j >= last.endingIndex + 1) {
				throw new ConcurrentModificationException();
			}

			if (j == 0) {// it's for listIterator.when node becomes null.
				node = first;
				elementDataPointer = node.elementDataPointer;
				i = 0;
			}

			final T val = node.elementData[i++];

			if (i >= elementDataPointer) {
				node = node.next;
				i = 0;
				elementDataPointer = (node != null) ? node.elementDataPointer : 0;
			}

			lastReturn = j++;

			return val;
		}

		@Override
		public void remove() {

			if (lastReturn < 0) {
				throw new IllegalStateException();
			}

			checkForComodification();

			try {
				GlueList.this.remove(lastReturn);

				j = lastReturn;

				lastReturn = -1;

				i = (--i < 0) ? 0 : i;

				elementDataPointer = (node != null) ? node.elementDataPointer : 0;

				expectedModCount = modCount;
			} catch (final IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}

		void checkForComodification() {
			if (modCount != expectedModCount) {
				throw new ConcurrentModificationException();
			}
		}
	}

	@Override
	public ListIterator<T> listIterator(int index) {

		checkPositionIndex(index);

		return new ListItr(index);
	}

	private void checkPositionIndex(int index) {

		if (((index < 0) || (index > size))) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
	}

	@Override
	public ListIterator<T> listIterator() {
		return new ListItr(0);
	}

	private class ListItr extends Itr implements ListIterator<T> {

		public ListItr(int index) {
			node = (index == size) ? last : getNode(index);
			j = index;
			i = index - node.startingIndex;
			elementDataPointer = node.elementDataPointer;
		}

		@Override
		public boolean hasPrevious() {
			return j != 0;
		}

		@Override
		public T previous() {

			checkForComodification();

			final int temp = j - 1;

			if (temp < 0) {
				throw new NoSuchElementException();
			}

			if (temp >= last.endingIndex + 1) {
				throw new ConcurrentModificationException();
			}

			if (j == size) {

				node = last;

				elementDataPointer = node.elementDataPointer;

				i = elementDataPointer;
			}

			final int index = j - node.startingIndex;
			if (index == 0) {

				node = node.pre;

				elementDataPointer = node.elementDataPointer;

				i = elementDataPointer;
			}

			final T val = node.elementData[--i];

			if (i < 0) {
				node = node.pre;
				i = (node != null) ? node.elementDataPointer : 0;
			}

			j = temp;

			lastReturn = j;

			return val;
		}

		@Override
		public int nextIndex() {
			return j;
		}

		@Override
		public int previousIndex() {
			return j - 1;
		}

		@Override
		public void set(T t) {

			if (lastReturn < 0) {
				throw new IllegalStateException();
			}

			checkForComodification();

			try {
				GlueList.this.set(lastReturn, t);
			} catch (final IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public void add(T t) {

			checkForComodification();

			try {
				final int temp = j;

				GlueList.this.add(temp, t);

				j = temp + 1;

				lastReturn = -1;

				i++;
				elementDataPointer = (node != null) ? node.elementDataPointer : 0;

				expectedModCount = modCount;
			} catch (final IndexOutOfBoundsException e) {
				throw new ConcurrentModificationException();
			}
		}
	}

	@Override
	public int size() {
		return size;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {

		try {
			final GlueList<T> clone = (GlueList<T>) super.clone();

			clone.first = clone.last = null;

			final int capacity = min(MAX_ARRAY_SIZE, max(clone.size, max(clone.initialCapacity, DEFAULT_CAPACITY)));

			final Node<T> initNode = new Node<>(null, null, 0, capacity);

			clone.initialCapacity = capacity;

			clone.first = clone.last = initNode;

			clone.modCount = 0;
			clone.size = 0;

			for (Node<T> node = first; node != null; node = node.next) {
				// for (int i = 0; i < node.elementDataPointer; i++) {
				// clone.add(node.elementData[i]);
				// }
				clone.addAll(Arrays.asList(node.elementData).subList(0, node.elementDataPointer));
			}

			return clone;
		} catch (final CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	static class Node<T> {

		Node<T> pre;
		Node<T> next;

		int listSize;

		int startingIndex;
		int endingIndex;

		T[] elementData;
		int elementDataPointer;

		@SuppressWarnings("unchecked")
		Node(Node<T> pre, Node<T> next, int listSize) {
			this.pre = pre;
			this.next = next;
			this.listSize = listSize;
			this.elementData = (T[]) new Object[listSize >>> 1];
			this.startingIndex = listSize;
			this.endingIndex = listSize + elementData.length - 1;
		}

		Node(Node<T> pre, Node<T> next, int listSize, int initialCapacity) {
			this.pre = pre;
			this.next = next;
			this.listSize = listSize;
			this.elementData = createElementData(initialCapacity);
			this.startingIndex = listSize;
			this.endingIndex = listSize + elementData.length - 1;
		}

		@SuppressWarnings("unchecked")
		T[] createElementData(int capacity) {

			if (capacity == 0 || capacity == 1) {
				return (T[]) new Object[DEFAULT_CAPACITY];
			} else if (capacity > 1) {
				return (T[]) new Object[capacity];
			} else {
				throw new IllegalArgumentException("Illegal Capacity: " + capacity);
			}
		}

		boolean isAddable() {
			return elementDataPointer < elementData.length;
		}

		void add(T element) {
			elementData[elementDataPointer++] = element;
		}

		@Override
		public String toString() {
			return String.format("[sIndex: %d - eIndex: %d | elementDataPointer: %d | elementDataLength: %d]",
					startingIndex, endingIndex, elementDataPointer, elementData.length);
		}
	}
}