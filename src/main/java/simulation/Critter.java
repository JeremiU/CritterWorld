package simulation;

import ast.Program;
import ast.ProgramImpl;
import ast.Rule;
import cms.util.maybe.Maybe;
import console.Logger;
import javafx.scene.paint.Color;
import main.Util;
import model.ReadOnlyCritter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static model.Constants.*;
import static model.Constants.DirectionConstants.DIR_AMOUNT;
import static model.Constants.MemoryConstants.*;


/**
 * Represents a critter in the world
 */
public class Critter implements ReadOnlyCritter {
    private World currentWorld;

    private final String species;
    private int direction;
    private Coordinate location;

    private final int[] mem;

    private final ProgramImpl program;

    private Rule lastRule;
    private int lastRuleLine = 0;

    private Color color = Color.AQUAMARINE;

    private int stepVal;

    public Critter(World currentWorld, String species, int memSize, int defense, int offense, int size, int energy, int posture, ProgramImpl program) {
        if (memSize < MIN_MEMORY) memSize = MIN_MEMORY;
        if (defense < 1) defense = 1;
        if (offense < 1) offense = 1;
        if (energy < 1) energy = INITIAL_ENERGY;
        if (posture > 99 || posture < 0) posture = INITIAL_POSTURE;

        mem = new int[memSize];
        mem[MEM_SIZE] = memSize;
        mem[DEFENSE] = defense;
        mem[OFFENSE] = offense;
        mem[SIZE] = size;
        mem[ENERGY] = energy;
        mem[POSTURE] = posture;
        this.program = program;
        this.species = species;

        this.currentWorld = currentWorld;
        direction = Util.randomInt(DIR_AMOUNT);
    }

    public void setLocation(Coordinate coordinate) {
        this.location = coordinate;
    }

