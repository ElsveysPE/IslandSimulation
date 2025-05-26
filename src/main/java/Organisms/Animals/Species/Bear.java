package Organisms.Animals.Species;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Foraging.Scavenger;
import Organisms.Animals.Behaviours.Foraging.StrongPredator;
import Organisms.Animals.Behaviours.Miscellaneous.Territorial;
import Organisms.Animals.Behaviours.Nutrition.Carnivore;
import Organisms.Animals.Behaviours.Nutrition.Herbivore;
import Organisms.Animals.Behaviours.Nutrition.Omnivore;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Tags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class Bear extends Animal implements Territorial, Omnivore, StrongPredator, Scavenger {
    private static final int BEAR_MAX_AGE = 1200;
    private static final int BEAR_INITIAL_CURR_AGE = 240;


    public Bear(MapStructure.Cell initialCell,
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
                        Tags.CARNY, Tags.HERBY, Tags.MATUTINAL, Tags.TERRITORIAL,
                        Tags.FIGHT, Tags.SCAVENGER
                )),
                BEAR_MAX_AGE,
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

        this.setCurrAge(BEAR_INITIAL_CURR_AGE);
        this.setMovementCost(Terrain.PLAIN, 1.0f);
        this.setMovementCost(Terrain.HILL, 1.75f);
        this.setMovementCost(Terrain.WATER, 1.25f);
        this.setMovementCost(Terrain.MOUNTAIN, 2.25f);

        Animal.logger.log(Level.CONFIG, "Bear instance created: ID {0}, Age: {1}/{2}, Cell: [{3},{4}]",
                new Object[]{this.getId(), this.getCurrAge(), this.getMaxAge(), initialCell.getX(), initialCell.getY()});
    }

    // --- Method Overrides to resolve interface conflicts ---
    public void performAttack(Animal defender) {
        this.attack(this, defender);
    }

    @Override
    public void attack(Animal attacker, Animal defender) {
        if (!attacker.equals(this)) {
            Animal.logger.log(Level.WARNING, "Bear.attack() called with an attacker (" + attacker.getId() + ") that is not this Bear instance (" + this.getId() + ").");
        }
        StrongPredator.super.attack(attacker, defender);
    }

    public void performEatDead(Corpse corpse) {
        this.eatDead(this, corpse);
    }

    @Override
    public void eatDead(Animal animal, Corpse corpse) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Bear.eatDead() called with an animal (" + animal.getId() + ") that is not this Bear instance (" + this.getId() + ").");
        }
        Scavenger.super.eatDead(animal, corpse);
    }
    @Override
    public void eatPlant(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Rabbit.eatPlant() called with an animal (" + animal.getId() + ") that is not this Rabbit instance (" + this.getId() + ").");
        }
        Omnivore.super.eatPlant(animal);
    }
    @Override
    public void eatAlive(Animal animal, Animal prey) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Wolf.eatAlive() called with an animal (" + animal.getId() + ") that is not this wolf instance (" + this.getId() + ").");
        }
        Omnivore.super.eatAlive(animal, prey);
    }


    // --- Public methods to expose inherited default behaviors from interfaces ---

    // Territorial behaviors
    public void performExpandTerritory(MapStructure.Cell cell) {
        Territorial.super.expandTerritory(this, cell);
    }

    public void performShrinkTerritory(MapStructure.Cell cell) {
        Territorial.super.shrinkTerritory(this, cell);
    }

    // StrongPredator behaviors
    public void performGrapple(Animal prey) {
        StrongPredator.super.grapple(this, prey);
    }

    public void performGetDown(Animal prey) {
        StrongPredator.super.getDown(this, prey);
    }

    public void performHeadStrike(Animal prey) {
        StrongPredator.super.headStrike(this, prey);
    }

    public void reactGetBackHere(Animal preyTryingToFlee) {
        StrongPredator.super.youAreStaying(this, preyTryingToFlee);
    }

    public void performHide() {
        StrongPredator.super.hide(this);
    }

    public void performGoReckless() {
        StrongPredator.super.goReckless(this);
    }

    public void performResting() {
        StrongPredator.super.resting(this);
    }

    public void performDeepResting() {
        StrongPredator.super.deepResting(this);
    }

    public void performDisengage() {
        StrongPredator.super.disengage(this);
    }
    public void performEatAlive(Animal prey) {
        Omnivore.super.eatAlive(this, prey);
    }

    public void performEatPlant() {
        Omnivore.super.eatPlant(this);
    }
}