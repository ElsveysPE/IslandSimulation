package util;

import BattleManagement.ActionThread;
import BattleManagement.Battle;
import BattleManagement.BattleCell;
import DTO.AnimalState;
import DTO.BattleState;
import DTO.CellState;
import DTO.SimState;
import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Conditions.Conditions;
import Organisms.Animals.Movement;
import ThreadManagement.ActionPool;
import ThreadManagement.MovementThread;
import ThreadManagement.PlantThread;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Map.MapGeneration.getIslandMap;
import static Organisms.Animals.Management.CheckUp.checkUp;
import static Organisms.Animals.Management.CheckUp.massDecay;

public class SimManager {
    private Logger logger = Logger.getLogger(SimManager.class.getName());
    private final ActionPool actionPool;
    private final BlockingQueue<Movement.MoveRequest> moveRequestQueue;
    private final MovementThread movementExecutor; // The Runnable
    private final Thread movementThread;         // The Thread

    private final ActionThread actionExecutor;   // The Runnable (Battle Processor)
    private final Thread actionThread;           // The Thread for battle processing

    private final PlantThread plantExecutor;     // The Runnable
    private final Thread plantThread;            // The Thread for plant growth

    public static AtomicBoolean battleInitiated = new AtomicBoolean(false); // Initialize
    public static final Object SIM_LOCK = new Object();

    public static volatile Battle newBattleToDispatch = null;
    public static HashSet<Animal> allAnimals = new HashSet<>();
    private MapStructure.Cell[][] map;
    private volatile boolean simulationRunning = false;
    private final int endTimes = 1000;
    private static int tick = 0;
    private static DayTime dayTime = DayTime.MORNING;

    private final Gson gson;
    private final String stateFilePath = "IslandSimulationUnity\\Assets\\StreamingAssets\\sim_state.json";

    public SimManager(HashSet<Animal> animals, MapStructure.Cell[][] map) {
        SimManager.allAnimals = animals; // Assign to static field
        this.map = map;
        // tick and dayTime are static, initialize if this is the primary setup point
        SimManager.tick = 0;
        SimManager.dayTime = DayTime.MORNING;

        // Initialize threading components
        this.moveRequestQueue = new LinkedBlockingQueue<>();
        this.actionPool = new ActionPool();
        this.movementExecutor = new MovementThread(this.moveRequestQueue);
        this.movementThread = new Thread(this.movementExecutor, "MovementThread");
        this.actionExecutor = new ActionThread(this.actionPool.getAnimalDecisionExecutor());
        this.actionThread = new Thread(this.actionExecutor, "BattleProcessorThread");
        this.plantExecutor = new PlantThread();
        this.plantThread = new Thread(this.plantExecutor, "PlantGrowthThread");

        this.gson = new GsonBuilder().setPrettyPrinting().create();
        logger.log(Level.INFO, "SimManager initialized.");
    }

    // Method for ActionPool to submit a new battle to the ActionThread
    public void dispatchBattleToProcessor(Battle newBattle) {
        if (this.actionExecutor != null) {
            logger.log(Level.INFO, "[SimManager] Dispatching battle to BattleProcessorThread for cell: [{0},{1}]",
                    new Object[]{newBattle.getMainMapBattleCell().getX(), newBattle.getMainMapBattleCell().getY()});
            this.actionExecutor.submitNewBattle(newBattle); // Call the method on the ActionThread Runnable
        } else {
            logger.log(Level.SEVERE, "[SimManager] ActionExecutor (BattleProcessor) is null. Cannot dispatch battle.");
            // Critical error, battle won't run. Reset flags and notify to prevent deadlock.
            SimManager.battleInitiated.set(false);
            synchronized(SimManager.SIM_LOCK) {
                SimManager.SIM_LOCK.notifyAll();
            }
        }
    }

    public int getEndTimes() {
        return endTimes;
    }

    public static int getTick() { return tick; }
    public static DayTime getDayTime() { return dayTime; }

    private void setDayTime() {
        // Using static tick and dayTime directly
        int timeIndex = SimManager.tick % 4;
        switch (timeIndex) {
            case 0: SimManager.dayTime = DayTime.MORNING; break;
            case 1: SimManager.dayTime = DayTime.AFTERNOON; break;
            case 2: SimManager.dayTime = DayTime.EVENING; break;
            case 3: SimManager.dayTime = DayTime.NIGHT; break;
        }
    }