    public void setLocation(int column, int row) {
        this.location = new Coordinate(column, row);
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getColumn() {
        return location.column();
    }

    public int getRow() {
        return location.row();
    }

    public String getSpecies() {
        return species;
    }

    @Override
    public int[] getMemory() {
        return Arrays.copyOf(this.mem, this.mem.length);
    }

    @Override
    public String getProgramString() {
        if (this.program == null || this.program.numRules() < 0) return "";

        StringBuilder prettyPgm = new StringBuilder();
        this.program.prettyPrint(prettyPgm);
        return prettyPgm.toString();
    }

    @Override
    public Maybe<String> getLastRuleString() {
        if (this.lastRule == null) return Maybe.none();

        StringBuilder prettyRule = new StringBuilder();
        lastRule.prettyPrint(prettyRule);

        return Maybe.from(prettyRule.toString());
    }

    public void setLastRule(Rule rule, int line) {
        this.lastRule = rule;
        this.lastRuleLine = line;
    }

    public int getLastRuleLine() {
        return lastRuleLine;
    }

    public int getMemSize() {
        return this.mem[MEM_SIZE];
    }

    public int getSize() {
        return this.mem[SIZE];
    }

    public int getPosture() {
        return this.mem[POSTURE];
    }

    public void tick() {
        Interpreter ip = new Interpreter(this);
        boolean actionUpdate = ip.run();
        this.mem[PASS] = 1;
        while (this.mem[PASS] < MAX_RULES_PER_TURN) {
            if (actionUpdate) break;
            actionUpdate = ip.run();
            this.mem[PASS]++;
        }
        rest();
        this.currentWorld.addFood();
    }

    public int getComplexity() {
        return getProgram().numRules() * RULE_COST + (mem[OFFENSE] + mem[DEFENSE] * ABILITY_COST);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * The critter waits until the next turn without doing anything except absorbing solar energy.
     */
    public void rest() {
        incrementEnergy(SOLAR_FLUX * mem[SIZE]);
    }

    /**
     * The critter rotates 60 degrees left. This takes little energy
     */
    public void turnLeft() {
        turn(-1);
    }

    /**
     * The critter rotates 60 degrees right. This takes little energy
     */
    public void turnRight() {
        turn(1);
    }

    private void turn(int dir) {
        if (!decrementEnergy(mem[SIZE])) return;
        direction = Math.floorMod((direction + dir), DIR_AMOUNT); //§2 fig. 1; §14
    }

    /**
     * A critter uses some energy to move forward to the hex in front of it.
     * If it attempts to move and there is a critter, food, or a rock in
     * the destination hex, the move fails but still takes energy
     */
    public void moveForward() {
        move(direction);
    }

   public void moveBackward() {
        move(Util.properMod(direction + DIR_AMOUNT / 2, DIR_AMOUNT));
    }

    private void move(int dir) {
        if (!decrementEnergy(mem[SIZE] * MOVE_COST) || ahead(1) != 0) return;
        if (!Hex.isValidHexCoordinate(location.getCoordinateAt(dir).column(), location.getCoordinateAt(dir).row())) return;
        if (currentWorld.hexAt(location.getCoordinateAt(dir)).getType() != Hex.HexType.EMPTY) return;

        Coordinate oldLocation = location;
        location = location.getCoordinateAt(dir);

        currentWorld.hexAt(oldLocation).setType(Hex.HexType.EMPTY);
        currentWorld.hexAt(location).setType(Hex.HexType.CRITTER);
        currentWorld.hexAt(location).setCritter(this);

        currentWorld.updateHex(oldLocation);
        currentWorld.updateHex(location);
    }

    /**
     * The critter may eat some of the food that might be available on the hex ahead of it, gaining the same
     * amount of energy as the food it consumes. It eats as much as it can; when the hex has more food than the
     * critter can absorb, the excess food is left on the hex.
     */
    public void eat() {
        Logger.info("EAT", "Critter:eat", Logger.FLAG_CRITTER_ACTION);
        if (!decrementEnergy(mem[SIZE])) return;
        if (ahead(1) >= -1) return; //NO FOOD FOUND
        int foodEnergy = -ahead(1) - 1; //-n -1

        Hex foodHex = hexAhead(1);
        if (foodEnergy != foodHex.getFoodValue()) Logger.error("FOOD ENERGY != FOOD HEX", "Critter:eat", Logger.FLAG_CRITTER_ACTION);

        int energyTaken = Math.min(foodEnergy, ENERGY_PER_SIZE * mem[SIZE]); //if foodVal > maximum, inc max

        incrementEnergy(energyTaken);
        foodHex.setFoodValue(foodHex.getFoodValue() - energyTaken);
        if (foodHex.getFoodValue() == 0) foodHex.setType(Hex.HexType.EMPTY);
        else foodHex.setType(Hex.HexType.FOOD);
        currentWorld.updateHex(foodHex.getCoordinate());
    }

    /**
     * SERVE
     * A critter may convert some of its own energy into food added to the hex in front of it, if that hex
     * is either empty or already contains some food.
     */
    public void serve(int energy) {
        Logger.info("SERVE," + energy, "Critter:serve", Logger.FLAG_CRITTER_ACTION);

        Hex frontHex = hexAhead(1);

        if (frontHex.getType() != Hex.HexType.FOOD && frontHex.getType() != Hex.HexType.EMPTY) return;

        energy = Math.min(energy, mem[ENERGY]); //ensures energy is not overspent
        decrementEnergy(mem[SIZE] + energy);

        frontHex.setFoodValue(frontHex.getFoodValue() + energy);
        currentWorld.updateHex(frontHex.getCoordinate());
        //TODO CRITTER CANT DIE OFF A SERVE
    }

    /**
     * ATTACK
     * A critter may attack another critter directly in front of it. The attack removes an
     * amount of energy from the attacked critter that is determined by the size and
     * offensive ability of the attacker and the defensive ability of the victim
     */
    public void attack() {
        Logger.info("ATTACK", "Critter:attack", Logger.FLAG_CRITTER_ACTION);
        if (!decrementEnergy(mem[SIZE] * ATTACK_COST)) return;

        Critter enemy = currentWorld.hexAt(location.getCoordinateAt(direction)).getCritter();
        if (enemy == null) return;

        int damage = (int) Math.round(BASE_DAMAGE * mem[SIZE] * p(DAMAGE_INC * (mem[SIZE] * mem[OFFENSE] - enemy.mem[SIZE] * enemy.mem[OFFENSE])));
        enemy.decrementEnergy(damage);
    }

    //the function p(x) as described in §10, used to simplify damage
    private double p(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * GROW
     * A critter may use energy to increase its size by one unit.
     */
    public void grow() {
        Logger.info("GROW", "Critter:grow", Logger.FLAG_CRITTER_ACTION);
        if (!decrementEnergy(mem[SIZE] * this.getComplexity() * GROW_COST)) return;
        mem[SIZE]++;
    }

    /**
     * BUD
     * A critter may use a large amount of its energy to produce a new, smaller critter behind
     * it with the same genome (possibly with some random mutations).
     */
    public void bud() {
        Logger.info("BUD", "Critter:bud", Logger.FLAG_CRITTER_ACTION);
        if (!decrementEnergy(BUD_COST * this.getComplexity())) return;
        Logger.info("bud condition passed", "Critter:bud", Logger.FLAG_CRITTER_ACTION);

        //Random mutation is performed on this Critter's program
        Program pc = (Program) this.program.clone();
        ProgramImpl mpc = (ProgramImpl) pc.mutate();
        Critter child = new Critter(currentWorld, species, mem[MEM_SIZE], mem[DEFENSE], mem[OFFENSE], INITIAL_SIZE, INITIAL_ENERGY, INITIAL_POSTURE, mpc);

        Coordinate coord = location.getBottom();
        if (currentWorld.hexAt(coord).getType() == Hex.HexType.INVALID) coord = location.getTop();

        currentWorld.insertCritterAtLocation(child, coord.row(), coord.column());
        Logger.info("BUD done", "Critter:bud", Logger.FLAG_CRITTER_ACTION);
    }

    public void mate() {
        Hex nearbyHex = hexAhead(1);
        if (nearbyHex.getType() != Hex.HexType.CRITTER) return;

        Critter partner = nearbyHex.getCritter();
        boolean thisIsMainPartner = Util.randomInt(2) == 1;
        int[] inheritedMem = thisIsMainPartner ? this.mem : partner.mem;

        //pick rules starting from the parent having the least number
        //of rules. Rules are picked in sequence from each parent
        ProgramImpl cProgram = new ProgramImpl();
        ProgramImpl father;
        ProgramImpl mother;
        if (this.program.numRules() < partner.program.numRules()) {
            mother = this.program;
            father = partner.program;
        } else {
            mother = partner.program;
            father = this.program;
        }

        for (int i = 0; i < mother.numRules(); i++)
            cProgram.addRule((i % 2 == 0) ? (Rule) mother.getRule(i).clone() : (Rule) father.getRule(i).clone());

        Critter child = new Critter(currentWorld, species, inheritedMem[MEM_SIZE], inheritedMem[DEFENSE], inheritedMem[OFFENSE], INITIAL_SIZE, INITIAL_ENERGY, INITIAL_POSTURE, cProgram);

        //place the child behind one of the parents chosen randomly
        Hex behindHex = currentWorld.hexAt(thisIsMainPartner ? location.getCoordinateAt(3) : partner.location.getCoordinateAt(3));
        if (behindHex.getType() != Hex.HexType.CRITTER)
            currentWorld.insertCritterAtLocation(child, behindHex.getColumn(), behindHex.getRow());
    }

    /**
     * The expression nearby[dir] reports the contents of the hex in direction dir,
     * where 0 ≤ dir ≤ 5. Here the direction is relative to the critter’s current
     * orientation, so 0 is always immediately in front, 1 is 60 degrees to the right,
     * and so on. (If d is out of bounds, its remainder when divided by 6 is used.)
     * The contents are reported as a number n, as follows:
     * • 0: the hex is completely empty.
     * • n > 0: the hex contains a critter with appearance n (see Section 8).
     * • n < −1: the hex contains some food, with total energy value (−n) − 1.
     * • n = −1: the hex contains a rock.
     *
     * @return n
     */
    public int nearby(int dir) {
        Hex nearbyHex = currentWorld.hexAt(location.getCoordinateAt(dir));
        return nearbyHex.evaluate(this);
    }

    /**
     * The expression ahead[dist] reports the contents of hex that is directly
     * ahead of the creature at distance dist, using the same scheme as
     * nearby. Thus, ahead[0] reports on the appearance of the current critter.
     * A negative distance is treated as zero distance.s
     */
    public int ahead(int distance) {
        return hexAhead(distance).evaluate(this);
    }

    private Hex hexAhead(int distance) {
        Coordinate coordinate = location;

        if (distance < 0) distance = 0;
        for (int i = 0; i < distance; i++) {
            coordinate = coordinate.getCoordinateAt(direction);
        }
        return currentWorld.hexAt(coordinate);
    }

    private static class FringeHex {
        Hex hx;
        boolean visited;
        int turns;

        int direction;

        int initialTurnDirection;

        FringeHex(Hex _hx) {
            hx = _hx;
            turns = 0;
            visited = false;
            direction = 0;
            initialTurnDirection = 0;
        }
    }

    /**
     * The expression smell uses the critter’s sense of smell to report the direction
     * and distance to the nearest food, up to a distance of MAX_SMELL_DISTANCE (= 10)
     * hexes. The result of the expression is 1, 000 · distance + direction, where
     * direction is relative to the critter’s current orientation, as depicted in Figure 4.
     * If the food is not precisely in one of the six directions, the direction closest to
     * the direction to the food is used in this expression. Ties between two directions
     * are broken in an implementation-defined manner. If there is no food within
     * MAX_SMELL_DISTANCE hexes, the result is 1,000,000.
     *
     * @param distance The distance to the nearest food hex
     * @return 1000 · distance + direction
     */
    public int smell(int distance) {
        int cost = 1_000_000;
        distance = Math.max(distance, MAX_SMELL_DISTANCE);

        List<ArrayList<FringeHex>> fringes = new ArrayList<>();
        List<FringeHex> visitedList = new ArrayList<>();
        //add this critter's hex to fringe[0]
        ArrayList<FringeHex> fringe = new ArrayList<>();
        Hex ch = this.currentWorld.hexAt(this.location);
        FringeHex fhx = new FringeHex(ch);
        fringe.add(fhx);
        fhx.visited = true;
        fhx.direction = this.direction;
        //add this FringeHex to the visited list
        visitedList.add(fhx);
        fringes.add(fringe);
        int foodFringeIndex = -1;
        for (int i = 1; (i < distance && (foodFringeIndex == -1)); i++) {
            //create the next fringe
            ArrayList<FringeHex> nextFringe = new ArrayList<FringeHex>();
            fringes.add(nextFringe);
            //for each Hex in the previous fringe (i-1)
            int previousFringeIndex = i - 1;
            ArrayList<FringeHex> previousFringe = fringes.get(previousFringeIndex);
            for (FringeHex cFhx : previousFringe) {
                //get a FringeHex for processing
                //check each of the 6 directions and
                //add each neighbor if they have not been visited
                //and are not blocked
                for (int dir = 0; dir < 6; dir++) {
                    Coordinate neighborCoordinate = cFhx.hx.getCoordinate().getCoordinateAt(dir);
                    if (!Hex.isValidHexCoordinate(neighborCoordinate.column(), neighborCoordinate.row())) continue;

                    Hex neighborHex = this.currentWorld.hexAt(neighborCoordinate);
                    //if this neighbor has not been visited and is not blocked
                    //add it to the next fringe
                    FringeHex nFhx = new FringeHex(neighborHex);
                    if (((nFhx.hx.getType() == Hex.HexType.EMPTY) || (nFhx.hx.getType() == Hex.HexType.FOOD)) && (containsFringeHex(visitedList, nFhx) == null)) {
                        //add this neighbor to the visited list
                        visitedList.add(nFhx);
                        nFhx.visited = true;
                        nFhx.turns = cFhx.turns;
                        nFhx.initialTurnDirection = cFhx.initialTurnDirection;
                        //if the direction of this FringeHex does not match the
                        //direction of the FringeHex we are currently processing
                        //increment the turns
                        if (cFhx.direction != dir) {
                            //store the initial turns the
                            //critter has to make to get to this FringeHex
                            if (previousFringeIndex == 0) {
                                nFhx.initialTurnDirection = computeInitialTurnDirection(cFhx.direction, dir);
                            }
                            nFhx.turns += computeTurns(cFhx.direction, dir);
                        }
                        nFhx.direction = dir;
                        nextFringe.add(nFhx);
                        //if food has been found
                        //then need to stop looking
                        //after processing this fringe
                        if (nFhx.hx.getType() == Hex.HexType.FOOD) {
                            foodFringeIndex = i;
                        }
                    }
                }
            }
        }

        //if food was found
        FringeHex selectedFoodFhx = null;
        if (foodFringeIndex != -1) {
            //get all the hex's with food
            //find all the neighbors for each food hex
            //if a neighbor is there in the previous fringe
            //then it means that we can get from their to the food
            //use neighbor as a candidate for cost computation
            ArrayList<FringeHex> foodFringe = fringes.get(foodFringeIndex);
            for (FringeHex foodFhx : foodFringe) {
                if (foodFhx.hx.getType() == Hex.HexType.FOOD) {
                    //get all the neighbors of this food hex
                    //look for each neighbor in the previous fringe list
                    for (int dir = 0; dir < 6; dir++) {
                        Coordinate cFhxCoordinate = foodFhx.hx.getCoordinate().getCoordinateAt(dir);
                        if (!Hex.isValidHexCoordinate(cFhxCoordinate.column(), cFhxCoordinate.row())) continue;

                        Hex neighborHex = this.currentWorld.hexAt(cFhxCoordinate);
                        FringeHex nFhx = new FringeHex(neighborHex);

                        if ((nFhx = containsFringeHex(fringes.get(foodFringeIndex - 1), nFhx)) != null) {
                            int foodDist = foodFringeIndex - 1 + nFhx.turns;
                            int foodCost = foodDist * 100;

                            int foodDirection = getDirection(nFhx, foodFhx);
                            if (foodDirection != nFhx.direction) {
                                foodDist += 1;
                                foodCost = foodDist * 100;
                            }
                            foodCost += nFhx.initialTurnDirection;

                            Logger.info("Food cost: " + foodCost + " for (" + nFhx.hx.getType() + ","
                                    + nFhx.hx.getColumn() + "," + nFhx.hx.getRow() + "," + nFhx.visited + ","
                                    + nFhx.turns + "," + nFhx.direction + "," + nFhx.initialTurnDirection + ")"
                                    + "(Food Hex=" + foodFhx.hx.getColumn() + "," + foodFhx.hx.getRow() + ")", "Critter:smell", Logger.FLAG_CRITTER_ACTION);

                            if (foodCost < cost) {
                                cost = foodCost;
                                selectedFoodFhx = foodFhx;
                            }
                        }
                    }
                }
            }
        }

        if (selectedFoodFhx != null)
            Logger.info("Selected food hex: (" + selectedFoodFhx.hx.getColumn() + "," + selectedFoodFhx.hx.getRow() + ")", "Critter:smell", Logger.FLAG_CRITTER_ACTION);
        return cost;
    }

    /**
     * The random expression generates a random integer from 0 up to one less than the value
     * of the given expression. Thus, random[2] gives either 0 or 1 randomly. For n < 2,
     * random[n] is always zero.
     *
     * @param n the maximum non-inclusive value
     * @return random integer from 0 to n-1
     */
    public int random(int n) {
        return Util.randomInt(n);
    }

    public ProgramImpl getProgram() {
        return (ProgramImpl) program.clone();
    }

    public void setMem(int index, int value) {
        this.mem[index] = value;
    }

    public int getDirection() {
        return this.direction;
    }

    @Override
    public String toString() {
        return "Critter{species='" + species + '\'' + ", direction=" + direction + ", mem=" + Arrays.toString(mem) + "}";
    }

    private boolean decrementEnergy(int val) {
        mem[ENERGY] -= val;
        if (mem[ENERGY] < 0) {
            mem[ENERGY] = 0;
            kill();
            return false;
        }
        return mem[ENERGY] > 0;
    }

    private void incrementEnergy(int val) {
        mem[ENERGY] += val;
        mem[ENERGY] = Math.min(mem[ENERGY], mem[SIZE] * ENERGY_PER_SIZE);
    }

    //kills the critter, generating the food and nullifying the object
    private void kill() {
        Hex deathHex = currentWorld.hexAt(location);
        deathHex.setFoodValue(deathHex.getFoodValue() + FOOD_PER_SIZE * mem[SIZE]);
        deathHex.becomeFood();
        currentWorld.decrementCritterNumber();
        currentWorld.updateHex(deathHex.getCoordinate());
    }

    public void setWorld(World currentWorld) {
        this.currentWorld = currentWorld;
    }

    public World getWorld() {
        return this.currentWorld;
    }

    private int getDirection(FringeHex from, FringeHex to) {
        int direction = 0;
        for (int dir = 0; dir < 6; dir++) {
            Coordinate dirCoordinate = from.hx.getCoordinate().getCoordinateAt(dir);
            if (!Hex.isValidHexCoordinate(dirCoordinate.column(), dirCoordinate.row())) continue;

            Hex neighborHex = this.currentWorld.hexAt(dirCoordinate);
            if ((neighborHex.getColumn() == to.hx.getColumn()) && (neighborHex.getRow() == to.hx.getRow())) {
                direction = dir;
                break;
            }
        }
        return direction;
    }

    private int computeInitialTurnDirection(int currentDir, int targetDir) {
        int turns = 0;
        boolean leftTurn = (currentDir - targetDir) > 0;
        int dirDiff = Math.abs(currentDir - targetDir);

        if (dirDiff > 3) leftTurn = !leftTurn;
        else turns = dirDiff;

        return leftTurn ? 6 - turns : turns;
    }

    private int computeTurns(int currentDir, int targetDir) {
        int dirDiff = Math.abs(currentDir - targetDir);

        return (dirDiff > 3) ? 6 - dirDiff : dirDiff;
    }

    private FringeHex containsFringeHex(List<FringeHex> list, FringeHex fhx) {
        if ((list == null) || (list.isEmpty())) return null;

        for (FringeHex hex : list)
            if ((hex.hx.getColumn() == fhx.hx.getColumn()) && (hex.hx.getRow() == fhx.hx.getRow())) return hex;
        return null;
    }

    public int getStepVal() {
        return stepVal;
    }

    public void setStepVal(int stepVal) {
        this.stepVal = stepVal;
    }
}