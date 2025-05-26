package ThreadManagement;

import BattleManagement.Battle;
import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Foraging.Scavenger;
import Organisms.Animals.Behaviours.Nutrition.Carnivore;
import Organisms.Animals.Behaviours.Nutrition.Herbivore;
import Organisms.Animals.Behaviours.Nutrition.Omnivore;
import Organisms.Animals.Conditions.Conditions;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Management.possibleActions;
import Organisms.Animals.Movement;
import Organisms.Animals.Tags;
import util.NamedThreadFactory;
import util.SemaphorePausableExecutor;
import util.SimManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static BattleManagement.Battle.initializeBattle;
import static Map.MapGeneration.getNeighbours;
import static Organisms.Animals.BasicChecks.perceptionCheck;
import static Organisms.Animals.BasicChecks.stealthCheck;
import static Organisms.Animals.Management.RestStuff.RestMechanics.deepRestCycle;
import static Organisms.Animals.Management.RestStuff.RestMechanics.rest;
import static Organisms.Animals.Management.actionChoice.actionChoice;
import static Organisms.Animals.Movement.MoveRequest.formMoveRequest;
import static util.SimManager.SIM_LOCK;
import static util.SimManager.battleInitiated;
import static util.Spawner.spawnAnimal;

public class ActionPool {
    private static final Logger logger = Logger.getLogger(ActionPool.class.getName());
    private final SemaphorePausableExecutor animalDecisionExecutor;
    public ActionPool() {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(1, numberOfCores - 5);
        logger.log(Level.INFO, "Initializing Animal Decision Pool with size: {0}", poolSize);
        this.animalDecisionExecutor = new SemaphorePausableExecutor(poolSize, poolSize, 0L,
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory("action-pool")
        );
    }

