package BattleManagement;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Basic;
import Organisms.Animals.Behaviours.BattleConditions.BattleConditions;
import Organisms.Animals.Behaviours.Defensive.DefensivePrey;
import Organisms.Animals.Behaviours.Foraging.AgilePredator;
import Organisms.Animals.Behaviours.Foraging.StrongPredator;
import Organisms.Animals.Corpses.CorpsyStuff;
import Organisms.Animals.Tags;
import util.ImportantMethods;

import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static BattleManagement.BattleCell.isBorder;
import static BattleManagement.actionBattleChoice.chooseBattleAction;
import static Map.MapGeneration.getNeighbours;
import static Organisms.Animals.BasicChecks.initiativeCheck;
import static Organisms.Animals.Management.RecountCR.recountCR;
import static util.SimManager.battleInitiated;

public class Battle {
    private static final Random random = new Random();
    private static final Logger logger = Logger.getLogger(Battle.class.getName());

    static HashSet<HashSet<Animal>> teams = new HashSet<>();

    public static void setTeams(HashSet<HashSet<Animal>> teams) {
        Battle.teams = teams;
    }

    static List<Animal> initiativeOrder = new ArrayList<>();

    public static void setInitiativeOrder(List<Animal> initiativeOrder) {
        Battle.initiativeOrder = initiativeOrder;
        logger.log(Level.INFO, "[Battle.init] Initiative order set with {0} combatants for BattleChoice.", initiativeOrder.size());
    }

    public static List<Animal> getInitiativeOrder() {
        return initiativeOrder;
    }

    static BattleCell[][] BattleMap = new BattleCell[8][8];

    public static BattleCell[][] getBattleMap() {
        return BattleMap;
    }

    public static void setBattleMap(BattleCell[][] battleMap) {
        BattleMap = battleMap;
    }
    private MapStructure.Cell mainMapBattleCell; // Instance field from constructor
    private boolean battleActuallyOver = false;   // Instance field
    private Animal currentActorInTurn;          // INSTANCE field: Needs to be set in commenceTurnAndExecuteActions
    private List<String> battleEventLog = new ArrayList<>();

    public BattleCell[][] getActualBattleGrid() {
        return Battle.BattleMap; // Returns the static map
    }
    public int getActualBattleGridWidth() {
        return (Battle.BattleMap != null && Battle.BattleMap.length > 0) ? Battle.BattleMap.length : 0;
    }
    public int getActualBattleGridHeight() {
        return (Battle.BattleMap != null && Battle.BattleMap.length > 0 && Battle.BattleMap[0] != null) ? Battle.BattleMap[0].length : 0;
    }
    public List<Animal> getCombatantsInOrder() {
        return Battle.initiativeOrder; // Returns the static list
    }
    public Animal getCurrentActor() {
        return this.currentActorInTurn; // This MUST be an instance field, set during commenceTurnAndExecuteActions
    }
    public List<String> getBattleEventLog() {
        return this.battleEventLog; // This should be an instance field
    }

    public Battle(MapStructure.Cell mainMapCell, HashSet<HashSet<Animal>> teamsParam, List<Animal> initiativeOrderParam) {
        this.mainMapBattleCell = mainMapCell;
        this.battleActuallyOver = false;
    }

