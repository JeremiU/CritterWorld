package mutations.fault;

import ast.Node;
import ast.Program;
import ast.ExprNum;
import ast.ExprSensor;
import cms.util.maybe.Maybe;
import main.Util;

import static ast.ExprSensor.SensorType;

/**
 * A class representing Faults for the Sensor Node
 */
public class SensorFault implements Fault {

	@Override
	public Maybe<Program> apply(Program program, Node node) {

		if (!canApply(node)) return Maybe.none();

		ExprSensor s = (ExprSensor) node;
		SensorType newType = Util.diffRandEnum(s.getSensorType());

		s.setType(newType);
		if (newType != SensorType.SMELL) s.setIndex(new ExprNum(Util.randomInt(101)));

		return Maybe.from(program);
	}

	@Override
	public boolean canApply(Node n) {
		return n instanceof ExprSensor;
	}
}