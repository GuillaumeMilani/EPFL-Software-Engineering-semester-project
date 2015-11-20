package ch.epfl.sweng.calamar.utils;

/**
 * A pair is a tuple of two Objects
 *
 * @param <L> The type of the left object
 * @param <R> The type of the right object
 */
public class Pair<L, R> {
    private L left;
    private R right;

    /**
     * Constructs a pair with the two given objects.
     *
     * @param left  The left object
     * @param right The right object
     */
    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    /**
     * Returns the left object
     *
     * @return the left object
     */
    public L getLeft() {
        return left;
    }

    /**
     * Returns the right object
     *
     * @return the right object
     */
    public R getRight() {
        return right;
    }

    /**
     * Sets the left object
     *
     * @param left the value the left object will be set to.
     */
    public void setLeft(L left) {
        this.left = left;
    }

    /**
     * Sets the right object
     *
     * @param right the value the right object will be set to.
     */
    public void setRight(R right) {
        this.right = right;
    }
}
