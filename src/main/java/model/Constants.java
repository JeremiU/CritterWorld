package model;

public final class Constants {

    private Constants() {
    }

    /**
     * BASE_DAMAGE (default: 100)
     * The multiplier for all damage done by attacking
     */
    public static final int BASE_DAMAGE = 100;

    /**
     * DAMAGE_INC (default: 0.2)
     * Controls how quickly increased offensive or defensive ability affects damage
     */
    public static final double DAMAGE_INC = 0.2;

    /**
     * ENERGY_PER_SIZE (default: 500)
     * How much energy a critter can have per point of size
     */
    public static final int ENERGY_PER_SIZE = 500;

    /**
     * FOOD_PER_SIZE (default: 200)
     * How much food is created per point of size when a critter dies
     */
    public static final int FOOD_PER_SIZE = 200;

    /**
     * MAX_SMELL_DISTANCE (default: 10)
     * Maximum distance at which food can be sensed
     */
    public static final int MAX_SMELL_DISTANCE = 10;

    /**
     * ROCK_VALUE (default: -1)
     * The value reported when a rock is sensed
     */
    public static final int ROCK_VALUE = -1;

    /**
     * WIDTH (default: 50)
     * Default number of columns in the world map
     */
    public static final int WIDTH = 50;

    /**
     * HEIGHT (default: 87)
     * Default height of the world map
     */
    public static final int HEIGHT = 87;

    /**
     * MAX_RULES_PER_TURN (default: 999)
     * The maximum number of rules that can be run per critter turn
     */
    public static final int MAX_RULES_PER_TURN = 999;

    /**
     * SOLAR_FLUX (default: 1)
     * Energy gained from sun by doing nothing
     */
    public static final int SOLAR_FLUX = 1;

    /**
     * MOVE_COST (default: 3)
     * Energy cost of moving (per unit size)
     */
    public static final int MOVE_COST = 3;

    /**
     * ATTACK_COST (default: 5)
     * Energy cost of attacking (per unit size)
     */
    public static final int ATTACK_COST = 5;

    /**
     * GROW_COST (default: 1)
     * Energy cost of growing (per size and complexity)
     */
    public static final int GROW_COST = 1;

    /**
     * BUD_COST (default: 9)
     * Energy cost of budding (per unit complexity)
     */
    public static final int BUD_COST = 9;

    /**
     * MATE_COST (default: 5)
     * Energy cost of successful mating (per unit complexity)
     */
    public static final int MATE_COST = 5;

    /**
     * RULE_COST (default: 2)
     * Complexity cost of having a rule
     */
    public static final int RULE_COST = 2;

    /**
     * ABILITY_COST (default: 25)
     * Complexity cost of having an ability point
     */
    public static final int ABILITY_COST = 25;

    /**
     * INITIAL_ENERGY (default: 250)
     * Energy of a newly birthed critter
     */
    public static final int INITIAL_ENERGY = 250;

    /**
     * INITIAL_BIRTHED_SIZE (default: 1)
     * Size of a newly birthed critter
     */
    public static final int INITIAL_BIRTHED_SIZE = 1;

    /**
     * INITIAL_BIRTHED_POSTURE (default: 1)
     * Posture of a newly birthed critter
     */
    public static final int INITIAL_BIRTHED_POSTURE = 0;

    /**
     * MIN_MEMORY (default: 7)
     * Minimum number of memory entries in a critter
     */
    public static final int MIN_MEMORY = 7;

    /**
     * MANNA_COUNT (default: 10)
     * Number of food items randomly dropped onto the map per time step per 1,000 hexes on the board
     */
    public static final int MANNA_COUNT = 10;

    /**
     * Class containing Memory Constants, namely indices
     */
    public static final class MemoryConstants {
        /**
         * mem[0]: the length of the critter’s memory (immutable, always at least 7)
         */
        public static final int MEM_SIZE = 0;

        /**
         * mem[1]: DEFENSIVE ability (immutable, ≥ 1)
         */
        public static final int DEFENSE = 1;

        /**
         * mem[2]: OFFENSIVE ability (immutable, ≥ 1)
         */
        public static final int OFFENSE = 2;

        /**
         * mem[3]: SIZE (variable, but cannot be assigned directly, ≥ 1)
         */
        public static final int SIZE = 3;

        /**
         * mem[4]: ENERGY (variable, but cannot be assigned directly, ≥ 1)
         */
        public static final int ENERGY = 4;

        /**
         * mem[5]: PASS NUMBER, explained below (variable, but cannot be assigned directly, ≥ 1).
         */
        public static final int PASS = 5;

        /**
         * mem[6]: POSTURE (assignable only to values between 0 and 99).
         */
        public static final int POSTURE = 6;
    }

    /**
     * Class containing Direction Constants, i.e. the directions
     */
    public static final class DirectionConstants {

        /**
         * Directly above the Critter
         */
        public static final int TOP = 0;

        /**
         * Above and right of the Critter
         */
        public static final int TOP_RIGHT = 1;

        /**
         * Below and right of the Critter
         */
        public static final int BOTTOM_RIGHT = 2;

        /**
         * Directly below the Critter
         */
        public static final int BOTTOM = 3;

        /**
         * Below and left of the Critter
         */
        public static final int BOTTOM_LEFT = 4;

        /**
         * Above and left of the Critter
         */
        public static final int TOP_LEFT = 5;

        /**
         * Used for modulo operations
         */
        public static final int DIR_AMOUNT = 6;
    }
}