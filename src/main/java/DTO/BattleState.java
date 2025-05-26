package DTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BattleState {
    public boolean isActive = false;
    public int battleMapWidth;
    public int battleMapHeight;
    public List<CellState> battleMapGrid;   // Terrain for the battle map cells
    public List<AnimalState> combatants;    // Full AnimalState for those in battle
    public long currentActorId;             // ID of animal whose turn it is in battle
    public List<String> recentBattleEvents; // Simple battle log (e.g., "Animal A attacks Animal B for X damage")
    public String battleOriginCoordinates;  // e.g. "[x,y]" of main map cell where battle started

    public BattleState() {
        this.battleMapGrid = new ArrayList<>();
        this.combatants = new ArrayList<>();
        this.recentBattleEvents = new ArrayList<>();
    }

    // Getters and Setters or public fields are fine.
    @Override
    public String toString() {
        return "BattleState{" +
                "isActive=" + isActive +
                ", combatants=" + (combatants != null ? combatants.size() : 0) +
                ", currentActorId=" + currentActorId +
                '}';
    }
}