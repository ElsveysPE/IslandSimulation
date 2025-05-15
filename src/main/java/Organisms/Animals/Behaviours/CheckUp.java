package Organisms.Animals.Behaviours;

import Organisms.Animals.Animal;
import Organisms.Animals.Conditions.Condition;
import Organisms.Animals.LifeStage;
import Organisms.Animals.Tags;
import Organisms.HealthStatus;
import util.DayTime;
import util.ImportantMethods;
import util.SimManager;

public class CheckUp {

    void checkHealth(Animal animal){
        double maxHealth = animal.getMaxHealth();
        double health = animal.getHealthPoints();

        if (health <= maxHealth * 0.2) animal.setHealthStatus(HealthStatus.GRAVELY_INJURED);
        else if (health <= maxHealth * 0.4) animal.setHealthStatus(HealthStatus.SEVERELY_INJURED);
        else if (health <= maxHealth * 0.6) animal.setHealthStatus(HealthStatus.INJURED);
        else if (health <= maxHealth * 0.8) animal.setHealthStatus(HealthStatus.SLIGHTLY_INJURED);
        else animal.setHealthStatus(HealthStatus.HEALTHY);
    }
    void checkHunger(Animal animal) {
        //clean-up
        animal.conditions.remove(Condition.HUNGRY);
        animal.conditions.remove(Condition.STARVING);
        animal.conditions.remove(Condition.SUPER_STARVING);

        if (animal.getMaxHealth() * 0.33 >= animal.getStoredEnergyPoints())
            animal.conditions.add(Condition.HUNGRY);
        else if (animal.getMaxHealth() * 0.15 >= animal.getStoredEnergyPoints())
            animal.conditions.add(Condition.STARVING);
        else if (animal.getMaxHealth() * 0.10 >= animal.getStoredEnergyPoints() && ImportantMethods.d4Straight() >= 3)
            animal.conditions.add(Condition.SUPER_STARVING);
    }
    void checkAge(Animal animal){
        int maxAge = animal.getMaxAge();
        int currAge = animal.getCurrAge();

        if (currAge < maxAge * 0.05) animal.setLifeStage(LifeStage.JUVENILE);
        else if (currAge < maxAge * 0.15) {
            animal.setLifeStage(LifeStage.ADOLESCENT);
            animal.setReadyForReproduction(true);
        }
        else if (currAge < maxAge * 0.25) animal.setLifeStage(LifeStage.ADULT);
        else if (currAge < maxAge * 0.75) {
            animal.setLifeStage(LifeStage.OLD);
            animal.setReadyForReproduction(false);
        }
        else animal.setLifeStage(LifeStage.ELDER);
    }
    void illnessProgression(Animal animal){
        int rollResult = ImportantMethods.d100roll(animal.getConstitution());
        if(animal.conditions.contains(Condition.POISONED)) rollResult-=17;
        if(animal.conditions.contains(Condition.HUNGRY)) rollResult-=5;
        if(animal.conditions.contains(Condition.STARVING)) rollResult-=15;
        if(animal.conditions.contains(Condition.FATIGUED)) rollResult-=5;
        if(animal.conditions.contains(Condition.EXHAUSTED)) rollResult-=15;
        if(animal.conditions.contains(Condition.BLEEDING)) rollResult-=7;
        if(animal.conditions.contains(Condition.SEVERELY_BLEEDING)) rollResult-=20;
        if(animal.conditions.contains(Condition.SUPER_STARVING)) rollResult+=10;
        if(animal.conditions.contains(Condition.RESTING)) rollResult+=7;
        if(animal.conditions.contains(Condition.DEEP_RESTING)) rollResult+=12;
        if (animal.conditions.contains(Condition.MILDLY_ILL)){
            animal.setHealthPoints((int) (animal.getHealthPoints() * 0.95));
            if (rollResult < 15) {
                animal.conditions.remove(Condition.MILDLY_ILL);
                animal.conditions.add(Condition.SEVERELY_ILL);
            }
            if (rollResult > 75) {
                animal.conditions.remove(Condition.MILDLY_ILL);
            }
        }
        else if (animal.conditions.contains(Condition.SEVERELY_ILL)){
            animal.setHealthPoints((int) (animal.getHealthPoints() * 0.75));
            if (rollResult > 90) {
                animal.conditions.remove(Condition.SEVERELY_ILL);
                animal.conditions.add(Condition.MILDLY_ILL);
            }
        }
        else {
            if(rollResult<3) animal.conditions.add(Condition.MILDLY_ILL);
        }

    }
    void bleedingProgression(Animal animal){
        if(animal.conditions.contains(Condition.BLEEDING)) {
            animal.setHealthPoints((int) (animal.getHealthPoints() * 0.9));
            int rollResult = ImportantMethods.d100roll(animal.getConstitution());
            if (rollResult < 25) {
                animal.conditions.remove(Condition.BLEEDING);
                animal.conditions.add(Condition.SEVERELY_BLEEDING);
            }
            if (rollResult > 75) {
                animal.conditions.remove(Condition.BLEEDING);
            }
        }
        else if(animal.conditions.contains(Condition.SEVERELY_BLEEDING)) {
            animal.setHealthPoints((int) (animal.getHealthPoints() * 0.75));
            int rollResult = ImportantMethods.d100roll(animal.getConstitution());
            if (rollResult > 90) {
                animal.conditions.remove(Condition.SEVERELY_BLEEDING);
                animal.conditions.add(Condition.BLEEDING);
            }
        }
    }
    void fatigueMechanics(Animal animal) {
        if (animal.conditions.contains(Condition.EXHAUSTED)){
            if (animal.getTags().contains(Tags.MATUTINAL) && SimManager.getDayTime().equals(DayTime.NIGHT))
                animal.setHealthPoints((int) (animal.getHealthPoints()*0.9));
            else if (animal.getTags().contains(Tags.DIURNAL) && SimManager.getDayTime().equals(DayTime.NIGHT))
                animal.setHealthPoints((int) (animal.getHealthPoints()*0.9));
            else if (animal.getTags().contains(Tags.VESPERTINE) && SimManager.getDayTime().equals(DayTime.MORNING))
                animal.setHealthPoints((int) (animal.getHealthPoints()*0.9));
            else if (animal.getTags().contains(Tags.NOCTURNAL) && SimManager.getDayTime().equals(DayTime.MORNING))
                animal.setHealthPoints((int) (animal.getHealthPoints()*0.9));
        }
        else if (animal.conditions.contains(Condition.FATIGUED)){
            if (animal.getTags().contains(Tags.MATUTINAL) && SimManager.getDayTime().equals(DayTime.NIGHT)) {
                animal.conditions.remove(Condition.FATIGUED);
                animal.conditions.add(Condition.EXHAUSTED);
            }
            else if (animal.getTags().contains(Tags.DIURNAL) && SimManager.getDayTime().equals(DayTime.NIGHT)) {
                animal.conditions.remove(Condition.FATIGUED);
                animal.conditions.add(Condition.EXHAUSTED);
            }
            else if (animal.getTags().contains(Tags.VESPERTINE) && SimManager.getDayTime().equals(DayTime.MORNING)) {
                animal.conditions.remove(Condition.FATIGUED);
                animal.conditions.add(Condition.EXHAUSTED);
            }
            else if (animal.getTags().contains(Tags.NOCTURNAL) && SimManager.getDayTime().equals(DayTime.MORNING)) {
                animal.conditions.remove(Condition.FATIGUED);
                animal.conditions.add(Condition.EXHAUSTED);
            }
        }
        else  {
            if (animal.getTags().contains(Tags.MATUTINAL) && SimManager.getDayTime().equals(DayTime.NIGHT))
                animal.conditions.add(Condition.FATIGUED);
            else if (animal.getTags().contains(Tags.DIURNAL) && SimManager.getDayTime().equals(DayTime.NIGHT))
                animal.conditions.add(Condition.FATIGUED);
            else if (animal.getTags().contains(Tags.VESPERTINE) && SimManager.getDayTime().equals(DayTime.MORNING))
                animal.conditions.add(Condition.FATIGUED);
            else if (animal.getTags().contains(Tags.NOCTURNAL) && SimManager.getDayTime().equals(DayTime.MORNING))
                animal.conditions.add(Condition.FATIGUED);
        }
    }
    //eh, not sure about that one below
    void emotionalRegulation(Animal animal){

    }
}
