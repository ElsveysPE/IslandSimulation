package DTO;

public class CellState {
    // Public fields are often used in DTOs for simplicity with JSON libraries like Gson
    public int x;
    public int y;
    public String terrainType; // e.g., "PLAIN", "HILL", "WATER", "MOUNTAIN"

    /**
     * Constructor to easily create CellData.
     * @param x The x-coordinate of the cell.
     * @param y The y-coordinate of the cell.
     * @param terrainType The terrain type as a String (e.g., from Terrain.name()).
     */
    public CellState(int x, int y, String terrainType) {
        this.x = x;
        this.y = y;
        this.terrainType = terrainType;
    }

    /**
     * Default constructor - often required by JSON libraries or frameworks.
     */
    public CellState() {}

    // Optional: Add getters/setters if needed, but public fields are common for DTOs.
    // Optional: Add toString() for debugging.
    @Override
    public String toString() {
        return "CellData{" +
                "x=" + x +
                ", y=" + y +
                ", terrainType='" + terrainType + '\'' +
                '}';
    }
}

