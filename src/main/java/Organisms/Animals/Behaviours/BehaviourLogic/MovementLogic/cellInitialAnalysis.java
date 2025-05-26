package Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Tags;

import java.util.HashSet;

import static Organisms.Animals.BasicChecks.hidden;


import java.util.logging.Level; // Added for logging
import java.util.logging.Logger; // Added for logging

// Assuming MapStructure.Cell, Animal, Tags, CellAnalysed, and Growth (enum) are defined elsewhere

public class cellInitialAnalysis { // Conventionally CellInitialAnalysis

    // Logger instance for this class
    private static final Logger logger = Logger.getLogger(cellInitialAnalysis.class.getName());

    public static CellAnalysed initialAnalysis(Animal animal, MapStructure.Cell cell) {
        CellAnalysed finRes = CellAnalysed.formCellAnalysed(cell);
        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Initial CellAnalysed object created/reset: {2}",
                new Object[]{animal, cell, finRes});

        HashSet<Tags> tags = animal.getTags();

        // Preparation for animals
        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Starting to identify sensed animals.",
                new Object[]{animal, cell});
        for (Animal i : cell.getAnimals()) {
            if (!hidden(i, animal)) {
                boolean added = finRes.sensedAnimals.add(i);
                if (added) {
                    logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Animal {2} ADDED to sensedAnimals. (Stealth: {3} <= Perception: {4})",
                            new Object[]{animal, cell, i, i.getCurrStealth(), animal.getCurrPerception()});
                }
            }
        }
        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Finished identifying sensed animals. Count: {2}",
                new Object[]{animal, cell, finRes.sensedAnimals.size()});

        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Starting classification of {2} sensed animals.",
                new Object[]{animal, cell, finRes.sensedAnimals.size()});
        for (Animal animalInCheck : finRes.sensedAnimals) {
            logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Evaluating sensed animal: {2}",
                    new Object[]{animal, cell, animalInCheck});


            float currentCR = animal.getBasicCR();
            float initialAnalyserCR = currentCR;
            if (animal.getTags().contains(Tags.SOCIAL)) {
                HashSet<Animal> analyserGroup = animal.getGroup();
                if (analyserGroup != null) {
                    float packBonusCR = 0;
                    for (Animal packMate : finRes.sensedAnimals) {

                        if (analyserGroup.contains(packMate) && !packMate.equals(animal)) {
                            packBonusCR += packMate.getBasicCR();
                        }
                    }
                    if (packBonusCR > 0) {
                        currentCR += packBonusCR;
                        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Analyser is SOCIAL. Base CR {2} buffed by sensed packmates (+{3}) to currentCR: {4}",
                                new Object[]{animal, cell, initialAnalyserCR, packBonusCR, currentCR});
                    }
                } else {
                    logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Analyser is SOCIAL but its group is null. currentCR remains: {2}",
                            new Object[]{animal, cell, currentCR});
                }
            } else {
                logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Analyser is NOT SOCIAL. currentCR: {2}",
                        new Object[]{animal, cell, currentCR});
            }


            if (animalInCheck.getTags().contains(Tags.SOCIAL) && !finRes.dangerousAnimals.contains(animalInCheck)) {
                HashSet<Animal> groupOfAnimalInCheck = animalInCheck.getGroup();
                if (groupOfAnimalInCheck != null && !groupOfAnimalInCheck.isEmpty()) {
                    double sumCR_sensedGroupMembers = 0;
                    for (Animal presentMember : groupOfAnimalInCheck) {
                        if (finRes.sensedAnimals.contains(presentMember)) {
                            sumCR_sensedGroupMembers += presentMember.getBasicCR() / 1.5f;
                        }
                    }
                    logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Sensed social animal {2}: its group's effective sensed CR is {3}. Comparing against analyser's currentCR {4}.",
                            new Object[]{animal, cell, animalInCheck, sumCR_sensedGroupMembers, currentCR});

                    if (sumCR_sensedGroupMembers * 0.75f >= currentCR) {
                        if (finRes.dangerousAnimals.add(animalInCheck)) {
                            logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Animal {2} classified as DANGEROUS (Group CR condition: {3}*0.75 >= {4}).",
                                    new Object[]{animal, cell, animalInCheck, sumCR_sensedGroupMembers, currentCR});
                        }
                        for (Animal presentMember : groupOfAnimalInCheck) {
                            if (finRes.sensedAnimals.contains(presentMember)) {
                                if (finRes.dangerousAnimals.add(presentMember)) {
                                    logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Sensed group member {2} of DANGEROUS group ({3}) also marked DANGEROUS.",
                                            new Object[]{animal, cell, presentMember, animalInCheck});
                                }
                            }
                        }
                    } else {
                        if (finRes.weakAnimals.add(animalInCheck)) {
                            logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Animal {2} classified as WEAK (Group CR condition: {3}*0.75 < {4}).",
                                    new Object[]{animal, cell, animalInCheck, sumCR_sensedGroupMembers, currentCR});
                        }
                        for (Animal presentMember : groupOfAnimalInCheck) {
                            if (finRes.sensedAnimals.contains(presentMember)) {
                                if (finRes.weakAnimals.add(presentMember)) {
                                    logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Sensed group member {2} of WEAK group ({3}) also marked WEAK.",
                                            new Object[]{animal, cell, presentMember, animalInCheck});
                                }
                            }
                        }
                    }
                } else { // Social animalInCheck with null or empty group
                    logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Sensed social animal {2} has null/empty group. Evaluating as individual against currentCR {3}.",
                            new Object[]{animal, cell, animalInCheck, currentCR});
                    if (animalInCheck.getBasicCR() * 0.75 > currentCR) {
                        if(finRes.dangerousAnimals.add(animalInCheck)) {
                            logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Lone social animal {2} classified as DANGEROUS (Individual CR condition: {3}*0.75 > {4}).",
                                    new Object[]{animal, cell, animalInCheck, animalInCheck.getBasicCR(), currentCR});
                        }
                    } else {
                        if (finRes.weakAnimals.add(animalInCheck)) {
                            logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Lone social animal {2} classified as WEAK (Individual CR condition: {3}*0.75 <= {4}).",
                                    new Object[]{animal, cell, animalInCheck, animalInCheck.getBasicCR(), currentCR});
                        }
                    }
                }
            } else if (!finRes.dangerousAnimals.contains(animalInCheck) && !finRes.weakAnimals.contains(animalInCheck)) {
                // This 'else if' is for non-social animals, or social ones already processed
                logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Sensed non-social animal {2} (or already processed social). Evaluating as individual against currentCR {3}.",
                        new Object[]{animal, cell, animalInCheck, currentCR});
                if (animalInCheck.getBasicCR() * 0.75 > currentCR) {
                    if (finRes.dangerousAnimals.add(animalInCheck)){
                        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Non-social animal {2} classified as DANGEROUS (Individual CR condition: {3}*0.75 > {4}).",
                                new Object[]{animal, cell, animalInCheck, animalInCheck.getBasicCR(), currentCR});
                    }
                } else {
                    if (finRes.weakAnimals.add(animalInCheck)) {
                        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Non-social animal {2} classified as WEAK (Individual CR condition: {3}*0.75 <= {4}).",
                                new Object[]{animal, cell, animalInCheck, animalInCheck.getBasicCR(), currentCR});
                    }
                }
            } else {
                logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Animal {2} was already classified as dangerous or weak. Skipping re-classification.",
                        new Object[]{animal, cell, animalInCheck});
            }
        }
        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Finished classification of sensed animals.", new Object[]{animal, cell});

        // These logs will now appear once per initialAnalysis call.

        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Starting cell property calculations (growth, corpses, etc.).", new Object[]{animal, cell});

        // Growth management
        String growthConditionStealth = "Growth: " + cell.getGrowth() + " with Analyser STEALTH: " + tags.contains(Tags.STEALTH);
        if (tags.contains(Tags.STEALTH)) {
            switch (cell.getGrowth()) {
                case RAMPANT:
                    finRes.cellHospitality += 20; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellHospitality to {2} (+20). {3}", new Object[]{animal, cell, finRes.cellHospitality, growthConditionStealth});
                    finRes.cellPotentialDanger += 5; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellPotentialDanger to {2} (+5). {3}", new Object[]{animal, cell, finRes.cellPotentialDanger, growthConditionStealth});
                    break;
                case SEVERE:
                    finRes.cellHospitality += 15; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellHospitality to {2} (+15). {3}", new Object[]{animal, cell, finRes.cellHospitality, growthConditionStealth});
                    finRes.cellPotentialDanger += 2; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellPotentialDanger to {2} (+2). {3}", new Object[]{animal, cell, finRes.cellPotentialDanger, growthConditionStealth});
                    break;
                case NOTICEABLE:
                    finRes.cellHospitality += 10; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellHospitality to {2} (+10). {3}", new Object[]{animal, cell, finRes.cellHospitality, growthConditionStealth});
                    break;
                case NEGLIGENT:
                    finRes.cellHospitality -= 5; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellHospitality to {2} (-5). {3}", new Object[]{animal, cell, finRes.cellHospitality, growthConditionStealth});
                    break;
                default:
                    finRes.cellHospitality -= 10; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellHospitality to {2} (-10) (Default Growth). {3}", new Object[]{animal, cell, finRes.cellHospitality, growthConditionStealth});
                    break;
            }
        } else { //non-stealth
            switch (cell.getGrowth()) {
                case RAMPANT:
                    finRes.cellPotentialDanger += 20; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellPotentialDanger to {2} (+20). {3}", new Object[]{animal, cell, finRes.cellPotentialDanger, growthConditionStealth});
                    break;
                case SEVERE:
                    finRes.cellPotentialDanger += 15; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellPotentialDanger to {2} (+15). {3}", new Object[]{animal, cell, finRes.cellPotentialDanger, growthConditionStealth});
                    break;
                case NOTICEABLE:
                    finRes.cellPotentialDanger += 10; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellPotentialDanger to {2} (+10). {3}", new Object[]{animal, cell, finRes.cellPotentialDanger, growthConditionStealth});
                    break;
                case NEGLIGENT:
                    finRes.cellPotentialDanger -= 5; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellPotentialDanger to {2} (-5). {3}", new Object[]{animal, cell, finRes.cellPotentialDanger, growthConditionStealth});
                    break;
                default:
                    finRes.cellPotentialDanger -= 10; logger.log(Level.FINEST, "[A:{0}, C:{1}] cellPotentialDanger to {2} (-10) (Default Growth). {3}", new Object[]{animal, cell, finRes.cellPotentialDanger, growthConditionStealth});
                    break;
            }
        }

        // Herby Food Availability
        String growthConditionHerby = "Growth: " + cell.getGrowth() + " with Analyser HERBY: " + tags.contains(Tags.HERBY);
        if (tags.contains(Tags.HERBY)) {
            switch (cell.getGrowth()) {
                case RAMPANT: finRes.cellFoodAvailability += 20; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+20). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
                case SEVERE: finRes.cellFoodAvailability += 15; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+15). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
                case NOTICEABLE: finRes.cellFoodAvailability += 10; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+10). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
                case NEGLIGENT: finRes.cellFoodAvailability += 5; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+5). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
                default: finRes.cellFoodAvailability -= 10; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (-10) (Default Growth). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
            }
        } else { // plants and non-herby
            switch (cell.getGrowth()) {
                case RAMPANT: finRes.cellFoodAvailability += 10; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+10). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
                case SEVERE: finRes.cellFoodAvailability += 7; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+7). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
                case NOTICEABLE: finRes.cellFoodAvailability += 5; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+5). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
                case NEGLIGENT: finRes.cellFoodAvailability += 2; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+2). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
                default: finRes.cellFoodAvailability -= 10; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (-10) (Default Growth). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, growthConditionHerby}); break;
            }
        }

        // Corpse stuff
        long corpseCapacity = cell.getCurrCorpseCapacity();
        String corpseConditionBase = "Corpse Capacity: " + corpseCapacity;
        if (tags.contains(Tags.SCAVENGER)) {
            String corpseReason = corpseConditionBase + " with Analyser SCAVENGER";
            if (corpseCapacity > 80) { finRes.cellFoodAvailability += 20; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+20). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 60) { finRes.cellFoodAvailability += 15; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+15). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 40) { finRes.cellFoodAvailability += 10; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+10). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 20) { finRes.cellFoodAvailability += 5; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+5). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 1) { finRes.cellFoodAvailability += 2; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+2). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
        } else if (tags.contains(Tags.CARNY)) {
            String corpseReason = corpseConditionBase + " with Analyser CARNY";
            if (corpseCapacity > 80) { finRes.cellFoodAvailability += 15; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+15). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 60) { finRes.cellFoodAvailability += 10; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+10). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 40) { finRes.cellFoodAvailability += 7; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+7). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 20) { finRes.cellFoodAvailability += 5; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+5). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 1) { finRes.cellFoodAvailability += 2; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+2). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
        } else { // Neither SCAVENGER nor CARNY
            String corpseReason = corpseConditionBase + " (Analyser is not Scavenger/Carny)";
            if (corpseCapacity > 80) { finRes.cellFoodAvailability += 10; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+10). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 60) { finRes.cellFoodAvailability += 7; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+7). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 40) { finRes.cellFoodAvailability += 5; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+5). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 20) { finRes.cellFoodAvailability += 2; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+2). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
            else if (corpseCapacity > 1) { finRes.cellFoodAvailability += 1; logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+1). {3}", new Object[]{animal,cell,finRes.cellFoodAvailability, corpseReason}); }
        }

        // Other animal stuff - interactions with classified weak/dangerous animals
        // These loops iterate over the *finalized* weakAnimals and dangerousAnimals lists.
        logger.log(Level.FINEST, "[A:{0},C:{1}] Evaluating interactions with {2} weak and {3} dangerous animals.", new Object[]{animal, cell, finRes.weakAnimals.size(), finRes.dangerousAnimals.size()});
        String analyserFightCarny = "Analyser FIGHT: " + tags.contains(Tags.FIGHT) + ", CARNY: " + tags.contains(Tags.CARNY);
        if (tags.contains(Tags.FIGHT) && tags.contains(Tags.CARNY)) {
            for (Animal potentialPrey : finRes.weakAnimals) {
                float foodChange = (potentialPrey.getHealthPoints() / 2f + potentialPrey.getStoredEnergyPoints()); // Ensure float division
                finRes.cellFoodAvailability += foodChange;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+{3}) from weak prey {4}. ({5})", new Object[]{animal,cell,finRes.cellFoodAvailability, foodChange, potentialPrey, analyserFightCarny});
                float dangerChange = potentialPrey.getBasicCR();
                finRes.cellPotentialDanger += dangerChange;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellPotentialDanger to {2} (+{3}) from weak prey {4}. ({5})", new Object[]{animal,cell,finRes.cellPotentialDanger, dangerChange, potentialPrey, analyserFightCarny});
            }
            for (Animal danger : finRes.dangerousAnimals) {
                float foodChange = (danger.getHealthPoints() / 10f + danger.getStoredEnergyPoints());
                finRes.cellFoodAvailability += foodChange;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+{3}) from dangerous animal {4}. ({5})", new Object[]{animal,cell,finRes.cellFoodAvailability, foodChange, danger, analyserFightCarny});
                double cellDangerChange = danger.getBasicCR() * 1.5;
                finRes.cellDanger += cellDangerChange;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellDanger to {2} (+{3}) from dangerous animal {4}. ({5})", new Object[]{animal,cell,finRes.cellDanger, cellDangerChange, danger, analyserFightCarny});
            }
        } else { // Not (FIGHT and CARNY)
            for (Animal potentialPrey : finRes.weakAnimals) {
                float foodChange = (potentialPrey.getHealthPoints() / 7f + potentialPrey.getStoredEnergyPoints());
                finRes.cellFoodAvailability += foodChange;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+{3}) from weak prey {4}. ({5})", new Object[]{animal,cell,finRes.cellFoodAvailability, foodChange, potentialPrey, analyserFightCarny});
                float dangerChange = potentialPrey.getBasicCR();
                finRes.cellPotentialDanger += dangerChange;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellPotentialDanger to {2} (+{3}) from weak prey {4}. ({5})", new Object[]{animal,cell,finRes.cellPotentialDanger, dangerChange, potentialPrey, analyserFightCarny});
            }
            for (Animal danger : finRes.dangerousAnimals) {
                float foodChange = (danger.getHealthPoints() / 15f + danger.getStoredEnergyPoints());
                finRes.cellFoodAvailability += foodChange;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellFoodAvailability to {2} (+{3}) from dangerous animal {4}. ({5})", new Object[]{animal,cell,finRes.cellFoodAvailability, foodChange, danger, analyserFightCarny});
                double cellDangerChange = danger.getBasicCR() * 2.0;
                finRes.cellDanger += cellDangerChange;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellDanger to {2} (+{3}) from dangerous animal {4}. ({5})", new Object[]{animal,cell,finRes.cellDanger, cellDangerChange, danger, analyserFightCarny});
            }
        }

        // Misc
        logger.log(Level.FINEST, "[A:{0},C:{1}] Applying miscellaneous modifiers.", new Object[]{animal, cell});
        if (tags.contains(Tags.CARING)) {
            if (animal.getLastKnownProgenyLocation() != null && animal.getLastKnownProgenyLocation().equals(cell)) { // Added null check
                finRes.cellImportance += 20;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellImportance to {2} (+20). (CARING tag, progeny in cell)", new Object[]{animal,cell,finRes.cellImportance});
            }
        }
        if (tags.contains(Tags.SOCIAL)) { // Analyser is social
            HashSet<Animal> analyserGroup = animal.getGroup();
            if (analyserGroup != null) {
                int sensedOwnPackMatesCount = 0;
                for (Animal sensedAnimal : finRes.sensedAnimals) { // Iterate sensed animals
                    if (analyserGroup.contains(sensedAnimal) && !sensedAnimal.equals(animal)) { // If sensed is in own group (and not self)
                        sensedOwnPackMatesCount++;
                        finRes.cellHospitality += 20; // Add 20 for each such packmate
                        logger.log(Level.FINEST, "[A:{0},C:{1}] cellHospitality to {2} (+20) due to sensed packmate {3}. (Analyser SOCIAL)", new Object[]{animal,cell,finRes.cellHospitality, sensedAnimal});
                    }
                }
                if (sensedOwnPackMatesCount == 0) {
                    logger.log(Level.FINEST, "[A:{0},C:{1}] Analyser is SOCIAL, but no *other* packmates sensed. No specific hospitality bonus from this rule.", new Object[]{animal,cell});
                }
            }
        }
        if (tags.contains(Tags.TERRITORIAL)) {
            if (animal.getTerritories() != null && animal.getTerritories().contains(cell)) { // Added null check
                finRes.cellImportance += 10;
                logger.log(Level.FINEST, "[A:{0},C:{1}] cellImportance to {2} (+10). (TERRITORIAL tag, cell in territories)", new Object[]{animal,cell,finRes.cellImportance});
            }
        }

        logger.log(Level.FINEST, "[Analyser: {0}, Cell: {1}] Finished initialAnalysis. Final finRes: {2}", new Object[]{animal, cell, finRes});
        return finRes;
    }
}