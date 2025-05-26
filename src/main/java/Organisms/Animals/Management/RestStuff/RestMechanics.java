package Organisms.Animals.Management.RestStuff;

import Organisms.Animals.Animal;
import Organisms.Animals.Conditions.ConditionEffects;
import Organisms.Animals.Conditions.Conditions;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Organisms.Animals.Conditions.ConditionEffects.*;
import static Organisms.Animals.Management.RestStuff.LevelUp.*;

public class RestMechanics {
    private static final Logger logger = Logger.getLogger(RestMechanics.class.getName());
    private static final Random random = new Random();

    private static final Set<Conditions> MINOR_CONDITIONS_REMOVABLE_BY_REST = Set.of(
            Conditions.STRESSED,
            Conditions.MILDLY_ILL,
            Conditions.DISORIENTED
    );

    private static final Set<Conditions> OTHER_CONDITIONS_REMOVABLE_AT_DEEPREST_CYCLE3 = Set.of(
            Conditions.SEVERELY_ILL,
            Conditions.POISONED,
            Conditions.HOBBLED,
            Conditions.DAZED,
            Conditions.STRESSED,
            Conditions.MILDLY_ILL,
            Conditions.DISORIENTED
    );

    private static final int BASE_REST_HEAL_AMOUNT = 10;
    private static final int BASE_DEEP_REST_HEAL_AMOUNT_CYCLE3 = 50;

    private static final int REST_ENERGY_COST = 5;
    private static final int DEEP_REST_CYCLE_ENERGY_COST = 4;
    private static final int LEVEL_UP_ENERGY_COST_PER_ATTEMPT = 30;
    private static final int MIN_ENERGY_FOR_LEVEL_UP_PHASE = 50;

    public static final int MAX_DEEP_REST_POINTS = 13;

    private static final int ACTION_POINTS_COST_REST = 1;
    private static final int ACTION_POINTS_COST_DEEP_REST_CYCLE = 1;

    public static void rest(Animal animal) {
        if (animal == null || animal.isDead()) {
            logger.log(Level.WARNING, "Rest attempt on null or dead animal: {0}", animal);
            return;
        }
        logger.log(Level.FINE, "Animal {0} performs a light rest cycle (Conceptual AP Cost: {1}).",
                new Object[]{animal, ACTION_POINTS_COST_REST});

        // Apply RESTING condition at the start
        restStart(animal);
        if (animal.conditions == null) animal.conditions = new HashSet<>(); // Ensure not null
        animal.conditions.add(Conditions.RESTING);
        logger.log(Level.FINE, "Animal {0} applied RESTING condition.", animal);


        int healAmount = BASE_REST_HEAL_AMOUNT + (animal.getConstitution() / 10);
        int currentHP = animal.getHealthPoints();
        int maxHP = animal.getMaxHealth();
        int newHP = Math.min(maxHP, currentHP + healAmount);
        if (newHP > currentHP) {
            animal.setHealthPoints(newHP);
            logger.log(Level.FINE, "Animal {0} healed by {1} HP during rest. Current HP: {2}/{3}",
                    new Object[]{animal, (newHP - currentHP), newHP, maxHP});
        }

        if (animal.conditions != null && animal.conditions.contains(Conditions.FATIGUED)) {
            fatiguedEnd(animal);
            animal.conditions.remove(Conditions.FATIGUED);
            logger.log(Level.INFO, "Animal {0} automatically removed FATIGUED condition during rest.", animal);
        }

        if (animal.conditions != null && !animal.conditions.isEmpty()) {
            List<Conditions> presentMinorConditions = new ArrayList<>();
            for (Conditions condition : MINOR_CONDITIONS_REMOVABLE_BY_REST) {
                if (animal.conditions.contains(condition)) {
                    presentMinorConditions.add(condition);
                }
            }

            if (!presentMinorConditions.isEmpty()) {
                Conditions conditionToRemove = presentMinorConditions.get(random.nextInt(presentMinorConditions.size()));
                // Directly call specific end effect from ConditionEffects
                switch (conditionToRemove) {
                    case STRESSED:
                       stressEnd(animal);
                        break;
                    case MILDLY_ILL:
                        mildIllnessEnd(animal);
                        break;
                    case DISORIENTED:
                        disorientedEnd(animal);
                        break;
                    default:
                        logger.log(Level.WARNING, "No specific ...End effect method in RestMechanics for minor condition: {0} on animal {1}", new Object[]{conditionToRemove, animal});
                        break;
                }
                animal.conditions.remove(conditionToRemove);
                logger.log(Level.INFO, "Animal {0} removed minor condition {1} during rest.",
                        new Object[]{animal, conditionToRemove});
            }
        }

        if (animal.getStoredEnergyPoints() >= REST_ENERGY_COST) {
            animal.setStoredEnergyPoints(animal.getStoredEnergyPoints() - REST_ENERGY_COST);
            logger.log(Level.FINE, "Animal {0} consumed {1} energy for resting. Remaining energy: {2}",
                    new Object[]{animal, REST_ENERGY_COST, animal.getStoredEnergyPoints()});
        } else {
            logger.log(Level.WARNING, "Animal {0} has insufficient energy ({1}) for resting (cost: {2}).",
                    new Object[]{animal, animal.getStoredEnergyPoints(), REST_ENERGY_COST});
        }

        // RESTING condition is NOT removed here; it's expected to be removed by the next action or game tick.
        logger.log(Level.INFO, "Animal {0} finished a light rest cycle. RESTING condition remains active.", animal);
        animal.actionPoints--;
    }

