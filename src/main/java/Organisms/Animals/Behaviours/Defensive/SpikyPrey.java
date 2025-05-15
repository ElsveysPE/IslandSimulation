package Organisms.Animals.Behaviours.Defensive;

import Organisms.Animals.Animal;

public interface SpikyPrey {
    public float reverseDamage=0.2f;
    default void spikesUp(Animal animal){
        animal.reverseDamage=0.5f;
        animal.battleActionPoints--;
    }
}
