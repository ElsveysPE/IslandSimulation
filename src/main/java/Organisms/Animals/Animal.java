package Organisms.Animals;

import BattleManagement.BattleCell;
import BattleManagement.possibleBattleActions;
import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Behaviours.BattleConditions.BattleConditions;
import Organisms.Animals.Conditions.Conditions;
import Organisms.Animals.Management.possibleActions;
import Organisms.HealthStatus;
import Organisms.Organism;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Organisms.Animals.Management.RecountCR.recountCR;

public abstract class Animal extends Organism {
    protected static final Logger logger = Logger.getLogger(Animal.class.getName());

    // --- Constants for Max Health Calculation (can be moved to a config/subclass if they vary) ---
    private static final int CONSTITUTION_HP_MULTIPLIER = 5;
    private static final int SIZE_HP_MULTIPLIER = 10;
    private static final int SPEED_MP_DIVISOR = 2;

    // Core attributes
    private float storedEnergyPoints;
    private int fatStorage;
    private int healthPoints;
    private int maxHealth;
    public int actionPoints = 4;
    public boolean outOfActions = false;
    private boolean dead = false;
    private MapStructure.Cell cell;
    private boolean female;
    private HealthStatus healthStatus;
    private final HashSet<Tags> tags; // Made final, initialized in constructor
    private int deepRestPoints;

    // Lifecycle attributes
    private int currAge = 0;
    private int maxAge;
    private LifeStage lifeStage;
    private boolean readyForReproduction = false;

    // Movement
    // canFly and swimmer are still final false here. Subclasses can define their own
    // movement capabilities, potentially by overriding methods or using specific components.
    private final boolean canFly = false;
    private final boolean swimmer = false;
    private float maxMovementPoints;
    private float currentMovementPoints;
    private final HashMap<Terrain, Float> movementCost;

    // Physical attributes (stats that LevelUp.increaseAttribute might target via reflection)
    private int size;
    private int agility;
    private int constitution;
    private int strength;
    private int speed;

    // Position
    private VerticalPosition verticalPosition;

    // Defense and perception attributes
    private float physRes;
    public float reverseDamage = 0f;
    private int currStealth;
    private int currPerception;
    private int stealth; // Base stat
    private int perception; // Base stat

    // Combat attributes
    public int battleActionPoints;
    private int attackAdv;
    private int stealthAdv;
    private int perceptionAdv;
    private int evasionAdv;
    private int minDamage;
    private BattleManagement.BattleCell battleCell;

    public HashSet<Conditions> conditions;
    public HashSet<BattleConditions> battleConditions;
    public int reactionPoints = 1;
    private float basicCR;
    private float dynamicCR;
    private possibleBattleActions battleAction;
    private possibleActions action;

    // Attributes for specific animal behaviors
    private HashSet<MapStructure.Cell> territories;
    private MapStructure.Cell lastKnownProgenyLocation;
    private HashSet<Animal> children;
    private HashSet<Animal> group;


