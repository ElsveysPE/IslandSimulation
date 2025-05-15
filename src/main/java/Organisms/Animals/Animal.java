package Organisms.Animals;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Behaviours.BattleConditions;
import Organisms.Animals.Conditions.Condition;
import Organisms.HealthStatus;
import Organisms.Organism;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

abstract public class Animal extends Organism {
    protected static final Logger logger = Logger.getLogger(Animal.class.getName());
    // Core attributes
    private float storedEnergyPoints;
    private int fatStorage;
    private int healthPoints;
    private int maxHealth;
    public int actionPoints = 4; //tick is basically quarter a day and each action takes +- 1.5 hours
    public boolean outOfActions = false;
    private boolean dead = false;
    private MapStructure.Cell cell;
    private boolean female;
    private HealthStatus healthStatus;
    private final List<Tags> tags = null;

    // Lifecycle attributes
    private int currAge = 0;
    private int maxAge;
    private LifeStage lifeStage;
    private boolean readyForReproduction = false;

    // Physical attributes
    private int size;
    private int agility;
    private int constitution;
    private int strength;
    private int speed;
    private final boolean canFly=false;
    private final boolean swimmer=false;
    private float currentMovementPoints;
    private float maxMovementPoints;
    //position
    private VerticalPosition verticalPosition;

    // Defense and perception attributes
    private float physRes;
    public float reverseDamage=0f;
    private int currStealth;
    private int currPerception;
    private int stealth;
    private int perception;

    // Combat attributes
    public int battleActionPoints;
    private int attackAdv;
    private int stealthAdv;
    private int perceptionAdv;
    private int evasionAdv;
    private int minDamage;

    public List<Condition> conditions;
    public List<BattleConditions> battleConditions;
    public int reactionPoints;

    // Getters and Setters
    public int getHealthPoints() {
        return healthPoints;
    }

    public void setHealthPoints(int healthPoints) {
        this.healthPoints = healthPoints;
    }

    public float getStoredEnergyPoints() {
        return storedEnergyPoints;
    }

    public void setStoredEnergyPoints(float storedEnergyPoints) {
        this.storedEnergyPoints = storedEnergyPoints;
    }

    public int getFatStorage() {
        return fatStorage;
    }

    public void setFatStorage(int fatStorage) {
        this.fatStorage = fatStorage;
    }

    public int getMaxHealth() {
        return maxHealth;
    }
    public void reduceMaxHealth(int maxHealth){
        maxHealth=(int) (maxHealth*0.9);
    }

    public float getPhysRes() {
        return physRes;
    }

    public void setPhysRes(float physRes) {
        this.physRes = physRes;
    }

    public int getCurrStealth() {
        return currStealth;
    }

    public HealthStatus getHealthStatus() {
        return healthStatus;
    }

    public void setCurrStealth(int currStealth) {
        this.currStealth = currStealth;
    }

    public int getCurrPerception() {
        return currPerception;
    }

    public void setCurrPerception(int currPerception) {
        this.currPerception = currPerception;
    }

    public int getSize() {
        return size;
    }

    public int getAgility() {
        return agility;
    }
    public int getConstitution() {return constitution;}
    public int getStrength() {
        return strength;
    }

    public int getSpeed() {
        return speed;
    }

    public int getStealth() {
        return stealth;
    }

    public int getPerception() {
        return perception;
    }

    public boolean isFemale() {
        return female;
    }

    public boolean isOutOfActions() {
        return outOfActions;
    }

    public int getCurrAge() {
        return currAge;
    }

    public void setCurrAge(int currAge) {
        this.currAge = currAge;
    }

    public int getMaxAge(){
        return maxAge;
    };
    public void resetMP() { this.currentMovementPoints = this.maxMovementPoints; }
    public float calculateMPCost(Terrain terrain) {
        switch (terrain) {
            case PLAIN: return 1.0f; case HILL: return 1.5f;
            case MOUNTAIN: return 2.0f; case WATER: return 1.75f;
            default: return Float.MAX_VALUE;
        }
    }

    public float getCurrentMovementPoints() {
        return currentMovementPoints;
    }

    public void setCurrentMovementPoints(int currentMovementPoints) {
        this.currentMovementPoints = currentMovementPoints;
    }
    public float getMaxMovementPoints(){return maxMovementPoints;}

    public void setMaxMovementPoints(int speed) {
        this.maxMovementPoints = speed/2F;
    }

    public boolean isDead() {
        return dead;
    }

    public LifeStage getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(LifeStage lifeStage) {
        this.lifeStage = lifeStage;
    }

    public boolean isReadyForReproduction() {
        return readyForReproduction;
    }

    public void setReadyForReproduction(boolean readyForReproduction) {
        this.readyForReproduction = readyForReproduction;
    }

    public MapStructure.Cell getCell() {
        return cell;
    }

    public void setCell(MapStructure.Cell cell) {
        this.cell = cell;
    }

    public int getAttackAdv() {
        return attackAdv;
    }

