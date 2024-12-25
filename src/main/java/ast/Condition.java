package ast;

/**
 * An abstract class representing a Boolean condition in a critter program.
 */
public abstract class Condition extends AbstractNode {

    protected boolean hasBraces;

    /**
     * Set whether a Condition is enclosed by braces
     *
     * @param hasBraces whether to include braces
     */
    public void setBraces(boolean hasBraces) {
        this.hasBraces = hasBraces;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.CONDITION;
    }
}