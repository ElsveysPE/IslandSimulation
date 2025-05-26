package DTO;

import java.util.List;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SimState {
    public int tick;
    public String dayTime;           // e.g., "MORNING", "NIGHT"
    public int worldMapWidth;
    public int worldMapHeight;
    public List<CellState> worldMapGrid;  // Renamed for clarity
    public List<AnimalState> animalsOnWorldMap; // Renamed for clarity
    public BattleState currentBattle;    // Can be null if no battle is active

    // Constructor
    public SimState(int tick, String dayTime,
                    int worldMapWidth, int worldMapHeight, List<CellState> worldMapGrid,
                    List<AnimalState> animalsOnWorldMap,
                    BattleState currentBattle) {
        this.tick = tick;
        this.dayTime = dayTime;
        this.worldMapWidth = worldMapWidth;
        this.worldMapHeight = worldMapHeight;
        this.worldMapGrid = worldMapGrid;
        this.animalsOnWorldMap = animalsOnWorldMap;
        this.currentBattle = currentBattle;
    }

    public SimState() { // Default constructor
        this.worldMapGrid = new ArrayList<>();
        this.animalsOnWorldMap = new ArrayList<>();
        this.currentBattle = new BattleState(); // Initialize with a non-null, inactive battle state
    }

    @Override
    public String toString() {
        return "SimState{" +
                "tick=" + tick +
                ", dayTime='" + dayTime + '\'' +
                ", animalsOnWorldMap=" + (animalsOnWorldMap != null ? animalsOnWorldMap.size() : 0) +
                ", battleActive=" + (currentBattle != null && currentBattle.isActive) +
                '}';
    }
}
