package ast;

import cms.util.maybe.Maybe;
import mutations.fault.Fault;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class AbstractNode implements Node {

    private final List<Node> children = new ArrayList<>();
    protected Node parent = null;

    /**
     * Finds the first matching node of NodeCategory type
     *
     * @param type NodeCategory to search for
     */
    public Maybe<Node> findNodeOfType(NodeCategory type) {
        for (int i = 0; i < this.size(); i++) {
            Node node = this.nodeAt(i);

            if (this == node) continue;
            if (node.getCategory() == type) return Maybe.from(node);
        }
        return Maybe.none();
    }

    /**
     * Returns all child nodes of NodeCategory type
     *
     * @param type NodeCategory to search for
     */
    public List<Node> findAllNodesOfType(NodeCategory type) {
        List<Node> nodes = new ArrayList<>();

        for (int i = 0; i < this.size(); i++) {
            Node child = this.nodeAt(i);
            if (child.getCategory() == type) nodes.add(child);
        }
        return nodes;
    }

    /**
     * Returns the parent of this {@code Node}, or {@code Maybe.none} if this {@code
     * Node} is the root.
     *
     * @return the parent of this {@code Node}, or {@code @Maybe.none} if this {@code
     * Node} is the root.
     * <p>
     * This method does not need to be implemented and may be removed from
     * the interface.
     */
    public Maybe<Node> getParent() {
        return Maybe.from(this.parent);
    }

    /**
     * Add a child node, which also set's the child's parent node to this object.
     *
     * @param child node to add
     */
    public void addChild(Node child) {
        this.children.add(child);
        ((AbstractNode) child).parent = this;
    }

    /**
     * Sets the parent of this {@code Node}.
     *
     * @param p the node to set as this {@code Node}'s parent.
     */
    public void setParent(Node p) {
        this.parent = p;
    }

    @Override
    public Maybe<Program> accept(Program p, Fault f) {
        return f.apply(p, this);
    }

    @Override
    public int size() {
        int childCount = 0;
        // perform a BFT
        Queue<Node> nodes = new LinkedList<>();
        nodes.add(this);
        while (!nodes.isEmpty()) {
            Node current = nodes.remove();

            childCount++;
            List<Node> children = current.getChildren();
            if (children != null) nodes.addAll(children);
        }
        return childCount;
    }

    @Override
    public Node nodeAt(int index) {
        // do a BFT and create a list of all children rooted at this node
        List<Node> childSubtree = new LinkedList<>();
        Queue<Node> nodesToProcess = new LinkedList<>();
        nodesToProcess.add(this);

        while (!nodesToProcess.isEmpty()) {
            // process the node at the front of the queue
            Node current = nodesToProcess.remove();
            childSubtree.add(current);
            List<Node> children = current.getChildren();
            if (children != null) nodesToProcess.addAll(children);
        }
        return childSubtree.get(index);
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb) {
        sb.append(this);
        return sb;
    }

    @Override
    public List<Node> getChildren() {
        return this.children;
    }

    /**
     * @return a deep-copy of the Abstract Node
     */
    public abstract Node clone();

    /**
     * @return the String representation of the tree rooted at this Node
     */
    public abstract String toString();
}