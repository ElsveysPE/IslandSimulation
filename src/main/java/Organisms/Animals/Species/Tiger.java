package Organisms.Animals.Species;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Foraging.AgilePredator;
import Organisms.Animals.Behaviours.Foraging.Scavenger;
import Organisms.Animals.Behaviours.Miscellaneous.Territorial;
import Organisms.Animals.Behaviours.Nutrition.Carnivore;
import Organisms.Animals.Behaviours.Nutrition.Herbivore;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Tags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class Tiger extends Animal implements AgilePredator, Carnivore, Territorial {
    private static final int TIGER_MAX_AGE = 1200;
    private static final int TIGER_INITIAL_CURR_AGE = 240;


    public Tiger(MapStructure.Cell initialCell,
                 int initialSize,
                 int initialAgility,
                 int initialConstitution,
                 int initialStrength,
                 int initialSpeed,
                 int initialPerception,
                 int initialStealth,
                 int initialMinDamage,
                 float initialPhysRes,
                 int initialAttackAdv,
                 int initialStealthAdv,
                 int initialPerceptionAdv,
                 int initialEvasionAdv,
                 int initialBattleActionPoints,
                 int initialReactionPoints,
                 float initialStoredEnergy,
                 int initialFatStorage) {

        super(initialCell,
                new HashSet<>(Arrays.asList(
                        Tags.FIGHT, Tags.MATUTINAL, Tags.TERRITORIAL, Tags.CARNY, Tags.STEALTH
                )),
                TIGER_MAX_AGE, // Use species-specific constant for maxAge
                initialSize,
                initialAgility,
                initialConstitution,
                initialStrength,
                initialSpeed,
                initialPerception,
                initialStealth,
                initialMinDamage,
                initialPhysRes,
                initialAttackAdv,
                initialStealthAdv,
                initialPerceptionAdv,
                initialEvasionAdv,
                initialBattleActionPoints,
                initialReactionPoints,
                initialStoredEnergy,
                initialFatStorage);

        this.setCurrAge(TIGER_INITIAL_CURR_AGE);
        this.setMovementCost(Terrain.PLAIN, 1.0f);
        this.setMovementCost(Terrain.HILL, 1.5f);
        this.setMovementCost(Terrain.WATER, 1.25f);
        this.setMovementCost(Terrain.MOUNTAIN, 2.25f);

        Animal.logger.log(Level.CONFIG, "Tiger instance created: ID {0}, Age: {1}/{2}, Cell: [{3},{4}]",
                new Object[]{this.getId(), this.getCurrAge(), this.getMaxAge(), initialCell.getX(), initialCell.getY()});
    }


    @Override
    public void attack(Animal attacker, Animal defender) {
        if (!attacker.equals(this)) {
            Animal.logger.log(Level.WARNING, "Tiger.attack() called with an attacker (" + attacker.getId() + ") that is not this Tiger instance (" + this.getId() + ").");
        }
        AgilePredator.super.attack(attacker, defender);
    }

    @Override
    public void eatDead(Animal animal, Corpse corpse) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Wolf.eatDead() called with an animal (" + animal.getId() + ") that is not this wolf instance (" + this.getId() + ").");
        }

        Carnivore.super.eatDead(animal, corpse);
    }
    @Override
    public void eatAlive(Animal animal, Animal prey) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Wolf.eatAlive() called with an animal (" + animal.getId() + ") that is not this wolf instance (" + this.getId() + ").");
        }
        Carnivore.super.eatAlive(animal, prey);
    }

    @Override
    public void eatPlant(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Wolf.eatPlant() called with an animal (" + animal.getId() + ") that is not this wolf instance (" + this.getId() + ").");
        }
        Animal.logger.log(Level.INFO, "Wolf " + this.getId() + " is attempting to eat plants. This is unusual for a carnivore.");
        Carnivore.super.eatPlant(animal);
    }

    // --- Public methods to expose inherited default behaviors from interfaces ---

    // Territorial behaviors
    public void performExpandTerritory(MapStructure.Cell cell) {
        Territorial.super.expandTerritory(this, cell);
    }

    public void performShrinkTerritory(MapStructure.Cell cell) {
        Territorial.super.shrinkTerritory(this, cell);
    }

    // AgilePredator behaviors
    public void performAttack(Animal defender) { // Convenience method
        this.attack(this, defender);
    }

    public void performGrapple(Animal prey) {
        AgilePredator.super.grapple(this, prey);
    }

    public void performGetDown(Animal prey) {
        AgilePredator.super.getDown(this, prey);
    }

    public void performArteryStrike(Animal prey) {
        AgilePredator.super.arteryStrike(this, prey);
    }

    public void reactGetBackHere(Animal preyTryingToFlee) {
        AgilePredator.super.getBackHere(this, preyTryingToFlee);
    }

    public void reactBugOff() {
        AgilePredator.super.bugOff(this);
    }

    // Carnivore behaviors (can be called directly or via these wrappers)
    public void performEatAlive(Animal prey) {
        Carnivore.super.eatAlive(this, prey);
    }

    public void performEatDead(Corpse corpse) {
        Carnivore.super.eatDead(this, corpse);
    }

    public void performEatPlant() {
         Animal.logger.log(Level.INFO, "Tiger " + this.getId() + " is attempting to eat plants.");
        Carnivore.super.eatPlant(this);
    }

    // Basic behaviors (inherited via AgilePredator)
    public void performHide() {
        AgilePredator.super.hide(this);
    }

    public void performGoReckless() {
        AgilePredator.super.goReckless(this);
    }

    public void performResting() {
        AgilePredator.super.resting(this);
    }

    public void performDeepResting() {
        AgilePredator.super.deepResting(this);
    }

    public void performDisengage() {
        AgilePredator.super.disengage(this);
    }
}
