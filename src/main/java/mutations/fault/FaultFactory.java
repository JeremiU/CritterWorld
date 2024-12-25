package mutations.fault;

/**
 * A factory that gives access to instances of various Faults.
 */
public class FaultFactory {

	/**
	 * @return a Sensor-specific Fault
	 */
	public static Fault getSensorFault() {
		return new SensorFault();
	}

	/**
	 * @return a Relation-specific Fault
	 */
	public static Fault getRelationFault() {
		return new RelationFault();
	}

	/**
	 * @return an Action-specific Fault
	 */
	public static Fault getActionFault() {
		return new CommandFault();
	}

	/**
	 * @return a Binary Expression-specific Fault
	 */
	public static Fault getBinaryExpressionFault() {
		return new BinaryExpressionFault();
	}

	/**
	 * @return a Binary Condition-specific Fault
	 */
	public static Fault getBinaryConditionFault() {
		return new BinaryConditionFault();
	}
}