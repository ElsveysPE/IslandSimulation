package Organisms.Animals.Management;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Conditions.Conditions;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Corpses.CorpsyStuff;
import Organisms.Animals.LifeStage;
import Organisms.Animals.Tags;
import Organisms.HealthStatus;
import util.DayTime;
import util.ImportantMethods;
import util.SimManager;
import util.SimParameters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Map.MapGeneration.*;
import static Organisms.Animals.Animal.*;
import static Organisms.Animals.Conditions.ConditionEffects.*;
import static Organisms.Animals.Corpses.CorpsyStuff.decay;


public class CheckUp {

    private static final Logger logger = Logger.getLogger(CheckUp.class.getName());

    private static void checkHealth(Animal animal) {
        if (animal == null) {
            logger.log(Level.WARNING, "checkHealth called with null animal.");
            return;
        }
        HealthStatus previousStatus = animal.getHealthStatus();
        double maxHealth = animal.getMaxHealth();
        double health = animal.getHealthPoints();
        HealthStatus newStatus;

        if (health <= 0) {
            newStatus = HealthStatus.GRAVELY_INJURED;
        } else if (health <= maxHealth * 0.2) newStatus = HealthStatus.GRAVELY_INJURED;
        else if (health <= maxHealth * 0.4) newStatus = HealthStatus.SEVERELY_INJURED;
        else if (health <= maxHealth * 0.6) newStatus = HealthStatus.INJURED;
        else if (health <= maxHealth * 0.8) newStatus = HealthStatus.SLIGHTLY_INJURED;
        else newStatus = HealthStatus.HEALTHY;

        if (previousStatus != newStatus) {
            animal.setHealthStatus(newStatus);
            logger.log(Level.FINE, "Animal {0} ({1}) health status changed from {2} to {3}. HP: {4}/{5}",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), previousStatus, newStatus, animal.getHealthPoints(), animal.getMaxHealth()});
        }
    }

    private static void checkHunger(Animal animal) {
        if (animal == null) return;

        boolean wasHungry = animal.conditions.contains(Conditions.HUNGRY);
        boolean wasStarving = animal.conditions.contains(Conditions.STARVING);
        boolean wasSuperStarving = animal.conditions.contains(Conditions.SUPER_STARVING);

        hungerEnd(animal);
        starvationEnd(animal);
        superStarvationEnd(animal);

        double energyPoints = animal.getStoredEnergyPoints();
        double maxHealthForEnergyCalc = animal.getMaxHealth();

        // User's specified order:
        if (maxHealthForEnergyCalc * 0.01 >= energyPoints && ImportantMethods.d4Straight() >= 3) {
            superStarvationStart(animal);
            if (!wasSuperStarving) {
                logger.log(Level.WARNING, "Animal {0} ({1}) is now SUPER_STARVING. Energy: {2}", new Object[]{animal.getId(), animal.getClass().getSimpleName(), energyPoints});
            }
        } else if (maxHealthForEnergyCalc * 0.1 >= energyPoints) {
            starvationStart(animal);
            if (!wasStarving) {
                logger.log(Level.WARNING, "Animal {0} ({1}) is now STARVING. Energy: {2}", new Object[]{animal.getId(), animal.getClass().getSimpleName(), energyPoints});
            }
        } else if (maxHealthForEnergyCalc * 0.2 >= energyPoints) {
            hungerStart(animal);
            if (!wasHungry) {
                logger.log(Level.INFO, "Animal {0} ({1}) is now HUNGRY. Energy: {2}", new Object[]{animal.getId(), animal.getClass().getSimpleName(), energyPoints});
            }
        } else {
            if (wasHungry || wasStarving || wasSuperStarving) {
                logger.log(Level.INFO, "Animal {0} ({1}) is no longer in a primary hunger state. Energy: {2}", new Object[]{animal.getId(), animal.getClass().getSimpleName(), energyPoints});
            }
        }
    }

    private static void checkAge(Animal animal) {
        if (animal == null) return;
        int maxAge = animal.getMaxAge();
        int currAge = animal.getCurrAge();
        LifeStage previousLifeStage = animal.getLifeStage();

        if (currAge >= maxAge * 0.75) {
                animal.setLifeStage(LifeStage.OLD);
                animal.setReadyForReproduction(false);
                logger.log(Level.FINE, "Animal {0} ({1}) became OLD at age {2}/{3}. No longer reproductive.",
                        new Object[]{animal.getId(), animal.getClass().getSimpleName(), currAge, maxAge});
        } else if (currAge >= maxAge * 0.1) {
            animal.setLifeStage(LifeStage.ADULT);
            animal.setReadyForReproduction(true);
            logger.log(Level.FINE, "Animal {0} ({1}) became ADULT at age {2}/{3}. Now reproductive.",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), currAge, maxAge});
        }
        else logger.log(Level.WARNING, "Animal {0} ({1}) became ADULT at age {2}/{3}. Is neither adult or old. Was not supposed to happen",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), currAge, maxAge});

        }

    private static void illnessProgression(Animal animal) {
        if (animal == null) return;
        int rollResult = ImportantMethods.d100roll(animal.getConstitution());

        if(animal.conditions.contains(Conditions.POISONED)) rollResult-=17;
        if(animal.conditions.contains(Conditions.HUNGRY)) rollResult-=5;
        if(animal.conditions.contains(Conditions.STARVING)) rollResult-=15;
        if(animal.conditions.contains(Conditions.FATIGUED)) rollResult-=5;
        if(animal.conditions.contains(Conditions.EXHAUSTED)) rollResult-=15;
        if(animal.conditions.contains(Conditions.BLEEDING)) rollResult-=7;
        if(animal.conditions.contains(Conditions.SEVERELY_BLEEDING)) rollResult-=20;
        if(animal.conditions.contains(Conditions.SUPER_STARVING)) rollResult+=10;
        if(animal.conditions.contains(Conditions.RESTING)) rollResult+=7;
        if(animal.conditions.contains(Conditions.DEEP_RESTING)) rollResult+=12;

        int oldHp = animal.getHealthPoints();

        if (animal.conditions.contains(Conditions.MILDLY_ILL)) {
            animal.setHealthPoints((int) (oldHp * 0.95));
            logger.log(Level.FINE, "Animal {0} ({1}) MILDLY_ILL: HP reduced from {2} to {3}. Constitution roll (modified): {4}",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), oldHp, animal.getHealthPoints(), rollResult});
            if (rollResult < 15) {
               mildIllnessEnd(animal);
               severeIllnessStart(animal);
                logger.log(Level.WARNING, "Animal {0} ({1}) illness worsened: MILDLY_ILL -> SEVERELY_ILL.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            } else if (rollResult > 75) {
                animal.conditions.remove(Conditions.MILDLY_ILL);
                logger.log(Level.INFO, "Animal {0} ({1}) recovered from MILDLY_ILL.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            }
        } else if (animal.conditions.contains(Conditions.SEVERELY_ILL)) {
            animal.setHealthPoints((int) (oldHp * 0.75));
            logger.log(Level.WARNING, "Animal {0} ({1}) SEVERELY_ILL: HP reduced from {2} to {3}. Constitution roll (modified): {4}",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), oldHp, animal.getHealthPoints(), rollResult});
            if (rollResult > 90) {
                severeIllnessEnd(animal);
                mildIllnessStart(animal);
                logger.log(Level.INFO, "Animal {0} ({1}) illness improved: SEVERELY_ILL -> MILDLY_ILL.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            }
        } else {
            if (rollResult < 3) {
                mildIllnessStart(animal);
                logger.log(Level.WARNING, "Animal {0} ({1}) became MILDLY_ILL. Constitution roll (modified): {2}",
                        new Object[]{animal.getId(), animal.getClass().getSimpleName(), rollResult});
            }
        }
    }

    private static void bleedingProgression(Animal animal) {
        if (animal == null) return;
        int oldHp = animal.getHealthPoints();

        if (animal.conditions.contains(Conditions.BLEEDING)) {
            animal.setHealthPoints((int) (oldHp * 0.9));
            int rollResult = ImportantMethods.d100roll(animal.getConstitution());
            logger.log(Level.FINE, "Animal {0} ({1}) BLEEDING: HP reduced from {2} to {3}. Constitution roll: {4}",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), oldHp, animal.getHealthPoints(), rollResult});
            if (rollResult < 25) {
                bleedingEnd(animal);
                severelyBleedingStart(animal);
                logger.log(Level.FINE, "Animal {0} ({1}) bleeding worsened: BLEEDING -> SEVERELY_BLEEDING.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            } else if (rollResult > 75) {
                bleedingEnd(animal);
                logger.log(Level.FINE, "Animal {0} ({1}) stopped BLEEDING.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            }
        } else if (animal.conditions.contains(Conditions.SEVERELY_BLEEDING)) {
            animal.setHealthPoints((int) (oldHp * 0.75));
            int rollResult = ImportantMethods.d100roll(animal.getConstitution());
            logger.log(Level.FINE, "Animal {0} ({1}) SEVERELY_BLEEDING: HP reduced from {2} to {3}. Constitution roll: {4}",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), oldHp, animal.getHealthPoints(), rollResult});
            if (rollResult > 75) {
                severelyBleedingEnd(animal);
                bleedingStart(animal);
                logger.log(Level.FINE, "Animal {0} ({1}) bleeding improved: SEVERELY_BLEEDING -> BLEEDING.", new Object[]{animal.getId(), animal.getClass().getSimpleName()});
            }
        }
    }

    private static void fatigueMechanics(Animal animal) {
        if (animal == null || SimManager.getDayTime() == null) {
            return;
        }
        DayTime currentTime = SimManager.getDayTime();

        if (animal.conditions.contains(Conditions.EXHAUSTED)) {
            boolean takeDamage = false;
            if (animal.getTags().contains(Tags.MATUTINAL) && currentTime == DayTime.NIGHT) takeDamage = true;
            else if (animal.getTags().contains(Tags.DIURNAL) && currentTime == DayTime.NIGHT) takeDamage = true;
            else if (animal.getTags().contains(Tags.VESPERTINE) && currentTime == DayTime.MORNING) takeDamage = true;
            else if (animal.getTags().contains(Tags.NOCTURNAL) && currentTime == DayTime.MORNING) takeDamage = true;

            if (takeDamage) {
                int oldHp = animal.getHealthPoints();
                animal.setHealthPoints((int) (oldHp * 0.9));
                logger.log(Level.FINE, "Animal {0} ({1}) EXHAUSTED and active during unfavored time ({2}). HP reduced from {3} to {4}.",
                        new Object[]{animal.getId(), animal.getClass().getSimpleName(), currentTime, oldHp, animal.getHealthPoints()});
            }
        } else if (animal.conditions.contains(Conditions.FATIGUED)) {
            boolean worsen = false;
            if (animal.getTags().contains(Tags.MATUTINAL) && currentTime == DayTime.NIGHT) worsen = true;
            else if (animal.getTags().contains(Tags.DIURNAL) && currentTime == DayTime.NIGHT) worsen = true;
            else if (animal.getTags().contains(Tags.VESPERTINE) && currentTime == DayTime.MORNING) worsen = true;
            else if (animal.getTags().contains(Tags.NOCTURNAL) && currentTime == DayTime.MORNING) worsen = true;

            if (worsen) {
                fatiguedEnd(animal);
                exhaustedStart(animal);
                logger.log(Level.FINE, "Animal {0} ({1}) FATIGUED and active during unfavored time ({2}). Became EXHAUSTED.",
                        new Object[]{animal.getId(), animal.getClass().getSimpleName(), currentTime});
            }
        } else {
            boolean becomeFatigued = false;
            if (animal.getTags().contains(Tags.MATUTINAL) && currentTime == DayTime.NIGHT) becomeFatigued = true;
            else if (animal.getTags().contains(Tags.DIURNAL) && currentTime == DayTime.NIGHT) becomeFatigued = true;
            else if (animal.getTags().contains(Tags.VESPERTINE) && currentTime == DayTime.MORNING) becomeFatigued = true;
            else if (animal.getTags().contains(Tags.NOCTURNAL) && currentTime == DayTime.MORNING) becomeFatigued = true;

            if (becomeFatigued) {
                fatiguedStart(animal);
                logger.log(Level.FINE, "Animal {0} ({1}) became FATIGUED due to activity during unfavored time ({2}).",
                        new Object[]{animal.getId(), animal.getClass().getSimpleName(), currentTime});
            }
        }
    }

    private static void energyExpenditure(Animal animal) {
        if (animal == null) return;
        int expense = animal.getSize();
        float currEn = animal.getStoredEnergyPoints();
        int fat = animal.getFatStorage();
        int health = animal.getHealthPoints();
        String expenditureSource = "stored energy";
        float initialEnergy = currEn;
        int initialFat = fat;
        int initialHealth = health;

        if (expense <= currEn) {
            animal.setStoredEnergyPoints(currEn - expense);
        } else {
            float energyNeededFromFat = expense - currEn;
            animal.setStoredEnergyPoints(0);
            expenditureSource = "fat reserves";
            if (energyNeededFromFat <= fat) {
                animal.setFatStorage(fat - (int)energyNeededFromFat);
            } else {
                float energyNeededFromHealth = energyNeededFromFat - fat;
                animal.setFatStorage(0);
                expenditureSource = "health points (critical!)";
                animal.setHealthPoints(health - (int)energyNeededFromHealth);
                logger.log(Level.SEVERE, "Animal {0} ({1}) is consuming HEALTH for energy! HP reduced by {2}. Initial HP: {3}, New HP: {4}",
                        new Object[]{animal.getId(), animal.getClass().getSimpleName(), (int)energyNeededFromHealth, initialHealth, animal.getHealthPoints()});
            }
        }
        logger.log(Level.FINEST, "Animal {0} ({1}) energy expenditure. Cost: {2}. Source: {3}. Energy: {4}->{5}. Fat: {6}->{7}. HP: {8}->{9}.",
                new Object[]{animal.getId(), animal.getClass().getSimpleName(), expense, expenditureSource,
                        initialEnergy, animal.getStoredEnergyPoints(),
                        initialFat, animal.getFatStorage(),
                        initialHealth, animal.getHealthPoints()});
    }

    public static void massDecay() {
        logger.log(Level.FINE, "Performing global mass decay for all corpses on the map.");
        HashSet<MapStructure.Cell> allCells = getMapInCells();
        if (allCells.isEmpty()) {
            logger.log(Level.WARNING, "globalMassDecay: No cells to process for decay (getMapInCells returned empty or map not available).");
            return;
        }

        int corpsesProcessed = 0;
        for (MapStructure.Cell cell : allCells) {
            if (cell.getCorpses() != null && !cell.getCorpses().isEmpty()) {
                List<Corpse> corpsesToDecay = new ArrayList<>(cell.getCorpses());
                for (Corpse corpse : corpsesToDecay) {
                    if (corpse != null) {
                        decay(corpse);
                        corpsesProcessed++;
                    }
                }
            }
        }
        logger.log(Level.FINE, "Global mass decay processed {0} corpses across all cells.", corpsesProcessed);
    }

    private static void death(Animal animal) {
        if (animal == null) return;
        if (animal.getHealthPoints() <= 0 || animal.isDead()) {
            logger.log(Level.INFO, "Animal {0} ({1}) has died (HP: {2}). Generating corpse.",
                    new Object[]{animal.getId(), animal.getClass().getSimpleName(), animal.getHealthPoints()});
            CorpsyStuff.generateCorpse(animal);
        }
    }

    private static void emotionalRegulation(Animal animal){
    }

    public static void checkUp(Animal animal) {
        if (animal == null) {
            logger.log(Level.WARNING, "Checkup called on null animal.");
            return;
        }
        if (animal.isDead()) {
            return;
        }

        logger.log(Level.FINEST, "--- Starting checkup for Animal {0} ({1}) ---", new Object[]{animal.getId(), animal.getClass().getSimpleName()});

        int initialHp = animal.getHealthPoints();
        float initialEnergy = animal.getStoredEnergyPoints();
        animal.setCurrAge(animal.getCurrAge() + 1);
        logger.log(Level.FINEST, "Animal {0} age incremented to {1}.", new Object[]{animal.getId(), animal.getCurrAge()});
        checkAge(animal);
        checkHunger(animal);
        bleedingProgression(animal);
        illnessProgression(animal);
        checkHealth(animal);
        fatigueMechanics(animal);
        emotionalRegulation(animal);
        death(animal);
        animal.setMaxMovementPoints(Math.max(1, (animal.getSpeed()/2)));
        animal.setMaxHealth(Math.max(1, (animal.getConstitution() * 5)
                + (animal.getSize() * 10)));
        animal.setCurrentMovementPoints(animal.getMaxMovementPoints());

        logger.log(Level.FINEST, "--- Finished checkup for Animal {0} ({1}). HP: {2}->{3}, Energy: {4}->{5} ---",
                new Object[]{animal.getId(), animal.getClass().getSimpleName(), initialHp, animal.getHealthPoints(), initialEnergy, animal.getStoredEnergyPoints()});
    }
}
