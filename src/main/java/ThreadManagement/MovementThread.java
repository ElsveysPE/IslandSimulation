package ThreadManagement;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Movement;
import util.NamedThreadFactory;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static util.SimManager.*;

public class MovementThread implements Runnable{
    private static final Logger logger = Logger.getLogger(MovementThread.class.getName());
    public BlockingQueue<Movement.MoveRequest> movementQueue;
    private volatile boolean running = true;
    public MovementThread(BlockingQueue<Movement.MoveRequest> moveRequestQueue) {
        this.movementQueue = moveRequestQueue;
    }
    public void assignAnimalAndMovement(Animal animal){
        Movement.MoveRequest moveRequest;
        moveRequest= Movement.MoveRequest.formMoveRequest(animal, Movement.whereToMove(animal));
        movementQueue.add(moveRequest);
        logger.log(Level.FINER, "Animal [{0}] successfully assigned to move to Cell [{1},{2}]",
                new Object[]{animal.getId(), moveRequest.getCell().getX(), moveRequest.getCell().getY()});
    }
    @Override
    public void run() {
        while (running) {
            synchronized (SIM_LOCK) {
                while (battleInitiated.get()) {
                    try {
                        System.out.println(Thread.currentThread().getName() + " (Movement) waiting: Battle active.");
                        SIM_LOCK.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        if (!running) {
                            System.out.println(Thread.currentThread().getName() + " (Movement) interrupted and stopping.");
                            return;
                        }
                    }
                }
            }
                try {
                    Movement.MoveRequest moveRequest = movementQueue.take();
                    Animal animal = moveRequest.getAnimal();
                    MapStructure.Cell targetCell = moveRequest.getCell();
                    MapStructure.Cell currentCell = moveRequest.getAnimal().getCell();
                    if (animal == null || targetCell == null || animal.isDead() || currentCell == null || currentCell == targetCell) {
                        logger.log(Level.WARNING, "Invalid/redundant MoveRequest skipped: {0}", moveRequest);
                        continue; // Skip to next request
                    }
                    animal.setCell(moveRequest.getCell());
                    Terrain terrainToMoveTo =moveRequest.getCell().getTerrain();
                    animal.setCurrentMovementPoints(animal.getCurrentMovementPoints()-
                            animal.getMovementCost(terrainToMoveTo));
                    logger.log(Level.FINER, "Animal [{0}] successfully moved to Cell [{1},{2}]",
                            new Object[]{animal.getId(), targetCell.getX(), targetCell.getY()});
                } catch (InterruptedException e) {
                    if (!running) {
                        logger.log(Level.INFO, "MovementThread interrupted during shutdown, stopping loop.");
                        break;
                    } else {
                        logger.log(Level.WARNING, "MovementThread interrupted unexpectedly. Re-interrupting.", e);
                        Thread.currentThread().interrupt();
                    }
                } catch (NullPointerException npe) {
                    logger.log(Level.SEVERE, "NullPointerException in MovementThread - was queue initialized correctly?", npe);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unexpected error in MovementThread run loop", e);
                }
            }
            logger.log(Level.INFO, "MovementThread finished run method.");
        }
        public void shutdown(){
        running=false;
    }
    public void powerUp(){
        running=true;
    }
}
