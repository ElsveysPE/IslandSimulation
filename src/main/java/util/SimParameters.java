package util;

import  Map.MapStructure.Cell;
import Map.Terrain;
public class SimParameters {
    private static final Cell[][] MapSize = new Cell[25][25];

    private static final Cell startingCell = new Cell(0, 0, Terrain.PLAIN);

    public static Cell getStartingCell() {
        return startingCell;
    }

    public static Cell[][] getMapSize() {
        return MapSize;
    }
    public static int getMapLength() {
        return MapSize.length;
    }
    public  static int getMapHeight(){
        return MapSize[0].length;
    }
    }
