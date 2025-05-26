package BattleManagement;

import util.NamedThreadFactory;
import util.SemaphorePausableExecutor;
import util.SimManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import static util.SimManager.*;

public class ActionThread implements Runnable {
    private static final Logger logger = Logger.getLogger(ActionThread.class.getName());

    // animalDecisionExecutor is held but not directly paused/resumed by this class.
    // Its activity is controlled by ActionPool via SimManager.battleInitiated.
    @SuppressWarnings("unused") // Potentially unused if not directly manipulated
    private final SemaphorePausableExecutor animalDecisionExecutor;

    private Battle currentBattleToProcess;
    private volatile boolean keepThreadAlive = true; // Controls the main lifecycle of this thread
    private volatile boolean newBattleAssigned = false; // Flag: a new battle is ready

    // This lock is specific to ActionThread for managing battle assignments.
    // It is NOT SimManager.SIM_LOCK.
    private final Object battleAssignmentLock = new Object();

    public static final int BATTLE_ROUND_DELAY_MS = 200;

    public ActionThread(SemaphorePausableExecutor mainAiExecutor) {
        this.animalDecisionExecutor = mainAiExecutor; // Store if needed for other reasons, else can be removed
        this.currentBattleToProcess = null;
    }
    public Battle getCurrentBattle() {
        synchronized (battleAssignmentLock) {
            // Return the battle it's currently tasked with or has most recently processed
            // It will be null if no battle is assigned or after it's fully cleaned up.
            return this.currentBattleToProcess;
        }
    }
    /**
     * Called by SimManager to give a new battle to this ActionThread.
     * This wakes up the ActionThread if it's waiting.
     */
    public void submitNewBattle(Battle battle) {
        synchronized (battleAssignmentLock) {
            if (this.currentBattleToProcess != null || newBattleAssigned) {
                logger.log(Level.WARNING,
                        "[ActionThread] submitNewBattle called while a battle may already be pending or active. Current assignment overwritten.",
                        new Object[]{this.currentBattleToProcess, battle});
                // This state (submitting a battle when one is ongoing/pending for this thread)
                // should ideally be prevented by SimManager.battleInitiated checks before calling this.
            }
            this.currentBattleToProcess = battle;
            this.newBattleAssigned = true;
            logger.log(Level.INFO, "[ActionThread] New battle submitted for cell: [{0},{1}]",
                    new Object[]{battle.getMainMapBattleCell().getX(), battle.getMainMapBattleCell().getY()});
            battleAssignmentLock.notify(); // Wake up the run() method's wait
        }
    }

    /**
     * Signals this thread to terminate its main loop.
     */
    public void shutdown() {
        logger.log(Level.INFO, "[ActionThread] Shutdown requested.");
        this.keepThreadAlive = false;
        synchronized (battleAssignmentLock) {
            // Wake up from wait() to check keepThreadAlive and exit loop
            battleAssignmentLock.notify();
        }
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "[ActionThread] Dedicated battle processing thread started.");

        while (keepThreadAlive) {
            Battle battleForThisIteration = null;

            synchronized (battleAssignmentLock) {
                // Wait for a new battle to be assigned
                while (!newBattleAssigned && keepThreadAlive) {
                    try {
                        logger.log(Level.FINE, "[ActionThread] Waiting for a new battle assignment...");
                        battleAssignmentLock.wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "[ActionThread] Interrupted while waiting for battle. Shutting down.");
                        Thread.currentThread().interrupt();
                        keepThreadAlive = false; // Ensure termination
                        break; // Exit wait loop
                    }
                }

                if (!keepThreadAlive) {
                    break; // Exit main while loop if shutdown requested during wait
                }

                if (newBattleAssigned && this.currentBattleToProcess != null) {
                    battleForThisIteration = this.currentBattleToProcess;
                    newBattleAssigned = false; // Mark assignment as consumed for this iteration
                } else {
                    // Spurious wakeup or inconsistent state (e.g., shutdown while assigning)
                    logger.log(Level.FINE, "[ActionThread] Woke up but no valid new battle assigned, or shutting down. Resetting flags.");
                    newBattleAssigned = false; // Reset
                    this.currentBattleToProcess = null; // Clear potentially stale battle
                    continue; // Go back to waiting
                }
            } // Release battleAssignmentLock

