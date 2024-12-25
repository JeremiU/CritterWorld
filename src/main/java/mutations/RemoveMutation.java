package mutations;

import ast.*;
import cms.util.maybe.Maybe;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The node, along with all its descendants, is removed. If the parent of the node being removed
 * needs a replacement child, one of the node’s direct children of the correct kind is randomly selected. For
 * example, a rule node is simply removed, whereas a binary operation node would be replaced with either
 * its left or its right child. Note that a legal program must contain at least one rule
 */
public class RemoveMutation implements Mutation {

    @Override
    public boolean equals(Mutation m) {
        return m instanceof RemoveMutation;
    }

    @Override
    public Maybe<Program> apply(Program program, Node node) {
        if (!canApply(node)) return Maybe.none();

        if (node instanceof Rule) removeRule((Rule) node);
        if (node instanceof Condition) removeCondition((Condition) node);
        if (node instanceof Cmd) removeCommand((Cmd) node);
        if (node instanceof Expr) removeExpr((Expr) node);

        return Maybe.from(program);
    }

    @Override
    public boolean canApply(Node n) {
        if (n instanceof Rule) return this.canRemoveRule((Rule) n);
        if (n instanceof Condition) return this.canRemoveCondition((Condition) n);
        if (n instanceof Cmd) return this.canRemoveCommand((Cmd) n);
        if (n instanceof Expr) return this.canRemoveExpr((Expr) n);
        return false;
    }

    private boolean canRemoveRule(Rule r) {
        AtomicBoolean val = new AtomicBoolean(false);

        r.getParent().thenDo(x -> val.set(x.getChildren().size() != 1));
        return val.get();
    }

    private boolean canRemoveCommand(Cmd c) {
        AtomicBoolean val = new AtomicBoolean(false);

        c.getParent().thenDo(x -> val.set(((Rule) x).cmdCnt() != 1));
        return val.get();
    }

    public boolean canRemoveCondition(Condition c) {
        // a condition can be removed only if
        // at least one of its immediate children is a condition
        for (int i = 0; i < c.getChildren().size(); i++)
            if (c.getChildren().get(i) instanceof Condition) return true;
        return false;
    }

    private boolean canRemoveExpr(Expr e) {
        return e instanceof ExprBinary;
    }

    private void removeRule(Rule r) {
        AtomicReference<ProgramImpl> parent = new AtomicReference<>();
        r.getParent().thenDo(x -> parent.set((ProgramImpl) x));
        if (parent.get() == null) return;

        for (int i = 0; i < parent.get().numRules(); i++)
            if (parent.get().getRule(i).equals(r)) {
                parent.get().removeRule(i);
                break;
            }
    }

    private void removeCommand(Cmd c) {
        //remove the command from the children list of the parent (Rule)
        AtomicReference<Node> parent = new AtomicReference<>();
        c.getParent().thenDo(parent::set);
        if (parent.get() == null) return;

        for (int i = 0; i < parent.get().getChildren().size(); i++) {
            Node child = parent.get().getChildren().get(i);
            if ((child instanceof Cmd) && child.equals(c)) {
                parent.get().getChildren().remove(i);
                break;
            }
        }

        //remove the command from the command list of the parent
        Rule r = (Rule) parent.get();
        for (int i = 0; i < r.cmdCnt(); i++)
            if (r.getCommand(i).equals(c)) r.removeCmd(i);
    }

    private void removeCondition(Condition c) {
        Condition grandChild = null;
        for (int i = 0; i < c.getChildren().size(); i++) {
            if (c.getChildren().get(i) instanceof Condition) {
                grandChild = (Condition) c.getChildren().get(i);
                break;
            }
        }

        // attach the child condition to the parent and remove this condition
        Node parent = parent(c);
        ((AbstractNode) parent).addChild(grandChild);

        // remove the reference of this condition to its parent
        c.setParent(null);

        // if parent is a Rule set grand child as the condition
        if (parent instanceof Rule) {
            Rule r = (Rule) parent;
            r.setCondition(grandChild);
        }
    }

    private void removeExpr(Expr e) {
        if (!(e instanceof ExprBinary)) return;
        Node parent = parent(e);

        //get the left child of this BinaryOperation and add it to the
        //parent
        Expr left = ((ExprBinary) e).getLeft();
        //the parent will be a relation or a BinaryOperation
        if (parent instanceof ConditionRelation) {
            ConditionRelation rel = (ConditionRelation) parent;
            rel.addChild(left);
            rel.setLeft(left);
        } else {
            ExprBinary bopc = (ExprBinary) parent;
            bopc.addChild(left);
            bopc.setLeft(left);
        }
        left.setParent(parent);

        e.setParent(null);
    }

    private Node parent(AbstractNode node) {
        AtomicReference<Node> parent = new AtomicReference<>();
        node.getParent().thenDo(parent::set);

        // remove this BinaryOperation from the children list of the parent
        for (int i = 0; i < parent.get().getChildren().size(); i++) {
            Node child = parent.get().getChildren().get(i);
            if (child.equals(node)) {
                parent.get().getChildren().remove(i);
                break;
            }
        }
        return parent.get();
    }
}