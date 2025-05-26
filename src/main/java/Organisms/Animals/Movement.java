package Organisms.Animals;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic.CellAnalysed;
import Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic.CellFavor;
import Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic.cellInitialAnalysis;
import Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic.cellFinalAnalysis;

import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import static Map.MapGeneration.getNeighbours;

public class Movement {
    private static final Logger logger = Logger.getLogger(Movement.class.getName());
    public static MapStructure.Cell whereToMove(Animal animal){
        int chosenCellFavor = Integer.MIN_VALUE;
        MapStructure.Cell finalChoice = new MapStructure.Cell(-1, -1, Terrain.PLACEHOLDER);
        List<MapStructure.Cell> cells = getNeighbours(animal.getCell());
        cells.add(animal.getCell());
        cells.add(animal.getCell());
        HashSet<CellFavor> cellFavors = new HashSet<>();
        for(MapStructure.Cell i : cells){
            CellAnalysed cellAnalysed = cellInitialAnalysis.initialAnalysis(animal, i);
            CellFavor cellFavor = cellFinalAnalysis.finalAnalysis(animal, cellAnalysed);
            cellFavors.add(cellFavor);
        }
        for(CellFavor chosenCell: cellFavors){
            if(chosenCell.getFavor()>chosenCellFavor){
                finalChoice = chosenCell.getCell();
                chosenCellFavor = chosenCell.getFavor();
            }
        }
        return finalChoice;
    }
    public static class MoveRequest{
        private Animal animal;
        private MapStructure.Cell cell;

        public MoveRequest(Animal animal, MapStructure.Cell cell){
            this.animal=animal;
            this.cell=cell;
        }
        public static MoveRequest formMoveRequest(Animal animal, MapStructure.Cell cell){
            return new MoveRequest(animal, cell);
        }

        public Animal getAnimal() {
            return animal;
        }

        public MapStructure.Cell getCell() {
            return cell;
        }
    }


}
