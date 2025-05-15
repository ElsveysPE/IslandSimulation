package util;

import DTO.AnimalState;
import DTO.CellState;
import DTO.SimState;
import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Conditions.Condition;
import Organisms.Animals.Movement;
import ThreadManagement.ActionPool;
import ThreadManagement.MovementThread;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SimManager {
    private Logger logger = Logger.getLogger(SimManager.class.getName());
    private final ActionPool actionPool;
    private final BlockingQueue<Movement.MoveRequest> moveRequestQueue;
    private final MovementThread movementExecutor;
    private final Thread movementThread;

    // private final PlantManager plantTask;
    // private final Thread plantThread;

    private List<Animal> allAnimals;
    private MapStructure.Cell[][] map;
    private volatile boolean simulationRunning = false;
    private final int endTimes = 10000;

    private final Gson gson;
    private final String stateFilePath = "sim_state.json";
    public SimManager(List<Animal> initialAnimals, MapStructure.Cell[][] map) {
        this.allAnimals = new ArrayList<>(initialAnimals);
        this.map = map;
        this.tick = 0;
        this.dayTime = DayTime.MORNING;

        // Initialize threading components
        this.moveRequestQueue = new LinkedBlockingQueue<>();
        this.actionPool = new ActionPool();
        this.movementExecutor = new MovementThread(this.moveRequestQueue);
        this.movementThread = new Thread(this.movementExecutor, "MovementThread");

        // Initialize plant stuff later
        // this.plantTask = new PlantManager(...);
        // this.plantThread = new Thread(this.plantTask, "PlantThread");

        this.gson = new GsonBuilder().setPrettyPrinting().create();

        logger.log(Level.INFO, "SimManager initialized.");
    }


    public int getEndTimes(){
        return endTimes;
    }
    private static int tick = 0; // reminder, each tick is a quarter of a day(morning, afternoon, evening, night)
    // cause we remade the concept stuff from abstract to more grounded cause I don`t know you just hate yourself it seems
    // no, we won`t make moon phases or even seasons, finish the planned stuff at least
    private static DayTime dayTime = DayTime.MORNING;
    public static int getTick() { return tick; }
    public static DayTime getDayTime() { return dayTime; }


    private void setDayTime() {
        int timeIndex = this.tick % 4;
        switch (timeIndex) {
            case 0:
                this.dayTime = DayTime.MORNING;
                break;
            case 1:
                this.dayTime = DayTime.AFTERNOON;
                break;
            case 2:
                this.dayTime = DayTime.EVENING;
                break;
            case 3:
                this.dayTime = DayTime.NIGHT;
                break;
        }
    }

    public void startSimulation() {
        if (simulationRunning) {
            logger.log(Level.WARNING, "Simulation start requested but already running.");
            return;
        }
        simulationRunning = true;
        logger.log(Level.INFO, "Starting simulation threads and loop...");


        movementThread.start();
        logger.log(Level.INFO, "Starting movement threads...");
        // plantThread.start(); // Start later if needed

        // Main Simulation Loop
        while (tick < endTimes && simulationRunning) {
            // --- Start of Tick ---
            logger.log(Level.INFO, "--- Starting Tick: {0} ---", tick);
            setDayTime(); // Update static day/night state

            // --- Reset Animal State ---
            for (Animal animal : allAnimals) {
                if (!animal.isDead()) {
                    animal.resetMP(); // Reset movement points
                    // animal.resetAP(4); // Reset action points later when implemented
                }
            }

            // --- Animal Planning Phase (Parallel) ---
            animalPhase(); // Calls actionPool.planning which blocks until done

            // --- Movement Execution Phase (Concurrent) ---
            // The MovementThread is processing the queue concurrently.
            // If strict synchronization is needed (e.g., plants react to *final*
            // positions of this tick), add synchronization here (e.g., wait for queue empty).
            // For visualization, overlap is likely fine.

            // --- Plant Phase (Placeholder) ---
            plantPhase();

            // --- Write State for Visualization ---
            writeSimulationStateToFile();

            // --- End of Tick Cleanup (Placeholder) ---
            // removeDeadAnimals();

            logger.log(Level.INFO, "--- Ending Tick: {0} (DayTime: {1}) ---", new Object[]{tick, dayTime});

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Simulation loop sleep interrupted.");
                Thread.currentThread().interrupt();
                simulationRunning = false;
            }
            tick++;
        }

        shutdown();
    }

    private void planAnimalActionsParallel() {
        List<Animal> activeAnimals = getActiveAnimals();
        actionPool.planning(activeAnimals, this.moveRequestQueue, this.map);
    }

    /**
     * Gathers the current state of the simulation (animal positions, tick, daytime)
     * and writes it to the JSON file for Unity to read.
     */
    private void writeSimulationStateToFile() {
        // --- 1. Gather Map Data ---
        // *** DERIVE width and height FROM the map instance variable ***
        int mapWidth = this.map.length;
        int mapHeight = this.map[0].length;
        // *** GENERATE mapGridData by iterating through the map instance variable ***
        List<CellState> mapGridData = new ArrayList<>(mapWidth * mapHeight);
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (this.map[x][y] != null) {
                    mapGridData.add(new CellState(
                            x,
                            y,
                            this.map[x][y].getTerrain().name() // Get terrain enum name as String
                    ));
                } else {
                    logger.log(Level.WARNING, "Null cell found at [{0},{1}] during state writing.", new Object[]{x,y});
                }
            }
        }

        List<AnimalState> currentAnimalStates = new ArrayList<>();
        for (Animal animal : this.allAnimals) {
            if (animal.getCell() != null && !animal.isDead()) {
                currentAnimalStates.add(new AnimalState(
                        animal.getId(),
                        animal.getCell().getX(),
                        animal.getCell().getY(),
                        animal.getClass().getSimpleName()
                ));
            }
            // TODO: Handle dead animals if Unity needs to show them differently
        }

        SimState currentState = new SimState( // Ensure this matches your DTO constructor
                tick,
                dayTime.name(),
                mapWidth,         // Pass map width
                mapHeight,        // Pass map height
                mapGridData,      // Pass map grid list
                currentAnimalStates
        );


        try (FileWriter writer = new FileWriter(stateFilePath)) {
            gson.toJson(currentState, writer);
            logger.log(Level.FINEST, "Successfully wrote simulation state to {0} for tick {1}",
                    new Object[]{stateFilePath, SimManager.tick});
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to write simulation state file: " + stateFilePath, e);
            shutdown();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during JSON serialization/writing", e);
        }
    }
    public void battle(){

    }
    private void plantPhase(){

    }
    private void animalPhase() {
        logger.log(Level.FINE, "Starting animal planning phase for tick {0}", this.tick);
        List<Animal> activeAnimals = getActiveAnimals();
        actionPool.planning(activeAnimals, this.moveRequestQueue, this.map);

        logger.log(Level.FINE, "Finished animal planning phase for tick {0}", this.tick);
    }

    private List<Animal> getActiveAnimals() {
        List<Animal> active = new ArrayList<>();
        for(Animal a : allAnimals) {
            if (!a.isDead() && !a.conditions.contains(Condition.INCAPACITATED)) {
                active.add(a);
            }
        }
        return active;
    }
    private void battles(){

    }

    public void shutdown() {
        logger.log(Level.INFO, "Initiating simulation shutdown...");
        simulationRunning = false;
        if (actionPool != null) actionPool.shutdown();

        if (movementExecutor != null) movementExecutor.shutdown();
        if (movementThread != null) movementThread.interrupt();

        try {
            if (movementThread != null) movementThread.join(5000);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        logger.log(Level.INFO, "Simulation shutdown complete.");
    }
}
