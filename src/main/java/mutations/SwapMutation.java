package mutations;

import ast.*;
import ast.Cmd;
import ast.ExprBinary;
import ast.Expr;
import cms.util.maybe.Maybe;
import main.Util;

import java.util.List;

/**
 * The order of two children of the node is switched. For example, this
 * allows swapping the positions of two rules, or changing a − b to b − a
 */
public class SwapMutation implements Mutation {

    @Override
    public boolean equals(Mutation m) {
        return m instanceof SwapMutation;
    }

    @Override
    public Maybe<Program> apply(Program program, Node node) {
        if (!canApply(node)) return Maybe.none();

        if (node instanceof Program) {
            Program pgm = (Program) node;

            //pick two random children and swap them
            int firstIndex = Util.randomInt(pgm.getChildren().size());
            int secondIndex = Util.diffRandNum(firstIndex, pgm.getChildren().size());

            Node temp = pgm.getChildren().get(firstIndex);
            pgm.getChildren().set(firstIndex, pgm.getChildren().get(secondIndex));
            pgm.getChildren().set(secondIndex, temp);
        }
        if (node instanceof ConditionBinary) {
            ConditionBinary bc = (ConditionBinary) node;

            Condition left = bc.getLeft();
            bc.setLeft(bc.getRight());
            bc.setRight(left);
        }
        if (node instanceof ExprBinary) {
            ExprBinary binOp = (ExprBinary) node;

            Expr left = binOp.getLeft();
            binOp.setLeft(binOp.getRight());
            binOp.setRight(left);
        }
        if (node instanceof Rule) {
            Rule rul = (Rule) node;
            List<Node> children = rul.getChildren();
            //from the rules children we need to find all the
            //commands and then randomly pick two and swap

            //swap the two children
            int firstIndex = Util.randomInt(rul.cmdCnt());
            int secondIndex = Util.diffRandNum(firstIndex, rul.cmdCnt());

            Node temp = children.get(firstIndex);
            children.set(firstIndex, children.get(secondIndex));
            children.set(secondIndex, temp);

            //also swap in the cmdlist in the Rule
            firstIndex = Util.randomInt(rul.cmdCnt());
            secondIndex = Util.diffRandNum(firstIndex, rul.cmdCnt());

            Cmd tempCmd = rul.getCommand(firstIndex);
            rul.setCommand(rul.getCommand(secondIndex), firstIndex);
            rul.setCommand(tempCmd, secondIndex);
        }
        return Maybe.from(program);
    }

    @Override
    public boolean canApply(Node n) {
        return n instanceof Program || n instanceof ConditionBinary ||
                n instanceof ExprBinary || n instanceof Rule;
    }
}