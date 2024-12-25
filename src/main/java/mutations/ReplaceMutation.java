package mutations;

import ast.*;
import cms.util.maybe.Maybe;
import main.Util;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The node and its descendants are replaced with a randomly selected subtree of the right kind.
 * Randomly selected subtrees are chosen from somewhere in the current AST. The entire AST subtree
 * rooted at the selected node is cloned (deep-copied).
 */
public class ReplaceMutation implements Mutation {

    @Override
    public boolean equals(Mutation m) {
        return m instanceof ReplaceMutation;
    }

    @Override
    public Maybe<Program> apply(Program program, Node node) {
        if (!canApply(node)) return Maybe.none();

        if (node instanceof ConditionBinary) {
            Maybe<Node> possibleCon = ((AbstractNode) node).findNodeOfType(NodeCategory.BINARY_CONDITION);
            possibleCon.thenDo(n -> replaceBinaryCondition((ConditionBinary) node, (ConditionBinary) n));

            Maybe<Node> possibleRel = ((AbstractNode) node).findNodeOfType(NodeCategory.RELATION);
            possibleRel.thenDo(n -> replaceBinaryCondition((ConditionBinary) node, (Condition) n));
            if (!possibleRel.isPresent() && !possibleCon.isPresent()) return Maybe.from(program);
        }
        if (node instanceof ExprBinary) {
            Maybe<Node> mb = null;
            for (NodeCategory nC : Util.exprNodes) {
                if (mb == null || !mb.isPresent()) mb = ((AbstractNode) node).findNodeOfType(nC);
                else break;
            }
            if (mb != null) mb.thenDo(n -> replaceBinaryOperation((ExprBinary) node, (Expr) n.clone()));
        }
        return Maybe.from(program);
    }

    @Override
    public boolean canApply(Node n) {
        if (n instanceof ConditionBinary) {
            // find a BinaryCondition or Relation in child subtree
            Maybe<Node> possibleBinCon = ((AbstractNode) n).findNodeOfType(NodeCategory.BINARY_CONDITION);
            Maybe<Node> possibleRel = ((AbstractNode) n).findNodeOfType(NodeCategory.RELATION);
            if (possibleBinCon.isPresent()) return true;
            if (possibleRel.isPresent()) {
                AtomicBoolean b = new AtomicBoolean();
                // a relation can replace a BinaryCondition only if the parent is a BinaryCondition or Rule
                ((ConditionBinary) n).getParent().thenDo(x -> b.set(x instanceof ConditionBinary || x instanceof Rule));
                if (b.get()) return true;
            }
        }
        if (n instanceof ConditionRelation) {
            return ((AbstractNode) n).findNodeOfType(NodeCategory.RELATION).isPresent();
        }
        if (n instanceof ExprBinary) {
            // find an expression in child subtree
            for (NodeCategory nodeCategory : Util.exprNodes)
                if (((AbstractNode) n).findNodeOfType(nodeCategory).isPresent()) return true;
        }
        return false;
    }

    // replace a BinaryCondition node with the matching replacement
    private void replaceBinaryOperation(ExprBinary nodeToReplace, Expr replacement) {
        AtomicReference<Node> ref = new AtomicReference<>();
        nodeToReplace.getParent().thenDo(ref::set);
        if (ref.get() == null) return;

        for (int i = 0; i < ref.get().getChildren().size(); i++) {
            Node child = ref.get().getChildren().get(i);
            if (child.toString().equals(nodeToReplace.toString())) {
                ref.get().getChildren().remove(i);
                break;
            }
        }

        ((AbstractNode) ref.get()).addChild(replacement);
        nodeToReplace.setParent(null);

        if (ref.get() instanceof ConditionRelation) {
            if (((ConditionRelation) ref.get()).getLeft().equals(nodeToReplace)) {
                System.out.println(((ConditionRelation) ref.get()).getLeft());
                ((ConditionRelation) ref.get()).setLeft(replacement);
            } else {
                ((ConditionRelation) ref.get()).setRight(replacement);
            }
        }
        if (ref.get() instanceof ExprBinary) {
            if (((ExprBinary) ref.get()).getLeft().equals(nodeToReplace)) {
                ((ExprBinary) ref.get()).setLeft(replacement);
            } else {
                ((ExprBinary) ref.get()).setRight(replacement);
            }
        }
    }

    private void replaceBinaryCondition(Condition nodeToReplace, Condition replacement) {
        replacement = (Condition) replacement.clone();
        AtomicReference<Node> ref = new AtomicReference<>();
        nodeToReplace.getParent().thenDo(ref::set);
        if (ref.get() == null) return;

        List<Node> children = ref.get().getChildren();
        for (int i = 0; i < children.size(); i++) {
            if (children.get(i).equals(nodeToReplace)) {
                children.remove(i);
                break;
            }
        } //remove the child nodeToReplace from the (nodeToReplace)'s parents

        ((AbstractNode) ref.get()).addChild(replacement);
        nodeToReplace.setParent(null);

        if (ref.get() instanceof Rule) ((Rule) ref.get()).setCondition(replacement);
        if (ref.get() instanceof ConditionBinary) {
            // need to check if the node to replace is the left or right child
            if (((ConditionBinary) ref.get()).getLeft().equals(nodeToReplace)) {
                ((ConditionBinary) ref.get()).setLeft(replacement);
            } else {
                ((ConditionBinary) ref.get()).setRight(replacement);
            }
        }
    }
}