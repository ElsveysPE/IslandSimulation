package Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Conditions.Conditions;
import Organisms.Animals.LifeStage;
import Organisms.Animals.Tags;
import Organisms.HealthStatus;

import java.util.HashSet;
import java.util.logging.Logger;
import java.util.logging.Level;

import static Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic.CellFavor.formCellFavor;

public class cellFinalAnalysis {

    public static CellFavor finalAnalysis(Animal animal, CellAnalysed cellAnalysed) {
        final Logger logger = Logger.getLogger(cellFinalAnalysis.class.getName());

        if (animal == null || cellAnalysed == null || cellAnalysed.getCell() == null) {
            logger.warning("finalAnalysis received null parameters. animal: " + animal + ", cellAnalysed: " + cellAnalysed);
            CellFavor errorResult = formCellFavor((cellAnalysed != null && cellAnalysed.getCell() != null) ? cellAnalysed.getCell() : null);
            errorResult.setFavor(Integer.MIN_VALUE);
            return errorResult;
        }
        CellFavor finRes = formCellFavor(cellAnalysed.getCell());

        int favor = 0;
        favor += cellAnalysed.cellHospitality;
        favor += cellAnalysed.cellFoodAvailability;
        favor += cellAnalysed.cellImportance * 2;
        favor -= cellAnalysed.cellDanger * 3;
        favor -= cellAnalysed.cellPotentialDanger;

        // --- 1. Health Status Modifiers ---
        // Using the HealthStatus enum: HEALTHY, SLIGHTLY_INJURED, INJURED, SEVERELY_INJURED, GRAVELY_INJURED
        HealthStatus healthStatus = animal.getHealthStatus();
        if (healthStatus != null) {
            switch (healthStatus) {
                case GRAVELY_INJURED:
                    favor += cellAnalysed.cellHospitality * 2;
                    favor -= cellAnalysed.cellDanger * 2;
                    favor -= cellAnalysed.cellPotentialDanger;
                    break;
                case SEVERELY_INJURED:
                    favor += cellAnalysed.cellHospitality;
                    favor -= cellAnalysed.cellDanger;
                    favor -= cellAnalysed.cellPotentialDanger / 2;
                    break;
                case INJURED:
                    favor += cellAnalysed.cellHospitality / 2; // Still need some safety
                    favor -= cellAnalysed.cellDanger / 2;
                    favor -= cellAnalysed.cellPotentialDanger / 4;
                    break;
                case SLIGHTLY_INJURED:
                    favor += cellAnalysed.cellHospitality / 4; // Minor preference for safety
                    favor -= cellAnalysed.cellDanger / 4;      // Slightly more cautious
                    break;
                case HEALTHY:
                    // No specific modification based on being healthy
                    break;
            }
        }

        // --- 2. Conditions Modifiers ---
        HashSet<Conditions> conditions = animal.conditions; // Assuming this method exists on Animal
        HashSet<Tags> tags = animal.getTags();                 // Assuming this method exists on Animal

        if (conditions != null && !conditions.isEmpty()) {
            if (conditions.contains(Conditions.SUPER_STARVING)) {
                favor += cellAnalysed.cellFoodAvailability * 3;
            } else if (conditions.contains(Conditions.STARVING)) {
                favor += cellAnalysed.cellFoodAvailability * 2;
            } else if (conditions.contains(Conditions.HUNGRY)) {
                favor += cellAnalysed.cellFoodAvailability;
            }

            if (conditions.contains(Conditions.SEVERELY_BLEEDING)) {
                favor += cellAnalysed.cellHospitality;
                favor -= cellAnalysed.cellDanger * 2;
                if (tags != null && tags.contains(Tags.STEALTH)) {
                    favor += cellAnalysed.cellHospitality;
                }
            }

            if (conditions.contains(Conditions.MILDLY_ILL)) {
                favor += cellAnalysed.cellHospitality / 2;
                favor -= cellAnalysed.cellDanger / 2;
                favor += cellAnalysed.cellFoodAvailability / 3;
            }
            if (conditions.contains(Conditions.SEVERELY_ILL)) {
                favor += cellAnalysed.cellHospitality;
                favor -= cellAnalysed.cellDanger;
                favor += cellAnalysed.cellFoodAvailability / 2;
                if (tags != null && tags.contains(Tags.STEALTH)) {
                    favor += cellAnalysed.cellHospitality;
                }
            }
            if (conditions.contains(Conditions.FATIGUED)) {
                favor += cellAnalysed.cellHospitality;
                favor -= cellAnalysed.cellDanger / 2;
                favor -= cellAnalysed.cellFoodAvailability / 4;
            }
            if (conditions.contains(Conditions.EXHAUSTED)) {
                favor += cellAnalysed.cellHospitality * 2;
                favor -= cellAnalysed.cellDanger;
                favor -= cellAnalysed.cellFoodAvailability / 2;
                if (tags != null && tags.contains(Tags.STEALTH)) {
                    favor += cellAnalysed.cellHospitality;
                }
            }
            if (conditions.contains(Conditions.POISONED)) {
                favor += (int) (cellAnalysed.cellHospitality * 1.5);
                favor -= (int) (cellAnalysed.cellDanger * 1.5);
                favor -= cellAnalysed.cellFoodAvailability / 3;
            }
            if (conditions.contains(Conditions.HOBBLED)) {
                favor += cellAnalysed.cellHospitality;
                favor -= cellAnalysed.cellDanger * 2;
            }
            if (conditions.contains(Conditions.PRONE)) {
                favor += cellAnalysed.cellHospitality * 3;
                favor -= cellAnalysed.cellDanger * 5;
                favor -= cellAnalysed.cellFoodAvailability;
                favor -= cellAnalysed.cellImportance;
            }
            if (conditions.contains(Conditions.GRAPPLED)) {
                favor += cellAnalysed.cellHospitality * 2;
                favor -= cellAnalysed.cellDanger * 4;
                favor -= (int) (cellAnalysed.cellFoodAvailability * 1.5);
                favor -= (int) (cellAnalysed.cellImportance * 1.5);
                favor -= 50;
            }
            if (conditions.contains(Conditions.STRESSED)) {
                favor += cellAnalysed.cellHospitality / 2;
                if (tags != null && tags.contains(Tags.FIGHT)) {
                    favor += cellAnalysed.cellPotentialDanger / 3;
                } else {
                    favor -= cellAnalysed.cellPotentialDanger / 2;
                }
            }
            if (conditions.contains(Conditions.ANGRY)) {
                if (tags != null && tags.contains(Tags.FIGHT)) {
                    favor -= cellAnalysed.cellHospitality / 3;
                    if (cellAnalysed.weakAnimals != null && !cellAnalysed.weakAnimals.isEmpty()) {
                        favor += cellAnalysed.cellPotentialDanger / 2;
                        favor += cellAnalysed.cellFoodAvailability / 3;
                    } else {
                        favor -= cellAnalysed.cellDanger / 2;
                    }
                } else {
                    favor += cellAnalysed.cellHospitality / 2;
                    favor -= cellAnalysed.cellDanger;
                }
            }
            if (conditions.contains(Conditions.BLEEDING)) {
                favor += cellAnalysed.cellHospitality;
                favor -= cellAnalysed.cellDanger;
                if (tags != null && tags.contains(Tags.STEALTH)) {
                    favor += cellAnalysed.cellHospitality / 2;
                }
            }
            if (conditions.contains(Conditions.DISORIENTED)) {
                favor += cellAnalysed.cellHospitality * 2;
                favor -= cellAnalysed.cellDanger * 3;
                favor -= cellAnalysed.cellFoodAvailability / 2;
                favor -= cellAnalysed.cellImportance / 2;
            }
            if (conditions.contains(Conditions.DAZED)) {
                favor += (int) (cellAnalysed.cellHospitality * 2.5);
                favor -= (int) (cellAnalysed.cellDanger * 3.5);
                favor -= cellAnalysed.cellFoodAvailability;
                favor -= cellAnalysed.cellImportance;
            }
            if (conditions.contains(Conditions.INCAPACITATED)) {
                if (cellAnalysed.cellDanger > 0 || cellAnalysed.cellPotentialDanger > 0) {
                    favor = Integer.MIN_VALUE + 1;
                } else {
                    favor += cellAnalysed.cellHospitality * 10;
                }
                favor -= cellAnalysed.cellFoodAvailability * 2;
                favor -= cellAnalysed.cellImportance * 2;
            }
            if (conditions.contains(Conditions.RESTING)) {
                favor += cellAnalysed.cellHospitality * 3;
                favor -= cellAnalysed.cellDanger * 2;
                favor -= cellAnalysed.cellFoodAvailability / 2;
                favor -= cellAnalysed.cellImportance / 2;
            }
            if (conditions.contains(Conditions.DEEP_RESTING)) {
                favor += cellAnalysed.cellHospitality * 5;
                favor -= cellAnalysed.cellDanger * 3;
                favor -= cellAnalysed.cellFoodAvailability;
                favor -= cellAnalysed.cellImportance;
            }

            if (conditions.contains(Conditions.PANICKING)) {
                int panicFavor = 0;
                panicFavor += cellAnalysed.cellHospitality * 5;
                panicFavor -= cellAnalysed.cellDanger * 10;
                panicFavor -= cellAnalysed.cellPotentialDanger * 5;
                panicFavor -= cellAnalysed.cellFoodAvailability / 2;
                panicFavor -= cellAnalysed.cellImportance / 2;
                favor = panicFavor; // PANIC OVERRIDES
            }
        }

        // --- 3. Life Stage Modifiers ---
        // Using LifeStage enum: BABY, ADULT, OLD
        LifeStage lifeStage = animal.getLifeStage(); // Assuming this method exists
        if (lifeStage != null) { // Added null check for robustness
            switch (lifeStage) {
                case BABY:
                    favor += cellAnalysed.cellHospitality;
                    favor -= cellAnalysed.cellDanger * 2;
                    favor -= cellAnalysed.cellPotentialDanger;
                    if (cellAnalysed.cellFoodAvailability < 5) {
                        favor -= 20; // Was -10, increased penalty for baby food scarcity
                    }
                    break;
                case OLD:
                    favor += cellAnalysed.cellHospitality;      // Safety and comfort are more important
                    favor -= cellAnalysed.cellDanger;          // More vulnerable/averse to danger
                    favor -= cellAnalysed.cellPotentialDanger / 2;
                    favor += cellAnalysed.cellFoodAvailability / 2; // Easy food is good
                    favor -= cellAnalysed.cellImportance / 2;   // Less inclined towards risky/demanding tasks
                    break;
                case ADULT:
                    // No specific universal modifiers for ADULT; their behavior is primarily driven by conditions, tags, and base needs.
                    break;
            }
        }
        // if(cellAnalysed.equals(animal.getCell())) favor-=20;
        finRes.setFavor(favor);

        logger.log(Level.FINE, "[FinalAnalysis for Animal: {0}, Cell: {1}] BaseFavorCalc. HealthMod. ConditionsMod. LifeStageMod. Final favor: {2}",
                new Object[]{animal, cellAnalysed.getCell(), favor});
        return finRes;
    }
}