    protected Animal(MapStructure.Cell initialCell,
                     HashSet<Tags> initialTags, // Added initialTags
                     // Lifecycle
                     int initialMaxAge,
                     // Physical Base Stats
                     int initialSize,
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
                     float initialStoredEnergy,
                     int initialFatStorage) {
        super();

        if (initialCell == null) {
            String errorMsg = "Initial cell cannot be null for Animal ID " + this.getId();
            logger.log(Level.SEVERE, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
        if (initialTags == null) {
            String errorMsg = "Initial tags cannot be null for Animal ID " + this.getId();
            logger.log(Level.SEVERE, errorMsg);
            throw new IllegalArgumentException(errorMsg); // Tags set is mandatory
        }
        if (initialSpeed <= 0 || initialSize <= 0 || initialMaxAge <= 0 || initialConstitution <= 0) {
            String errorMsg = "Initial speed, size, maxAge, and constitution must be positive for Animal ID " + this.getId();
            logger.log(Level.SEVERE, errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        this.tags = initialTags; // Assign final tags
        this.cell = initialCell;
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
        this.storedEnergyPoints = initialStoredEnergy;
        this.fatStorage = initialFatStorage;

        this.maxHealth = calculateInitialMaxHealth(initialConstitution, initialSize);
        this.healthPoints = this.maxHealth;

        setMaxMovementPoints(calculateInitialMaxMovementPoints(this.speed));
        resetMP();

        this.currPerception = this.perception;
        this.currStealth = this.stealth;
        this.dead = false;
        this.outOfActions = false;
        this.actionPoints = 4;
        this.currAge = 0;
        this.lifeStage = LifeStage.ADULT;
        this.healthStatus = HealthStatus.HEALTHY;
        this.female = new Random().nextBoolean();
        this.readyForReproduction = false;
        this.verticalPosition = VerticalPosition.ON_LAND;
        this.reverseDamage = 0f;
        this.deepRestPoints = 0;

        this.movementCost = new HashMap<>(); // Populated by subclasses
        this.conditions = new HashSet<>();
        this.battleConditions = new HashSet<>();
        this.territories = new HashSet<>();
        this.children = new HashSet<>();
        this.group = new HashSet<>();

        this.action = null;
        this.battleAction = null;
        this.battleCell = null;

        calculateCombatRatings(); // Call to update CR using external utility

        logger.log(Level.CONFIG,
                "Animal {0} ({1}) initialized. Cell:[{2},{3}], Spd:{4}, MaxHP:{5}, Str:{6}, Agi:{7}, Con:{8}, Per:{9}, Stl:{10}, Size:{11}, BasicCR:{12}, DynamicCR:{13}, Tags:{14}",
                new Object[]{this.getId(), this.getClass().getSimpleName(),
                        this.cell.getX(), this.cell.getY(),
                        this.speed, this.maxHealth, this.strength, this.agility, this.constitution,
                        this.perception, this.stealth, this.size,
                        String.format("%.2f", this.basicCR), String.format("%.2f", this.dynamicCR),
                        this.tags});

        this.cell.addAnimal(this);
    }

    private int calculateInitialMaxHealth(int con, int siz) {
        return Math.max(1, (con * CONSTITUTION_HP_MULTIPLIER) + (siz * SIZE_HP_MULTIPLIER));
    }
    private int calculateInitialMaxMovementPoints(int speed){
        return Math.max(1, (speed/SPEED_MP_DIVISOR));
    }

    /**
     * Triggers recalculation of Combat Ratings (Basic and Dynamic).
     * This method now delegates to an external utility.
     */
    protected void calculateCombatRatings() {
        recountCR(this);
    }

    // Internal setters for CR, to be used by CombatRatingUtil to avoid recursive calls from public setters
    public void setBasicCR(float basicCR) {
        this.basicCR = basicCR;
    }
    public void setDynamicCR(float dynamicCR) {
        this.dynamicCR = dynamicCR;
    }

    // Getters and Setters
    public int getHealthPoints() { return healthPoints; }
    public void setHealthPoints(int healthPoints) {
        this.healthPoints = Math.max(0, Math.min(healthPoints, this.maxHealth));
        death();
        calculateCombatRatings(); // Update CR
    }

    public float getStoredEnergyPoints() { return storedEnergyPoints; }
    public void setStoredEnergyPoints(float storedEnergyPoints) { this.storedEnergyPoints = Math.max(0, storedEnergyPoints); }

    public int getFatStorage() { return fatStorage; }
    public void setFatStorage(int fatStorage) { this.fatStorage = Math.max(0, fatStorage); }

    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int newMaxHealth) {
        this.maxHealth = Math.max(1, newMaxHealth);
        if (this.healthPoints > this.maxHealth) {
            this.healthPoints = this.maxHealth;
        }
        calculateCombatRatings(); // Update CR
    }

    public void applyMaxHealthDegradationFactor(float reductionFactor) {
        if (reductionFactor < 0 || reductionFactor > 1) {
            logger.log(Level.WARNING, "Invalid reduction factor for max health: " + reductionFactor + ". Must be between 0 and 1.");
            return;
        }
        int oldMaxHealth = this.maxHealth;
        this.maxHealth = Math.max(1, (int) (this.maxHealth * reductionFactor));
        if (this.healthPoints > this.maxHealth) {
            this.healthPoints = this.maxHealth;
        }
        logger.log(Level.INFO, "Animal " + getId() + " max health degraded from " + oldMaxHealth + " to " + this.maxHealth);
        calculateCombatRatings(); // Update CR
        death();
    }

    public float getPhysRes() { return physRes; }
    public void setPhysRes(float physRes) {
        this.physRes = physRes;
        calculateCombatRatings();
    }

    public int getCurrStealth() { return currStealth; }
    public void setCurrStealth(int currStealth) { this.currStealth = currStealth; }

    public HealthStatus getHealthStatus() { return healthStatus; }
    public void setHealthStatus(HealthStatus healthStatus) { this.healthStatus = healthStatus; }

    public int getCurrPerception() { return currPerception; }
    public void setCurrPerception(int currPerception) { this.currPerception = currPerception; }

    public int getSize() { return size; }

    public int getAgility() { return agility; }
    public int getConstitution() { return constitution; }
    public int getStrength() { return strength; }
    public int getSpeed() { return speed; }
    public int getStealth() { return stealth; }
    public int getPerception() { return perception; }

    public boolean isFemale() { return female; }
    public boolean isOutOfActions() { return outOfActions; }

    public int getCurrAge() { return currAge; }
    public void setCurrAge(int currAge) { this.currAge = currAge; }

    public int getMaxAge() { return maxAge; }

    public void resetMP() { this.currentMovementPoints = this.maxMovementPoints; }

    /**
     * Gets the movement cost for a given terrain from the animal's specific movementCost map.
     * If the terrain is not in the map, it's considered impassable or very high cost.
     * @param terrain The terrain type.
     * @return The movement cost, or Float.MAX_VALUE if not defined for the terrain.
     */
    public float getMovementCost(Terrain terrain) {
        return movementCost.getOrDefault(terrain, Float.MAX_VALUE);
    }
    // Method to allow subclasses to define movement costs
    protected void setMovementCost(Terrain terrain, float cost) {
        this.movementCost.put(terrain, cost);
    }


    public float getCurrentMovementPoints() { return currentMovementPoints; }
    public void setCurrentMovementPoints(float currentMovementPoints) { this.currentMovementPoints = currentMovementPoints; }

    public float getMaxMovementPoints() { return maxMovementPoints; }
    public void setMaxMovementPoints(int currentSpeed) { // Parameter changed to currentSpeed for clarity
        this.maxMovementPoints = Math.max(1.0f, currentSpeed / 2.0F);
    }

    public boolean isDead() { return dead; }
    protected void death() {
        boolean previouslyDead = this.dead;
        this.dead = this.healthPoints <= 0;
        if (this.dead && !previouslyDead) {
            logger.log(Level.INFO, "Animal " + getId() + " (" + this.getClass().getSimpleName() + ") has died.");
            this.actionPoints = 0;
            this.currentMovementPoints = 0;
        }
    }

    public LifeStage getLifeStage() { return lifeStage; }
    public void setLifeStage(LifeStage lifeStage) { this.lifeStage = lifeStage; }

    public boolean isReadyForReproduction() { return readyForReproduction; }
    public void setReadyForReproduction(boolean readyForReproduction) { this.readyForReproduction = readyForReproduction; }

    public MapStructure.Cell getCell() { return cell; }
    public void setCell(MapStructure.Cell newCell) {
        if (this.cell != null && this.cell != newCell) {
            this.cell.removeAnimal(this);
        }
        this.cell = newCell;
        if (this.cell != null) {
            this.cell.addAnimal(this);
        }
    }

    public int getAttackAdv() { return attackAdv; }
    public void setAttackAdv(int attackAdv) { this.attackAdv = attackAdv; }

    public int getStealthAdv() { return stealthAdv; }
    public void setStealthAdv(int stealthAdv) { this.stealthAdv = stealthAdv; }

    public int getPerceptionAdv() { return perceptionAdv; }
    public void setPerceptionAdv(int perceptionAdv) { this.perceptionAdv = perceptionAdv; }

    public int getEvasionAdv() { return evasionAdv; }
    public void setEvasionAdv(int evasionAdv) { this.evasionAdv = evasionAdv; }

    public int getMinDamage() { return minDamage; }

    public HashSet<Tags> getTags() { return tags; } // Getter for final field

    public VerticalPosition getVerticalPosition() { return verticalPosition; }
    public void setVerticalPosition(VerticalPosition verticalPosition) { this.verticalPosition = verticalPosition; }


    public HashSet<MapStructure.Cell> getTerritories() { return territories; }
    public void setTerritories(HashSet<MapStructure.Cell> territories) { this.territories = territories; }

    public MapStructure.Cell getLastKnownProgenyLocation() { return lastKnownProgenyLocation; }
    public void setLastKnownProgenyLocation(MapStructure.Cell lastKnownProgenyLocation) { this.lastKnownProgenyLocation = lastKnownProgenyLocation; }

    public HashSet<Animal> getChildren() { return children; }
    public void setChildren(HashSet<Animal> children) { this.children = children; } // Typically managed internally

    public HashSet<Animal> getGroup() { return group; }
    // No public setter for group, assumed to be managed by social AI / group mechanics

    public float getBasicCR() { return basicCR; }
    public float getDynamicCR() { return dynamicCR; }

    public int getDeepRestPoints() { return deepRestPoints; }
    public void setDeepRestPoints(int deepRestPoints) { this.deepRestPoints = deepRestPoints; }

    public BattleManagement.BattleCell getBattleCell() { return battleCell; }
    public void setBattleCell(BattleManagement.BattleCell battleCell) { this.battleCell = battleCell; }

    public possibleBattleActions getBattleAction() { return battleAction; }
    public void setBattleAction(possibleBattleActions battleAction) { this.battleAction = battleAction; }

    public possibleActions getAction() { return action; }
    public void setAction(possibleActions action) { this.action = action; }
    private boolean hasEatenThisTick = false;

    // ... your existing constructor and methods ...

    public boolean hasEatenThisTick() {
        return this.hasEatenThisTick;
    }

    public void setHasEatenThisTick(boolean hasEaten) {
        this.hasEatenThisTick = hasEaten;
    }

    // Call this method for every animal at the VERY START of each new tick in SimManager
    public void resetPerTickFlags() {
        this.hasEatenThisTick = false;
        // also call this.resetMP(); here if that's also a per-tick reset
        // and any other flags that need resetting each tick.
    }

}

