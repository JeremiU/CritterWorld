package simulation;

import ast.*;
import main.Util;

import java.util.concurrent.atomic.AtomicReference;

import static ast.Cmd.CmdType;
import static model.Constants.MemoryConstants.POSTURE;

public class Interpreter {

    private final Critter critter;
    private final Program program;

    public Interpreter(Critter critter) {
        this.critter = critter;
        this.program = critter.getProgram();
    }

    //Based on the rule required rule execution logic
    //run the Interpreter and find the first rule whose condition
    //is true. If this rule is just an update without an action then
    //return false. If no rule's condition evaluates to true, return false
    public boolean run() {
        boolean actionUpdate = false;
        int numRules = ((ProgramImpl) program).numRules();

        for (int i = 0; i < numRules; i++) {
            Rule rule = ((ProgramImpl) program).getRule(i);
            //for this rule evaluate the condition
            boolean condPassed = parseCond((Condition) rule.nodeAt(1));
            System.out.println(rule.nodeAt(1) + " --> " + rule.nodeAt(2) + ";");
            //if the condition is true, evaluate the command
            if (!condPassed) return false;
            //execute all the commands one after the other
            for (int c = 0; c < rule.cmdCnt(); c++) {
                parseCommand(rule.getCommand(c));
                if (rule.getCommand(c).getType() != Cmd.CmdType.UPDATE) {
                    System.out.println("Action command " + rule.getCommand(c).getType() + " executed ");
                    actionUpdate = true;
                    break;
                }
            }
        }
        return actionUpdate;
    }

    public boolean parseCond(Condition cond) {
        if (cond instanceof ConditionBinary) {
            ConditionBinary binCond = (ConditionBinary) cond;
            boolean a = parseCond((Condition) cond.nodeAt(1));
            boolean b = parseCond((Condition) cond.nodeAt(2));
            if (binCond.getOpr() == ConditionBinary.BinCondOperator.OR) return a || b;
            if (binCond.getOpr() == ConditionBinary.BinCondOperator.AND) return a && b;
        }
        return parseRel((ConditionRelation) cond);
    }

    public boolean parseRel(ConditionRelation relation) {
        switch (relation.getOpr()) {
            case EQ:
                return parseExpr(relation.getLeft()) == parseExpr(relation.getRight());
            case GE:
                return parseExpr(relation.getLeft()) >= parseExpr(relation.getRight());
            case GT:
                return parseExpr(relation.getLeft()) > parseExpr(relation.getRight());
            case LE:
                return parseExpr(relation.getLeft()) <= parseExpr(relation.getRight());
            case LT:
                return parseExpr(relation.getLeft()) < parseExpr(relation.getRight());
            case NE:
                return parseExpr(relation.getLeft()) != parseExpr(relation.getRight());
        }
        return false;
    }

    public int parseExpr(Expr expr) {
        if (expr instanceof ExprBinary) {
            ExprBinary binExpr = (ExprBinary) expr;
            switch (binExpr.getOpr()) {
                case PLUS:
                    return parseExpr(binExpr.getLeft()) + parseExpr(binExpr.getRight());
                case MINUS:
                    return parseExpr(binExpr.getLeft()) - parseExpr(binExpr.getRight());
                case MUL:
                    return parseExpr(binExpr.getLeft()) * parseExpr(binExpr.getRight());
                case DIV:
                    if (parseExpr(binExpr.getRight()) == 0) return 0;
                    return Math.floorDiv(parseExpr(binExpr.getLeft()), parseExpr(binExpr.getRight()));
                case MOD:
                    return Util.properMod(parseExpr(binExpr.getLeft()), parseExpr(binExpr.getRight()));
            }
        }
        if (expr instanceof ExprMem) {
            int index = parseExpr(((ExprMem) expr).getIndex());
            if (index > critter.getMemSize() || index < 0) return 0;
            return critter.getMemory()[index];
        }
        if (expr instanceof ExprNum) return ((ExprNum) expr).getVal();

        if (expr instanceof ExprSensor) {
            ExprSensor sensorExpr = (ExprSensor) expr;
            switch (sensorExpr.getSensorType()) {
                case AHEAD: {
                    AtomicReference<Expr> sensorIndex = new AtomicReference<>();
                    sensorExpr.getIndex().thenDo(sensorIndex::set);

                    int index = parseExpr(sensorIndex.get());
                    if (index < 0) index = 0;
                    return critter.ahead(index);
                }
                case SMELL:
                    return critter.smell(-1);
                case NEARBY: {
                    AtomicReference<Expr> sensorIndex = new AtomicReference<>();
                    sensorExpr.getIndex().thenDo(sensorIndex::set);

                    return critter.nearby(parseExpr(sensorIndex.get()));
                }
                case RANDOM:
                    AtomicReference<Expr> sensorIndex = new AtomicReference<>();
                    sensorExpr.getIndex().thenDo(sensorIndex::set);

                    return critter.random(parseExpr(sensorIndex.get()));
            }
        }
        return 0;
    }

    public void parseCommand(Cmd cmd) {
        if (cmd instanceof CmdServe) {
            CmdServe serveCmd = (CmdServe) cmd;
            int energy = parseExpr(serveCmd.getIndex());
            if (energy < 0) energy = 0;
            critter.serve(energy);
        } else if (cmd instanceof CmdUpdate) {
            CmdUpdate updateCmd = (CmdUpdate) cmd;
            int index = parseExpr(updateCmd.getMemIndex());
            int value = parseExpr(updateCmd.getValue());
            if (index < POSTURE) return; //none of the values before posture can be assigned directly
            if (index == POSTURE && value < 0 || value > 99)
                return; //POSTURE is assignable only to values between 0 and 99.
            critter.setMem(index, value);
        } else {
            //this could be a switch statement, but this is more compact and functionally equivalent
            switch (cmd.getType()) {
                case BUD: critter.bud(); break;
                case EAT: critter.eat(); break;
                case GROW: critter.grow(); break;
                case LEFT: critter.turnLeft(); break;
                case RIGHT: critter.turnRight(); break;
                case BACKWARD: critter.moveBackward(); break;
                case FORWARD: critter.moveForward(); break;
                case MATE: critter.mate(); break;
                case WAIT: critter.rest(); break;
                case ATTACK: critter.attack(); break;
            }
        }
    }
//    public static void main(String[] args) {
//        //TODO: check if this is OK enough for testing
//        World currentWorld = new World();
//
//
//        Critter cr = CritterFactory.fromFile(currentWorld, "files/critter_loader_test_1.txt");
//
//        Interpreter i = new Interpreter(cr);
//        i.run();
//    }
}