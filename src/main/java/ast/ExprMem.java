package ast;

/**
 * Represents a value stored in a critter's memory.
 */
public class ExprMem extends Expr {

    private Expr index;

    /**
     * Create a new Memory Expression
     *
     * @param index the index of the critter's memory
     */
    public ExprMem(Expr index) {
        this.index = index;
        this.index.setParentheses(true);
    }

    /**
     * @return the memory index, i.e. the value in between the brackets: mem[index]
     */
    public Expr getIndex() {
        return index;
    }

    /**
     * Sets a new memory index
     *
     * @param index set the memory index
     */
    public void setIndex(Expr index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "mem[" + index + "]";
    }

    @Override
    public boolean classInv() {
        return index != null && index.classInv();
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.MEM;
    }

    @Override
    public Node clone() {
        return new ExprMem((Expr) this.index.clone());
    }
}