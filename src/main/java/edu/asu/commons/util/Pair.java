package edu.asu.commons.util;

/**
 * Pair utility class.
 */
public class Pair<T, S> {

    private final T first;
    private final S second;

    public Pair(T first, S second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;
        if (object instanceof Pair) {
            try {
                Pair<T, S> other = (Pair<T, S>) object;
                return first.equals(other.first) && second.equals(other.second);
            } catch (ClassCastException exception) {
                return false;
            }
        }
        return false;
    }

    public String toString() {
        return String.format("[%s, %s]", first, second);
    }

}
