package ThreadManagement;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Movement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionPool {
    private static final Logger logger = Logger.getLogger(ActionPool.class.getName());
    private final ExecutorService animalDecisionExecutor;
    public ActionPool() {
        int numberOfCores = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(1, numberOfCores - 2);
        logger.log(Level.INFO, "Initializing Animal Decision Pool with size: {0}", poolSize);
        this.animalDecisionExecutor = Executors.newFixedThreadPool(poolSize);
    }

    public void planning(List<Animal> animals, BlockingQueue<Movement.MoveRequest> movementQueue, MapStructure.Cell[][] islandMap){
        if (animals == null || animals.isEmpty()) {
            logger.log(Level.FINE, "No active animals for planning.");
            return;
        }

        logger.log(Level.FINE, "Submitting planning tasks for {0} animals.", animals.size());
        List<Callable<Void>> plannedTasks = new ArrayList<>();
        for (Animal animal : animals) {
            final Animal currentAnimal = animal;

            plannedTasks.add(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        MapStructure.Cell targetCell = Movement.whereToMoveTest(animal);
                        if (targetCell != null && !targetCell.equals(animal.getCell())) {
                            Movement.MoveRequest request = new Movement.MoveRequest(animal, targetCell);
                            movementQueue.put(request);
                            logger.log(Level.FINEST, "Animal {0} queued move request to [{1},{2}]",
                                    new Object[]{animal.getId(), targetCell.getX(), targetCell.getY()});

                        } else {
                            logger.log(Level.FINEST, "Animal {0} planned no move.", animal.getId());
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        logger.log(Level.WARNING, "Planning task for animal {0} interrupted.", animal.getId());
                    } catch (Exception e) {
                        logger.log(Level.SEVERE, "Error during planning task for animal " + animal.getId(), e);
                    }
                    return null;
                }
            });
        }
        try {
            logger.log(Level.FINER, "Invoking all {0} planning tasks...", plannedTasks.size());
            animalDecisionExecutor.invokeAll(plannedTasks);
            logger.log(Level.FINE, "All animal planning tasks completed for this tick.");
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Animal planning phase interrupted while waiting for tasks to complete!", e);
            Thread.currentThread().interrupt();
        }

        }
    public void shutdown() {
        logger.log(Level.INFO, "Shutting down Animal Decision Pool...");
        animalDecisionExecutor.shutdown();
        try {
            if (!animalDecisionExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                logger.log(Level.WARNING, "Animal Pool did not terminate in 60s, forcing shutdown...");
                animalDecisionExecutor.shutdownNow();
            } else {
                logger.log(Level.INFO, "Animal Pool terminated.");
            }
        } catch (InterruptedException ie) {
            logger.log(Level.WARNING, "Interrupted during Animal Pool shutdown wait.");
            animalDecisionExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
