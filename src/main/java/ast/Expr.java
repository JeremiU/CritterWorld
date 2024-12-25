package ast;

/**
 * An abstract class representing an Expression. Can be negative.
 * Inheriting classes:
 * • MemExpr: represents a value stored in memory
 * • NumExpr: represents an integer value
 * • SensorExpr: represents a value calculated from a sensor
 * • BinaryExpr: represents a way of performing mathematical operations on 2 Expressions
 */
public abstract class Expr extends AbstractNode {

    private boolean isNegative, hasParentheses;

    /**
     * Set whether the Expression is negative
     *
     * @param isNegative whether the Expression is negative
     */
    public void setNegative(boolean isNegative) {
        this.isNegative = isNegative;
    }

    /**
     * Set whether the Expression has parentheses.
     *
     * @param hasParentheses whether the Expression has parentheses
     */
    public void setParentheses(boolean hasParentheses) {
        this.hasParentheses = hasParentheses;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.EXPRESSION;
    }

    @Override
    public String toString() {
        return isNegative ? "-" : "";
    }

    //only for BinaryExpr, so not public
    protected boolean hasParentheses() {
        return this.hasParentheses;
    }
}