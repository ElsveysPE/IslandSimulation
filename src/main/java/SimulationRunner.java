import Map.MapGeneration; // Assuming your MapGeneration class exists and works
import Map.MapStructure;
import Organisms.Animals.Animal;
import util.DayTime; // Assuming DayTime enum is in util
import util.SimManager; // Your Simulation Manager class
import util.Spawner; // Your Spawner class

import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

public class SimulationRunner {
//Think about Postgres integration, to save simulations, or maybe just save game files? Eh, deal with it if you have the time
    private static final Logger logger = Logger.getLogger(SimulationRunner.class.getName());

    // --- Simulation Parameters ---
    // NOTE: Ensure MapGeneration uses these or SimParameters internally if not passed to constructor
    private static final int MAP_WIDTH = 50; // Example map size
    private static final int MAP_HEIGHT = 50;
    private static final int INITIAL_WOLVES = 10; // Example population
    private static final int INITIAL_RABBITS = 50;
    // SIMULATION_TICKS is now handled internally by SimManager's endTimes field

    public static void main(String[] args) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.ALL);

        for (Handler handler : rootLogger.getHandlers()) {
            if (handler instanceof ConsoleHandler) {
                handler.setLevel(Level.ALL);
                // handler.setFormatter(new SimpleFormatter());
                System.out.println("ConsoleHandler level set to ALL."); // Confirm it's set
            }
        }
        // Add a FileHandler if you want logs in a file too

    try {
        FileHandler fileHandler = new FileHandler("simulation.log");
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(new SimpleFormatter());
        rootLogger.addHandler(fileHandler);
        System.out.println("Added FileHandler.");
    } catch (java.io.IOException e) {
        System.err.println("Could not add FileHandler: " + e.getMessage());
    }



        logger.log(Level.INFO, "Starting Island Simulation Setup...");

        // 1. Initialize Map
        MapStructure.Cell[][] map = null; // Declare map variable
        // Instantiate MapGeneration (assuming no-args constructor)
        MapGeneration mapGenerator = new MapGeneration();
        try {
            // Call the method that generates the map internally
            mapGenerator.generateMap();
            // Get the generated map using the getter method
            map = mapGenerator.getIslandMap(); // Assign the generated map

            if (map == null || map.length == 0 || map[0].length == 0) { // Check if map is valid
                logger.log(Level.SEVERE, "Map generation failed or returned invalid map!");
                return; // Exit if map generation fails
            }
            // Use map dimensions for logging if needed, assuming MapGeneration used them correctly
            logger.log(Level.INFO, "Map generated successfully ({0}x{1}).", new Object[]{map.length, map[0].length});
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception during map generation!", e);
            return; // Exit on error
        }


        // 2. Initialize Animal List
        List<Animal> animalList = new ArrayList<>();

        // 3. Spawn Initial Population using Spawner
        // The Spawner adds animals to the list and places them on the map cells
        Spawner.spawnInitialPopulation(animalList, map, INITIAL_WOLVES, INITIAL_RABBITS);

        // Check if spawning was successful
        if (animalList.isEmpty()) {
            logger.log(Level.WARNING, "No animals were spawned, simulation might be empty.");
            // Decide if you want to exit or continue
        }

        // 4. Create Simulation Manager instance
        SimManager simManager = new SimManager(animalList, map);

        // 5. Start the Simulation
        logger.log(Level.INFO, "Starting simulation run (max ticks defined in SimManager)...");
        try {
            // Call startSimulation WITHOUT the ticks argument
            simManager.startSimulation(); // Runs the main loop using internal endTimes
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unhandled exception during simulation run!", e);
            // Ensure shutdown is still attempted
            simManager.shutdown();
        }

        // Simulation loop finishes here (or shutdown was called)
        logger.log(Level.INFO, "Simulation run finished or stopped.");

        // Optional: Add a small delay before exiting to ensure final logs/shutdown completes
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        System.exit(0); // Explicit exit might be needed depending on lingering threads if shutdown fails
    }
}
