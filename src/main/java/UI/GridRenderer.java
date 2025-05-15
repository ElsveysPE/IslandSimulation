package UI;
import Map.MapGeneration;
import Map.MapStructure;
import Map.Terrain;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Cell;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static Map.Terrain.PLAIN;
import static util.SimParameters.getMapHeight;
import static util.SimParameters.getMapLength;

public class GridRenderer {

    private Color getColorForTerrain(Terrain terrain){
        switch (terrain){
            case PLAIN:
                return Color.GREEN;
            case MOUNTAIN:
                return Color.BROWN;
            case HILL:
                return Color.YELLOW;
            case WATER:
                return Color.BLUE;
            default:
                return Color.BLACK;
        }
    }
    public Canvas renderGrid(MapStructure.Cell[][] Map, int cellSize){
        int canvasLength = getMapLength() * cellSize;
        int canvasHeight = getMapHeight() * cellSize;
        Canvas canvas = new Canvas(canvasLength, canvasHeight);
        GraphicsContext graphCont = canvas.getGraphicsContext2D();
        for (int i=0; i<getMapLength(); i++){
            for (int j=0; j<getMapHeight();j++){
                MapStructure.Cell currCell = Map[i][j];
                graphCont.setFill(getColorForTerrain(currCell.getTerrain()));
                graphCont.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
        return canvas;
    }
}
