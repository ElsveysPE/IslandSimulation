package Organisms.Animals.Species; // Example package

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Basic;
import Organisms.Animals.Behaviours.Foraging.Scavenger;
import Organisms.Animals.Behaviours.Foraging.StrongPredator;
import Organisms.Animals.Behaviours.Miscellaneous.Social;
import Organisms.Animals.Behaviours.Nutrition.Carnivore;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Tags;
// Import other necessary classes/enums if needed by constructor signature (unlikely here)
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class Wolf extends Animal implements Social, Scavenger, StrongPredator, Carnivore {
    private static final int WOLF_MAX_AGE = 1200;
    private static final int WOLF_INITIAL_CURR_AGE = 240;
    public Wolf(MapStructure.Cell initialCell,
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
                        Tags.CARNY, Tags.SCAVENGER, Tags.FIGHT,
                        Tags.SOCIAL, Tags.CARING, Tags.MATUTINAL
                )),
                WOLF_MAX_AGE,
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

        this.setCurrAge(WOLF_INITIAL_CURR_AGE);

        this.setMovementCost(Terrain.PLAIN, 1.0f);
        this.setMovementCost(Terrain.HILL, 1.5f);
        this.setMovementCost(Terrain.WATER, 1.25f);
        this.setMovementCost(Terrain.MOUNTAIN, 2.0f);

        Animal.logger.log(Level.CONFIG, "Wolf instance created: ID {0}, Age: {1}/{2}, Cell: [{3},{4}]",
                new Object[]{this.getId(), this.getCurrAge(), this.getMaxAge(), initialCell.getX(), initialCell.getY()});
    }

    // --- Method Overrides to resolve interface conflicts ---

    public void performAttack(Animal defender) {
        this.attack(this, defender);
    }


    @Override
    public void attack(Animal attacker, Animal defender) {
        if (!attacker.equals(this)) {
            Animal.logger.log(Level.WARNING, "Wolf.attack() called with an attacker (" + attacker.getId() + ") that is not this wolf instance (" + this.getId() + ").");
        }
        StrongPredator.super.attack(attacker, defender);
    }


    public void performEatDead(Corpse corpse) {
        this.eatDead(this, corpse);
    }

    @Override
    public void eatDead(Animal animal, Corpse corpse) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Wolf.eatDead() called with an animal (" + animal.getId() + ") that is not this wolf instance (" + this.getId() + ").");
        }

        Scavenger.super.eatDead(animal, corpse);
    }


    public void applyPackTacticsBonus() {
        Social.super.packTactics(this);
    }

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
        StrongPredator.super.youAreStaying(this, preyTryingToFlee); // Calls youAreStaying from StrongPredator
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


    @Override
    public void hide(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Wolf.hide() called with an animal (" + animal.getId() + ") that is not this wolf instance (" + this.getId() + ").");
        }
        StrongPredator.super.hide(animal);
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
}