    public void planning(List<Animal> animals, BlockingQueue<Movement.MoveRequest> movementQueue) {
        logger.log(Level.INFO, "[ActionPool.planning] Method called. Initial SimManager.battleInitiated: {0}", SimManager.battleInitiated.get());

        synchronized (SimManager.SIM_LOCK) {
            if (SimManager.battleInitiated.get()) {
                logger.log(Level.INFO, "[ActionPool.planning] Battle currently active (flag is true). Waiting on SIM_LOCK.");
                while (SimManager.battleInitiated.get()) {
                    try {
                        SimManager.SIM_LOCK.wait();
                        logger.log(Level.INFO, "[ActionPool.planning] Woke from SIM_LOCK.wait(). battleInitiated={0}", SimManager.battleInitiated.get());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.log(Level.SEVERE, "[ActionPool.planning] Interrupted while waiting for battle to complete!", e);
                        return;
                    }
                }
                logger.log(Level.INFO, "[ActionPool.planning] Exited SIM_LOCK wait loop (battle flag is now false).");
            } else {
                logger.log(Level.FINE, "[ActionPool.planning] No battle active (flag is false). Proceeding with planning.");
            }
        }

        if (Thread.currentThread().isInterrupted()) {
            logger.log(Level.WARNING, "[ActionPool.planning] Thread interrupted after SIM_LOCK check. Exiting planning phase.");
            return;
        }

        if (animals == null || animals.isEmpty()) {
            logger.log(Level.INFO, "[ActionPool.planning] Animals list is null or empty. No planning will occur.");
            return;
        }
        logger.log(Level.INFO, "[ActionPool.planning] Received {0} animals for planning.", animals.size());

        if (this.animalDecisionExecutor == null || this.animalDecisionExecutor.isShutdown()) {
            logger.log(Level.SEVERE, "[ActionPool.planning] animalDecisionExecutor is unusable (null or shutdown)! Cannot submit planning tasks. Exiting.");
            return;
        }

        List<Callable<Void>> plannedTasks = new ArrayList<>();
        for (Animal animalLoopVar : animals) {
            final Animal currentAnimal = animalLoopVar;

            if (currentAnimal.actionPoints <= 0 || currentAnimal.isDead() ||
                    (currentAnimal.conditions != null && currentAnimal.conditions.contains(Conditions.INCAPACITATED))) {
                logger.log(Level.FINE, "[ActionPool.planning] Skipping {0} (AP: {1}, Dead: {2}, Incap: {3})",
                        new Object[]{currentAnimal.getClass().getSimpleName(), currentAnimal.actionPoints, currentAnimal.isDead(),
                                (currentAnimal.conditions != null && currentAnimal.conditions.contains(Conditions.INCAPACITATED))});
                continue;
            }

            plannedTasks.add(() -> {
                // Use currentAnimal.toString() for logs if it's safe and doesn't call forbidden methods.
                // Otherwise, use currentAnimal.getClass().getSimpleName() for more generic logging.
                String animalLogName = currentAnimal.getClass().getSimpleName() + "@" + Integer.toHexString(System.identityHashCode(currentAnimal)); // Example for unique-ish log ref

                try {
                    logger.log(Level.INFO, "[ActionPool.Task] Animal {0} starting multi-action turn with {1} AP.",
                            new Object[]{animalLogName, currentAnimal.actionPoints});

                    while (currentAnimal.actionPoints > 0 && !SimManager.battleInitiated.get()) {
                        currentAnimal.setCurrPerception(perceptionCheck(currentAnimal));
                        possibleActions chosenAction = actionChoice(currentAnimal); // Calls method in this ActionPool class

                        if (chosenAction == null || chosenAction == possibleActions.PASS_ACTION) {
                            logger.log(Level.INFO, "[ActionPool.Task] Animal {0} chose PASS_ACTION or no action. APs left: {1}. Ending actions for this turn.",
                                    new Object[]{animalLogName, currentAnimal.actionPoints});
                            // currentAnimal.actionPoints = Math.max(0, currentAnimal.actionPoints - 1); // Deduct AP for PASS if it costs AP
                            break;
                        }

                        logger.log(Level.FINE, "[ActionPool.Task] Animal {0} (AP before: {1}) attempting action: {2}",
                                new Object[]{animalLogName, currentAnimal.actionPoints, chosenAction});

                        // --- Action Execution Switch ---
                        // REMEMBER: EACH ACTION METHOD CALLED MUST DEDUCT APs from currentAnimal.actionPoints
                        switch (chosenAction) {
                            case MOVE:
                                // Movement.whereToMove might need SimManager.getIslandMap() if it uses map data
                                MapStructure.Cell targetCell = Movement.whereToMove(currentAnimal /*, SimManager.getIslandMap() */);
                                if (targetCell != null && !targetCell.equals(currentAnimal.getCell())) {
                                    Movement.MoveRequest request = formMoveRequest(currentAnimal, targetCell);
                                    movementQueue.put(request); // Can block if queue is full
                                    logger.log(Level.INFO, "[ActionPool.Task] Animal {0} queued move to [{1},{2}]",
                                            new Object[]{animalLogName, targetCell.getX(), targetCell.getY()});
                                } else {
                                    logger.log(Level.INFO, "[ActionPool.Task] Animal {0} planned no move.", animalLogName);
                                    // currentAnimal.actionPoints = Math.max(0, currentAnimal.actionPoints - 1); // Optional AP cost for thinking/failed move
                                }
                                break;
                            case HIDE:
                                logger.log(Level.INFO, "[ActionPool.Task] Animal {0} chose HIDE", animalLogName);
                                currentAnimal.setCurrStealth(stealthCheck(currentAnimal)); // AP deduction in stealthCheck
                                break;
                            case REST:
                                logger.log(Level.INFO, "[ActionPool.Task] Animal {0} chose REST", animalLogName);
                                rest(currentAnimal); // AP deduction in rest
                                break;
                            case DEEP_REST:
                                logger.log(Level.INFO, "[ActionPool.Task] Animal {0} chose DEEP_REST", animalLogName);
                                deepRestCycle(currentAnimal); // AP deduction in deepRestCycle
                                break;
                            case FORNICATE:
                                logger.log(Level.INFO, "[ActionPool.Task] Animal {0} chose FORNICATE", animalLogName);
                                spawnAnimal(currentAnimal); // AP deduction in spawnAnimal
                                break;
                            case EAT:
                                logger.log(Level.INFO, "[ActionPool.Task] Animal {0} attempting EAT action.", animalLogName);
                                if (currentAnimal.getCell() == null) {
                                    logger.log(Level.WARNING, "[ActionPool.Task] Animal {0} cannot EAT: current cell is null.", animalLogName);
                                    break; // Exit this action attempt, still in while loop
                                }

                                // --- YOUR REINSTATED DETAILED EAT LOGIC ---
                                if (currentAnimal.getTags().contains(Tags.HERBY) && currentAnimal.getTags().contains(Tags.SCAVENGER)) {
                                    if (currentAnimal.getCell().getCurrPlantCapacity() <= currentAnimal.getCell().getCurrCorpseCapacity()) {
                                        logger.log(Level.FINE, "[ActionPool.Task] Animal {0} (Herby/Scavenger) choosing corpse over plant.", animalLogName);
                                        eatCorpse(currentAnimal); // This method MUST deduct AP
                                    } else {
                                        logger.log(Level.FINE, "[ActionPool.Task] Animal {0} (Herby/Scavenger) choosing plant.", animalLogName);
                                        eatPlant(currentAnimal);  // This method MUST deduct AP
                                    }
                                } else if (currentAnimal.getTags().contains(Tags.HERBY)) {
                                    // Your original logic: if (currentAnimal.getCell().getCurrPlantCapacity() * 2 >= currentAnimal.getCell().getCurrCorpseCapacity())
                                    // Let's assume this implies a preference for plants unless corpses are much more abundant.
                                    if (currentAnimal.getCell().getCurrPlantCapacity() * 2 >= currentAnimal.getCell().getCurrCorpseCapacity()) {
                                        logger.log(Level.FINE, "[ActionPool.Task] Animal {0} (Herby) choosing plant.", animalLogName);
                                        eatPlant(currentAnimal);  // This method MUST deduct AP
                                    } else {
                                        logger.log(Level.FINE, "[ActionPool.Task] Animal {0} (Herby) choosing corpse (more abundant than x2 plants).", animalLogName);
                                        eatCorpse(currentAnimal); // This method MUST deduct AP
                                    }
                                } else { // Assumed Carnivore or Omnivore (not Herby)
                                    // Your original logic: if (currentAnimal.getCell().getCurrPlantCapacity() >= currentAnimal.getCell().getCurrCorpseCapacity() * 1.5)
                                    // This implies carnivores/omnivores prefer corpses unless plants are significantly more abundant.
                                    if (currentAnimal.getCell().getCurrPlantCapacity() >= currentAnimal.getCell().getCurrCorpseCapacity() * 1.5) {
                                        logger.log(Level.FINE, "[ActionPool.Task] Animal {0} (Non-Herby) choosing plant (significantly more abundant).", animalLogName);
                                        eatPlant(currentAnimal);  // This method MUST deduct AP
                                    } else {
                                        logger.log(Level.FINE, "[ActionPool.Task] Animal {0} (Non-Herby) choosing corpse.", animalLogName);
                                        eatCorpse(currentAnimal); // This method MUST deduct AP
                                    }
                                }
                                // --- END OF YOUR REINSTATED DETAILED EAT LOGIC ---

                                logger.log(Level.FINE, "[ActionPool.Task] Animal {0} has now eaten this tick.", animalLogName);
                                break; // End EAT case
                            case INITIATE_BATTLE:
                                logger.log(Level.INFO, "[ActionPool.Task] Animal {0} chose INITIATE_BATTLE", animalLogName);
                                if (SimManager.battleInitiated.get()) {
                                    logger.log(Level.INFO, "[ActionPool.Task] Animal {0} chose INITIATE_BATTLE, but a battle is already active globally.", animalLogName);
                                } else if (SimManager.newBattleToDispatch != null) {
                                    logger.log(Level.INFO, "[ActionPool.Task] Animal {0} chose INITIATE_BATTLE, but another battle is already pending dispatch by SimManager. Passing turn for now.", animalLogName);
                                } else { // No current battle and no battle pending dispatch
                                    MapStructure.Cell cell = currentAnimal.getCell();
                                    if (cell == null) {
                                        logger.log(Level.SEVERE, "[ActionPool.Task] Animal {0} cannot INITIATE_BATTLE: its current cell is null.", animalLogName);
                                        break; // Exit while loop for this animal's turn
                                    }
                                    logger.log(Level.INFO, "[ActionPool.Task] Animal {0} processing INITIATE_BATTLE at cell [{1},{2}].",
                                            new Object[]{animalLogName, cell.getX(), cell.getY()});

                                    List<Animal> initialCombatantsInCellList = new ArrayList<>(cell.getAnimals()); // For BattleManagement

                                    // Your friend joining logic to potentially modify initialCombatantsInCellList
                                    // For example:
                                    Set<Animal> finalCombatantsSet = new HashSet<>(initialCombatantsInCellList);
                                    List<Animal> toCheckForFriends = new ArrayList<>(initialCombatantsInCellList);
                                    for (Animal combatant : toCheckForFriends) {
                                        if (combatant.getTags().contains(Tags.SOCIAL) && combatant.getGroup() != null && !combatant.getGroup().isEmpty()) {
                                            List<MapStructure.Cell> neighboringCells = getNeighbours(cell); // Ensure getNeighbours is implemented
                                            for (Animal groupMember : new ArrayList<>(combatant.getGroup())) { // Iterate copy
                                                if (groupMember.isDead() || finalCombatantsSet.contains(groupMember)) continue;
                                                MapStructure.Cell memberCell = groupMember.getCell();
                                                if (memberCell != null && !memberCell.equals(cell) && neighboringCells.contains(memberCell)) {
                                                    logger.log(Level.FINE, "[ActionPool.Task] Ally {0} of {1} joining battle.",
                                                            new Object[]{groupMember.getClass().getSimpleName(), combatant.getClass().getSimpleName()});
                                                    memberCell.removeAnimal(groupMember); groupMember.setCell(cell); cell.addAnimal(groupMember);
                                                    finalCombatantsSet.add(groupMember);
                                                }
                                            }
                                        }
                                    }
                                    List<Animal> finalCombatantsList = new ArrayList<>(finalCombatantsSet);
                                    // --- End Friend Joining Logic Example ---

                                    // Call your static method to create/initialize a Battle object
                                    Battle newBattle = BattleManagement.Battle.initializeBattle(cell, finalCombatantsList);

                                    if (newBattle != null) {
                                        // IMPORTANT: Deduct AP from currentAnimal for initiating the battle
                                        // Example: currentAnimal.actionPoints = Math.max(0, currentAnimal.actionPoints - BATTLE_INITIATION_AP_COST);

                                        logger.log(Level.INFO, "[ActionPool.Task] Animal {0} successfully created battle object. Signaling SimManager.", animalLogName);
                                        SimManager.newBattleToDispatch = newBattle;
                                        SimManager.battleInitiated.set(true);
                                        logger.log(Level.INFO, "[ActionPool.Task] SimManager.battleInitiated set to TRUE by initiator's task for Animal {0}.", animalLogName);

                                    } else {
                                        logger.log(Level.WARNING, "[ActionPool.Task] Animal {0} failed to initialize battle object.", animalLogName);
                                        // currentAnimal.actionPoints = Math.max(0, currentAnimal.actionPoints - 1); // Optional AP cost for failed attempt
                                    }
                                }
                                break;
                            default:
                                logger.log(Level.WARNING, "[ActionPool.Task] Animal {0} chose an unhandled action: {1}. Passing.",
                                        new Object[]{animalLogName, chosenAction});
                                // currentAnimal.actionPoints = Math.max(0, currentAnimal.actionPoints - 1); // Cost AP for unknown action
                                break;
                        }
                        // --- End Action Execution Switch ---

                        if (currentAnimal.actionPoints <= 0 && !SimManager.battleInitiated.get()) {
                            logger.log(Level.INFO, "[ActionPool.Task] Animal {0} is now out of AP (post-action check in loop).", animalLogName);
                            break;
                        }
                    }
                    Thread.sleep(100);// --- End of while (multi-AP) loop ---

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.log(Level.WARNING, "[ActionPool.Task] Planning task for " + animalLogName + " interrupted.", e);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "[ActionPool.Task] Error during planning task for " + animalLogName, e);
                    if (currentAnimal != null) {
                        currentAnimal.actionPoints = 0;
                    }
                }
                logger.log(Level.INFO, "[ActionPool.Task] Animal {0} finished processing its turn actions. Final APs: {1}",
                        new Object[]{animalLogName, (currentAnimal != null ? currentAnimal.actionPoints : "N/A")});
                return null;
            });
        }

