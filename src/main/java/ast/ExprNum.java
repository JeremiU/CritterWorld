package ast;

/**
 * An Expression representing an integer value.
 */
public class ExprNum extends Expr {

    private final int val;

    /**
     * Create a new Number Expression
     *
     * @param val the integer value to represent
     */
    public ExprNum(int val) {
        this.val = val;
    }

    /**
     * @return the integer value of this NumExpr
     */
    public int getVal() {
        return val;
    }

    @Override
    public boolean classInv() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + val;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.NUMBER;
    }

    @Override
    public Node clone() {
        return new ExprNum(this.getVal());
    }
}