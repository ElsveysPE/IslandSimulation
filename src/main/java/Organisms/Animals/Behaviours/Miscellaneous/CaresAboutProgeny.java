package Organisms.Animals.Behaviours.Miscellaneous;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;

import java.util.HashSet;

public interface CaresAboutProgeny {
    public MapStructure.Cell lastKnownProgenyLocation = new MapStructure.Cell(-1, -1, Terrain.PLACEHOLDER);
    public HashSet<Animal> children = new HashSet<>();

}
