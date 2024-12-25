package model;

import cms.util.maybe.Maybe;

public interface ReadOnlyCritter {

    /**
     * @return critter species.
     */
    String getSpecies();

    /**
     * Hint: you should consider making a defensive copy of the array.
     *
     * @return an array representation of critter's memory.
     */
    int[] getMemory();

    /**
     * @return current program string of the critter.
     */
    String getProgramString();

    /**
     * @return last rule executed by the critter on its previous turn, or {@code Maybe.none()} if it has not
     * executed any.
     */
    Maybe<String> getLastRuleString();
}