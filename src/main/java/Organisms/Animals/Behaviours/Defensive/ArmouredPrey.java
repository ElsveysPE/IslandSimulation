package Organisms.Animals.Behaviours.Defensive;

import Organisms.Animals.Animal;

public interface ArmouredPrey {
    default void ShieldUp(Animal animal){
        animal.setPhysRes(0.01f);
        animal.battleActionPoints--;
    }
}
