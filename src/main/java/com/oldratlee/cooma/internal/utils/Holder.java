package com.oldratlee.cooma.internal.utils;

/**
 * Holds a value of type <code>T</code>.
 * 
 * @author oldratlee
 * @since 0.1.0
 **/
public final class Holder<T> {

    /**
     * The value contained in the holder.
     **/
    private volatile T value;
    
    /**
     * Creates a new holder with a <code>null</code> value.
     **/
    public Holder() {
    }

    /**
     * Create a new holder with the specified value.
     * 
     * @param value The value to be stored in the holder.
     **/
    public Holder(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
