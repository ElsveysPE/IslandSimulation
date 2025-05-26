package Organisms.Animals.Management;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic.CellAnalysed;
import Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic.cellInitialAnalysis;
import Organisms.Animals.Conditions.Conditions;
import Organisms.Animals.Movement;
import Organisms.Animals.Tags;
import Organisms.HealthStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static Map.MapGeneration.getNeighbours;
import static Organisms.Animals.Conditions.ConditionEffects.deepRestEnd;
import static Organisms.Animals.Conditions.ConditionEffects.restEnd;

public class actionChoice {

    private static final Logger logger = Logger.getLogger(actionChoice.class.getName());

    // --- Configurable Thresholds ---
    private static final int HOSPITALITY_FOR_EFFECTIVE_HIDE = 15;
    private static final int DANGER_THRESHOLD_FOR_REST = 10;
    private static final int DANGER_THRESHOLD_FOR_DEEP_REST = 5;
    private static final int DANGER_THRESHOLD_FOR_SOCIAL_ACTIONS = 12;
    private static final int MIN_FOOD_AVAILABILITY_TO_EAT = 5;
    private static final int MIN_FOOD_FOR_OPPORTUNISTIC_EAT = 20;
    private static final float MIN_COMBAT_ADVANTAGE_RATIO_FOR_PREY = 1.1f;
    private static final float MIN_COMBAT_ADVANTAGE_RATIO_GENERAL = 0.9f;
    private static final int SOCIAL_SAFETY_BONUS = 3;

