package simulation;

import ast.*;
import console.Logger;
import main.Util;

import java.util.concurrent.atomic.AtomicReference;

import static model.Constants.MemoryConstants.POSTURE;

public class Interpreter {

    private final Critter critter;
    private final Program program;

    public Interpreter(Critter critter) {
        this.critter = critter;
        this.program = critter.getProgram();
    }

    public boolean run() {
        boolean actionUpdate = false;
        int numRules = ((ProgramImpl) program).numRules();

        for (int i = 0; i < numRules; i++) {
            Rule rule = ((ProgramImpl) program).getRule(i);
            boolean cmdCondition = parseCond((Condition) rule.nodeAt(1));

            if (!cmdCondition) continue;
            Logger.info("condition true!", "Interpreter:run", Logger.FLAG_INTERPRETER);

            for (int c = 0; c < rule.cmdCnt(); c++) {
                parseCommand(rule.getCommand(c));
                if (rule.getCommand(c).getType() != Cmd.CmdType.UPDATE) {
                    Logger.info("Action command " + rule.getCommand(c).getType() + " executed ", "Interpreter:run", Logger.FLAG_INTERPRETER);
                    actionUpdate = true;
                    break;
                }
            }
            this.critter.setLastRule(rule, i);
            Logger.info("LAST RULE: ("+i+") " + this.critter.getLastRuleString(), "Interpreter:run", Logger.FLAG_INTERPRETER);
            return actionUpdate;
        }
        return actionUpdate;
    }

    public boolean parseCond(Condition cond) {
        if (cond instanceof ConditionBinary binCond) {
            boolean a = parseCond((Condition) cond.nodeAt(1));
            boolean b = parseCond((Condition) cond.nodeAt(2));
            if (binCond.getOpr() == ConditionBinary.BinCondOperator.OR) return a || b;
            if (binCond.getOpr() == ConditionBinary.BinCondOperator.AND) return a && b;
        }
        if (cond instanceof ConditionRelation relCond) return parseRel(relCond);
        return false; //can only be a binary or relation condition
    }

    public boolean parseRel(ConditionRelation relation) {
        return switch (relation.getOpr()) {
            case EQ -> parseExpr(relation.getLeft()) == parseExpr(relation.getRight());
            case GE -> parseExpr(relation.getLeft()) >= parseExpr(relation.getRight());
            case GT -> parseExpr(relation.getLeft()) > parseExpr(relation.getRight());
            case LE -> parseExpr(relation.getLeft()) <= parseExpr(relation.getRight());
            case LT -> parseExpr(relation.getLeft()) < parseExpr(relation.getRight());
            case NE -> parseExpr(relation.getLeft()) != parseExpr(relation.getRight());
        };
    }

    public int parseExpr(Expr expr) {
        if (expr instanceof ExprBinary binExpr) {
            return switch (binExpr.getOpr()) {
                case PLUS -> parseExpr(binExpr.getLeft()) + parseExpr(binExpr.getRight());
                case MINUS -> parseExpr(binExpr.getLeft()) - parseExpr(binExpr.getRight());
                case MUL -> parseExpr(binExpr.getLeft()) * parseExpr(binExpr.getRight());
                case DIV -> {
                    if (parseExpr(binExpr.getRight()) == 0) yield 0;
                    yield Math.floorDiv(parseExpr(binExpr.getLeft()), parseExpr(binExpr.getRight()));
                }
                case MOD -> Util.properMod(parseExpr(binExpr.getLeft()), parseExpr(binExpr.getRight()));
            };
        }
        if (expr instanceof ExprMem) {
            int index = parseExpr(((ExprMem) expr).getIndex());
            if (index > critter.getMemSize() || index < 0) return 0;
            return critter.getMemory()[index];
        }
        if (expr instanceof ExprNum) return ((ExprNum) expr).getVal();

        if (expr instanceof ExprSensor sensorExpr) {
            return switch (sensorExpr.getSensorType()) {
                case AHEAD -> {
                    AtomicReference<Expr> sensorIndex = new AtomicReference<>();
                    sensorExpr.getIndex().thenDo(sensorIndex::set);
                    int index = parseExpr(sensorIndex.get());
                    if (index < 0) index = 0;
                    yield critter.ahead(index);
                }
                case SMELL -> critter.smell(-1);
                case NEARBY -> {
                    AtomicReference<Expr> sensorIndex = new AtomicReference<>();
                    sensorExpr.getIndex().thenDo(sensorIndex::set);

                    yield critter.nearby(parseExpr(sensorIndex.get()));
                }
                case RANDOM -> {
                    AtomicReference<Expr> sensorIndex = new AtomicReference<>();
                    sensorExpr.getIndex().thenDo(sensorIndex::set);
                    yield critter.random(parseExpr(sensorIndex.get()));
                }
            };
        }
        return 0;
    }

    public void parseCommand(Cmd cmd) {
        if (cmd instanceof CmdServe serveCmd) {
            int energy = Math.min(parseExpr(serveCmd.getIndex()), 0);
            critter.serve(energy);
        } else if (cmd instanceof CmdUpdate updateCmd) {
            int index = parseExpr(updateCmd.getMemIndex());
            int value = parseExpr(updateCmd.getValue());
            if (index < POSTURE) return; //none of the values before posture can be assigned directly

            if (index == POSTURE && value < 0) value = 0;
            if (index == POSTURE && value > 99) value = 99;
            critter.setMem(index, value);
        } else {
            switch (cmd.getType()) {
                case BUD -> critter.bud();
                case EAT -> critter.eat();
                case GROW -> critter.grow();
                case LEFT -> critter.turnLeft();
                case RIGHT -> critter.turnRight();
                case BACKWARD -> critter.moveBackward();
                case FORWARD -> critter.moveForward();
                case MATE -> critter.mate();
                case WAIT -> critter.rest();
                case ATTACK -> critter.attack();
            }
        }
    }
}