    public void setAttackAdv(int attackAdv) {
        this.attackAdv = attackAdv;
    }

    public int getStealthAdv() {
        return stealthAdv;
    }

    public void setStealthAdv(int stealthAdv) {
        this.stealthAdv = stealthAdv;
    }

    public int getPerceptionAdv() {
        return perceptionAdv;
    }

    public void setPerceptionAdv(int perceptionAdv) {
        this.perceptionAdv = perceptionAdv;
    }

    public int getEvasionAdv() {
        return evasionAdv;
    }

    public void setEvasionAdv(int evasionAdv) {
        this.evasionAdv = evasionAdv;
    }

    public int getMinDamage() {
        return minDamage;
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public List<Tags> getTags() {
        return tags;
    }

    public VerticalPosition getVerticalPosition() {
        return verticalPosition;
    }

    public void setVerticalPosition(VerticalPosition verticalPosition) {
        this.verticalPosition = verticalPosition;
    }

    // Core methods
    protected void death() {
        this.dead = this.healthPoints <= 0;
    }
    protected Animal(MapStructure.Cell initialCell,
                     // Core Survival Stats
                     int initialMaxHealth,
                     int initialMaxAge,
                     int initialSize,
                     // Physical Base Stats
                     int initialAgility,
                     int initialConstitution,
                     int initialStrength,
                     int initialSpeed,
                     // Perception/Stealth Base Stats
                     int initialPerception,
                     int initialStealth,
                     // Combat Base Stats
                     int initialMinDamage,
                     float initialPhysRes,
                     int initialAttackAdv,
                     int initialStealthAdv,
                     int initialPerceptionAdv,
                     int initialEvasionAdv,
                     int initialBattleActionPoints,
                     int initialReactionPoints,
                     // Resource Stats
                     float initialEnergy,
                     int initialFat
            /* Add other stats if they aren't derived/default */) {

        super(); // Calls Organism() constructor to set the unique ID first

        // --- Validate Essential Parameters ---
        if (initialCell == null) {
            String errorMsg = "Initial cell cannot be null for Animal ID " + this.getId();
            logger.log(Level.SEVERE, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (initialSpeed <= 0 || initialMaxHealth <= 0 || initialSize <= 0 || initialMaxAge <= 0) {
            String errorMsg = "Initial speed, maxHealth, size, and maxAge must be positive for Animal ID " + this.getId();
            logger.log(Level.SEVERE, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // --- Assign Core Stats from Parameters ---
        this.cell = initialCell;
        this.maxHealth = initialMaxHealth;
        this.maxAge = initialMaxAge;
        this.size = initialSize;
        this.agility = initialAgility;
        this.constitution = initialConstitution;
        this.strength = initialStrength;
        this.speed = initialSpeed;
        this.perception = initialPerception;
        this.stealth = initialStealth;
        this.minDamage = initialMinDamage;
        this.physRes = initialPhysRes;
        this.attackAdv = initialAttackAdv;
        this.stealthAdv = initialStealthAdv;
        this.perceptionAdv = initialPerceptionAdv;
        this.evasionAdv = initialEvasionAdv;
        this.battleActionPoints = initialBattleActionPoints;
        this.reactionPoints = initialReactionPoints;
        this.storedEnergyPoints = initialEnergy;
        this.fatStorage = initialFat;
        // All parameters from signature are now assigned above.

        // --- Initialize Dependent & Default States ---
        this.healthPoints = this.maxHealth; // Start at full health
        setMaxMovementPoints(this.speed); // Initialize max MP based on speed
        resetMP(); // Initialize current MP to max
        this.currPerception = this.perception; // Current perception starts at base
        this.currStealth = this.stealth; // Current stealth starts at base
        this.dead = false;
        this.outOfActions = false;
        this.actionPoints = 4; // Reset initial AP
        this.currAge = 0;
        this.lifeStage = LifeStage.JUVENILE; // Default starting lifestage
        this.healthStatus = HealthStatus.HEALTHY; // Default starting health status
        this.female = new Random().nextBoolean(); // Example: Randomize gender
        this.readyForReproduction = false;
        this.verticalPosition = VerticalPosition.ON_LAND; // Example default position
        this.reverseDamage = 0f; // Default

        // Initialize Lists to avoid NullPointerExceptions later
        this.conditions = new ArrayList<>();
        this.battleConditions = new ArrayList<>();

        // --- Log Creation ---
        // Added more stats to the log message
        logger.log(Level.CONFIG,
                "Animal {0} ({1}) initialized. Cell:[{2},{3}], Spd:{4}, MaxHP:{5}, Str:{6}, Agi:{7}, Con:{8}, Per:{9}, Stl:{10}, Size:{11}",
                new Object[]{this.getId(), this.getClass().getSimpleName(),
                        this.cell.getX(), this.cell.getY(),
                        this.speed, this.maxHealth, this.strength, this.agility, this.constitution,
                        this.perception, this.stealth, this.size});

        // --- IMPORTANT: Add self to the starting cell's list ---
        this.cell.addAnimal(this);
    }
}



