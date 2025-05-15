package Organisms.Animals.Behaviours;

import Map.MapStructure;
import Organisms.Animals.Animal;

import java.util.ArrayList;
import java.util.List;

import static Map.MapGeneration.getAllNeighbours;


public interface SurroundingsAnalysis{
/*    default List<CellPostAnalysis> analysis(Animal animal){
        List<CellPostAnalysis> cellsPA = new ArrayList<>();
        int senses = animal.getCurrPerception()/25;
        List<MapStructure.Cell> cells = getAllNeighbours(animal.getCell(), senses);
        for(int i=0; i<cells.size(); i++){
            CellPostAnalysis cellPA = CellPostAnalysis.fromCell(cells.get(i));
            List<Animal> sensedAnimals = cells.get(i).getSensedAnimals(animal);
            if (cellPA.currPlantCapacity>=50) {
                cellPA.cellPotentialDanger++;
                cellPA.cellFoodAvailability=+5;
                cellPA.cellHospitality=+2;
            }
            if (cellPA.currPlantCapacity>=75) {
                cellPA.cellPotentialDanger=+4;
                cellPA.cellFoodAvailability=+10;
                cellPA.cellHospitality=+6;
            }
        }
        return null;
    }
*/

}
