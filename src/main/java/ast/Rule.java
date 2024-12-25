package ast;

import parse.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * A representation of a critter rule.
 */
public class Rule extends AbstractNode {

    private Condition condition;
    private final List<Cmd> cmdList = new ArrayList<>();

    /**
     * Represents a rule, i.e. a group of commands that will run if
     * the given condition evaluates to true
     * @param condition condition to decide whether the commands will run
     * @param commands commands to run if condition is true
     */
    public Rule(Condition condition, List<Cmd> commands) {
        setCondition(condition);
        commands.forEach(this::addCommand);
    }

    /**
     * Replace the Rule's condition
     *
     * @param condition new condition to be used
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
        this.addChild(condition);
    }

    /**
     * Add a command to the rule
     *
     * @param cmd command to add to the rule
     */
    public void addCommand(Cmd cmd) {
        cmdList.add(cmd);
        this.addChild(cmd);
    }

    /**
     * @return how many commands are in the rule
     */
    public int cmdCnt() {
        return cmdList.size();
    }

    /**
     * Remove command at index i
     */
    public void removeCmd(int i) {
        cmdList.remove(i);
    }

    /**
     * @return command at index i
     */
    public Cmd getCommand(int i) {
        return cmdList.get(i);
    }

    /**
     * Set a command based off the node index
     * @param cmd the new command
     * @param i the index to replace
     */
    public void setCommand(Cmd cmd, int i) {
        cmdList.set(i, cmd);
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.RULE;
    }

    @Override
    public String toString() {
        return formatList(condition + " " + TokenType.ARR + " ", cmdList) + ";";
    }

    @Override
    public boolean classInv() {
        if (getChildren() != null && this.getChildren().size() >= 2) {
            if (nodeAt(0).getCategory() == NodeCategory.CONDITION) {
                boolean val = true;
                for (Node n : getChildren()) if (!n.classInv()) val = false;
                return val;
            }
        }
        return false;
    }

    @Override
    public Node clone() {
        return new Rule((Condition) this.condition.clone(), new ArrayList<>(this.cmdList));
    }

    private String formatList(String s, List<?> list) {
        String buffer = " ".repeat(s.length() - 1);
        String listStr = list.toString();
        listStr = listStr.substring(1, listStr.length() - 1);
        listStr = listStr.replace(",", "\n" + buffer);
        return s + listStr;
    }
}