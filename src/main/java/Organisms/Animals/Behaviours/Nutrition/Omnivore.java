package Organisms.Animals.Behaviours.Nutrition;

import Organisms.Animals.Animal;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Plants.Plant;

public interface Omnivore {
    default void eatAlive(Animal animal, Animal prey){
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints()+ (float) prey.getHealthPoints()*0.75f+ prey.getStoredEnergyPoints());
        prey.setHealthPoints(-1);
    }
    default void eatDead(Animal animal, Corpse corpse){
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints((float) (animal.getStoredEnergyPoints()+ (float) corpse.getEnergyPoints()*0.8));
        corpse.getCell().removeCorpse(corpse);
    }
    default void eatPlant(Animal animal, Plant plant){
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints()+ plant.getCurrEnergyPoints()*0.75f);
        plant.getCell().removePlant(plant);
    }
}