        try {
            if (!plannedTasks.isEmpty()) {
                logger.log(Level.INFO, "[ActionPool.planning] Invoking {0} animal planning tasks...", plannedTasks.size());
                this.animalDecisionExecutor.invokeAll(plannedTasks);
                logger.log(Level.INFO, "[ActionPool.planning] All animal planning tasks completed for this tick.");
            } else {
                logger.log(Level.INFO, "[ActionPool.planning] No tasks to invoke (all animals skipped or none eligible).");
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "[ActionPool.planning] Interrupted while waiting for invokeAll to complete!", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "[ActionPool.planning] Exception during animalDecisionExecutor.invokeAll!", e);
        }
    }
    public void shutdown() {
        logger.log(Level.INFO, "Shutting down Animal Decision Pool...");
        animalDecisionExecutor.shutdown();
        try {
            if (!animalDecisionExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.log(Level.WARNING, "Animal Pool did not terminate in 60s, forcing shutdown...");
                animalDecisionExecutor.shutdownNow();
            } else {
                logger.log(Level.INFO, "Animal Pool terminated.");
            }
        } catch (InterruptedException ie) {
            logger.log(Level.WARNING, "Interrupted during Animal Pool shutdown wait.");
            animalDecisionExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    public void start(){

    }

    public SemaphorePausableExecutor getAnimalDecisionExecutor() {
        return animalDecisionExecutor;
    }
    private void eatCorpse(Animal animal) {
        if (animal == null || animal.getCell() == null) {
            logger.log(Level.WARNING, "eatCorpse called with null animal or null cell.");
            return;
        }
        if (animal.getCell().getCorpses() == null || animal.getCell().getCorpses().isEmpty()) {
            logger.log(Level.FINE, "Animal {0} ({1}) tried to eat corpse, but none available in cell [{2},{3}].",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), animal.getCell().getX(), animal.getCell().getY()});
            return;
        }

        Corpse chosenCorpse = null;
        float maxEnergy = -1f;

        for (Corpse currentCorpse : animal.getCell().getCorpses()) {
            if (currentCorpse != null && currentCorpse.getEnergyPoints() > maxEnergy) {
                maxEnergy = currentCorpse.getEnergyPoints();
                chosenCorpse = currentCorpse;
            }
        }

        if (chosenCorpse == null) {
            logger.log(Level.FINE, "Animal {0} ({1}) found no suitable corpse to eat in cell [{2},{3}] despite attempting.",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), animal.getCell().getX(), animal.getCell().getY()});
            return;
        }

        boolean ate = false;

        if (animal instanceof Scavenger) {
            logger.log(Level.FINEST, "Animal {0} ({1}) attempting to eat corpse ({2}) as Scavenger.", new Object[]{animal.getId(), animal.getClass().getSimpleName(), chosenCorpse});
            ((Scavenger) animal).eatDead(animal, chosenCorpse);
            ate = true;
        } else if (animal instanceof Carnivore) {
            logger.log(Level.FINEST, "Animal {0} ({1}) attempting to eat corpse ({2}) as Carnivore.", new Object[]{animal.getId(), animal.getClass().getSimpleName(), chosenCorpse});
            ((Carnivore) animal).eatDead(animal, chosenCorpse);
            ate = true;
        } else if (animal instanceof Omnivore) {
            logger.log(Level.FINEST, "Animal {0} ({1}) attempting to eat corpse ({2}) as Omnivore.", new Object[]{animal.getId(), animal.getClass().getSimpleName(), chosenCorpse});
            ((Omnivore) animal).eatDead(animal, chosenCorpse);
            ate = true;
        } else if (animal instanceof Herbivore) { // Herbivore eating corpse (as per user's "everyone can eat everything if desperate")
            logger.log(Level.INFO, "Animal {0} ({1}) is HERBIVORE attempting to eat corpse ({2}) due to desperation/interface definition.", new Object[]{animal.getId(), animal.getClass().getSimpleName(), chosenCorpse});
            ((Herbivore) animal).eatDead(animal, chosenCorpse); // Assumes Herbivore interface has eatDead
            ate = true;
        }


        if (ate) {
            logger.log(Level.FINE, "Animal {0} ({1}) successfully ate corpse ({2}). Action points consumed by interface method.", new Object[]{animal.getId(), animal.getClass().getSimpleName(), chosenCorpse});
        } else {
            logger.log(Level.WARNING, "Animal {0} ({1}) chose to eat corpse ({2}) but did not match a known eating interface that implements eatDead.", new Object[]{animal.getId(), animal.getClass().getSimpleName(), chosenCorpse});
        }
    }

    private void eatPlant(Animal animal) {
        if (animal == null || animal.getCell() == null) {
            logger.log(Level.WARNING, "eatPlant called with null animal or null cell.");
            return;
        }
        if (animal.getCell().getCurrPlantCapacity() <= 0) {
            logger.log(Level.FINE, "Animal {0} ({1}) tried to eat plants, but no plant capacity in cell [{2},{3}].",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), animal.getCell().getX(), animal.getCell().getY()});
            return;
        }

        boolean ate = false;
        if (animal instanceof Herbivore) {
            logger.log(Level.FINEST, "Animal {0} ({1}) attempting to eat plant as Herbivore.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            ((Herbivore) animal).eatPlant(animal);
            ate = true;
        } else if (animal instanceof Omnivore) {
            logger.log(Level.FINEST, "Animal {0} ({1}) attempting to eat plant as Omnivore.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            ((Omnivore) animal).eatPlant(animal);
            ate = true;
        } else if (animal instanceof Carnivore) { // Carnivore eating plant (as per user's "everyone can eat everything if desperate")
            logger.log(Level.INFO, "Animal {0} ({1}) is CARNIVORE attempting to eat plant due to desperation/interface definition.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            ((Carnivore) animal).eatPlant(animal); // Assumes Carnivore interface has eatPlant
            ate = true;
        }


        if (ate) {
            logger.log(Level.FINE, "Animal {0} ({1}) successfully ate plants. Action points consumed by interface method.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
        } else {
            logger.log(Level.WARNING, "Animal {0} ({1}) chose to eat plant but did not match a known plant-eating interface.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
        }
    }
}
