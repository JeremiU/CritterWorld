package ast;

/**
 * Represents a Binary Condition, comparing two Conditions using either AND/OR
 * <p>
 * Extends expression, so a Binary Condition can contain further
 * binary conditions, e.g. ((mem[6] = 1 AND mem[2] > 3) OR mem[3] = 7) is a valid binary expression
 */
public class ConditionBinary extends Condition {

	private Condition left, right;
	private BinCondOperator opr;

	/**
	 * Create a Binary Condition
	 *
	 * @param left     left condition
	 * @param operator OR/AND
	 * @param right    right condition
	 */
	public ConditionBinary(Condition left, BinCondOperator operator, Condition right) {
		this.left = left;
		this.right = right;
		this.opr = operator;

		this.addChild(left);
		this.addChild(right);

		if (!(this.left instanceof ConditionBinary)) this.left.setBraces(false);
		if (!(this.right instanceof ConditionBinary)) this.right.setBraces(false);

		if (!this.left.hasBraces || !this.right.hasBraces) {
			this.left.setBraces(false);
			this.right.setBraces(false);
			return;
		}

		BinCondOperator leftOp = ((ConditionBinary) left).opr;
		BinCondOperator rightOp = ((ConditionBinary) right).opr;

		if (leftOp == rightOp) {
			this.left.setBraces(false);
			this.right.setBraces(false);
		}
	}

	/**
	 * @return the left-hand side Condition
	 */
	public Condition getLeft() {
		return left;
	}

	/**
	 * @return the right-hand side Condition
	 */
	public Condition getRight() {
		return right;
	}

	/**
	 * @return the mathematical operator (AND, OR)
	 * being applied to left and right expressions
	 */
	public BinCondOperator getOpr() {
		return opr;
	}

	/**
	 * Set the left-hand condition
	 *
	 * @param left the new left-hand condition
	 */
	public void setLeft(Condition left) {
		this.left = left;
	}

	/**
	 * Set the right-hand condition
	 *
	 * @param right the new right-hand condition
	 */
	public void setRight(Condition right) {
		this.right = right;
	}

	/**
	 * Set the boolean operator being applied to left & right
	 *
	 * @param opr the boolean operator (AND, OR)
	 */
	public void setOpr(BinCondOperator opr) {
		this.opr = opr;
	}

	/**
	 * An enumeration of all possible binary condition operators.
	 */
	public enum BinCondOperator {AND, OR}

	@Override
	public boolean classInv() {
		return left.classInv() && right.classInv();
	}

	@Override
	public String toString() {
		return format(left) + " " + opr.toString().toLowerCase() + " " + format(right);
	}

	@Override
	public NodeCategory getCategory() {
		return NodeCategory.BINARY_CONDITION;
	}

	@Override
	public Node clone() {
		return new ConditionBinary((Condition) left.clone(), this.opr, (Condition) right.clone());
	}

	//adds braces where necessary
	private String format(Condition c) {
		String returnStr = c.toString();
		if (c.hasBraces) returnStr = "{" + returnStr + "}";
		return returnStr;
	}
}