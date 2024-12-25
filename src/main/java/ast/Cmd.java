package ast;

/**
 * Represents any type of command (action/update). This can be a:
 * • Serve action: will use type ServeCmd
 * • Update: will use type UpdateCmd
 * • Action: will use the CmdType enum
 */
public class Cmd extends AbstractNode {

    private CmdType type;

    /**
     * Create a new command (action/update) of type type
     *
     * @param type type of command (action/update) being created
     */
    public Cmd(CmdType type) {
        this.type = type;
    }

    /**
     * @return the Command (action/update) type
     */
    public CmdType getType() {
        return type;
    }

    /**
     * Set the Command's type
     *
     * @param type type to set
     */
    public void setType(CmdType type) {
        this.type = type;
    }

    /**
     * Enumeration of all different types of commands.
     * Two types, Update & Serve, have subclasses.
     */
    public enum CmdType {WAIT, FORWARD, BACKWARD, LEFT, RIGHT, EAT, ATTACK, GROW, BUD, MATE, SERVE, UPDATE}

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.ACTION;
    }

    @Override
    public String toString() {
        return type.name().toLowerCase();
    }

    @Override
    public boolean classInv() {
        return super.parent instanceof Rule;
    }

    @Override
    public Node clone() {
        return new Cmd(this.type);
    }
}