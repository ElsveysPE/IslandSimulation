package Organisms.Animals.Behaviours.Nutrition;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Corpses.Corpse;

public interface Herbivore {
    default void eatAlive(Animal animal, Animal prey){
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints()+ (float) prey.getMaxHealth()*0.33f+ prey.getStoredEnergyPoints()*0.6f);
        prey.setHealthPoints(-1);
    }
    default void eatDead(Animal animal, Corpse corpse){
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints()+ corpse.getEnergyPoints()*0.4f);
        corpse.getCell().removeCorpse(corpse);
    }
    default void eatPlant(Animal animal){
        MapStructure.Cell cell = animal.getCell();
        animal.actionPoints= animal.actionPoints-1;
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints()+ cell.getCurrPlantCapacity()*1.2f);
        cell.setCurrPlantCapacity(cell.getCurrPlantCapacity()-animal.getSize()/2);
    }
}
