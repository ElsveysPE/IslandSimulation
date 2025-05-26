package BattleManagement;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static BattleManagement.Battle.BattleMap;
import static BattleManagement.Battle.getBattleMap;

public class BattleCell {
    private int x;
    private int y;
    private Terrain terrain;
    private Animal animal;
    public BattleCell(int x, int y, Terrain terrain){
        this.x=x;
        this.y=y;
        this.terrain=terrain;
    }

    public Animal getAnimal() {
        return animal;
    }

    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public int getY() {
        return y;
    }
    public int getX() {
        return x;
    }
    public static HashSet<BattleCell> getBattleNeighbours(BattleCell cell){
        HashSet<BattleCell> neighbours = new HashSet<>();
        int x = cell.getX();
        int y = cell.getY();
        int Length = BattleMap.length;
        int Height = BattleMap[0].length;

        if(x!=0) neighbours.add(BattleMap[x-1][y]);
        if(x!=Length-1) neighbours.add(BattleMap[x+1][y]);
        if(y!=0) neighbours.add(BattleMap[x][y-1]);
        if(y!=Height-1) neighbours.add(BattleMap[x][y+1]);
        if(x!=0 && y!=Height-1) neighbours.add(BattleMap[x-1][y+1]);
        if(x!=Length-1 && y!=Height-1) neighbours.add(BattleMap[x+1][y+1]);
        if(x!=0 && y!=0) neighbours.add(BattleMap[x-1][y-1]);
        if(x!=Length-1 && y!=0) neighbours.add(BattleMap[x+1][y-1]);

        return neighbours;
    }
    static boolean isBorder(BattleCell cell) {
        BattleCell[][] currentBattleMap = getBattleMap();
        if (cell == null || currentBattleMap == null || currentBattleMap.length == 0 || currentBattleMap[0].length == 0) {
            // logger.log(Level.WARNING, "[Battle] isBorder: Invalid cell or map.");
            return false;
        }
        int x = cell.getX();
        int y = cell.getY();
        int width = currentBattleMap.length;
        int height = currentBattleMap[0].length;
        return x == 0 || y == 0 || x == width - 1 || y == height - 1;
    }
    public boolean isOccupied() { return this.animal != null; }
}
