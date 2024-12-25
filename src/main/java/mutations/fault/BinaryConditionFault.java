package mutations.fault;

import ast.*;
import cms.util.maybe.Maybe;

import static ast.ConditionBinary.BinCondOperator;

/**
 * A class representing Faults for the BinaryCondition Node
 */
public class BinaryConditionFault implements Fault {

	@Override
	public Maybe<Program> apply(Program program, Node node) {
		if (!canApply(node)) return Maybe.none();

		ConditionBinary con = (ConditionBinary) node;
		con.setOpr((con.getOpr() == BinCondOperator.AND) ? BinCondOperator.OR : BinCondOperator.AND);

		Condition leftTemp = con.getLeft();
		con.setLeft(con.getRight());
		con.setRight(leftTemp);

		return Maybe.from(program);
	}

	@Override
	public boolean canApply(Node n) {
		return n instanceof ConditionBinary;
	}
}