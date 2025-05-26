package Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic;

import Map.MapStructure;
import Organisms.Animals.Animal;

public class CellFavor {
    private MapStructure.Cell cell;
    private int favor;
    public static CellFavor formCellFavor(MapStructure.Cell cell) {
        CellFavor cellFA = new CellFavor();
        cellFA.cell= cell;
        cellFA.favor= 0;
        return cellFA;
    }

    public int getFavor() {
        return favor;
    }

    public void setFavor(int favourability) {
        this.favor = favourability;
    }

    public MapStructure.Cell getCell() {
        return cell;
    }
}
