package mutations.fault;

import ast.*;
import ast.Expr;
import ast.ExprBinary;
import cms.util.maybe.Maybe;
import main.Util;

/**
 * A class representing Faults for the BinaryExpr Node
 */
public class BinaryExpressionFault implements Fault {

	@Override
	public Maybe<Program> apply(Program program, Node node) {
		if (!canApply(node)) return Maybe.none();

		ExprBinary binOpr = (ExprBinary) node;
		binOpr.setOpr(Util.diffRandEnum(binOpr.getOpr()));

		Expr temp = binOpr.getLeft();
		binOpr.setLeft(binOpr.getRight());
		binOpr.setRight(temp);

		return Maybe.from(program);
	}

	@Override
	public boolean canApply(Node n) {
		return n instanceof ExprBinary;
	}
}