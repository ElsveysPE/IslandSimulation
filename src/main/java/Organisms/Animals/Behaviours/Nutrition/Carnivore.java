package Organisms.Animals.Behaviours.Nutrition;

import Organisms.Animals.Animal;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Plants.Plant;

public interface Carnivore {
    default void eatAlive(Animal animal, Animal prey){
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints()+ (float) prey.getHealthPoints()*0.75f+ prey.getStoredEnergyPoints());
        prey.setHealthPoints(-1);
    }
    default void eatDead(Animal animal, Corpse corpse){
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints()+ corpse.getEnergyPoints()*0.8f);
        corpse.getCell().removeCorpse(corpse);
    }
    default void eatPlant(Animal animal, Plant plant){
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints()+ plant.getCurrEnergyPoints()*0.5f);
        plant.getCell().removePlant(plant);
    }
}
