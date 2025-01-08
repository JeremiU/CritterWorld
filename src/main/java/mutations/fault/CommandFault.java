package mutations.fault;

import ast.*;
import ast.Cmd;
import ast.CmdServe;
import ast.CmdUpdate;
import ast.ExprNum;
import cms.util.maybe.Maybe;
import main.Util;

import static ast.Cmd.CmdType;

/**
 * A class representing Faults for the Cmd Node
 */
public class CommandFault implements Fault {

    @Override
    public Maybe<Program> apply(Program program, Node node) {
        if (!canApply(node)) return Maybe.none();

        Cmd cmdNode = (Cmd) node;
        CmdType newType = Util.diffRandEnum(cmdNode.getType(), CmdType.class);

        cmdNode.setType(newType);
        if (cmdNode instanceof CmdServe) {
            CmdServe serve = (CmdServe) node;
            serve.setIndex(new ExprNum(Util.randomInt(101)));
        }
        if (cmdNode instanceof CmdUpdate) {
            CmdUpdate updateCmd = (CmdUpdate) node;
            updateCmd.setMemIndex(new ExprNum(Util.randomInt(6)));
        }
        return Maybe.from(program);
    }

    @Override
    public boolean canApply(Node n) {
        return n instanceof Cmd;
    }
}