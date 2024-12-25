package ast;

/**
 * Represents the Serve Command, i.e. a Critter attempting to
 * converting its energy to food which it places in front of them.
 */
public class CmdServe extends Cmd {

    private Expr index;

    /**
     * Create a new Serve Command
     *
     * @param index how much energy to serve
     */
    public CmdServe(Expr index) {
        super(CmdType.SERVE);
        this.index = index;
        this.addChild(index);
    }

    /**
     * @return the Index of the Serve Command
     */
    public Expr getIndex() {
        return index;
    }

    /**
     * Set this Serve Command's Index
     *
     * @param index index to set
     */
    public void setIndex(Expr index) {
        this.index = index;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.COMMAND;
    }

    @Override
    public boolean classInv() {
        return super.classInv() && index.classInv();
    }

    @Override
    public String toString() {
        return "serve[" + index + "]";
    }

    @Override
    public Node clone() {
        return new CmdServe((Expr) this.index.clone());
    }
}