package Organisms.Animals;

import Map.MapStructure;
import Organisms.Organism;

import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Map.MapGeneration.getNeighbours;
import static util.ImportantMethods.d4Straight;
import static util.ImportantMethods.d8Straight;

public class Movement {
    private static final Logger logger = Logger.getLogger(Movement.class.getName());
    public static MapStructure.Cell whereToMoveTest(Animal animal){
        MapStructure.Cell chosenCell;
        List<MapStructure.Cell> cells = getNeighbours(animal.getCell());
        if(cells.size()==8) {
            chosenCell= cells.get(d8Straight());
        }
        else chosenCell= cells.get(0);
        logger.log(Level.FINER, "Animal [{0}] successfully chose a Cell [{1},{2}]",
                new Object[]{animal.getId(), chosenCell.getX(), chosenCell.getY()});
        return chosenCell;
    }
    public static class MoveRequest{
        private Animal animal;
        private MapStructure.Cell cell;

        public MoveRequest(Animal animal, MapStructure.Cell cell){
            this.animal=animal;
            this.cell=cell;
        }

        public Animal getAnimal() {
            return animal;
        }

        public MapStructure.Cell getCell() {
            return cell;
        }
    }


}
