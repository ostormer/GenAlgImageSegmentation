package src;

import java.util.Objects;

public class Pair<X, Y> {

    public final X x;
    public final Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X getX() {
        return this.x;
    }

    public Y getY() {
        return this.y;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Pair<?, ?>)) {
            return false;
        }

        // Both elements must be equal
        Pair<?, ?> c = (Pair<?, ?>) o;
        return Objects.equals(c.x, this.x) && Objects.equals(c.y, this.y);
    }

    @Override
    public int hashCode() {
        return (x == null ? 0 : x.hashCode()) ^ (y == null ? 0 : y.hashCode());
    }
}