    public void startSimulation() {
        if (simulationRunning) {
            logger.log(Level.WARNING, "Simulation start requested but already running.");
            return;
        }
        simulationRunning = true;
        logger.log(Level.INFO, "Starting simulation threads and loop...");

        // --- START LONG-RUNNING THREADS ONCE ---
        if (movementThread != null && !movementThread.isAlive()) {
            movementThread.start();
            logger.log(Level.INFO, "MovementThread started.");
        } else if (movementThread == null) {
            logger.log(Level.SEVERE, "MovementThread is null and cannot be started!");
        }

        if (plantThread != null && !plantThread.isAlive()) {
            plantThread.start(); // Start PlantThread ONCE
            logger.log(Level.INFO, "PlantGrowthThread started.");
        } else if (plantThread == null) {
            logger.log(Level.SEVERE, "PlantThread is null and cannot be started!");
        }

        if (this.actionThread != null && !this.actionThread.isAlive()) {
            logger.log(Level.INFO, "Starting dedicated BattleProcessorThread.");
            this.actionThread.start(); // Start battle processor thread ONCE
        } else if (this.actionThread == null) {
            logger.log(Level.SEVERE, "ActionThread (BattleProcessor) is null and cannot be started!");
        }
        // No .join() for these worker threads here, they run in the background.

        // Main Simulation Loop
        while (SimManager.tick < endTimes && simulationRunning) {
            logger.log(Level.INFO, "--- Starting Tick: {0} ---", SimManager.tick);
            setDayTime();

            for (Animal animal : allAnimals) {
                if (!animal.isDead()) {
                    animal.resetMP();
                }
            }

            for (Animal animal : allAnimals) { // Assuming allAnimals is your master list
                if (!animal.isDead()) { // Only for living animals
                    animal.resetMP(); // Your existing call
                    animal.resetPerTickFlags(); // NEW: This will set hasEatenThisTick to false
                }
            }

            actionPhase();
            animalPhase();

            if (SimManager.newBattleToDispatch != null) {
                Battle battleToStart = SimManager.newBattleToDispatch;
                SimManager.newBattleToDispatch = null; // Consume the request
                this.logger.log(Level.INFO, "[SimManager] Main loop detected battle request. Dispatching battle for cell: [{0},{1}]",
                        new Object[]{battleToStart.getMainMapBattleCell().getX(), battleToStart.getMainMapBattleCell().getY()});
                this.dispatchBattleToProcessor(battleToStart); // 'this' is the SimManager instance
            }


            plantPhase();

            writeSimulationStateToFile();
            massDecay();
            for (Animal animal : allAnimals) {
                checkUp(animal); // Assuming checkUp is a defined method
            }
            logger.log(Level.INFO, "--- Ending Tick: {0} (DayTime: {1}) ---", new Object[]{SimManager.tick, SimManager.dayTime});

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "Simulation loop sleep interrupted.");
                Thread.currentThread().interrupt();
                simulationRunning = false;
            }
            if(!battleInitiated.get())  {
                SimManager.tick++;
                for(Animal animal: allAnimals)
                {
                    animal.actionPoints=4;
                }
            }
        }
        shutdown();
    }

    private void writeSimulationStateToFile() {
        // Збираємо дані для стану, як і раніше
        int worldMapWidth = 0;
        int worldMapHeight = 0;
        List<CellState> worldMapGridData = new ArrayList<>();
        if (getIslandMap() != null && getIslandMap().length > 0) {
            worldMapWidth = getIslandMap().length;
            worldMapHeight = getIslandMap()[0].length;
            for (int x = 0; x < worldMapWidth; x++) {
                for (int y = 0; y < worldMapHeight; y++) {
                    if (getIslandMap()[x][y] != null) {
                        worldMapGridData.add(new CellState(x, y, getIslandMap()[x][y].getTerrain().name()));
                    }
                }
            }
        }

        List<AnimalState> worldAnimalStates = new ArrayList<>();
        if (SimManager.allAnimals != null) {
            HashSet<Animal> animalsSnapshot = new HashSet<>(SimManager.allAnimals); // Уникаємо ConcurrentModificationException
            for (Animal animal : animalsSnapshot) {
                if (animal.getCell() != null && !animal.isDead() && animal.getBattleCell() == null) {
                    worldAnimalStates.add(AnimalState.fromAnimal(animal, false));
                }
            }
        }

        BattleState battleStateDTO = new BattleState();
        if (SimManager.battleInitiated.get()) {
            Battle currentActualBattle = null;
            if (this.actionExecutor != null) {
                currentActualBattle = this.actionExecutor.getCurrentBattle();
            }
            if (currentActualBattle != null && !currentActualBattle.isBattleOver()) {
                battleStateDTO.isActive = true;
                battleStateDTO.battleMapWidth = currentActualBattle.getActualBattleGridWidth();
                battleStateDTO.battleMapHeight = currentActualBattle.getActualBattleGridHeight();
                if (currentActualBattle.getMainMapBattleCell() != null) {
                    battleStateDTO.battleOriginCoordinates = "[" + currentActualBattle.getMainMapBattleCell().getX() + "," + currentActualBattle.getMainMapBattleCell().getY() + "]";
                } else {
                    battleStateDTO.battleOriginCoordinates = "[N/A]";
                }
                BattleCell[][] battleGridFromGetter = currentActualBattle.getActualBattleGrid();
                if (battleGridFromGetter != null) {
                    for (int x = 0; x < battleStateDTO.battleMapWidth; x++) {
                        for (int y = 0; y < battleStateDTO.battleMapHeight; y++) {
                            if (x < battleGridFromGetter.length && y < battleGridFromGetter[x].length && battleGridFromGetter[x][y] != null) {
                                battleStateDTO.battleMapGrid.add(
                                        new CellState(x, y, battleGridFromGetter[x][y].getTerrain().name())
                                );
                            } else {
                                battleStateDTO.battleMapGrid.add(new CellState(x,y, "UNKNOWN_BCELL_TERRAIN"));
                            }
                        }
                    }
                }
                List<Animal> combatantsInOrder = currentActualBattle.getCombatantsInOrder();
                if (combatantsInOrder != null) {
                    for (Animal combatant : combatantsInOrder) {
                        if (combatant != null && !combatant.isDead() && combatant.getBattleCell() != null) {
                            battleStateDTO.combatants.add(AnimalState.fromAnimal(combatant, true));
                        }
                    }
                }
                Animal currentActor = currentActualBattle.getCurrentActor();
                if (currentActor != null) {
                    battleStateDTO.currentActorId = currentActor.getId();
                }
                List<String> events = currentActualBattle.getBattleEventLog();
                if (events != null) {
                    battleStateDTO.recentBattleEvents.addAll(events);
                }
            } else {
                battleStateDTO.isActive = false;
            }
        } else {
            battleStateDTO.isActive = false;
        }

        SimState currentState = new SimState(
                SimManager.tick,
                SimManager.dayTime.name(),
                worldMapWidth, worldMapHeight, worldMapGridData,
                worldAnimalStates,
                battleStateDTO);

        // --- НОВА ЛОГІКА ЗАПИСУ З ТИМЧАСОВИМ ФАЙЛОМ ---
        java.io.File finalFile = new java.io.File(stateFilePath);
        java.io.File tempFile = new java.io.File(stateFilePath + ".tmp");

        // Переконуємося, що батьківська директорія існує
        java.io.File parentDir = finalFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (parentDir.mkdirs()) {
                logger.log(Level.INFO, "Створено директорію для sim_state.json: " + parentDir.getAbsolutePath());
            } else {
                logger.log(Level.SEVERE, "Не вдалося створити директорію для sim_state.json: " + parentDir.getAbsolutePath());
                // Розглянь, чи варто продовжувати, якщо директорію не створено
            }
        }


        // 1. Записуємо у тимчасовий файл
        try (FileWriter writer = new FileWriter(tempFile)) {
            gson.toJson(currentState, writer);
            // logger.log(Level.FINEST, "SimState (tick {0}) written to temporary file: {1}", new Object[]{SimManager.tick, tempFile.getAbsolutePath()});
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Не вдалося записати стан у тимчасовий файл: " + tempFile.getAbsolutePath(), e);
            return; // Виходимо, якщо не змогли записати у тимчасовий файл
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Неочікувана помилка під час серіалізації/запису у тимчасовий файл", e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.severe(sw.toString());
            return;
        }

        // 2. Безпечно замінюємо основний файл тимчасовим
        try {
            // Використовуємо java.nio.file.Files для більш надійного переміщення/заміни
            java.nio.file.Path tempPath = tempFile.toPath();
            java.nio.file.Path finalPath = finalFile.toPath();

            // Переміщуємо з заміною існуючого файлу. Це зазвичай атомарна операція.
            java.nio.file.Files.move(tempPath, finalPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            // logger.log(Level.FINEST, "SimState (tick {0}) successfully moved to final file: {1}", new Object[]{SimManager.tick, finalFile.getAbsolutePath()});

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Не вдалося перейменувати/перемістити тимчасовий файл '" + tempFile.getAbsolutePath() + "' у '" + finalFile.getAbsolutePath() + "'", e);
            // Спроба видалити тимчасовий файл, якщо він ще існує, щоб не смітити
            if (tempFile.exists()) {
                if (!tempFile.delete()) {
                    logger.log(Level.WARNING, "Не вдалося видалити тимчасовий файл: " + tempFile.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Неочікувана помилка під час заміни основного файлу тимчасовим", e);
            if (tempFile.exists() && !tempFile.delete()) { // Спроба видалити .tmp якщо щось пішло не так
                logger.log(Level.WARNING, "Не вдалося видалити тимчасовий файл після неочікуваної помилки: " + tempFile.getAbsolutePath());
            }
        }
    }


            private void plantPhase() {
        // The PlantGrowthThread (this.plantThread) should have been started once.
        // This phase should not call .start() on it.
        // If PlantThread needs a per-tick signal, that logic would go here.
        // Or if plant growth is synchronous: this.plantExecutor.doWorkForTick();
        logger.log(Level.FINE, "SimManager.plantPhase() for tick {0}. Plant growth handled by dedicated thread or synchronous call.", SimManager.tick);
    }

    private void animalPhase() {
        logger.log(Level.FINE, "Starting animal planning phase for tick {0}", SimManager.tick);
        List<Animal> activeAnimals = getActiveAnimals();
        // ActionPool.planning needs access to SimManager to call dispatchBattleToProcessor
        // This is handled by passing 'this' to ActionPool's constructor.
        actionPool.planning(activeAnimals, this.moveRequestQueue);
        logger.log(Level.FINE, "Finished animal planning phase for tick {0}", SimManager.tick);
    }

    private void actionPhase() {
        // The BattleProcessorThread (this.actionThread) is already running.
        // It processes battles when they are submitted to it.
        // This method should NO LONGER call this.actionThread.start().
        logger.log(Level.FINE, "SimManager.actionPhase() for tick {0}. Battle processing is event-driven.", SimManager.tick);
    }

    private List<Animal> getActiveAnimals() {
        List<Animal> active = new ArrayList<>();
        // Iterate over a snapshot if allAnimals can be modified by other threads during this call.
        // For simplicity, assuming it's safe during this specific call context.
        HashSet<Animal> currentAnimals = new HashSet<>(allAnimals);
        for (Animal a : currentAnimals) {
            if (!a.isDead() && (a.conditions == null || !a.conditions.contains(Conditions.INCAPACITATED))) { // Added null check for conditions
                active.add(a);
            }
        }
        return active;
    }

    public void shutdown() {
        logger.log(Level.INFO, "Initiating simulation shutdown...");
        simulationRunning = false; // Signal main loop and other loops to stop

        // Shutdown worker Runnables first (they will signal their threads)
        if (actionExecutor != null) {
            actionExecutor.shutdown();
        }
        if (plantExecutor != null && plantExecutor instanceof PlantThread) { // Assuming PlantThread has shutdown()
            // ((PlantThread) plantExecutor).shutdown(); // Make sure your PlantThread has this
        } else if (plantExecutor != null) {
            logger.log(Level.INFO, "PlantExecutor is not an instance of PlantThread with a specific shutdown method, relying on interrupt.");
        }
        if (movementExecutor != null && movementExecutor instanceof MovementThread) { // Assuming MovementThread has shutdown()
            // ((MovementThread) movementExecutor).shutdown(); // Make sure your MovementThread has this
        } else if (movementExecutor != null) {
            logger.log(Level.INFO, "MovementExecutor is not an instance of MovementThread with a specific shutdown method, relying on interrupt.");
        }


        // Shutdown thread pools
        if (actionPool != null) {
            actionPool.shutdown();
        }

        // Interrupt threads to unblock from waits/sleeps
        // Give Runnables a chance to process shutdown flag before interrupting.
        // A small delay might be useful here if Runnables don't react to interrupt immediately.
        // try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }


        if (actionThread != null) {
            logger.log(Level.INFO, "Interrupting ActionThread (BattleProcessor)...");
            actionThread.interrupt();
        }
        if (plantThread != null) {
            logger.log(Level.INFO, "Interrupting PlantGrowthThread...");
            plantThread.interrupt();
        }
        if (movementThread != null) {
            logger.log(Level.INFO, "Interrupting MovementThread...");
            movementThread.interrupt();
        }

        // Join threads with timeouts
        try {
            if (actionThread != null && actionThread.isAlive()) {
                logger.log(Level.INFO, "Waiting for ActionThread (BattleProcessor) to join...");
                actionThread.join(5000);
                if (actionThread.isAlive()) logger.log(Level.WARNING, "ActionThread did not join in time.");
            }
            if (plantThread != null && plantThread.isAlive()) {
                logger.log(Level.INFO, "Waiting for PlantGrowthThread to join...");
                plantThread.join(5000);
                if (plantThread.isAlive()) logger.log(Level.WARNING, "PlantThread did not join in time.");
            }
            if (movementThread != null && movementThread.isAlive()) {
                logger.log(Level.INFO, "Waiting for MovementThread to join...");
                movementThread.join(5000);
                if (movementThread.isAlive()) logger.log(Level.WARNING, "MovementThread did not join in time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.log(Level.WARNING, "Interrupted during thread join on shutdown.");
        }
        logger.log(Level.INFO, "Simulation shutdown complete.");
    }

    public static HashSet<Animal> getAllAnimals() {
        return allAnimals;
    }
}