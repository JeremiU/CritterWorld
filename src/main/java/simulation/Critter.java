package simulation;

import ast.Program;
import ast.ProgramImpl;
import ast.Rule;
import cms.util.maybe.Maybe;
import controller.ControllerFactory;
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

    // Stores the world that the critter is currently inside
    private World currentWorld;

    private final String species;
    private int direction;
    private Coordinate location;

    private final int[] mem;

    // Represents the AST / Critter ruleset for this Critter.
    private final ProgramImpl program;

    // Full constructor
    public Critter(World currentWorld, String species, int memSize, int defense, int offense, int size, int energy, int posture, ProgramImpl program) {
        //ensure values are within correct bounds
        if (memSize < MIN_MEMORY) memSize = MIN_MEMORY;
        if (defense < 1) defense = 1;
        if (offense < 1) offense = 1;
        if (energy < 1) energy = 1;
        if (posture > 99) posture = 99;
        if (posture < 0) posture = 0;

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
        //pass number?
    }

    // Changing the critter's position
    public void setLocation(Coordinate coordinate) {
        this.location = coordinate;
    }

    public void setLocation(int column, int row) {
        this.location = new Coordinate(column, row);
    }

    // Basic DIRECTION methods
    public void setDirection(int direction) {
        this.direction = direction;
    }

    // Basic getters
    public int getColumn() {
        return location.getColumn();
    }

    public int getRow() {
        return location.getRow();
    }

    public String getSpecies() {
        return species;
    }

    @Override //returns copy
    public int[] getMemory() {
        return Arrays.copyOf(this.mem, this.mem.length);
    }

    @Override
    public String getProgramString() {
        String pgmString = "";
        if (this.program != null && this.program.numRules() > 0) {
            StringBuilder prettyPgm = new StringBuilder();
            this.program.prettyPrint(prettyPgm);
            return prettyPgm.toString();
        }
        return pgmString;
    }

    @Override
    public Maybe<String> getLastRuleString() {
        if (this.program == null || this.program.numRules() == 0) return Maybe.none();

        int numRules = this.program.numRules();
        Rule lastRule = this.program.getRule(numRules - 1);
        StringBuilder prettyRule = new StringBuilder();
        lastRule.prettyPrint(prettyRule);
        return Maybe.from(prettyRule.toString());
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

    // Method runs every time-step
    public void tick() {
        System.out.println(species + " was ticked");
        //execute the rules of this critter
        //multiple passes will be made until an action command is
        //executed. The number of passes made will be saved in mem[5]
        //If the number of passes reach 999 the critter will go into a
        //wait state
        Interpreter ip = new Interpreter(this);
        boolean actionUpdate = ip.run();
        this.mem[PASS] = 1;
        while (this.mem[PASS] < 999 && !actionUpdate) {
            actionUpdate = ip.run();
            this.mem[PASS]++;
        }
        System.out.println("Number of passes: " + this.mem[PASS]);
    }

    public int getComplexity() {
        return getProgram().numRules() * RULE_COST + (mem[OFFENSE] + mem[DEFENSE] * ABILITY_COST);
    }

    // Critter ACTIONS
    // These are called by the interpreter

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

    /**
     * A critter uses some energy to move backward to the hex behind it.
     * If it attempts to move and there is a critter, food, or a rock in
     * the destination hex, the move fails but still takes energy
     */
    public void moveBackward() {
        move(Util.properMod(direction + DIR_AMOUNT / 2, DIR_AMOUNT));
    }

    private void move(int dir) {
        if (!decrementEnergy(mem[SIZE] * MOVE_COST) || ahead(dir) != 0) return;
        location = location.getCoordinateAt(direction);
    }

    /**
     * The critter may eat some of the food that might be available on the hex ahead of it, gaining the same
     * amount of energy as the food it consumes. It eats as much as it can; when the hex has more food than the
     * critter can absorb, the excess food is left on the hex.
     */
    public void eat() {
        System.out.println("EAT Action Executed");
        if (!decrementEnergy(mem[SIZE])) return;
        if (ahead(1) >= -1) return; //NO FOOD FOUND
        int foodEnergy = -ahead(1) - 1; //-n -1

        Hex foodHex = hexAhead(1);
        if (foodEnergy != foodHex.getFoodValue()) System.err.println("FOOD ENERGY != FOOD HEX");

        int energyTaken = Math.min(foodEnergy, ENERGY_PER_SIZE * mem[SIZE]); //if foodVal > maximum, inc max

        incrementEnergy(energyTaken);
        foodHex.setFoodValue(foodHex.getFoodValue() - energyTaken);
    }

    /**
     * SERVE
     * A critter may convert some of its own energy into food added to the hex in front of it, if that hex
     * is either empty or already contains some food.
     */
    public void serve(int energy) {
        System.out.println("SERVE Action Executed");

        Hex frontHex = hexAhead(1);

        if (frontHex.getType() != Hex.HexType.FOOD && frontHex.getType() != Hex.HexType.EMPTY) return;

        energy = Math.min(energy, mem[ENERGY]); //ensures energy is not overspent
        decrementEnergy(mem[SIZE] + energy);

        frontHex.setFoodValue(frontHex.getFoodValue() + energy);
    }

    /**
     * ATTACK
     * A critter may attack another critter directly in front of it. The attack removes an
     * amount of energy from the attacked critter that is determined by the size and
     * offensive ability of the attacker and the defensive ability of the victim
     */
    public void attack() {
        System.out.println("ATTACK Action Executed");
        if (!decrementEnergy(mem[SIZE] * ATTACK_COST)) return;

        Critter enemy = currentWorld.hexAt(location.getCoordinateAt(direction)).getCritter();
        if (enemy == null) return;

        int damage = (int) Math.round(BASE_DAMAGE * mem[SIZE] * p(DAMAGE_INC * (mem[SIZE] * mem[OFFENSE] - enemy.mem[SIZE] * enemy.mem[OFFENSE])));
        enemy.decrementEnergy(damage);
    }

    //the function p(x) as described in §10, used to simplify damage = ...
    private double p(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    /**
     * GROW
     * A critter may use energy to increase its size by one unit.
     */
    public void grow() {
        System.out.println("GROW Action Executed");
        if (!decrementEnergy(mem[SIZE] * this.getComplexity() * GROW_COST)) return;
        mem[SIZE]++;
    }

    /**
     * BUD
     * A critter may use a large amount of its energy to produce a new, smaller critter behind
     * it with the same genome (possibly with some random mutations).
     */
    public void bud() {
        if (!decrementEnergy(BUD_COST * this.getComplexity())) return;

        //new Critter is created and placed directly
        //behind this critter
        //Random mutation is performed on this Critter's program
        Program pc = (Program) this.program.clone();
        //perform a random mutation on the program
        ProgramImpl mpc = (ProgramImpl) pc.mutate();
        //set up the attributes of the child as per the specification
        Critter child = new Critter(currentWorld, species, mem[MEM_SIZE], mem[DEFENSE], mem[OFFENSE], INITIAL_BIRTHED_SIZE, INITIAL_ENERGY, 0, mpc);
        //place the child behind this critter
        //need to handle the case when this critter is on the edge of the world
        int row = location.getRow();
        int col = location.getColumn();
        //add the child at the specified location in this critter's world
        currentWorld.insertCritterAtLocation(child, this.location.getRow(), this.location.getColumn() - 2);
    }

    public void mate() {
        Hex nearbyHex = hexAhead(1);
        if (nearbyHex.getType() != Hex.HexType.CRITTER) return;

        Critter partner = nearbyHex.getCritter();
        //randomly choose attributes(0-2) from one of the two parents
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

        //get rules in sequence from the father and mother alternatively
        for (int i = 0; i < mother.numRules(); i++) {
            Rule cr;
            if (i % 2 == 0) {
                cr = (Rule) mother.getRule(i).clone();
            } else {
                cr = (Rule) father.getRule(i).clone();
            }
            cProgram.addRule(cr);
        }

        //set up the attributes of the child as per the specification
        Critter child = new Critter(currentWorld, species, inheritedMem[MEM_SIZE], inheritedMem[DEFENSE], inheritedMem[OFFENSE], INITIAL_BIRTHED_SIZE, INITIAL_ENERGY, INITIAL_BIRTHED_POSTURE, cProgram);

        //place the child behind one of the parents chosen randomly
        Hex behindHex;
        if (thisIsMainPartner) {
            behindHex = currentWorld.hexAt(location.getCoordinateAt(3));
            //if this hex does not already have a critter place the child here
        } else {
            behindHex = currentWorld.hexAt(partner.location.getCoordinateAt(3));
            //if this hex does not already have a critter place the child here
        }
        if (behindHex.getType() != Hex.HexType.CRITTER) {
            currentWorld.insertCritterAtLocation(child, behindHex.getColumn(), behindHex.getRow());
        }
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
        System.out.println("nearby: " + nearbyHex.getCoordinate());
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

    private class FringeHex {
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
        int cost = 1000000;
        if (distance > MAX_SMELL_DISTANCE) {
            distance = MAX_SMELL_DISTANCE;
        }
        //fringes is a list of lists
        List<ArrayList<FringeHex>> fringes = new ArrayList<>();
        //list of visited FringeHex's
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
                    //skip invalid coordinates. These are typically there at the edges
                    //of the world
                    if (!Hex.isValidHexCoordinate(neighborCoordinate.getColumn(), neighborCoordinate.getRow())) {
                        continue;
                    }
                    //if the neighbor coordinate is valid, get the neighbor hex
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
                                nFhx.turns += computeTurns(cFhx.direction, dir);
                            } else {
                                nFhx.turns += computeTurns(cFhx.direction, dir);
                            }
                        }
                        nFhx.direction = dir;
                        //add this neighbor to the next fringe
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
            for (int i = 0; i < foodFringe.size(); i++) {
                FringeHex foodFhx = foodFringe.get(i);
                if (foodFhx.hx.getType() == Hex.HexType.FOOD) {
                    //get all the neighbors of this food hex
                    //look for each neighbor in the previous fringe list
                    for (int dir = 0; dir < 6; dir++) {
                        Coordinate cFhxCoordinate = foodFhx.hx.getCoordinate().getCoordinateAt(dir);
                        //skip invalid coordinates. These are typically there at the edges
                        //of the world
                        if (!Hex.isValidHexCoordinate(cFhxCoordinate.getColumn(), cFhxCoordinate.getRow())) {
                            continue;
                        }
                        //if the neighbor coordinate is valid, get the neighbor hex
                        Hex neighborHex = this.currentWorld.hexAt(cFhxCoordinate);
                        FringeHex nFhx = new FringeHex(neighborHex);
                        //look for this neighbor in the previous fringe
                        if ((nFhx = containsFringeHex(fringes.get(foodFringeIndex - 1), nFhx)) != null) {
                            //cost will be (distance*100) + direction
                            int foodDist = foodFringeIndex - 1 + nFhx.turns;
                            int foodCost = foodDist * 100;
                            //if the direction does not match the direction of the food hex
                            int foodDirection = getDirection(nFhx, foodFhx);
                            if (foodDirection != nFhx.direction) {
                                foodDist += 1;
                                foodCost = foodDist * 100;
                            }
                            //add the initial turn
                            foodCost += nFhx.initialTurnDirection;

                            System.out.println("Food cost: " + foodCost + " for (" + nFhx.hx.getType() + "," + nFhx.hx.getColumn() + "," + nFhx.hx.getRow() + "," + nFhx.visited + "," + nFhx.turns + "," + nFhx.direction + "," + nFhx.initialTurnDirection + ")" + "(Food Hex=" + foodFhx.hx.getColumn() + "," + foodFhx.hx.getRow() + ")");

                            if (foodCost < cost) {
                                cost = foodCost;
                                selectedFoodFhx = foodFhx;
                            }

                        }
                    }
                }
            }
        }

        if (selectedFoodFhx != null) {
            System.out.println("Selected food hex: (" + selectedFoodFhx.hx.getColumn() + "," + selectedFoodFhx.hx.getRow() + ")");
        }

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

    /**
     * @return a clone of the program, useful for reading
     */
    public ProgramImpl getProgram() {
        return (ProgramImpl) program.clone();
    }


    //temp method, should be replaced since not read-only
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

    // Decrease energy. Returns whether the Critter is still alive, allowing to break the program if not.
    private boolean decrementEnergy(int val) {
        mem[ENERGY] -= val;
        if (mem[ENERGY] < 0) {
            mem[ENERGY] = 0;
            kill();
        }
        return mem[ENERGY] > 0;
    }

    //Increases energy. Ensures the energy stays within the maximum bounds
    private void incrementEnergy(int val) {
        mem[ENERGY] += val;
        mem[ENERGY] = Math.min(mem[ENERGY], mem[SIZE] * ENERGY_PER_SIZE);
    }

    //kills the critter, generating the food and nullifying the object
    private void kill() {
        Hex deathHex = currentWorld.hexAt(location);
        deathHex.setFoodValue(deathHex.getFoodValue() + FOOD_PER_SIZE * mem[SIZE]);
        deathHex.becomeFood();
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
            //skip invalid coordinates. These are typically there at the edges
            //of the world
            if (!Hex.isValidHexCoordinate(dirCoordinate.getColumn(), dirCoordinate.getRow())) {
                continue;
            }
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
        int initialTurnDirection = 0;
        boolean leftTurn = false;
        if ((currentDir - targetDir) > 0) {
            leftTurn = true;
        }
        int dirDiff = Math.abs(currentDir - targetDir);
        //if the difference is more than 3 then
        //flip the direction to reduce the number
        //of turns
        if (dirDiff > 3) {
            leftTurn = !leftTurn;
        } else {
            turns = dirDiff;
        }

        if (leftTurn) {
            initialTurnDirection = 6 - turns;
        } else {
            initialTurnDirection = turns;
        }

        return initialTurnDirection;
    }

    private int computeTurns(int currentDir, int targetDir) {
        int turns = 0;
        int dirDiff = Math.abs(currentDir - targetDir);

        if (dirDiff > 3) {
            turns = 6 - dirDiff;
        } else {
            turns = dirDiff;
        }

        return turns;
    }

    private FringeHex containsFringeHex(List<FringeHex> list, FringeHex fhx) {
        if ((list == null) || (list.size() == 0)) {
            return null;
        }

        for (FringeHex lfhx : list) {
            if ((lfhx.hx.getColumn() == fhx.hx.getColumn()) && (lfhx.hx.getRow() == fhx.hx.getRow())) {
                return lfhx;
            }
        }
        return null;
    }
}