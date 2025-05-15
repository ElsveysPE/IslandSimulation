package Organisms.Animals.Corpses;

import Map.MapStructure;
import util.SimManager;

public class Corpse {
    float energyPoints;
    int size;
    DecayStage decayStage;
    DecaySpeed decaySpeed;
    MapStructure.Cell cell;
    private final int deathDate = SimManager.getTick(); //when was corpse created

    public int getSize() {
        return size;
    }

    public float getEnergyPoints() {
        return energyPoints;
    }

    public MapStructure.Cell getCell() {
        return cell;
    }

    public int getDeathDate() {
        return deathDate;
    }
}
