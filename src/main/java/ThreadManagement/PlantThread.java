package ThreadManagement;

import util.SimManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import static Map.plantCheck.plantCheck;
import static util.SimManager.*;

public class PlantThread  implements Runnable{
    private volatile boolean running = true;
    private static final Logger logger = Logger.getLogger(PlantThread.class.getName());

    public PlantThread() {

    }

    @Override
    public void run(){
        while(running){
            synchronized (SIM_LOCK) {
                while (battleInitiated.get()) {
                    try {
                        System.out.println(Thread.currentThread().getName() + " (Movement) waiting: Battle active.");
                        SIM_LOCK.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        if (!running) {
                            System.out.println(Thread.currentThread().getName() + " (Plants) interrupted and stopping.");
                            return;
                        }
                    }
                }
            }
            try{
                plantCheck();
            } catch (NullPointerException npe) {
                logger.log(Level.SEVERE, "NullPointerException in PlantThread", npe);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unexpected error in PlantThread run loop", e);
            }
        }
        logger.log(Level.INFO, "PlantThread finished run method.");
    }
    public void shutdown(){
        running=false;
    }
}
