package mutations.fault;

import ast.*;
import ast.Expr;
import cms.util.maybe.Maybe;
import main.Util;

/**
 * A class representing Faults for the Relation Node
 */
public class RelationFault implements Fault {

	@Override
	public Maybe<Program> apply(Program program, Node node) {
		if (!canApply(node)) return Maybe.none();

		ConditionRelation rel = (ConditionRelation) node;

		rel.setOpr(Util.diffRandEnum(rel.getOpr()));

		Expr left = rel.getLeft();
		rel.setLeft(rel.getRight());
		rel.setRight(left);

		return Maybe.from(program);
	}

	@Override
	public boolean canApply(Node n) {
		return n instanceof ConditionRelation;
	}
}