            // --- A Battle is now assigned in battleForThisIteration ---
            logger.log(Level.INFO, "[ActionThread] Starting battle process for: " + battleForThisIteration);

            SimManager.battleInitiated.set(true); // Notify system that a battle is active
            // ActionPool.planning() will now wait on SimManager.SIM_LOCK

            // No direct pause/resume calls on animalDecisionExecutor from here.
            // Its "pausing" is managed by ActionPool checking SimManager.battleInitiated.

            try {
                while (!battleForThisIteration.isBattleOver() && keepThreadAlive) {
                    // Check keepThreadAlive here in case shutdown is called mid-battle
                    if (!keepThreadAlive) {
                        logger.log(Level.INFO, "[ActionThread] Shutdown requested during battle processing.");
                        Thread.currentThread().interrupt(); // Attempt to break sleep/long ops
                        break;
                    }
                    Thread.sleep(100);
                    logger.log(Level.INFO, "[ActionThread] Commencing new round for battle at [{0},{1}]",
                            new Object[]{battleForThisIteration.getMainMapBattleCell().getX(), battleForThisIteration.getMainMapBattleCell().getY()});

                    battleForThisIteration.commenceTurnAndExecuteActions();

                    if (battleForThisIteration.isBattleOver()) {
                        logger.log(Level.INFO, "[ActionThread] Battle at [{0},{1}] has concluded.",
                                new Object[]{battleForThisIteration.getMainMapBattleCell().getX(), battleForThisIteration.getMainMapBattleCell().getY()});
                        break;
                    }
                    Thread.sleep(BATTLE_ROUND_DELAY_MS);
                }
            } catch (InterruptedException e) {
                logger.log(Level.WARNING, "[ActionThread] Battle loop interrupted for battle at cell [{0},{1}].",
                        new Object[]{battleForThisIteration.getMainMapBattleCell().getX(), battleForThisIteration.getMainMapBattleCell().getY()});
                Thread.currentThread().interrupt(); // Preserve interrupt status
            } catch (Exception e) {
                logger.log(Level.SEVERE, "[ActionThread] Unhandled exception in battle loop for battle at cell [{0},{1}]!",
                        new Object[]{battleForThisIteration.getMainMapBattleCell().getX(), battleForThisIteration.getMainMapBattleCell().getY(), e});
            } finally {
                logger.log(Level.INFO, "[ActionThread] Battle process ending for cell: [{0},{1}]",
                        new Object[]{battleForThisIteration.getMainMapBattleCell().getX(), battleForThisIteration.getMainMapBattleCell().getY()});

                // Set global flag: Battle is OFF (do this first)
                SimManager.battleInitiated.set(false);

                // Notify SimManager.SIM_LOCK to wake up ActionPool.planning()
                synchronized (SimManager.SIM_LOCK) {
                    SimManager.battleInitiated.set(false);
                    SimManager.SIM_LOCK.notifyAll();
                }

                // No direct resume call on animalDecisionExecutor from here.
                // ActionPool will proceed once it acquires SIM_LOCK and sees battleInitiated is false.

                logger.log(Level.INFO, "[ActionThread] Battle cleanup complete for cell: [{0},{1}]",
                        new Object[]{battleForThisIteration.getMainMapBattleCell().getX(), battleForThisIteration.getMainMapBattleCell().getY()});

                // Clear the battle reference for the next cycle
                synchronized(battleAssignmentLock){ // Ensure visibility and atomicity with submitNewBattle
                    this.currentBattleToProcess = null;
                }
            }
        } // End of while(keepThreadAlive) loop
        logger.log(Level.INFO, "[ActionThread] Battle processing thread has terminated.");
    }
}