    public static possibleActions actionChoice(Animal animal) {
        // --- 0. Initial Setup ---
        deepRestEnd(animal);
        restEnd(animal);
        animal.actionPoints--;
        if (animal == null || animal.getCell() == null) {
            logger.log(Level.WARNING, "actionChoice: Animal or its cell is null. Defaulting to REST. Animal: {0}",
                    (animal != null ? String.valueOf(animal.getId()) : "null"));
            return possibleActions.REST;
        }

        MapStructure.Cell currentCell = animal.getCell();
        CellAnalysed currentCellAnalysed = cellInitialAnalysis.initialAnalysis(animal, currentCell);

        HashSet<Conditions> conditions = animal.conditions;
        if (conditions == null) conditions = new HashSet<>(); // Ensure conditions is not null
        HashSet<Tags> tags = animal.getTags();
        if (tags == null) tags = new HashSet<>(); // Ensure tags is not null
        HealthStatus health = animal.getHealthStatus();

        logger.log(Level.INFO, "[ActionChoice START] Animal ID: {0} ({1}), Cell: [{2},{3}], HP: {4}/{5}, DRP: {6}, Conditions: {7}",
                new Object[]{
                        animal.getId(), animal.getClass().getSimpleName(),
                        currentCell.getX(), currentCell.getY(),
                        animal.getHealthPoints(), animal.getMaxHealth(), animal.getDeepRestPoints(),
                        conditions
                });

        List<Animal> friendsInCell = new ArrayList<>();
        int numberOfFriendsInCell = 0;
        HashSet<Animal> animalGroup = animal.getGroup();
        boolean isEffectivelySocial = animalGroup != null && !animalGroup.isEmpty();
        if (isEffectivelySocial && currentCellAnalysed.sensedAnimals != null) {
            friendsInCell = currentCellAnalysed.sensedAnimals.stream()
                    .filter(a -> !a.equals(animal) && animalGroup.contains(a))
                    .collect(Collectors.toList());
            numberOfFriendsInCell = friendsInCell.size();
        }
        boolean isIsolatedSocialAnimal = isEffectivelySocial && numberOfFriendsInCell == 0;

        int effectiveDangerThresholdDeepRest = DANGER_THRESHOLD_FOR_DEEP_REST + (numberOfFriendsInCell > 0 ? SOCIAL_SAFETY_BONUS : 0);
        int effectiveDangerThresholdRest = DANGER_THRESHOLD_FOR_REST + (numberOfFriendsInCell > 0 ? SOCIAL_SAFETY_BONUS : 0);
        int effectiveDangerThresholdSocial = DANGER_THRESHOLD_FOR_SOCIAL_ACTIONS + (numberOfFriendsInCell > 0 ? SOCIAL_SAFETY_BONUS : 0);

        boolean canDeepRestHere = currentCellAnalysed.cellDanger < effectiveDangerThresholdDeepRest &&
                currentCellAnalysed.cellHospitality >= HOSPITALITY_FOR_EFFECTIVE_HIDE / 2.0;
        boolean canRestHere = currentCellAnalysed.cellDanger < effectiveDangerThresholdRest &&
                currentCellAnalysed.cellHospitality > 5;

        // --- Decision 1: MOVE ---
        MapStructure.Cell preferredCellForMovement = Movement.whereToMove(animal);
        if (preferredCellForMovement != null && !preferredCellForMovement.equals(currentCell)) {
            logger.log(Level.INFO, "Animal ID: {0} chose action: MOVE to cell [{1},{2}]",
                    new Object[]{animal.getId(), preferredCellForMovement.getX(), preferredCellForMovement.getY()});
            return possibleActions.MOVE;
        }
        // Removed the "Did not MOVE" log from here to avoid clutter if it successfully chooses an action in the current cell.
        // Will log "stays in cell" if no action is taken or if it's a deliberate choice to stay.

        // --- Animal stays in current cell ---
        logger.log(Level.FINE, "Animal ID: {0} - Considering actions in current cell [{1},{2}].",
                new Object[]{animal.getId(), currentCell.getX(), currentCell.getY()});


        // --- Decision 2: PANIC State Override ---
        if (conditions.contains(Conditions.PANICKING)) {
            logger.log(Level.FINE, "Animal ID: {0} is PANICKING.", animal.getId());
            if (currentCellAnalysed.cellHospitality >= HOSPITALITY_FOR_EFFECTIVE_HIDE / 2.0 &&
                    currentCellAnalysed.cellDanger < effectiveDangerThresholdSocial) {
                logger.log(Level.INFO, "Animal ID: {0} chose action: HIDE (due to panic).", animal.getId());
                return possibleActions.HIDE;
            }
            possibleActions panicFallback = (currentCellAnalysed.cellHospitality > 5) ? possibleActions.HIDE : possibleActions.REST;
            logger.log(Level.INFO, "Animal ID: {0} chose action: {1} (panic fallback).", new Object[]{animal.getId(), panicFallback});
            return panicFallback;
        }

        // --- Decision 3: Critical Health or Exhaustion (DRP check is REMOVED from this trigger) ---
        boolean isCriticallyInjured = health == HealthStatus.GRAVELY_INJURED || health == HealthStatus.SEVERELY_INJURED ||
                conditions.contains(Conditions.SEVERELY_BLEEDING) || animal.getHealthPoints() < animal.getMaxHealth()*0.5;
        boolean isExhausted = conditions.contains(Conditions.EXHAUSTED);
        boolean isFatigued = conditions.contains(Conditions.FATIGUED);
        boolean lowDRP = animal.getDeepRestPoints()<0;

        boolean needsCriticalRecoveryDueToState = isExhausted || (isCriticallyInjured && (isFatigued || isExhausted) );

        if (needsCriticalRecoveryDueToState) {
            logger.log(Level.FINE, "Animal ID: {0} needs critical recovery (Exhausted:{1}, CritInj:{2}, Fatigued:{3}). DRP is {4}.",
                    new Object[]{animal.getId(), isExhausted, isCriticallyInjured, isFatigued, animal.getDeepRestPoints()});
            if (canDeepRestHere) {
                logger.log(Level.INFO, "Animal ID: {0} chose action: DEEP_REST (critical state).", animal.getId());
                return possibleActions.DEEP_REST;
            }
            if (canRestHere) {
                logger.log(Level.INFO, "Animal ID: {0} chose action: REST (critical state, normal rest).", animal.getId());
                return possibleActions.REST;
            }
            if (currentCellAnalysed.cellHospitality >= HOSPITALITY_FOR_EFFECTIVE_HIDE / 2.0) {
                logger.log(Level.INFO, "Animal ID: {0} chose action: HIDE (critical state, cannot rest).", animal.getId());
                return possibleActions.HIDE;
            }
            logger.log(Level.INFO, "Animal ID: {0} chose action: REST (critical state fallback, no safe recovery option).", animal.getId());
            return possibleActions.REST;
        }

        // --- Decision 4: INITIATE_BATTLE ---
        boolean canFightEffectively = animalIsFitToFight(animal, health, conditions);
        boolean isTerritorialAnimalInItsTerritory = false;
        if (tags.contains(Tags.TERRITORIAL) && animal.getTerritories() != null && animal.getTerritories().contains(currentCell)) {
            isTerritorialAnimalInItsTerritory = true;
        }
        if (canFightEffectively) {
            if (tags.contains(Tags.CARNY)) {
                Animal prey = findSuitablePrey(animal, currentCellAnalysed.weakAnimals, MIN_COMBAT_ADVANTAGE_RATIO_FOR_PREY, friendsInCell);
                if (prey != null) {
                    logger.log(Level.INFO, "Animal ID: {0} chose action: INITIATE_BATTLE (predation on ID: {1}).", new Object[]{animal.getId(), prey.getId()});
                    return possibleActions.INITIATE_BATTLE;
                }
            }
            if (tags.contains(Tags.FIGHT)) {
                Animal target = findSuitableCombatTarget(animal, currentCellAnalysed.sensedAnimals,
                        currentCellAnalysed.dangerousAnimals, friendsInCell,
                        isTerritorialAnimalInItsTerritory, tags);
                if (target != null) {
                    logger.log(Level.INFO, "Animal ID: {0} chose action: INITIATE_BATTLE (FIGHT engagement with ID: {1}).", new Object[]{animal.getId(), target.getId()});
                    return possibleActions.INITIATE_BATTLE;
                }
            }
        }

        // --- Decision 5: FORNICATE ---
        boolean isStarving = conditions.contains(Conditions.STARVING);
        boolean isSuperStarving = conditions.contains(Conditions.SUPER_STARVING);
        if (animal.isReadyForReproduction() &&
                !isCriticallyInjured && !isExhausted && !(isStarving || isSuperStarving) &&
                currentCellAnalysed.cellDanger < effectiveDangerThresholdSocial) {
            Animal mate = findWillingMate(animal, currentCellAnalysed.sensedAnimals);
            if (mate != null) {
                logger.log(Level.INFO, "Animal ID: {0} chose action: FORNICATE with ID: {1}.", new Object[]{animal.getId(), mate.getId()});
                return possibleActions.FORNICATE;
            }
        }

        // --- Decision 6: ADDRESS ACTUAL HUNGER ---
         // You had this defined earlier
        boolean isHungry = conditions.contains(Conditions.HUNGRY);
        boolean isActuallyHungryByCondition = isSuperStarving || isStarving || isHungry;

        if (!animal.hasEatenThisTick() && isActuallyHungryByCondition) { // <<< ADDED !animal.hasEatenThisTick() CHECK
            if (currentCellAnalysed.cellFoodAvailability >= MIN_FOOD_AVAILABILITY_TO_EAT) {
                logger.log(Level.INFO, "Animal {0} chose action: EAT (to satisfy hunger).", animal.getId());
                return possibleActions.EAT;
            }
        }

        // --- Decision 7: OPPORTUNISTIC EAT ---
        if (!animal.hasEatenThisTick() && currentCellAnalysed.cellFoodAvailability >= MIN_FOOD_FOR_OPPORTUNISTIC_EAT) { // <<< ADDED !animal.hasEatenThisTick() CHECK
            boolean safeForOpportunisticEat = currentCellAnalysed.cellDanger < DANGER_THRESHOLD_FOR_REST; // Your original condition
            boolean healthyEnoughForOpportunisticEat = health != HealthStatus.GRAVELY_INJURED && health != HealthStatus.SEVERELY_INJURED && !isExhausted; // Your original condition
            if (safeForOpportunisticEat && healthyEnoughForOpportunisticEat) {
                logger.log(Level.INFO, "Animal {0} chose action: EAT (opportunistic).", animal.getId());
                return possibleActions.EAT;
            }
        }


        // --- Decision 8: GENERAL FATIGUE or MINOR INJURY RECOVERY ---
        // Animals choose DEEP_REST to recover fatigue/exhaustion and gain DRPs if possible.
        // isFatigued and isExhausted are already defined.
        boolean isMinorlyInjured = health == HealthStatus.INJURED || health == HealthStatus.SLIGHTLY_INJURED;
        if (isFatigued || isExhausted || isMinorlyInjured) { // If exhausted, it didn't meet critical recovery (Decision 3 means spot was bad)
            logger.log(Level.FINE, "Animal ID: {0} is fatigued/exhausted/minorly_injured. Considering recovery.", animal.getId());
            if (canDeepRestHere) { // Best option for fatigue/exhaustion, and increases DRP
                logger.log(Level.INFO, "Animal ID: {0} chose action: DEEP_REST (for fatigue/exhaustion/minor injury).", animal.getId());
                return possibleActions.DEEP_REST;
            }
            if (canRestHere) { // Second best for fatigue/minor injury
                logger.log(Level.INFO, "Animal ID: {0} chose action: REST (for fatigue/minor injury, cannot deep rest).", animal.getId());
                return possibleActions.REST;
            }
            if (currentCellAnalysed.cellHospitality >= HOSPITALITY_FOR_EFFECTIVE_HIDE / 2.0) {
                logger.log(Level.INFO, "Animal ID: {0} chose action: HIDE (fatigued/exhausted/minorly_injured, cannot rest/deep_rest).", animal.getId());
                return possibleActions.HIDE;
            }
        }

        // --- Decision 9: PROACTIVE HIDE (Strategic choice) ---
        boolean shouldProactivelyHide = (tags.contains(Tags.STEALTH) || isIsolatedSocialAnimal) &&
                currentCellAnalysed.cellPotentialDanger > (DANGER_THRESHOLD_FOR_DEEP_REST / 2.0) &&
                currentCellAnalysed.cellHospitality >= HOSPITALITY_FOR_EFFECTIVE_HIDE;
        if (shouldProactivelyHide) {
            logger.log(Level.INFO, "Animal ID: {0} chose action: HIDE (proactive).", animal.getId());
            return possibleActions.HIDE;
        }

        // --- Fallback Logic ---

        // --- Decision 10: CONSCIOUS OPTIONAL REST ---
        if (canRestHere) {
            logger.log(Level.INFO, "Animal ID: {0} chose action: REST (conscious optional choice).", animal.getId());
            return possibleActions.REST;
        }

        // --- Decision 11: BASIC FALLBACK EAT ---
        if (currentCellAnalysed.cellFoodAvailability >= MIN_FOOD_FOR_OPPORTUNISTIC_EAT) { // <<< ADDED !animal.hasEatenThisTick() CHECK
            boolean safeEnoughForFallbackRest = currentCellAnalysed.cellDanger < (DANGER_THRESHOLD_FOR_REST + 5);
            if (safeEnoughForFallbackRest) {
                logger.log(Level.INFO, "Animal {0} chose action: EAT (BASIC FALLBACK, food available).", animal.getId());
                return possibleActions.DEEP_REST;
            }
        }

        // --- Decision 12: LAST RESORT HIDE ---
        if (currentCellAnalysed.cellHospitality >= HOSPITALITY_FOR_EFFECTIVE_HIDE / 2.0) {
            logger.log(Level.INFO, "Animal ID: {0} chose action: HIDE (last resort).", animal.getId());
            return possibleActions.HIDE;
        }

        // --- Decision 13: ABSOLUTE FALLBACK - MINIMAL REST ---
        logger.log(Level.INFO, "Animal ID: {0} chose action: REST (ABSOLUTE FALLBACK).", animal.getId());
        return possibleActions.REST;
    }
        // --- Helper Methods ---