    public static BattleCell[][] createMap(MapStructure.Cell cell){
        BattleCell[][] battleMapCreation = new BattleCell[8][8];
        Terrain terrain = cell.getTerrain();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                battleMapCreation[x][y] = new BattleCell(x, y, terrain);
            }
        }
        Battle.setBattleMap(battleMapCreation);
        return battleMapCreation;
    }

    public static Battle initializeBattle(MapStructure.Cell cell, List<Animal> animals) {
        battleInitiated.set(true);
        teams.clear();
        BattleCell[][] map= createMap(cell);
        HashMap<Animal, Integer> initiatives = new HashMap<>();
        List<Animal> sortedAnimals = new ArrayList<>(animals);
        for (Animal i : animals) {
            HashSet<Animal> team = new HashSet<>();
            team.add(i);
            if (i.getTags().contains(Tags.SOCIAL)) {
                HashSet<Animal> pack = i.getGroup();
                if (pack != null) {
                    for (Animal packMate : animals) {
                        if (pack.contains(packMate) && !packMate.equals(i)) {
                            team.add(packMate);
                        }
                    }
                }
            }
            teams.add(team);
        }
        for (Animal i : animals) {
            i.setBattleCell(findRandomValidCell(map));
            i.getBattleCell().setAnimal(i);
            initiatives.put(i, initiativeCheck(i));
            recountCR(i);
        }
        sortedAnimals.sort((animal1, animal2) -> {
            int initiative1 = initiatives.get(animal1);
            int initiative2 = initiatives.get(animal2);
            return Integer.compare(initiative2, initiative1);
        });
        setTeams(teams);
        setInitiativeOrder(sortedAnimals);
        return new Battle(cell, teams, sortedAnimals);
    }

    public void commenceTurnAndExecuteActions() { // islandMapParam removed
        if (isBattleOver()) {
            return;
        }
        logger.log(Level.FINE, "[Battle.commenceTurn] Processing round for {0} combatants.", Battle.initiativeOrder.size());

        List<Animal> actorsThisRound = new ArrayList<>(Battle.initiativeOrder);

        for (Animal actor : actorsThisRound) {
            if (actor == null || actor.isDead() || actor.getBattleCell() == null || !Battle.initiativeOrder.contains(actor)) {
                continue;
            }
            if (isBattleOver()) break;

            logger.log(Level.FINER, "[Battle.Turn] Actor's turn: {0} ({1}) at BattleCell [{2},{3}]",
                    new Object[]{actor.getId(), actor.getClass().getSimpleName(), actor.getBattleCell().getX(), actor.getBattleCell().getY()});

            chooseBattleAction(actor);

            logger.log(Level.INFO, "[Battle.Turn] Actor {0} ({1}) chose action: {2}",
                    new Object[]{actor.getId(), actor.getClass().getSimpleName(), actor.getBattleAction()});

            executeChosenBattleAction(actor); // islandMap removed

            if (actor.getHealthPoints() <= 0 && !actor.isDead()) {
                processDeathInBattle(actor);
            }

            logger.log(Level.FINER, "[Battle.Turn] Recounting CR for all living combatants after action by {0}.", actor.getId());
            for (Animal combatant : new ArrayList<>(Battle.initiativeOrder)) {
                if (combatant != null && !combatant.isDead()) {
                    recountCR(combatant);
                }
            }

            if (isBattleOver()) {
                logger.log(Level.INFO, "[Battle.Turn] Action by {0} (or subsequent CR recount) resulted in battle end condition.", actor.getId());
                break;
            }
        }
        checkBattleEndCondition();
    }

    // islandMap parameter removed
    private static void executeChosenBattleAction(Animal actor) {
        if (actor == null || actor.getBattleAction() == null || actor.isDead()) return;

        possibleBattleActions action = actor.getBattleAction();
        logger.log(Level.FINE, "[Battle.ExecuteAction] Actor {0} executing: {1}", new Object[]{actor.getId(), action});

        switch (action) {
            case ATTACK:
                executeAttackAction(actor);
                break;
            case RETREAT:
                executeRetreatAction(actor); // islandMap removed
                break;
            case MOVE_OUT:
                executeMoveOut(actor);
                break;
            case MOVE_TO_ENEMY:
                BattleCell targetMoveCell = executeMoveToEnemy(actor, BattleMap, initiativeOrder);
                if (targetMoveCell != null && actor.getBattleCell() != null && !targetMoveCell.isOccupied()) {
                    logger.log(Level.INFO, "[Battle.MoveToEnemy] Actor {0} ({1}) moving from [{2},{3}] to [{4},{5}].",
                            new Object[]{actor.getId(), actor.getClass().getSimpleName(),
                                    actor.getBattleCell().getX(), actor.getBattleCell().getY(),
                                    targetMoveCell.getX(), targetMoveCell.getY()});
                    actor.getBattleCell().setAnimal(null);
                    targetMoveCell.setAnimal(actor);
                    actor.setBattleCell(targetMoveCell);
                } else if (targetMoveCell != null && targetMoveCell.isOccupied()){
                    logger.log(Level.FINE, "[Battle.MoveToEnemy] Actor {0} ({1}) wanted to move to [{2},{3}], but cell is occupied by {4}.",
                            new Object[]{actor.getId(), actor.getClass().getSimpleName(), targetMoveCell.getX(), targetMoveCell.getY(), targetMoveCell.getAnimal().getId()});
                } else {
                    logger.log(Level.FINE, "[Battle.MoveToEnemy] Actor {0} ({1}) could not find a valid cell. Standing ground.",
                            new Object[]{actor.getId(), actor.getClass().getSimpleName()});
                }
                break;
            case STAND_GROUND:
                logger.log(Level.INFO, "[Battle.StandGround] Actor {0} ({1}) chose STAND_GROUND.", new Object[]{actor.getId(), actor.getClass().getSimpleName()});
                break;
            default:
                logger.log(Level.WARNING, "[Battle.ExecuteAction] Actor {0} ({1}) had an unknown battle action: {2}", new Object[]{actor.getId(), actor.getClass().getSimpleName(), action});
                break;
        }
    }

    private static BattleCell findRandomValidCell(BattleCell[][] battleMap) { // Renamed parameter for clarity
        if (battleMap == null || battleMap.length == 0 || battleMap[0].length == 0) {
            logger.log(Level.WARNING, "[Battle] findRandomValidCell: battleMap is null or empty.");
            return null;
        }
        int width = battleMap.length;
        int height = battleMap[0].length;
        int attempts = 0;
        int maxAttempts = width * height * 2;
        Random random = new Random();

        while (attempts < maxAttempts) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            BattleCell cell = battleMap[x][y];
            if (cell != null && cell.getAnimal() == null) {
                logger.log(Level.FINEST, "[Battle] findRandomValidCell: Found empty cell at [{0},{1}]", new Object[]{x,y});
                return cell;
            }
            attempts++;
        }

        logger.log(Level.WARNING, "[Battle] findRandomValidCell: Could not find an empty cell after {0} attempts.", maxAttempts);
        return null;
    }
    public static void executeMoveOut(Animal actor) {
        if (actor == null || actor.isDead() || actor.getBattleCell() == null) {
            logger.log(Level.WARNING, "[MoveOut] Cannot execute: Actor invalid. Actor: {0}", actor != null ? actor.getId() : "NULL_ACTOR");
            return;
        }
        BattleCell currentBattleCell = actor.getBattleCell();
        logger.log(Level.INFO, "[MoveOut] Actor {0} ({1}) at BattleCell [{2},{3}] attempting MOVE_OUT.",
                new Object[]{actor.getId(), actor.getClass().getSimpleName(), currentBattleCell.getX(), currentBattleCell.getY()});

        BattleCell targetBorderCell = findTargetBorderCellForMoveOut(actor, BattleMap);

        if (targetBorderCell != null && !targetBorderCell.isOccupied()) {
            logger.log(Level.INFO, "[MoveOut] Actor {0} ({1}) moving from [{2},{3}] directly to border BattleCell [{4},{5}].",
                    new Object[]{actor.getId(), actor.getClass().getSimpleName(),
                            currentBattleCell.getX(), currentBattleCell.getY(),
                            targetBorderCell.getX(), targetBorderCell.getY()});
            currentBattleCell.setAnimal(null);
            actor.setBattleCell(targetBorderCell);
            targetBorderCell.setAnimal(actor);
        } else if (targetBorderCell != null && targetBorderCell.isOccupied()) {
            logger.log(Level.WARNING, "[MoveOut] Actor {0} ({1}) target border cell [{2},{3}] is occupied by {4}. Cannot move out.",
                    new Object[]{actor.getId(), actor.getClass().getSimpleName(),
                            targetBorderCell.getX(), targetBorderCell.getY(),
                            targetBorderCell.getAnimal().getId()});
            actor.setBattleAction(possibleBattleActions.STAND_GROUND);
        }
        else {
            logger.log(Level.WARNING, "[MoveOut] Actor {0} ({1}) could not find an unobstructed border cell to move towards. Action failed.",
                    new Object[]{actor.getId(), actor.getClass().getSimpleName()});
             actor.setBattleAction(possibleBattleActions.STAND_GROUND);
        }
    }
    public static BattleCell findTargetBorderCellForMoveOut(Animal animal, BattleCell[][] battleMap) {
        if (animal == null || animal.getBattleCell() == null || battleMap == null || battleMap.length != 8) {
            logger.log(Level.WARNING, "Invalid input for findTargetBorderCellForMoveOut: null animal, animal cell, or invalid map size.");
            return null;
        }
        if (battleMap[0] == null || battleMap[0].length != 8) {
            logger.log(Level.WARNING, "Invalid input for findTargetBorderCellForMoveOut: battleMap is not 8x8.");
            return null;
        }

        BattleCell currentAnimalCell = animal.getBattleCell();
        int currentX = currentAnimalCell.getX();
        int currentY = currentAnimalCell.getY();

        BattleCell closestCell = null;
        int minDistance = Integer.MAX_VALUE;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 0 || i == 7 || j == 0 || j == 7) {
                    BattleCell borderCandidate = battleMap[i][j];

                    if (borderCandidate == null) {
                        logger.log(Level.WARNING, "findTargetBorderCellForMoveOut: Found null BattleCell at border [{0},{1}]", new Object[]{i, j});
                        continue;
                    }

                    if (borderCandidate.getAnimal() == null) {
                        int distance = Math.abs(currentX - i) + Math.abs(currentY - j);
                        if (distance < minDistance) {
                            minDistance = distance;
                            closestCell = borderCandidate;
                        }
                    }
                }
            }
        }
        if (closestCell != null) {
            logger.log(Level.FINER, "[Battle.findTargetBorderCellForMoveOut] Actor {0} target border cell is [{1},{2}].",
                    new Object[]{animal.getId(), closestCell.getX(), closestCell.getY()});
        } else {
            logger.log(Level.WARNING, "[Battle.findTargetBorderCellForMoveOut] Actor {0} could not find any unobstructed border cell.", animal.getId());
        }
        return closestCell;
    }

    private static int getManhattanDistance(BattleCell cell1, BattleCell cell2) {
        if (cell1 == null || cell2 == null) {
            return Integer.MAX_VALUE;
        }
        return Math.abs(cell1.getX() - cell2.getX()) + Math.abs(cell1.getY() - cell2.getY());
    }

    private static List<BattleCell> getBattleMapNeighbours(BattleCell cell, BattleCell[][] currentBattleMap) { // Pass the map
        List<BattleCell> neighbours = new ArrayList<>();
        if (cell == null || currentBattleMap == null || currentBattleMap.length == 0 || currentBattleMap[0].length == 0) {
            // logger.log(Level.WARNING, "[Battle] getBattleMapNeighbours: Invalid cell or map.");
            return neighbours;
        }
        int x = cell.getX();
        int y = cell.getY();
        int width = currentBattleMap.length;
        int height = currentBattleMap[0].length;

        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        for (int i = 0; i < 8; i++) {
            int newX = x + dx[i];
            int newY = y + dy[i];

            if (newX >= 0 && newX < width && newY >= 0 && newY < height) { // Use dynamic width/height
                if (currentBattleMap[newX][newY] != null) {
                    neighbours.add(currentBattleMap[newX][newY]);
                }
            }
        }
        return neighbours;
    }

    // Corrected isBorder

    public static BattleCell executeMoveToEnemy(Animal actor, BattleCell[][] battleMap, List<Animal> allAnimalsInBattle) {
        if (actor == null || actor.getBattleCell() == null || battleMap == null || allAnimalsInBattle == null || allAnimalsInBattle.isEmpty()) {
            Battle.logger.log(Level.WARNING, "Invalid arguments for findClosestUnobstructedCellNeighbouringClosestEnemy.");
            return null;
        }

        // 1. Find Actor's Team
        Set<Animal> actorTeam = null;
        for (Set<Animal> team : teams) {
            if (team.contains(actor)) {
                actorTeam = team;
                break;
            }
        }
        if (actorTeam == null) {
            logger.log(Level.FINE, "Actor " + actor.getId() + " not found in any team.");
            actorTeam = new HashSet<>();
            actorTeam.add(actor);
        }

        // 2. Identify Enemies and Find Closest Enemy
        Animal closestEnemy = null;
        int minDistanceToEnemy = Integer.MAX_VALUE;
        BattleCell actorCell = actor.getBattleCell();

        for (Animal potentialEnemy : allAnimalsInBattle) {
            if (potentialEnemy == null || potentialEnemy.isDead() || potentialEnemy.equals(actor) || actorTeam.contains(potentialEnemy)) {
                continue; // Skip self, dead animals, or teammates
            }

            BattleCell enemyCell = potentialEnemy.getBattleCell();
            if (enemyCell == null) continue;

            int distance = getManhattanDistance(actorCell, enemyCell);
            if (distance < minDistanceToEnemy) {
                minDistanceToEnemy = distance;
                closestEnemy = potentialEnemy;
            }
        }

        if (closestEnemy == null) {
            logger.log(Level.FINE, "Actor " + actor.getId() + " has no valid enemies on the battle map.");
            return null;
        }
        logger.log(Level.FINER, "Actor " + actor.getId() + " closest enemy is " + closestEnemy.getId() + " at distance " + minDistanceToEnemy);


        BattleCell closestEnemyCell = closestEnemy.getBattleCell();
        List<BattleCell> enemyNeighbours = getBattleMapNeighbours(closestEnemyCell, battleMap);
        List<BattleCell> unobstructedEnemyNeighbours = new ArrayList<>();

        for (BattleCell neighbour : enemyNeighbours) {
            if (neighbour.getAnimal() == null) { // Unobstructed
                unobstructedEnemyNeighbours.add(neighbour);
            }
        }

        if (unobstructedEnemyNeighbours.isEmpty()) {
            logger.log(Level.FINE, "Closest enemy " + closestEnemy.getId() + " has no unobstructed neighbours.");
            return null;
        }

        // 4. Find Closest Unobstructed Neighbor (of enemy) to Actor
        BattleCell targetCell = null;
        int minDistanceToActorFromNeighbour = Integer.MAX_VALUE;

        for (BattleCell unobstructedNeighbour : unobstructedEnemyNeighbours) {
            int distance = getManhattanDistance(actorCell, unobstructedNeighbour);
            if (distance < minDistanceToActorFromNeighbour) {
                minDistanceToActorFromNeighbour = distance;
                targetCell = unobstructedNeighbour;
            }
        }

        if (targetCell != null) {
            logger.log(Level.FINER, "Actor " + actor.getId() + " found target cell [{" + targetCell.getX() + "},{" + targetCell.getY() + "}] near enemy " + closestEnemy.getId());
        } else {
            logger.log(Level.FINE, "Actor " + actor.getId() + " could not find a suitable cell near enemy " + closestEnemy.getId());
        }

        return targetCell;
    }

    public static void executeAttackAction(Animal actor) {
        if (actor == null || actor.isDead() || actor.getBattleCell() == null) {
            logger.log(Level.WARNING, "[Attack] Cannot execute attack: Actor is null, dead, or not in a battle cell. Actor: {0}", actor != null ? actor.getId() : "NULL_ACTOR");
            return;
        }
        logger.log(Level.INFO, "[Attack] Actor {0} ({1}) at [{2},{3}] is executing ATTACK action.",
                new Object[]{actor.getId(), actor.getClass().getSimpleName(), actor.getBattleCell().getX(), actor.getBattleCell().getY()});

        // 1. Identify Actor's Team
        Set<Animal> actorTeam = null;
        for (Set<Animal> team : teams) { // Using static Battle.teams
            if (team.contains(actor)) {
                actorTeam = team;
                break;
            }
        }
        if (actorTeam == null) { // Should not happen if teaming is correct during battle init
            actorTeam = new HashSet<>();
            actorTeam.add(actor); // Treat as a team of one if not found
            logger.log(Level.WARNING, "[Attack] Actor {0} was not found in any pre-defined team. Treating as a team of one.", actor.getId());
        }

        // 2. Target Selection
        Animal targetEnemy = null;
        List<Animal> adjacentEnemies = new ArrayList<>();
        List<BattleCell> actorNeighbours = getBattleMapNeighbours(actor.getBattleCell(), BattleMap);

        for (BattleCell neighbourCell : actorNeighbours) {
            Animal potentialTarget = neighbourCell.getAnimal();
            if (potentialTarget != null && !potentialTarget.isDead() && !actorTeam.contains(potentialTarget)) {
                adjacentEnemies.add(potentialTarget);
            }
        }

        if (!adjacentEnemies.isEmpty()) {
            targetEnemy = adjacentEnemies.get(random.nextInt(adjacentEnemies.size())); // Random adjacent enemy
            logger.log(Level.FINE, "[Attack] Actor {0} targeting adjacent enemy {1} at [{2},{3}].",
                    new Object[]{actor.getId(), targetEnemy.getId(), targetEnemy.getBattleCell().getX(), targetEnemy.getBattleCell().getY()});
        } else {
            // No adjacent enemies, find closest enemy on the field
            int minDistance = Integer.MAX_VALUE;
            for (Animal potentialEnemy : initiativeOrder) { // Using static Battle.initiativeOrder
                if (potentialEnemy == null || potentialEnemy.isDead() || potentialEnemy.equals(actor) || actorTeam.contains(potentialEnemy) || potentialEnemy.getBattleCell() == null) {
                    continue;
                }
                int dist = getManhattanDistance(actor.getBattleCell(), potentialEnemy.getBattleCell());
                if (dist < minDistance) {
                    minDistance = dist;
                    targetEnemy = potentialEnemy;
                }
            }
            if (targetEnemy != null) {
                logger.log(Level.FINE, "[Attack] Actor {0} targeting closest enemy {1} at [{2},{3}] (distance: {4}).",
                        new Object[]{actor.getId(), targetEnemy.getId(), targetEnemy.getBattleCell().getX(), targetEnemy.getBattleCell().getY(), minDistance});
            }
        }

        if (targetEnemy == null) {
            logger.log(Level.INFO, "[Attack] Actor {0} found no valid enemies to attack.", actor.getId());
            actor.setBattleAction(possibleBattleActions.STAND_GROUND);
            return;
        }

        // 3. Check Target's Condition
        boolean targetIsOnBorder = isBorder(targetEnemy.getBattleCell());
        boolean targetIsRetreating = targetEnemy.battleConditions != null && targetEnemy.battleConditions.contains(BattleConditions.RETREATING);

        // 4. Select and Execute Specific Attack Method
        String attackUsedDescription = "Basic Attack";

        if (actor instanceof DefensivePrey) {
            DefensivePrey dpActor = (DefensivePrey) actor;
            if (targetIsOnBorder || targetIsRetreating) {
                dpActor.getDown(actor, targetEnemy);
                attackUsedDescription = "DefensivePrey GetDown (border/retreating)";
            } else {
                int choice = random.nextInt(3);
                if (choice == 0) {
                    dpActor.attack(actor, targetEnemy);
                    attackUsedDescription = "DefensivePrey Attack";
                } else if (choice == 1) {
                    dpActor.headStrike(actor, targetEnemy);
                    attackUsedDescription = "DefensivePrey HeadStrike";
                } else {
                    dpActor.getDown(actor, targetEnemy);
                    attackUsedDescription = "DefensivePrey GetDown";
                }
            }
        } else if (actor instanceof AgilePredator) {
            AgilePredator apActor = (AgilePredator) actor;
            if (targetIsOnBorder || targetIsRetreating) {
                int choice = random.nextInt(2);
                if (choice == 0) {
                    apActor.getDown(actor, targetEnemy);
                    attackUsedDescription = "AgilePredator GetDown (border/retreating)";
                } else {
                    apActor.grapple(actor, targetEnemy);
                    attackUsedDescription = "AgilePredator Grapple (border/retreating)";
                }
            } else {
                int choice = random.nextInt(4);
                if (choice == 0) {
                    apActor.attack(actor, targetEnemy);
                    attackUsedDescription = "AgilePredator Attack";
                } else if (choice == 1) {
                    apActor.grapple(actor, targetEnemy);
                    attackUsedDescription = "AgilePredator Grapple";
                } else if (choice == 2) {
                    apActor.getDown(actor, targetEnemy);
                    attackUsedDescription = "AgilePredator GetDown";
                } else {
                    apActor.arteryStrike(actor, targetEnemy);
                    attackUsedDescription = "AgilePredator ArteryStrike";
                }
            }
        } else if (actor instanceof StrongPredator) {
            StrongPredator spActor = (StrongPredator) actor;
            if (targetIsOnBorder || targetIsRetreating) {
                int choice = random.nextInt(2);
                if (choice == 0) {
                    spActor.getDown(actor, targetEnemy);
                    attackUsedDescription = "StrongPredator GetDown (border/retreating)";
                } else {
                    spActor.grapple(actor, targetEnemy);
                    attackUsedDescription = "StrongPredator Grapple (border/retreating)";
                }
            } else {
                int choice = random.nextInt(4);
                if (choice == 0) {
                    spActor.attack(actor, targetEnemy);
                    attackUsedDescription = "StrongPredator Attack";
                } else if (choice == 1) {
                    spActor.grapple(actor, targetEnemy);
                    attackUsedDescription = "StrongPredator Grapple";
                } else if (choice == 2) {
                    spActor.getDown(actor, targetEnemy);
                    attackUsedDescription = "StrongPredator GetDown";
                } else {
                    spActor.headStrike(actor, targetEnemy);
                    attackUsedDescription = "StrongPredator HeadStrike";
                }
            }
        } else {
            Basic basicActor = (Basic) actor;
            basicActor.attack(actor, targetEnemy);
            attackUsedDescription = "Basic Attack";
        }
        logger.log(Level.INFO, "[Attack] Actor {0} ({1}) used {2} on Target {3} ({4}).",
                new Object[]{actor.getId(), actor.getClass().getSimpleName(), attackUsedDescription, targetEnemy.getId(), targetEnemy.getClass().getSimpleName()});

        // 5. Check for eatAlive Condition
        // Ensure targetEnemy.isDead() is updated if HP drops to 0 by the attack.
        // The setHealthPoints method in Animal should call its death() method, which sets the isDead flag.
        if (targetEnemy.getHealthPoints() <= 0) {
            // It's crucial that targetEnemy.isDead() is true here if HP <= 0.
            // If not, the death processing might not have completed.
            // For safety, one could call a method like targetEnemy.processDeathIfNecessary();

            logger.log(Level.FINE, "[Attack] Target {0} HP is {1} after attack from {2}.", new Object[]{targetEnemy.getId(), targetEnemy.getHealthPoints(), actor.getId()});

            if (targetEnemy.isDead() && targetEnemy.getSize() * 5 <= actor.getSize()) {
                logger.log(Level.INFO, "[Attack] Target {0} killed by {1} and is small enough (Actor size: {2}, Target size: {3}). Attempting eatAlive.",
                        new Object[]{targetEnemy.getId(), actor.getId(), actor.getSize(), targetEnemy.getSize()});
                try {
                    // Ensure the actor's class actually has this method publicly.
                    // This assumes eatAlive is defined in the concrete species class or a public method in Animal.
                    Method eatAliveMethod = actor.getClass().getMethod("eatAlive", Animal.class, Animal.class);
                    eatAliveMethod.invoke(actor, actor, targetEnemy); // actor is instance, (actor, targetEnemy) are args
                    logger.log(Level.INFO, "[Attack] Actor {0} successfully performed eatAlive on {1}.", new Object[]{actor.getId(), targetEnemy.getId()});
                } catch (NoSuchMethodException e) {
                    logger.log(Level.SEVERE, "[Attack] Actor {0} ({1}) class does not have a public eatAlive(Animal, Animal) method for reflection. Error: {2}",
                            new Object[]{actor.getId(), actor.getClass().getSimpleName(), e.getMessage()});
                } catch (Exception e) { // IllegalAccessException, InvocationTargetException
                    logger.log(Level.SEVERE, "[Attack] Error invoking eatAlive for Actor {0} ({1}) on {2}. Error: {3}",
                            new Object[]{actor.getId(), actor.getClass().getSimpleName(), targetEnemy.getId(), e.getMessage()});
                }
            } else if (targetEnemy.isDead()) {
                logger.log(Level.FINE, "[Attack] Target {0} killed by {1}, but not eligible for eatAlive (Actor size: {2}, Target size: {3}).",
                        new Object[]{targetEnemy.getId(), actor.getId(), actor.getSize(), targetEnemy.getSize()});
            }
        }
    }public static void executeRetreatAction(Animal actor) {
        if (actor == null || actor.isDead() || actor.getBattleCell() == null) {
            logger.log(Level.WARNING, "[Retreat] Cannot execute retreat: Actor is null, dead, or not in a battle cell. Actor: {0}", actor != null ? actor.getId() : "NULL_ACTOR");
            return;
        }

        MapStructure.Cell currentMainMapBattleCell = actor.getCell(); // This should be the main map cell where the battle is.
        if (currentMainMapBattleCell == null) {
            logger.log(Level.SEVERE, "[Retreat] Actor {0} has no associated main map cell. Cannot determine retreat path.", actor.getId());
            return;
        }

        logger.log(Level.INFO, "[Retreat] Actor {0} ({1}) at battle cell [{2},{3}] (main map cell [{4},{5}]) is attempting to RETREAT.",
                new Object[]{actor.getId(), actor.getClass().getSimpleName(),
                        actor.getBattleCell().getX(), actor.getBattleCell().getY(),
                        currentMainMapBattleCell.getX(), currentMainMapBattleCell.getY()});


        List<MapStructure.Cell> mainMapNeighbours = getNeighbours(currentMainMapBattleCell);
        List<MapStructure.Cell> validRetreatCells = new ArrayList<>();
        for (MapStructure.Cell neighbour : mainMapNeighbours) {
            if (neighbour != null) {
                validRetreatCells.add(neighbour);
            }
        }

        if (validRetreatCells.isEmpty()) {
            logger.log(Level.WARNING, "[Retreat] Actor {0} ({1}) found no valid adjacent main map cells to retreat to from cell [{2},{3}]. Retreat failed.",
                    new Object[]{actor.getId(), actor.getClass().getSimpleName(), currentMainMapBattleCell.getX(), currentMainMapBattleCell.getY()});
             actor.setBattleAction(possibleBattleActions.STAND_GROUND);
            return;
        }

        MapStructure.Cell retreatDestinationCell = validRetreatCells.get(random.nextInt(validRetreatCells.size()));
        logger.log(Level.INFO, "[Retreat] Actor {0} ({1}) chose to retreat to main map cell [{2},{3}].",
                new Object[]{actor.getId(), actor.getClass().getSimpleName(), retreatDestinationCell.getX(), retreatDestinationCell.getY()});

        // 2. Update Actor's State & Battle Structures
        BattleCell actorOldBattleCell = actor.getBattleCell();
        if (actorOldBattleCell != null) {
            actorOldBattleCell.setAnimal(null); // Vacate the battle cell
        }
        actor.setBattleCell(null); // Animal is no longer on the battle map

        // Update main map position
        currentMainMapBattleCell.removeAnimal(actor); // Remove from current main map cell (battle location)
        retreatDestinationCell.addAnimal(actor);    // Add to new main map cell
        actor.setCell(retreatDestinationCell);      // Update animal's internal cell reference

        // Remove from battle lists
        boolean removedFromInitiative = initiativeOrder.remove(actor);

        Set<Animal> teamToRemoveFrom = null;
        boolean removedFromTeam = false;
        for (Set<Animal> team : teams) {
            if (team.remove(actor)) {
                teamToRemoveFrom = team; // For logging or if team needs to be removed if empty
                removedFromTeam = true;
                break;
            }
        }
        if (teamToRemoveFrom != null && teamToRemoveFrom.isEmpty()) {
         teams.remove(teamToRemoveFrom);
         logger.log(Level.FINE, "[Retreat] Team of actor {0} became empty and was removed.", actor.getId());
        }


        // Clear battle conditions
        if (actor.battleConditions != null) {
            actor.battleConditions.remove(BattleConditions.RETREATING);
            // Clear other relevant battle conditions if necessary
        }
        actor.setBattleAction(null); // Clear battle action as they've left

        logger.log(Level.INFO, "[Retreat] Actor {0} ({1}) successfully retreated to main map cell [{2},{3}]. Removed from initiative: {4}, from team: {5}.",
                new Object[]{actor.getId(), actor.getClass().getSimpleName(), retreatDestinationCell.getX(), retreatDestinationCell.getY(), removedFromInitiative, removedFromTeam});


    }
    private static void processDeathInBattle(Animal animal) {
        if (animal == null || animal.isDead()) { // Check if already processed or null
            return;
        }

        logger.log(Level.INFO, "[Battle.ProcessDeath] Processing death for Animal {0} ({1}). HP: {2}",
                new Object[]{animal.getId(), animal.getClass().getSimpleName(), animal.getHealthPoints()});

        if (animal.getHealthPoints() <= 0 || animal.isDead()) CorpsyStuff.generateCorpse(animal); // Generate corpse on the main map cell

        BattleCell battleCell = animal.getBattleCell();
        if (battleCell != null) {
            logger.log(Level.FINE, "[Battle.ProcessDeath] Removing {0} from BattleCell [{1},{2}].", new Object[]{animal.getId(), battleCell.getX(), battleCell.getY()});
            battleCell.setAnimal(null);
        }
        animal.setBattleCell(null);

        boolean removedFromInit = initiativeOrder.remove(animal);
        logger.log(Level.FINE, "[Battle.ProcessDeath] Animal {0} removed from initiative: {1}.", new Object[]{animal.getId(), removedFromInit});

        Iterator<HashSet<Animal>> teamIterator = teams.iterator();
        while (teamIterator.hasNext()) {
            Set<Animal> team = teamIterator.next();
            if (team.remove(animal)) {
                logger.log(Level.FINE, "[Battle.ProcessDeath] Animal {0} removed from a team.", animal.getId());
                if (team.isEmpty()) {
                    teamIterator.remove(); // Remove empty team
                    logger.log(Level.INFO, "[Battle.ProcessDeath] A team became empty and was removed.");
                }

            }
        }

    }
    private void checkBattleEndCondition() {
        if (Battle.initiativeOrder.isEmpty()) {
            this.battleActuallyOver = true;
            logger.log(Level.INFO, "[Battle] Battle ended at cell [{0},{1}]: No animals left in initiative.",
                    new Object[]{this.mainMapBattleCell.getX(), this.mainMapBattleCell.getY()});
            return;
        }

        HashSet<Animal> firstLivingTeamRef = null;
        boolean multipleDistinctTeamsFound = false;

        // Create a list of sets, where each set represents the living members of a team currently in initiative
        List<Set<Animal>> livingTeamsInInitiative = new ArrayList<>();
        for (HashSet<Animal> originalTeam : Battle.teams) {
            Set<Animal> livingMembersInInitiative = new HashSet<>();
            for (Animal member : originalTeam) {
                if (!member.isDead() && Battle.initiativeOrder.contains(member)) {
                    livingMembersInInitiative.add(member);
                }
            }
            if (!livingMembersInInitiative.isEmpty()) {
                livingTeamsInInitiative.add(livingMembersInInitiative);
            }
        }

        if (livingTeamsInInitiative.isEmpty()) { // No living animals from any original team left in initiative
            this.battleActuallyOver = true;
            logger.log(Level.INFO, "[Battle] Battle ended at cell [{0},{1}]: No living animals from any team left in initiative.",
                    new Object[]{this.mainMapBattleCell.getX(), this.mainMapBattleCell.getY()});
            return;
        }

        // Check if all remaining living animals belong to the same core team group
        // This is tricky if groups can merge or animals can be in multiple 'teams' in the HashSet<HashSet<Animal>>
        // A simpler check: count distinct groups among living animals.
        if (livingTeamsInInitiative.size() <= 1) {
            this.battleActuallyOver = true;
            logger.log(Level.INFO, "[Battle] Battle ended at cell [{0},{1}]: One or zero distinct living teams remain.",
                    new Object[]{this.mainMapBattleCell.getX(), this.mainMapBattleCell.getY()});
        } else {
            // More robust check: Are there actual opponents?
            // Take the first living team. Are there any living animals NOT in this team's broader alliance?
            Set<Animal> referenceTeam = livingTeamsInInitiative.get(0);
            boolean foundOpponent = false;
            for (int i = 1; i < livingTeamsInInitiative.size(); i++) {
                Set<Animal> otherTeam = livingTeamsInInitiative.get(i);
                // Check if otherTeam is truly distinct (no shared members with referenceTeam)
                boolean isDistinct = true;
                for (Animal memberRef : referenceTeam) {
                    if (otherTeam.contains(memberRef)) {
                        isDistinct = false;
                        break;
                    }
                }
                if (isDistinct) {
                    foundOpponent = true;
                    break;
                }
            }
            if (!foundOpponent) {
                this.battleActuallyOver = true;
                logger.log(Level.INFO, "[Battle] Battle ended at cell [{0},{1}]: All remaining combatants are allied.",
                        new Object[]{this.mainMapBattleCell.getX(), this.mainMapBattleCell.getY()});
            } else {
                this.battleActuallyOver = false;
            }
        }
    }
    public MapStructure.Cell getMainMapBattleCell(){
        return mainMapBattleCell;
    }
    public boolean isBattleOver() {
        return this.battleActuallyOver;
    }
}
