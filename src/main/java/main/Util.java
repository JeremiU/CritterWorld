package main;

import ast.Node;
import ast.NodeCategory;
import ast.Program;

import java.util.Random;

import static ast.ExprBinary.BinExprOperator;

/**
 * Utility methods for performing mutations as well as other functions
 */
public class Util {

    /**
     * All the different types of mutations
     */
    public enum RulesetType {REMOVE, SWAP, REPLACE, TRANSFORM, INSERT, DUPLICATE}

    /**
     * All NodeCategory constants which represent expressions
     */
    public static final NodeCategory[] exprNodes = new NodeCategory[]
            {NodeCategory.BINARY_OPERATOR, NodeCategory.NUMBER, NodeCategory.SENSOR, NodeCategory.MEM};

    /**
     * Return a random node from Program p
     */
    public static Node pickNode(Program p) {
        return p.nodeAt(Util.randomInt(p.size()));
    }

    /**
     * Return a random Ruleset Mutation Type
     */
    public static RulesetType pickRulesetMutation() {
        return randEnumFromClass(RulesetType.class);
    }

    /**
     * Return a random Binary Expression Operator
     */
    public static BinExprOperator randBinExprOpr() {
        return randEnumFromClass(BinExprOperator.class);
    }

    /**
     * Given enumClass, a class of Enums, return a random enum
     */
    public static <E> E randEnumFromClass(Class<E> enumClass) {
        E[] enumConstants = enumClass.getEnumConstants();
        return enumConstants[randomInt(enumConstants.length)];
    }

    /**
     * Given currentType, a constant of enum E, return a different constant of E
     */
    public static <E> E diffRandEnum(E currentType) {
        E newType = currentType;
        while (newType == currentType) {
            newType = (E) randEnumFromClass(currentType.getClass());
        }
        return newType;
    }

    /**
     * Returns an integer in the range [0,max)
     */
    public static int randomInt(int max) {
        if (max < 2) return 0;
        return new Random().nextInt(max);
    }

    /**
     * Returns an integer in the range [0,max) which is not equal to NOT
     */
    public static int diffRandNum(int not, int max) {
        int result = randomInt(max);
        while (result == not) result = randomInt(max);
        return result;
    }

    /**
     * Modulo operation which follows the behavior specified in §14
     *
     * @return positive mod x % y
     */
    public static int properMod(int x, int y) {
        if (y == 0) return 0;
        return Math.floorMod(x, y);
    }
}