package mutations;

import ast.*;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import main.Util;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A newly created node is inserted as the parent of the mutated node. The old parent of the
 * mutated node becomes the parent of the inserted node, and the mutated node becomes a child of the
 * inserted node. If the inserted node requires more than one child, the children that are not the original
 * node are copies of randomly chosen nodes of the right kind from the entire rule set.
 */
public class InsertMutation implements Mutation {

    @Override
    public boolean equals(Mutation m) {
        return m instanceof InsertMutation;
    }

    @Override
    public Maybe<Program> apply(Program program, Node node) {
        if (!canApply(node)) return Maybe.none();

        if (node instanceof ConditionBinary) applyToBinaryCondition(program, (ConditionBinary) node);
        if (node instanceof ConditionRelation) applyToRelation(program, (ConditionRelation) node);
        if (node instanceof ExprBinary) applyToBinaryOperation(program, (ExprBinary) node);
        else if (node instanceof Expr) this.applyToExpr(program, (Expr) node);
        return Maybe.from(program);
    }

    @Override
    public boolean canApply(Node n) {
        return (n instanceof ConditionBinary) || (n instanceof ConditionRelation) || (n instanceof ExprBinary)
                || (n instanceof ExprMem) || (n instanceof ExprSensor) || (n instanceof ExprNum);
    }

    private void applyToBinaryCondition(Program program, ConditionBinary bcon) {
        // create another BinaryCondition node and insert it in between
        // this node and its parent

        Condition right = null;
        // find a matching relation node
        List<Node> nodes = ((AbstractNode) program).findAllNodesOfType(NodeCategory.RELATION);
        if ((nodes != null) && (nodes.size() > 0))
            right = (Condition) nodes.get(0).clone();

        // since we could not find a matching Relation, look for matching
        // BinaryCondition
        if (right == null) {
            nodes = ((AbstractNode) program).findAllNodesOfType(NodeCategory.BINARY_CONDITION);
            for (Node n : nodes)
                if (n != bcon) {
                    right = (Condition) n.clone();
                    break;
                }
        }
        if (right == null) return;

        AtomicReference<Node> ref = new AtomicReference<>();
        bcon.getParent().thenDo(ref::set);
        if (ref.get() == null) return;

        ConditionBinary nBcon = dup2(ref.get(), bcon, right);

        // if the parent is a Rule
        // then set the new condition in the rule
        //the setConditon method also adds the bCon as the child of the rule
        if (ref.get() instanceof Rule) ((Rule) ref.get()).setCondition(nBcon);
        else
            // add the new node as the child of the parent
            ((AbstractNode) ref.get()).addChild(nBcon);
    }

    private void applyToRelation(Program program, ConditionRelation rel) {
        Condition right = null;
        // find a matching relation node
        List<Node> nodes = ((AbstractNode) program).findAllNodesOfType(NodeCategory.RELATION);
        for (Node n : nodes) {
            if (rel != n) {
                right = (Condition) n.clone();
                break;
            }
        }

        // since we could not find a matching Relation, look for matching
        // BinaryCondition
        if (right == null) {
            nodes = ((AbstractNode) program).findAllNodesOfType(NodeCategory.BINARY_CONDITION);
            if ((nodes != null) && (nodes.size() > 0))
                right = (Condition) nodes.get(0).clone();
        }
        if (right == null) return;

        AtomicReference<Node> parent = new AtomicReference<>();
        rel.getParent().thenDo(parent::set);
        if (parent.get() == null) return;

        ConditionBinary nBcon = dup2(parent.get(), rel, right);

        // if the parent is a Rule
        // then set the new condition in the rule
        if (parent.get() instanceof Rule) {
            ((Rule) parent.get()).setCondition(nBcon);
        }
        if (parent.get() instanceof ConditionBinary) {
            ((AbstractNode) parent.get()).addChild(nBcon);

            // need to check if this node is left or right child of its parent
            if (((ConditionBinary) parent.get()).getLeft().equals(rel)) {
                ((ConditionBinary) parent.get()).setLeft(nBcon);
            } else {
                ((ConditionBinary) parent.get()).setRight(nBcon);
            }
        }
    }

