package ast;

import parse.TokenCategory;
import parse.TokenType;

/**
 * Represents a mathematical operation between 2 expressions
 * <p>
 * Extends expression, so a Binary Expression can contain further
 * binary expressions, e.g. (2+3)+4 is a valid binary expression
 */
public class ExprBinary extends Expr {

    private Expr left, right;
    private BinExprOperator opr;

    /**
     * Create a new Binary Expression
     *
     * @param left  the left-hand side Expression
     * @param opr   the mathematical operation between left & right (+,-,mod (%),*,/)
     * @param right the right-hand side Expression
     */
     public ExprBinary(Expr left, BinExprOperator opr, Expr right) {
        this.left = left;
        this.right = right;
        this.opr = opr;

        this.addChild(this.left);
        this.addChild(this.right);

        //logic to see whether parentheses can be removed (bc they are redundant)
        removeParentheses(left, opr, right);
        removeParentheses(right, opr, left);
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
     * @return the mathematical operator (+,-,mod (%),*,/)
     * being applied to left and right expressions
     */
    public BinExprOperator getOpr() {
        return opr;
    }

    /**
     * Set the left-hand expression
     *
     * @param left the new left-hand expression
     */
    public void setLeft(Expr left) {
        this.left = left;
    }

    /**
     * Set the right-hand expression
     *
     * @param right the new right-hand expression
     */
    public void setRight(Expr right) {
        this.right = right;
    }

    /**
     * Set the mathematical operator being applied to left & right
     *
     * @param opr the mathematical operator (+,-,mod (%),*,/)
     */
    public void setOpr(BinExprOperator opr) {
        this.opr = opr;
    }

    /**
     * Enumeration of all different types of mathematical operators.
     * Each constant has a category representing the type of operator it is:
     * either an ADDOP (+,-) or a MULOP (*, /, mod (%))
     */
    public enum BinExprOperator {
        PLUS(TokenCategory.ADDOP),
        MINUS(TokenCategory.ADDOP),
        MUL(TokenCategory.MULOP),
        DIV(TokenCategory.MULOP),
        MOD(TokenCategory.MULOP);

        private final TokenCategory type;

        BinExprOperator(TokenCategory type) {
            this.type = type;
        }
    }

    @Override
    public boolean classInv() {
        return this.left.classInv() && this.right.classInv();
    }

    @Override
    public String toString() {
        return format(left) + " " + TokenType.valueOf(opr.name()) + " " + format(right);
    }

    @Override
    public Node clone() {
        return new ExprBinary((Expr) left.clone(), this.opr, (Expr) right.clone());
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.BINARY_OPERATOR;
    }

    //adds parentheses where necessary
    private String format(Expr e) {
        String returnStr = e.toString();
        if (e.hasParentheses()) returnStr = "(" + returnStr + ")";
        return returnStr;
    }

    //removes redundant parenthesis
    private void removeParentheses(Expr first, BinExprOperator opr, Expr second) {
        if (!(first instanceof ExprBinary) && second instanceof ExprBinary) first.setParentheses(false);

        BinExprOperator firstOpr = (first instanceof ExprBinary) ? ((ExprBinary) first).opr : null;
        BinExprOperator secondOpr = (second instanceof ExprBinary) ? ((ExprBinary) second).opr : null;

        if (firstOpr == null && secondOpr == null) return;

        //all three operators are of same type, i.e. associative property
        if (firstOpr != null && secondOpr != null && firstOpr.type == secondOpr.type && secondOpr.type == opr.type) {
            first.setParentheses(false);
            second.setParentheses(false);
            return;
        }

        //all two operators are of same type, i.e. associative property
        if (firstOpr == null && secondOpr.type == opr.type) second.setParentheses(false);

        // (x * y) + z == x * y + z
        if (firstOpr != null && firstOpr.type == TokenCategory.MULOP) first.setParentheses(false);
    }
}