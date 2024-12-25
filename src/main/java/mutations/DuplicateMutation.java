package mutations;

import ast.Node;
import ast.Program;
import ast.ProgramImpl;
import ast.Rule;
import ast.Cmd;
import ast.CmdUpdate;
import cms.util.maybe.Maybe;
import main.Util;

/**
 * For nodes with a variable number of children, a randomly selected subtree of the right type
 * (as in Replace mutations) is appended to the end of the list of children. This applies to the root node,
 * where a new rule can be added, and also to command nodes, where the sequence of updates can be
 * extended with another update.
 */
public class DuplicateMutation implements Mutation {

    @Override
    public boolean equals(Mutation m) {
        return m instanceof DuplicateMutation;
    }

    @Override
    public Maybe<Program> apply(Program program, Node node) {
        if (!canApply(node)) return Maybe.none();

        ProgramImpl programImpl = (ProgramImpl) program;

        if (node instanceof ProgramImpl) {
            programImpl.addRule((Rule) getRandomRule((ProgramImpl) node).clone());
        }
        if (node instanceof Rule) {
            Rule r = (Rule) node;

            //add the clone as the child of the rule
            r.addCommand((Cmd) getRandomCommand(r).clone());
        }
        return Maybe.from(program);
    }

    @Override
    public boolean canApply(Node n) {
        if (n instanceof Rule) {
            Rule r = (Rule) n;

            for (int i = 0; i < r.cmdCnt(); i++) {
                if (r.getCommand(i) instanceof CmdUpdate)
                    return true;
            }
        }
        return n instanceof Program;
    }

    private Rule getRandomRule(ProgramImpl prog) {
        return prog.getRule(Util.randomInt(prog.numRules()));
    }

    private Cmd getRandomCommand(Rule r) {
        return r.getCommand(Util.randomInt(r.cmdCnt()));
    }
}