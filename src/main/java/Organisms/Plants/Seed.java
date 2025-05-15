package Organisms.Plants;

import Map.MapStructure;
import Map.Terrain;

public class Seed {
    private Plant parentPlant;
    private int countEnergyPoints(){
        int randFactor= (int) (Math.random()*10);
        int countedEnergyPoints=parentPlant.getAverageSeedEnergyPoints()+(randFactor*util.ImportantMethods.plusOrMinus());
        return Math.max(countedEnergyPoints, 1);
    }
    private final int energyPoints= countEnergyPoints();
    public Plant getParentPlant() {
        return parentPlant;
    }

    private boolean doesItSprout(MapStructure.Cell cell){
        int d100 = (int) (Math.random() * 100);
        if (parentPlant.getPreferableTerrain().contains(cell.getTerrain())) {
            return (d100 * 1.25 + energyPoints*0.1)>cell.getCurrPlantCapacity() ;
        } else if (parentPlant.getTolerableTerrain().contains(cell.getTerrain())) {
            return (d100 * 0.75 + energyPoints*0.1)>cell.getCurrPlantCapacity() ;
        }
        else return false;
    }
}
