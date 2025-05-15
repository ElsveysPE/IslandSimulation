package Organisms.Animals.Behaviours.Miscellaneous;

import Map.MapStructure;

import java.util.ArrayList;
import java.util.List;

public interface Territorial {
    public List<MapStructure.Cell> territories = new ArrayList<>();
    private void expandTerritory(MapStructure.Cell cell){
        territories.add(cell);
    }
    private void shrinkTerritory(MapStructure.Cell cell){
        territories.remove(cell);
    }
}
