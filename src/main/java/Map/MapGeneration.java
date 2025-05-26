package Map;


import util.ImportantMethods;
import util.SimParameters;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class MapGeneration {

    static private MapStructure.Cell[][] IslandMap = SimParameters.getMapSize();
    public static MapStructure.Cell[][] getIslandMap(){
        return IslandMap;
    }
    static int Length = SimParameters.getMapLength();
    static int Height = SimParameters.getMapHeight();

    public static int getLength() {
        return Length;
    }

    public static int getHeight() {
        return Height;
    }
    public static HashSet<MapStructure.Cell> getMapInCells() {
        HashSet<MapStructure.Cell> cells = new HashSet<>();
        for (int i = 0; i < SimParameters.getMapLength(); i++) {
            for (int j = 0; j < SimParameters.getMapHeight(); j++) {
                cells.add(IslandMap[i][j]);
            }
        }
        return cells;
    }

    public static List<MapStructure.Cell> getNeighbours(MapStructure.Cell cell){
        List<MapStructure.Cell> neighbours = new ArrayList<>();
        int x = cell.getX();
        int y = cell.getY();

        if(x!=0) neighbours.add(IslandMap[x-1][y]);
        if(x!=Length-1) neighbours.add(IslandMap[x+1][y]);
        if(y!=0) neighbours.add(IslandMap[x][y-1]);
        if(y!=Height-1) neighbours.add(IslandMap[x][y+1]);
        if(x!=0 && y!=Height-1) neighbours.add(IslandMap[x-1][y+1]);
        if(x!=Length-1 && y!=Height-1) neighbours.add(IslandMap[x+1][y+1]);
        if(x!=0 && y!=0) neighbours.add(IslandMap[x-1][y-1]);
        if(x!=Length-1 && y!=0) neighbours.add(IslandMap[x+1][y-1]);

        return neighbours;
    }
    //will be used later
    public static List<MapStructure.Cell> getAllNeighbours(MapStructure.Cell cell, int howFarSensesCanSee) {
        List<MapStructure.Cell> allNeighbours = getNeighbours(cell);
        int x = cell.getX();
        int y = cell.getY();
        for (int i = 0; i < howFarSensesCanSee; i++) {
            for (MapStructure.Cell cell1 : allNeighbours) {
                List<MapStructure.Cell> neighbours1 = getNeighbours(cell1);
                for (MapStructure.Cell cell2 : neighbours1) {
                    if (!allNeighbours.contains(cell2)) allNeighbours.add(cell2);
                }
            }
        }
        return allNeighbours;
    }
    public void generateTerrain(MapStructure.Cell Cell){

        int plainProb = 53;
        int mountProb = 7;
        int hillProb = 25;
        List<MapStructure.Cell> neighbours = getNeighbours(Cell);
        int neighbourCount = neighbours.size();
        for (int i =0; i<neighbourCount; i++){
            try {
                MapStructure.Cell neighbour = neighbours.get(i);
                if (neighbour.getTerrain().equals(Terrain.MOUNTAIN)) {
                    plainProb -= 12;
                    mountProb += 4;
                    hillProb += 8;
                }
                else if (neighbour.getTerrain().equals(Terrain.HILL)){
                    plainProb -= 8;
                    mountProb += 3;
                    hillProb += 5;
                }
                else if ((neighbour.getTerrain().equals(Terrain.WATER))){
                    plainProb -= 3;
                    mountProb -= 3;
                    hillProb -= 3;
                }
            }
            catch (NullPointerException e){
                continue;
            }
        }
        mountProb = mountProb+plainProb;
        hillProb = hillProb+mountProb;
        int d100 = (int) (Math.random()*100);
        if (d100<plainProb) Cell.setTerrain(Terrain.PLAIN);
                else if (d100<mountProb) Cell.setTerrain(Terrain.MOUNTAIN);
                    else if (d100<hillProb) Cell.setTerrain(Terrain.HILL);
                        else Cell.setTerrain(Terrain.WATER);
    }

    public void generateMap() {
        IslandMap[0][0] = SimParameters.getStartingCell();
        if (Length <= 0 || Height <= 0) {
            throw new IllegalStateException("Map dimensions must be greater than 0");
        }
        int LengthRoof = Length-1;
        int HeightRoof = Height-1;
        int LengthFloor = 0;
        int HeightFloor = 0;
        int currLength = 1;
        int currHeight = 0;

        boolean reverseAlg = false;
        while (!ImportantMethods.isArrayFull(IslandMap)) {
            MapStructure.Cell currCell = new MapStructure.Cell(currLength, currHeight, Terrain.PLAIN);
            currCell.setX(currLength);
            currCell.setY(currHeight);
            generateTerrain(currCell);
            currCell.setCurrPlantCapacity(50);
            IslandMap[currLength][currHeight] = currCell;
            if(currLength==LengthRoof && currHeight==HeightRoof) {
                reverseAlg=true;
                LengthRoof--;
                HeightRoof--;
                continue;
            }
            if(currLength==LengthFloor && currHeight==HeightFloor) {
                reverseAlg=false;
                LengthFloor++;
                HeightFloor++;
                continue;
            }
            if(!reverseAlg) {
                if(currLength!=LengthRoof) currLength++;
                else currHeight++;
            }
            if(reverseAlg) {
                if(currLength!=LengthFloor) currLength--;
                else currHeight--;
            }
            }
    }
    }