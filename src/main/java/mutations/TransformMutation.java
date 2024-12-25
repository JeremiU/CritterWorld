package mutations;

import ast.ConditionBinary;
import ast.Node;
import ast.Program;
import ast.ConditionRelation;
import ast.Cmd;
import ast.ExprBinary;
import ast.ExprSensor;
import cms.util.maybe.Maybe;
import mutations.fault.Fault;
import mutations.fault.FaultFactory;

/**
 * The node is replaced with a random, newly created node of the same kind (for example,
 * replacing attack with eat, or + with *), but its children remain the same. Literal integer
 * constants are randomly adjusted up or down by the value of java.lang.Integer.MAX_VALUE/r.nextInt(),
 * where legal, assuming that r is an object of class java.util.Random.
 */
public class TransformMutation implements Mutation {

	@Override
	public boolean equals(Mutation m) {
		return m instanceof TransformMutation;
	}

	@Override
	public Maybe<Program> apply(Program program, Node node) {
		if (!canApply(node)) return Maybe.none();

		Fault fault = null;
		if (node instanceof ExprSensor) fault = FaultFactory.getSensorFault();
		if (node instanceof ExprBinary) fault = FaultFactory.getBinaryExpressionFault();
		if (node instanceof ConditionBinary) fault = FaultFactory.getBinaryConditionFault();
		if (node instanceof ConditionRelation) fault = FaultFactory.getRelationFault();
		if (node instanceof Cmd) fault = FaultFactory.getActionFault();

		return fault.apply(program, node);
	}

	@Override
	public boolean canApply(Node n) {
		return n instanceof ExprSensor || n instanceof ExprBinary || n instanceof ConditionBinary
				|| n instanceof ConditionRelation || n instanceof Cmd;
	}
}