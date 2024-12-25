package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import parse.TokenType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a Sensor Expression, which represents information about
 * the Critter's immediate surroundings.
 * <p>
 * The Expression can evaluate to different values depending on the type:
 * • nearby[dir] : the contents of the hex in direction dir
 * • ahead[dist] : the contents directly ahead of the critter dist hexes away
 * • random[num] : generates a random integer in range [0, num)
 * • smell : the direction & distance to the nearest food, up to MAX_SMELL_DISTANCE
 */
public class ExprSensor extends Expr {

	private SensorType sensorType;
	private Maybe<Expr> index;

	/**
	 * Create a sensor of a given type
	 *
	 * @param sensorType the type of sensor this expr will contain
	 */
	public ExprSensor(SensorType sensorType) {
		this.sensorType = sensorType;
	}

	/**
	 * @return the type of sensory (NEARBY, AHEAD, SMELL, RANDOM)
	 */
	public SensorType getSensorType() {
		return sensorType;
	}

	/**
	 * Give the sensor a new type
	 *
	 * @param type the updated sensor type
	 */
	public void setType(SensorType type) {
		this.sensorType = type;
	}

	/**
	 * Gets the index of the sensor (wrapped in a Maybe), which
	 * has different values depending on the sensor type:
	 * • nearby[dir] : the direction [dir] for which to evaluate the hex
	 * • ahead[dist] : the distance [dist] away from the Critter for which to evaluate the hex
	 * • random[num] : 1 more than the maximum of the random; creating the range [0, num)
	 *
	 * @return the index of the sensor (wrapped in a Maybe)
	 */
	public Maybe<Expr> getIndex() {
		return index;
	}

	/**
	 * Sets the index of the sensor, which has
	 * different values depending on the sensor type:
	 * • nearby[dir] : the direction [dir] for which to evaluate the hex
	 * • ahead[dist] : the distance [dist] away from the Critter for which to evaluate the hex
	 * • random[num] : 1 more than the maximum of the random; creating the range [0, num)
	 *
	 * @param index either dir, dist, or num
	 */
	public void setIndex(Expr index) {
		this.index = Maybe.from(index);
	}

	/**
	 * An enumeration of all possible sensor types
	 */
	public enum SensorType {NEARBY, AHEAD, SMELL, RANDOM}

	@Override
	public NodeCategory getCategory() {
		return NodeCategory.SENSOR;
	}

	@Override
	public boolean classInv() {
		if (sensorType == SensorType.SMELL) return true;

		AtomicBoolean indexCorrect = new AtomicBoolean(false);
		this.index.thenDo(x -> indexCorrect.set(x.classInv()));

		return indexCorrect.get() && this.sensorType != null;
	}

	@Override
	public String toString() {
		StringBuilder bldr = new StringBuilder(TokenType.valueOf(sensorType.name()).toString());

		if (this.sensorType != SensorType.SMELL) bldr.append("[").append(index.toString()).append("]");
		return bldr.toString();
	}

	@Override
	public Node clone() {
		AtomicReference<Expr> ec = new AtomicReference<>();
		this.index.thenDo(ec::set);

		ExprSensor clone = new ExprSensor(this.sensorType);
		clone.setIndex(ec.get());
		return clone;
	}
}