    public static void deepRestCycle(Animal animal) {
        int currentDeepRestCycleCounter=animal.getDeepRestPoints();
        if (animal == null || animal.isDead()) {
            logger.log(Level.WARNING, "Deep rest cycle attempt on null or dead animal: {0}", animal);
            return;
        }
        if (animal.conditions == null) animal.conditions = new HashSet<>();

        boolean wasPreviouslyDeepResting = animal.conditions.contains(Conditions.DEEP_RESTING);

        if (currentDeepRestCycleCounter >= 2 &&
                (animal.conditions.contains(Conditions.FATIGUED) || animal.conditions.contains(Conditions.EXHAUSTED))) {
            logger.log(Level.FINE, "Animal {0} became FATIGUED or EXHAUSTED again during deep rest session (was at cycle {1}). Resetting deep rest cycle count.",
                    new Object[]{animal, currentDeepRestCycleCounter});
            if (wasPreviouslyDeepResting) { // If it was deep resting, end the condition before reset
                deepRestEnd(animal);
                animal.conditions.remove(Conditions.DEEP_RESTING);
                logger.log(Level.FINE, "Animal {0} ended DEEP_RESTING due to cycle reset.", animal);
            }
            currentDeepRestCycleCounter = 0;
        }

        int nextCycleCount = currentDeepRestCycleCounter + 1;
        logger.log(Level.INFO, "Animal {0} performs deep rest cycle {1} (Conceptual AP Cost: {2}).",
                new Object[]{animal, nextCycleCount, ACTION_POINTS_COST_DEEP_REST_CYCLE});

        // Apply DEEP_RESTING condition if this is the start of a new session (cycle 1)
        // or if it was removed due to a reset and now we are starting cycle 1 again.
        if (nextCycleCount == 1 && !wasPreviouslyDeepResting) {
           deepRestStart(animal);
            animal.conditions.add(Conditions.DEEP_RESTING);
            logger.log(Level.FINE, "Animal {0} started DEEP_RESTING.", animal);
        } else if (nextCycleCount == 1 && wasPreviouslyDeepResting && !animal.conditions.contains(Conditions.DEEP_RESTING)){
            // This case handles if DEEP_RESTING was removed due to cycle reset and now it's cycle 1 again
           deepRestStart(animal);
            animal.conditions.add(Conditions.DEEP_RESTING);
            logger.log(Level.FINE, "Animal {0} re-started DEEP_RESTING after cycle reset.", animal);
        }


        if (animal.getDeepRestPoints() < MAX_DEEP_REST_POINTS) {
            animal.conditions.add(Conditions.DEEP_RESTING);
            animal.setDeepRestPoints(animal.getDeepRestPoints() + 1);
            logger.log(Level.FINE, "Animal {0} gained 1 deepRestPoint (stamina). Total: {1}/{2}",
                    new Object[]{animal, animal.getDeepRestPoints(), MAX_DEEP_REST_POINTS});
        }

        if (animal.getStoredEnergyPoints() >= DEEP_REST_CYCLE_ENERGY_COST) {
            animal.setStoredEnergyPoints(animal.getStoredEnergyPoints() - DEEP_REST_CYCLE_ENERGY_COST);
            logger.log(Level.FINE, "Animal {0} consumed {1} energy for deep rest cycle. Remaining energy: {2}",
                    new Object[]{animal, DEEP_REST_CYCLE_ENERGY_COST, animal.getStoredEnergyPoints()});
        } else {
            logger.log(Level.WARNING, "Animal {0} has insufficient energy ({1}) for deep rest cycle {2} (cost: {3}). Benefits may be limited.",
                    new Object[]{animal, animal.getStoredEnergyPoints(), nextCycleCount, DEEP_REST_CYCLE_ENERGY_COST});
        }

        if (nextCycleCount == 2) {
            logger.log(Level.INFO, "Animal {0} reached deep rest cycle 2: Fatigue/Exhaustion removal phase.", animal);
            if (animal.conditions.contains(Conditions.FATIGUED)) {
                fatiguedEnd(animal);
                logger.log(Level.INFO, "Animal {0} removed FATIGUED at deep rest cycle 2.", animal);
            }
            if (animal.conditions.contains(Conditions.EXHAUSTED)) {
                exhaustedEnd(animal);
                logger.log(Level.INFO, "Animal {0} removed EXHAUSTED at deep rest cycle 2.", animal);
            }
        }

        if (nextCycleCount == 3) {
            logger.log(Level.INFO, "Animal {0} reached deep rest cycle 3: Healing & other condition removal phase.", animal);
            int healAmount = BASE_DEEP_REST_HEAL_AMOUNT_CYCLE3 + (animal.getConstitution() / 5);
            int currentHP = animal.getHealthPoints();
            int maxHP = animal.getMaxHealth();
            int newHP = Math.min(maxHP, currentHP + healAmount);
            if (newHP > currentHP) {
                animal.setHealthPoints(newHP);
                logger.log(Level.FINE, "Animal {0} healed by {1} HP at deep rest cycle 3. Current HP: {2}/{3}",
                        new Object[]{animal, (newHP - currentHP), newHP, maxHP});
            }

            if (!animal.conditions.isEmpty()) {
                List<Conditions> presentOtherConditions = new ArrayList<>();
                for (Conditions condition : OTHER_CONDITIONS_REMOVABLE_AT_DEEPREST_CYCLE3) {
                    if (animal.conditions.contains(condition)) {
                        presentOtherConditions.add(condition);
                    }
                }
                if (!presentOtherConditions.isEmpty()) {
                    Conditions conditionToRemove = presentOtherConditions.get(random.nextInt(presentOtherConditions.size()));
                    switch (conditionToRemove) {
                        case SEVERELY_ILL: severeIllnessEnd(animal); break;
                        case POISONED: poisonEnd(animal); break;
                        case HOBBLED: hobbledEnd(animal); break;
                        case DAZED: dazedEnd(animal); break;
                        case STRESSED: stressEnd(animal); break;
                        case MILDLY_ILL: mildIllnessEnd(animal); break;
                        case DISORIENTED: disorientedEnd(animal); break;
                        default: logger.log(Level.WARNING, "No specific ...End effect method in RestMechanics for other condition: {0} on animal {1}", new Object[]{conditionToRemove, animal}); break;
                    }
                    animal.conditions.remove(conditionToRemove);
                    logger.log(Level.INFO, "Animal {0} removed other condition {1} at deep rest cycle 3.",
                            new Object[]{animal, conditionToRemove});
                }
            }
        }

        if (nextCycleCount == 6) {
            logger.log(Level.INFO, "Animal {0} reached deep rest cycle 6: Level up phase.", animal);
            boolean hasRemovableConditionsForLevelUp = false;
            if (animal.conditions.contains(Conditions.FATIGUED) || animal.conditions.contains(Conditions.EXHAUSTED)) {
                hasRemovableConditionsForLevelUp = true;
            }
            if (!hasRemovableConditionsForLevelUp) {
                for (Conditions cond : OTHER_CONDITIONS_REMOVABLE_AT_DEEPREST_CYCLE3) {
                    if (animal.conditions.contains(cond)) {
                        hasRemovableConditionsForLevelUp = true;
                        break;
                    }
                }
            }

            if (animal.getHealthPoints() >= animal.getMaxHealth() * 0.85 &&
                    !hasRemovableConditionsForLevelUp &&
                    animal.getStoredEnergyPoints() >= MIN_ENERGY_FOR_LEVEL_UP_PHASE) {
                logger.log(Level.INFO, "Animal {0} is attempting to level up. Energy available for attempt: {1}",
                        new Object[]{animal, animal.getStoredEnergyPoints()});
                if (animal.getStoredEnergyPoints() >= LEVEL_UP_ENERGY_COST_PER_ATTEMPT) {
                    int choice = random.nextInt(3);
                    if (choice == 0 && (animal.getMaxHealth() < animal.getSize() * 20 || animal.getHealthPoints() == animal.getMaxHealth())) {
                        try {
                            increaseMaxHealth(animal, LEVEL_UP_ENERGY_COST_PER_ATTEMPT);
                            logger.log(Level.INFO, "Animal {0} increased Max Health. Energy remaining: {1}", new Object[]{animal, animal.getStoredEnergyPoints()});
                        } catch (ArithmeticException e) { logger.log(Level.WARNING, "Animal {0} failed to increase Max Health: {1}", new Object[]{animal, e.getMessage()});}
                    } else if (choice == 1 || (choice == 0 && !(animal.getMaxHealth() < animal.getSize() * 20 || animal.getHealthPoints() == animal.getMaxHealth()))) {
                        String[] coreAttributes = {"strength", "agility", "constitution", "speed", "perception", "stealth"};
                        String statToIncrease = coreAttributes[random.nextInt(coreAttributes.length)];
                        try {
                            increaseAttribute(animal, statToIncrease, LEVEL_UP_ENERGY_COST_PER_ATTEMPT);
                            logger.log(Level.INFO, "Animal {0} increased {1}. Energy remaining: {2}", new Object[]{animal, statToIncrease, animal.getStoredEnergyPoints()});
                        } catch (ArithmeticException e) { logger.log(Level.WARNING, "Animal {0} failed to increase {1}: {2}", new Object[]{animal, statToIncrease, e.getMessage()});}
                    } else {
                        try {
                            increaseFatStorage(animal, LEVEL_UP_ENERGY_COST_PER_ATTEMPT);
                            logger.log(Level.INFO, "Animal {0} increased Fat Storage. Energy remaining: {1}", new Object[]{animal, animal.getStoredEnergyPoints()});
                        } catch (ArithmeticException e) { logger.log(Level.WARNING, "Animal {0} failed to increase Fat Storage: {1}", new Object[]{animal, e.getMessage()});}
                    }
                } else {
                    logger.log(Level.FINE, "Animal {0} does not have enough energy ({1}) for a level up attempt (cost: {2}).", new Object[]{animal, animal.getStoredEnergyPoints(), LEVEL_UP_ENERGY_COST_PER_ATTEMPT});
                }
            } else {
                logger.log(Level.FINE, "Animal {0} not eligible for level up at deep rest cycle 6. HP: {1}/{2}, HasRemovableConditions: {3}, Energy: {4} (Min Req: {5})", new Object[]{animal, animal.getHealthPoints(), animal.getMaxHealth(), hasRemovableConditionsForLevelUp, animal.getStoredEnergyPoints(), MIN_ENERGY_FOR_LEVEL_UP_PHASE});
            }
        }

        logger.log(Level.INFO, "Animal {0} finished deep rest cycle {1}.", new Object[]{animal, nextCycleCount});
        animal.setDeepRestPoints(nextCycleCount);
        animal.actionPoints--;
    }
}