        private static boolean animalIsFitToFight(Animal animal, HealthStatus health, HashSet<Conditions> conditions) {
            // Null check for conditions before using .contains()
            if (health == HealthStatus.GRAVELY_INJURED || health == HealthStatus.SEVERELY_INJURED) return false;
            if (animal.getHealthPoints() < animal.getMaxHealth() * 0.25f) return false;

            if (conditions != null) {
                if (conditions.contains(Conditions.EXHAUSTED) || conditions.contains(Conditions.PRONE) ||
                        conditions.contains(Conditions.GRAPPLED) || conditions.contains(Conditions.INCAPACITATED) ||
                        conditions.contains(Conditions.DAZED) || conditions.contains(Conditions.SEVERELY_ILL)) {
                    return false;
                }
            }
            return true;
        }

        private static float calculateGroupCR(Animal leader, List<Animal> groupMembersInCell) {
            float totalCR = leader.getDynamicCR();
            if (groupMembersInCell != null) { // Null check for safety
                for (Animal member : groupMembersInCell) {
                    totalCR += member.getDynamicCR() * 0.75f;
                }
            }
            return totalCR;
        }

    // In actionChoice.java helper methods:
    private static Animal findSuitableCombatTarget(Animal self, HashSet<Animal> sensedAnimals,
                                                   HashSet<Animal> dangerousAnimals, List<Animal> friendsInCell,
                                                   boolean isTerritorialEncounter, HashSet<Tags> selfTags) {
        if (sensedAnimals == null || sensedAnimals.isEmpty()) return null;

        float selfEffectiveCR = calculateGroupCR(self, friendsInCell);
        HashSet<Animal> selfGroup = self.getGroup();

        Animal potentialTarget = null;
        float bestScore = Float.NEGATIVE_INFINITY; // To pick the best target based on CR ratio

        for (Animal other : sensedAnimals) {
            if (other.equals(self) || (selfGroup != null && selfGroup.contains(other)) ||
                    (friendsInCell != null && friendsInCell.contains(other))) { // Skip self, own group, and allies in cell
                continue;
            }

            float otherCR = other.getDynamicCR();
            if (otherCR <= 0) otherCR = 0.1f; // Avoid division by zero/fighting "nothing"

            float currentRatio = selfEffectiveCR / otherCR;

            boolean isSpecificallyTriggered = (self.conditions != null && self.conditions.contains(Conditions.ANGRY)) || isTerritorialEncounter;
            boolean generalAggressionViable = selfTags.contains(Tags.FIGHT); // Does the animal have the FIGHT tag?

            if (isSpecificallyTriggered) {
                if (currentRatio >= MIN_COMBAT_ADVANTAGE_RATIO_GENERAL) { // e.g., 0.8
                    // If target is dangerous & self is not angry (i.e., territorial but calm), demand better odds
                    if (dangerousAnimals != null && dangerousAnimals.contains(other) &&
                            (self.conditions == null || !self.conditions.contains(Conditions.ANGRY))) {
                        if (currentRatio >= MIN_COMBAT_ADVANTAGE_RATIO_GENERAL * 1.2f) { // Needs ~0.96 advantage
                            if (currentRatio > bestScore) { bestScore = currentRatio; potentialTarget = other; }
                        }
                    } else { // Easier target, or self is ANGRY (less cautious)
                        if (currentRatio > bestScore) { bestScore = currentRatio; potentialTarget = other; }
                    }
                }
            } else if (generalAggressionViable) { // Has FIGHT tag, not specifically triggered - opportunistic aggression
                // Avoid attacking 'dangerousAnimals' unless the advantage is clearly high
                if (dangerousAnimals != null && dangerousAnimals.contains(other)) {
                    if (currentRatio >= MIN_COMBAT_ADVANTAGE_RATIO_FOR_PREY) { // Needs significant advantage (e.g., 1.2)
                        if (currentRatio > bestScore) { bestScore = currentRatio; potentialTarget = other; }
                    }
                } else { // Target is not classified as 'dangerous' to self
                    if (currentRatio >= MIN_COMBAT_ADVANTAGE_RATIO_GENERAL) { // Standard advantage (e.g., 0.8)
                        if (currentRatio > bestScore) { bestScore = currentRatio; potentialTarget = other; }
                    }
                }
            }
        }
        if (potentialTarget != null) {
            logger.log(Level.FINEST, "Animal {0} (GroupCR: {1}) found suitable combat target: {2} (Best CR Ratio: {3}, Territorial: {4})",
                    new Object[]{self.getId(), selfEffectiveCR, potentialTarget.getId(), bestScore, isTerritorialEncounter});
        }
        return potentialTarget;
    }

