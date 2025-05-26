package DTO;

import Organisms.Animals.Animal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class AnimalState {
    // Identification & Type
    public long id;             // Unique identifier from Animal.getId()
    public String speciesType;  // From animal.getClass().getSimpleName()
    public boolean isFemale;

    // Position (can be main map or battle map coordinates)
    public int x;
    public int y;

    // Core Stats & Status
    public int healthPoints;
    public int maxHealth;
    public String healthStatus; // e.g., "HEALTHY", "INJURED"
    public boolean isDead;

    // Action & Movement Points (World)
    public int actionPoints; // Current world AP
    public float currentMovementPoints;
    public float maxMovementPoints;

    // Battle-Specific Stats (can be populated from Animal or Battle context)
    public String currentBattleAction;
    public String currentAction;
    // Conditions & States
    public List<String> conditions;    // e.g., "HUNGRY", "FATIGUED", "PANICKING"
    public List<String> battleConditions;   // e.g., "RECKLESS", "RETREATING", "PRONE"
    public boolean hasEatenThisTick;
    public int deepRestPoints;

    // Key Attributes for Display (add more as needed)
    public int size;
    public int agility;
    public int constitution;
    public int strength;
    public int speed;
    public int perception; // Base perception
    public int stealth;    // Base stealth
    public int currentStealthValue; // Effective stealth if different from base
    public float physicalResistance;

    // Grouping/Social
    public List<Long> group;

    // Constructor
    public AnimalState(long id, String speciesType, boolean isFemale,
                       int x, int y,
                       int healthPoints, int maxHealth, String healthStatus, boolean isDead,
                       int actionPoints, float currentMovementPoints, float maxMovementPoints,
                        String currentBattleAction, String currentAction,
                       List<String> Conditions, List<String> battleConditions,
                       boolean hasEatenThisTick, int deepRestPoints,
                       int size, int agility, int constitution, int strength, int speed,
                       int perception, int stealth, int currentStealthValue, float physicalResistance, List<Long> group) {
        this.id = id;
        this.speciesType = speciesType;
        this.isFemale = isFemale;
        this.x = x;
        this.y = y;
        this.healthPoints = healthPoints;
        this.maxHealth = maxHealth;
        this.healthStatus = healthStatus;
        this.isDead = isDead;
        this.actionPoints = actionPoints;
        this.currentMovementPoints = currentMovementPoints;
        this.maxMovementPoints = maxMovementPoints;
        this.currentBattleAction = currentBattleAction;
        this.currentAction = currentAction;
        this.conditions = new ArrayList<>(Conditions);
        this.battleConditions = new ArrayList<>(battleConditions);
        this.hasEatenThisTick = hasEatenThisTick;
        this.deepRestPoints = deepRestPoints;
        this.size = size;
        this.agility = agility;
        this.constitution = constitution;
        this.strength = strength;
        this.speed = speed;
        this.perception = perception;
        this.stealth = stealth;
        this.currentStealthValue = currentStealthValue;
        this.physicalResistance = physicalResistance;
        this.group = group;
    }

    public AnimalState() { // Default constructor for deserialization
        this.conditions = new ArrayList<>();
        this.battleConditions = new ArrayList<>();
    }

    // Utility to populate from your Animal object
    // Call this where you create AnimalState instances
    public static AnimalState fromAnimal(Animal animal, boolean inBattle) {
        if (animal == null) return null;

        List<String> conds = animal.conditions != null ?
                animal.conditions.stream().map(Enum::name).collect(Collectors.toList()) :
                new ArrayList<>();
        List<String> battleConds = animal.battleConditions != null ?
                animal.battleConditions.stream().map(Enum::name).collect(Collectors.toList()) :
                new ArrayList<>();


        int currentX = inBattle && animal.getBattleCell() != null ? animal.getBattleCell().getX() : (animal.getCell() != null ? animal.getCell().getX() : -1);
        int currentY = inBattle && animal.getBattleCell() != null ? animal.getBattleCell().getY() : (animal.getCell() != null ? animal.getCell().getY() : -1);List<Long> groupIds = new ArrayList<>(); // Ініціалізуємо порожнім списком одразу
        if (animal.getGroup() != null && !animal.getGroup().isEmpty()) { // Перевіряємо, що група не null і не порожня
            for (Animal groupMember : animal.getGroup()) {
                if (groupMember != null) { // Додаткова перевірка, якщо член групи може бути null
                    groupIds.add(groupMember.getId());
                }
            }
        }
        new ArrayList<>();
        return new AnimalState(
                animal.getId(),
                animal.getClass().getSimpleName(),
                animal.isFemale(),
                currentX,
                currentY,
                animal.getHealthPoints(),
                animal.getMaxHealth(),
                animal.getHealthStatus().name(),
                animal.isDead(),
                animal.actionPoints, // World AP
                animal.getCurrentMovementPoints(),
                animal.getMaxMovementPoints(),
                animal.getBattleAction() != null ? animal.getBattleAction().name() : (inBattle ? "PENDING" : "N/A"),
                animal.getAction() != null ? animal.getAction().name() : (inBattle ? "PENDING" : "N/A"),
                conds,
                battleConds,
                animal.hasEatenThisTick(),
                animal.getDeepRestPoints(),
                animal.getSize(),
                animal.getAgility(),
                animal.getConstitution(),
                animal.getStrength(),
                animal.getSpeed(),
                animal.getPerception(), // Base perception
                animal.getStealth(),    // Base stealth
                animal.getCurrStealth(),// Current effective stealth
                animal.getPhysRes(),
                groupIds
        );
    }

    @Override
    public String toString() {
        return speciesType + "{" + "id=" + id + ", x=" + x + ", y=" + y + ", hp=" + healthPoints + "/" + maxHealth + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnimalState that = (AnimalState) o;
        return id == that.id; // ID is the primary key for equality
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
