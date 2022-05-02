package src;

public enum Gene {
    LEFT, RIGHT, UP, DOWN, NONE;

    public static Gene fromNeighborNumber(int neighbor) {
        Gene g;
        switch (neighbor) {
            case 1 -> g = RIGHT;
            case 2 -> g = LEFT;
            case 3 -> g = UP;
            case 4 -> g = DOWN;
            case 0 -> g = NONE;
            default -> throw new IllegalArgumentException(neighbor + " is not a supported gene number.");
        }
        ;
        return g;
    }

    public static Gene fromUnitVector(int x, int y) {
        // Requires x,y to be a 1 long vector in any cardinal direction
        return switch(x) {
            case  1 -> RIGHT;
            case -1 -> LEFT;
            case  0 -> switch(y) {
                case  1 -> DOWN;
                case -1 -> UP;
                case  0 -> NONE;
                default -> throw new IllegalArgumentException("Not a unit vector: " + x + " " + y);
            };
            default -> throw new IllegalArgumentException("Not a unit vector: " + x + " " + y);
        };
    }

}
