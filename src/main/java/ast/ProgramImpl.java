package ast;

import cms.util.maybe.Maybe;
import mutations.Mutation;
import mutations.MutationFactory;
import main.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A data structure representing a critter program.
 */
public class ProgramImpl extends AbstractNode implements Program {

    private final List<Rule> ruleList = new ArrayList<>();

    /**
     * @param r rule to add to the program
     */
    public void addRule(Rule r) {
        ruleList.add(r);
        this.addChild(r);
    }

    /**
     * Remove the rule at the specified index
     */
    public void removeRule(int index) {
        this.getChildren().remove(index);
        ruleList.remove(index);
    }

    /**
     * @return the rule at the specified index
     */
    public Rule getRule(int index) {
        return this.ruleList.get(index);
    }

    /**
     * @return amount of rules
     */
    public int numRules() {
        return this.ruleList.size();
    }

    @Override
    public Program mutate() {
        AtomicReference<Program> mutatedProgram = new AtomicReference<>();

        Node n = Util.pickNode(this);

        //should not be null bc pickRulesetMutation doesn't return null
        Mutation m = MutationFactory.fromType(Util.pickRulesetMutation());

        Maybe<Program> mp = m.apply(this, n);

        mp.thenDo(mutatedProgram::set);

        return (mutatedProgram.get() != null) ? mutatedProgram.get() : this;
    }

    @Override
    public Maybe<Program> mutate(int index, Mutation m) {
        return m.apply(this, this.nodeAt(index));
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.PROGRAM;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Rule rule : ruleList) builder.append(rule).append("\n");
        return builder.toString();
    }

    @Override
    public boolean classInv() {
        return this.getChildren() != null && this.getChildren().size() > 0;
    }

    @Override
    public Node clone() {
        ProgramImpl pc = new ProgramImpl();
        for (Rule rul : ruleList) pc.addRule((Rule) rul.clone());
        return pc;
    }
}