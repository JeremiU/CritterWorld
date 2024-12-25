package mutations.fault;

import ast.Node;
import ast.Program;
import cms.util.maybe.Maybe;

/**
 * Fault injection is a cost-effective technique for generating many programs as test cases.
 * The idea is to make small random changes to a valid program to produce many useful test cases.
 */
public interface Fault {

	/**
     * Applies this fault to the given {@code Node} within this {@code
     * Program}
     *
     * @param program the program in which to introduce the fault.
     * @param node the specific node to perform fault injection on.
     * @return a fault injected program or {@code Maybe.none} if the fault injection is
     *      unsuccessful.
     */
    Maybe<Program> apply(Program program, Node node);

    /**
     * Returns true if and only if this type of fault injection can be applied to the
     *     given node.
     * @param n the node to apply fault injection on
     * @return whether this fault injection can be applied to {@code n}
     */
    boolean canApply(Node n);
}