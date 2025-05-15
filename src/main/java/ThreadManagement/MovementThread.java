package ThreadManagement;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Movement;
import Organisms.Organism;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovementThread implements Runnable{
    private static final Logger logger = Logger.getLogger(MovementThread.class.getName());
    public BlockingQueue<Movement.MoveRequest> movementQueue;
    private volatile boolean running = true;
    public MovementThread(BlockingQueue<Movement.MoveRequest> moveRequestQueue) {
        this.movementQueue = moveRequestQueue;
    }
    public void assignAnimalAndMovement(Animal animal){
        Movement.MoveRequest moveRequest;
        moveRequest= new Movement.MoveRequest(animal, Movement.whereToMoveTest(animal));
        movementQueue.add(moveRequest);
        logger.log(Level.FINER, "Animal [{0}] successfully assigned to move to Cell [{1},{2}]",
                new Object[]{animal.getId(), moveRequest.getCell().getX(), moveRequest.getCell().getY()});
    }
    @Override
    public void run() {
        while (running) {
            try {
                Movement.MoveRequest moveRequest= movementQueue.take();
                Animal animal = moveRequest.getAnimal();
                MapStructure.Cell targetCell = moveRequest.getCell();
                MapStructure.Cell currentCell = moveRequest.getAnimal().getCell();
                if (animal == null || targetCell == null || animal.isDead() || currentCell == null || currentCell.equals(targetCell)) {
                    logger.log(Level.WARNING, "Invalid/redundant MoveRequest skipped: {0}", moveRequest);
                    continue; // Skip to next request
                }
                currentCell.removeAnimal(moveRequest.getAnimal());
                animal.setCell(moveRequest.getCell());
                targetCell.addAnimal(moveRequest.getAnimal());
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