    private void applyToBinaryOperation(Program program, ExprBinary bop) {
        // find a BinaryOperation in the entire rule set
        // which is not same as this and add as the right operand
        Expr right;
        // find a matching BinaryOperation node
        List<Node> nodes = ((AbstractNode) program).findAllNodesOfType(NodeCategory.BINARY_OPERATOR);
        for (Node n : nodes) if (bop != n) break;

        // since we could not find a matching BinaryOperation, look for other matching
        // Exp types
        right = findMatchingExpr(program, bop);
        if (right == null) return;

        // get the parent of the node and remove this node from
        // children list
        AtomicReference<Node> parent = new AtomicReference<>();
        bop.getParent().thenDo(parent::set);
        if (parent.get() == null) return;

        ExprBinary nBop = dup(parent.get(), bop, right);

        if (parent.get() instanceof ExprBinary) {
            // need to check if this node is left or right child of its parent
            if (((ExprBinary) parent.get()).getLeft().equals(bop)) {
                // set the left operand to be the new node
                ((ExprBinary) parent.get()).setLeft(nBop);
            } else {
                // set the right operand to be the clone
                ((ExprBinary) parent.get()).setLeft(nBop);
            }
        }
        if (parent.get() instanceof ConditionRelation) {
            if (((ConditionRelation) parent.get()).getLeft().equals(bop)) {
                ((ConditionRelation) parent.get()).setLeft(nBop);
            } else {
                ((ConditionRelation) parent.get()).setLeft(nBop);
            }
        }
    }

    public void applyToExpr(Program program, Expr ex) {
        //if the parent of this Factor is a BinaryOperation
        // create a BinaryOperation node and insert it in between
        // this node and its parent
        AtomicReference<Node> parent = new AtomicReference<>();
        ex.getParent().thenDo(parent::set);
        if (parent.get() instanceof ExprBinary) {
            Expr right;

            // find a matching BinaryOperation node which is not the same as the parent
            List<Node> nodes = ((AbstractNode) program).findAllNodesOfType(NodeCategory.BINARY_OPERATOR);
            for (Node n : nodes)
                if (!parent.get().equals(n)) break;

            // since we could not find a matching BinaryOperation, look for other
            //Expr types
            right = findMatchingExpr(program, ex);

            if (right != null) {
                ExprBinary nBop = dup(parent.get(), ex, right);

                // need to check if this node is left or right child of its parent
                if (((ExprBinary) parent.get()).getLeft().equals(ex)) {
                    // set the left operand to be the new node
                    ((ExprBinary) parent.get()).setLeft(nBop);
                } else {
                    // set the right operand to be the clone
                    ((ExprBinary) parent.get()).setLeft(nBop);
                }
            }
        }
        if (parent.get() instanceof ConditionRelation) {
            Expr right;
            // find a matching BinaryOperation node which is not the same as the parent

            List<Node> nodes = ((AbstractNode) program).findAllNodesOfType(NodeCategory.BINARY_OPERATOR);
            for (Node n : nodes) {
                if (parent.get() != n) {
                    right = (Expr) n.clone();
                    break;
                }
            }

            right = findMatchingExpr(program, ex);

            if (right != null) {
                ExprBinary nBop = dup(parent.get(), ex, right);

                // need to check if this node is left or right child of its parent
                if (((ConditionRelation) parent.get()).getLeft().equals(ex)) {
                    // set the left operand to be the new node
                    ((ConditionRelation) parent.get()).setLeft(nBop);
                } else {
                    // set the right operand to be the clone
                    ((ConditionRelation) parent.get()).setRight(nBop);
                }
            }
        }
    }

    private Expr findMatchingExpr(Program program, Expr selfRef) {
        if (selfRef == null) return null;
        Expr clone = null;

        for (NodeCategory exprNode : Util.exprNodes)
            for (Node n : ((AbstractNode) program).findAllNodesOfType(exprNode))
                if (n != selfRef) {
                    clone = (Expr) n.clone();
                    break;
                }
        return clone;
    }

    private ExprBinary dup(Node parent, Expr ex, Expr right) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            Node child = parent.getChildren().get(i);
            if (child.equals(ex)) {
                parent.getChildren().remove(i);
                break;
            }
        }
        ExprBinary nBop = new ExprBinary(ex, Util.randBinExprOpr(), right);

        // add the new node as the child of the parent
        ((AbstractNode) parent).addChild(nBop);

        return nBop;
    }

    private ConditionBinary dup2(Node parent, Condition rel, Condition right) {
        for (int i = 0; i < parent.getChildren().size(); i++) {
            Node child = parent.getChildren().get(i);
            if (child.equals(rel)) {
                parent.getChildren().remove(i);
                break;
            }
        }

        //the constructor makes bcon the child of nBcon
        return new ConditionBinary(rel, ConditionBinary.BinCondOperator.AND, right);
    }
}