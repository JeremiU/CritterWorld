package ast;

import parse.TokenType;

/**
 * Represents a Condition comparing 2 expressions using
 * the basic math comparisons (>,>=, =, !=, <=, <)
 */
public class ConditionRelation extends Condition {

    private Expr left, right;
    private RelOperator opr;

    /**
     * Create a new Relation
     * @param left the left-hand side Expression
     * @param opr the comparison between left & right (>,>=, =, !=, <=, <)
     * @param right the right-hand side Expression
     */
    public ConditionRelation(Expr left, RelOperator opr, Expr right) {
        this.left = left;
        this.right = right;
        this.opr = opr;

        this.addChild(left);
        this.addChild(right);
    }

    /**
     * @return the left-hand side Expression
     */
    public Expr getLeft() {
        return left;
    }

    /**
     * @return the right-hand side Expression
     */
    public Expr getRight() {
        return right;
    }

    /**
     * @return the comparison being applied (>,>=, =, !=, <=, <)
     */
    public RelOperator getOpr() {
        return opr;
    }

    /**
     * Set the left-hand expression
     * @param left the new left-hand expression
     */
    public void setLeft(Expr left) {
        this.left = left;
    }

    /**
     * Set the right-hand expression
     * @param right the new right-hand expression
     */
    public void setRight(Expr right) {
        this.right = right;
    }

    /**
     * Set the comparison being applied to left & right
     * @param opr the comparison (>,>=, =, !=, <=, <)
     */
    public void setOpr(RelOperator opr) {
        this.opr = opr;
    }

    /** An enum of all possible relational operators */
    public enum RelOperator {
        LT, //LESS THAN
        LE, //LESS THAN OR EQUAL TO
        EQ, //EQUAL TO
        GT, //GREATER THAN
        GE, //GREATER THAN OR EQUAL TO
        NE //NOT EQUAL TO
    }

    @Override
    public NodeCategory getCategory() { return NodeCategory.RELATION; }

    @Override
    public String toString() {
        return left + " " + TokenType.valueOf(opr.name()) + " " + right;
    }

    @Override
    public boolean classInv() {
        return left.classInv() && right.classInv() && opr != null;
    }

    @Override
    public Node clone() {
        return new ConditionRelation((Expr) left.clone(), this.opr, (Expr) right.clone());
    }
}