package DTO;

import java.util.List;

public class SimState {
    public int tick;
    public String dayTime;
    public int mapWidth;  // Ensure these exist
    public int mapHeight; // Ensure these exist
    public List<CellState> mapGrid; // Ensure this exists
    public List<AnimalState> animals;

    // Constructor including map fields
    public SimState(int tick, String dayTime, int mapWidth, int mapHeight, List<CellState> mapGrid, List<AnimalState> animals) {
        this.tick = tick;
        this.dayTime = dayTime;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.mapGrid = mapGrid;
        this.animals = animals;
    }
    public SimState() {} // Default constructor
}
