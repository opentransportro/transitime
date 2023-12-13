/* (C)2023 */
package org.transitclock.utils;

import java.util.ArrayList;

/**
 * An ArrayList that when remove() is called will trim down the capacity of the list if
 * significantly below its max size. This way if an array is temporarily large then it will be
 * trimmed back down when it gets smaller, and will use less memory.
 *
 * <p>This class is not intended to perfectly keep track of the maximum size of the array. It is
 * only intended to trim the array capacity when it is known that not as much is needed.
 *
 * @author Skibu Smith
 */
public class TrimmableArrayList<E> extends ArrayList<E> {

    private int maxSize = 0;

    // If size() becomes below maxSize by more than TRIM_THRESHOLD then
    // capacity will be trimmed
    private static final int TRIM_THRESHOLD = 10;

    private static final long serialVersionUID = 5708182748798889326L;

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param initialCapacity
     */
    public TrimmableArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    /**
     * Add element and keep track of max size.
     *
     * @param e element to be appended to this list
     * @return true (as specified by Collection.add(E))
     */
    @Override
    public boolean add(E e) {
        boolean result = super.add(e);
        maxSize = Math.max(maxSize, size());
        return result;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     */
    @Override
    public void add(int index, E element) {
        super.add(index, element);
        maxSize = Math.max(maxSize, size());
    }

    /**
     * Removes element. Also trims capacity to size if size() becomes below maxSize by more than
     * TRIM_THRESHOLD
     *
     * @param index the index of the element to be removed
     */
    @Override
    public E remove(int index) {
        E result = super.remove(index);

        // If size() becomes below maxSize by more than TRIM_THRESHOLD then
        // trim capacity
        if (size() < maxSize - TRIM_THRESHOLD) {
            trimToSize();
            maxSize = size();
        }

        return result;
    }

    /**
     * Returns probably maximum size of the array
     *
     * @return maxSize
     */
    public int getMaxSize() {
        return maxSize;
    }
}
