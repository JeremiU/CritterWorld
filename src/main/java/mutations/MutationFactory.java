package mutations;

import static main.Util.RulesetType;

/** A factory that produces the public static Mutation objects corresponding to
 *  each mutation
 */
public class MutationFactory {

    /**
     * @return a new DUPLICATE mutation
     */
    public static Mutation getDuplicate() { return new DuplicateMutation(); }

    /**
     * @return a new INSERT mutation
     */
    public static Mutation getInsert() { return new InsertMutation(); }

    /**
     * @return a new REMOVE mutation
     */
    public static Mutation getRemove() { return new RemoveMutation(); }

    /**
     * @return a new REPLACE mutation
     */
    public static Mutation getReplace() { return new ReplaceMutation(); }

    /**
     * @return a new SWAP mutation
     */
    public static Mutation getSwap() {
        return new SwapMutation();
    }

    /**
     * @return a new TRANSFORM mutation
     */
    public static Mutation getTransform() { return new TransformMutation(); }

    /**
     * @return a Mutation based on given type
     */
    public static Mutation fromType(RulesetType type) {
        if (type == RulesetType.DUPLICATE) return MutationFactory.getDuplicate();
        if (type == RulesetType.INSERT) return MutationFactory.getInsert();
        if (type == RulesetType.REMOVE) return MutationFactory.getRemove();
        if (type == RulesetType.REPLACE) return MutationFactory.getReplace();
        if (type == RulesetType.SWAP) return MutationFactory.getSwap();
        if (type == RulesetType.TRANSFORM) return MutationFactory.getTransform();
        return null;
    }
}