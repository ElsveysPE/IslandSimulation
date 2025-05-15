package Organisms;

import Map.MapStructure;

import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import java.util.logging.Level;

public abstract class Organism {
    private static final Logger logger = Logger.getLogger(Organism.class.getName());
    private static final AtomicLong idCounter = new AtomicLong(0);
    private final long id;
    protected Organism() {
        this.id = idCounter.incrementAndGet();
        logger.log(Level.FINE, "Organism created with ID: {0}", this.id);
    }

    public long getId() {
        return id;
    }

    private int capacityUsed;
    private float growthPoints=0;
    private float storedEnergyPoints=0;
    private int MAX_Age;
    private int currAge=0;
    private boolean isDead = false;

    public boolean isDead() {
        return isDead;
    }

    private HealthStatus healthStatus;

    private MapStructure.Cell cell;

}
