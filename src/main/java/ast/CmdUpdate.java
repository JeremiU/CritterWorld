package ast;

/**
 * Represents an update. This takes two expressions, one for the memory location,
 * and the other for the value which will be stored in the location.
 */
public class CmdUpdate extends Cmd {

    private Expr memIndex, value;

    /**
     * Create a new Update
     *
     * @param memIndex the index where the value will be stored
     * @param value    the value to set the memory to
     */
    public CmdUpdate(Expr memIndex, Expr value) {
        super(CmdType.UPDATE);

        this.value = value;
        this.memIndex = memIndex;

        this.addChild(value);
        this.addChild(memIndex);
    }

    /**
     * @return the memory index (position) of the Update
     */
    public Expr getMemIndex() {
        return memIndex;
    }

    /**
     * @return the value which the memory will be updated to
     */
    public Expr getValue() {
        return value;
    }

    /**
     * @param memIndex the memory index to update
     */
    public void setMemIndex(Expr memIndex) {
        this.memIndex = memIndex;
    }

    /**
     * @param value the new value to assign to the memory index
     */
    public void setValue(Expr value) {
        this.value = value;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.UPDATE;
    }

    @Override
    public boolean classInv() {
        return super.classInv() && value.classInv() && memIndex.classInv();
    }

    @Override
    public String toString() {
        return "mem[" + memIndex + "] := " + value;
    }

    @Override
    public Node clone() {
        return new CmdUpdate((Expr) this.memIndex.clone(), (Expr) this.value.clone());
    }
}