        private static Animal findWillingMate(Animal self, HashSet<Animal> sensedAnimals) {
            if (sensedAnimals == null || sensedAnimals.isEmpty()) return null;
            Animal foundMate = null;
            for (Animal other : sensedAnimals) {
                if (other.equals(self)) continue;

                if (self.getClass().equals(other.getClass()) &&
                        self.isFemale() != other.isFemale() &&
                        other.isReadyForReproduction()) {
                    foundMate = other;
                    break; // Found a mate
                }
            }
            logger.log(Level.FINEST, "Animal {0} found willing mate: {1}", new Object[]{self, foundMate});
            return foundMate;
        }
    private static Animal findSuitablePrey(Animal predator, HashSet<Animal> weakAnimals, float advantageNeeded, List<Animal> friendsInCell) {
        if (weakAnimals == null || weakAnimals.isEmpty()) return null;
        float predatorEffectiveCR = calculateGroupCR(predator, friendsInCell);
        Animal chosenPrey = null;
        float bestRatio = 0;

        for (Animal prey : weakAnimals) {
            if (prey.equals(predator) || (friendsInCell != null && friendsInCell.contains(prey))) continue;
            float preyCR = prey.getDynamicCR();
            if (preyCR <= 0) preyCR = 0.1f;
            if (predatorEffectiveCR / preyCR >= advantageNeeded) {
                float currentRatio = predatorEffectiveCR / preyCR;
                if (chosenPrey == null || currentRatio > bestRatio) {
                    bestRatio = currentRatio;
                    chosenPrey = prey;
                }
            }
        }
        if (chosenPrey != null) {
            logger.log(Level.FINEST, "Predator ID: {0} (GroupCR: {1}) found suitable prey ID: {2} (Ratio: {3})",
                    new Object[]{predator.getId(), predatorEffectiveCR, chosenPrey.getId(), bestRatio});
        }
        return chosenPrey;
    }
}


