/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jcsp.helpers;

import jcsp.util.BufferSizeError;
import jcsp.util.ChannelDataStore;
import static jcsp.util.ChannelDataStore.EMPTY;
import static jcsp.util.ChannelDataStore.NONEMPTYFULL;
import jcsp.util.InfiniteBuffer;

/**
 * This is a copy of InfiniteBuffer, but you can get the number of messages
 * stored in the buffer via `getCount()`, AND `clone()` returns `this`, to
 * circumvent some internal JCSP protections.
 * 
 * @see InfiniteBuffer
 */
public class BackpressureBuffer<T> implements ChannelDataStore<T> {
    /** The default size of the buffer */
    private static final int DEFAULT_SIZE = 8;

    /** The initial size of the buffer */
    private int initialSize;

    /** The storage for the buffered Objects */
    private T[] buffer;

    /** The number of Objects stored in the BackpressureBuffer */
    private int counter = 0;

    /** The index of the oldest element (when counter > 0) */
    private int firstIndex = 0;

    /** The index of the next free element (when counter < buffer.length) */
    private int lastIndex = 0;

    /**
     * Construct a new <TT>BackpressureBuffer</TT> with the default size (of 8).
     */
    public BackpressureBuffer()
    {
        this(DEFAULT_SIZE);
    }

    /**
     * Construct a new <TT>BackpressureBuffer</TT> with the specified initial size.
     *
     * @param initialSize the number of Objects
     * the <TT>BackpressureBuffer</TT> can initially store.
     * @throws BufferSizeError if <TT>size</TT> is zero or negative.  Note: no action
     * should be taken to <TT>try</TT>/<TT>catch</TT> this exception
     * - application code generating it is in error and needs correcting.
     */
    public BackpressureBuffer(int initialSize)
    {
        if (initialSize <= 0)
            throw new BufferSizeError
                    ("\n*** Attempt to create a buffered channel with an initially negative or zero capacity");
        this.initialSize = initialSize;
        buffer = (T[]) new Object[initialSize];
    }

    /**
     * Returns the oldest <TT>Object</TT> from the <TT>BackpressureBuffer</TT> and removes it.
     * <P>
     * <I>Pre-condition</I>: <TT>getState</TT> must not currently return <TT>EMPTY</TT>.
     *
     * @return the oldest <TT>Object</TT> from the <TT>BackpressureBuffer</TT>
     */
    public T get()
    {
        T value = buffer[firstIndex];
        buffer[firstIndex] = null;
        firstIndex = (firstIndex + 1) % buffer.length;
        counter--;
        return value;
    }
    
    /**
     * Returns the oldest object from the buffer but does not remove it.
     * 
     * <I>Pre-condition</I>: <TT>getState</TT> must not currently return <TT>EMPTY</TT>.
     *
     * @return the oldest <TT>Object</TT> from the <TT>Buffer</TT>
     */
    public T startGet()
    {
      return buffer[firstIndex];
    }
    
    /**
     * Removes the oldest object from the buffer.     
     */
    public void endGet()
    {
      buffer[firstIndex] = null;
      firstIndex = (firstIndex + 1) % buffer.length;
      counter--;
    }

    /**
     * Puts a new <TT>Object</TT> into the <TT>BackpressureBuffer</TT>.
     * <P>
     * <I>Implementation note:</I> if <TT>BackpressureBuffer</TT> is full, a new internal
     * buffer with double the capacity is constructed and the old data copied across.
     *
     * @param value the Object to put into the BackpressureBuffer
     */
    public void put(T value)
    {
        if (counter == buffer.length)
        {
            T[] temp = buffer;
            buffer = (T[]) new Object[buffer.length * 2];
            System.arraycopy(temp, firstIndex, buffer, 0, temp.length - firstIndex);
            System.arraycopy(temp, 0, buffer, temp.length - firstIndex, firstIndex);
            firstIndex = 0;
            lastIndex = temp.length;
        }
        buffer[lastIndex] = value;
        lastIndex = (lastIndex + 1) % buffer.length;
        counter++;
    }

    /**
     * Returns the current state of the <TT>BackpressureBuffer</TT>.
     *
     * @return the current state of the <TT>BackpressureBuffer</TT> (<TT>EMPTY</TT> or
     * <TT>NONEMPTYFULL</TT>)
     */
    public int getState()
    {
        if (counter == 0)
            return EMPTY;
        else
            return NONEMPTYFULL;
    }

    /**
     * RETURNS `THIS`.<br/>
     * Buffers are cloned when passed to a channel, which defeats the purpose of this class.
     * 
     * @return `this`
     */
    public Object clone()
    {
        return this;
    }
    
    public void removeAll()
    {
        counter = 0;
        firstIndex = 0;
        lastIndex = 0;
        
        for (int i = 0;i < buffer.length;i++) {
        	//Null the objects so they can be garbage collected:
        	buffer[i] = null;
        }
    }

    /**
     * 
     * @return the number of messages currently stored in the buffer
     */
    public int getCount() {
        return counter;
    }
}
