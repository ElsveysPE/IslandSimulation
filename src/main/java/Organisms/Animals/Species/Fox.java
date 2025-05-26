package Organisms.Animals.Species; // Assumed package

import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Defensive.HidingPrey;
import Organisms.Animals.Behaviours.Foraging.AgilePredator;
import Organisms.Animals.Behaviours.Foraging.Scavenger;
import Organisms.Animals.Behaviours.Miscellaneous.Social;
import Organisms.Animals.Behaviours.Nutrition.Carnivore;
import Map.MapStructure;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Tags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class Fox extends Animal implements AgilePredator, Scavenger, Carnivore, Social, HidingPrey {

    // Species-specific constants for Fox
    private static final int FOX_MAX_AGE = 480;
    private static final int FOX_INITIAL_CURR_AGE = 120;

    public Fox(MapStructure.Cell initialCell,
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
                        Tags.NOCTURNAL, Tags.SOCIAL, Tags.CARNY, Tags.SCAVENGER, Tags.STEALTH
                )),
                FOX_MAX_AGE, // Use species-specific constant for maxAge
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

        this.setCurrAge(FOX_INITIAL_CURR_AGE);

        // Set Fox-specific movement costs
        this.setMovementCost(Terrain.PLAIN, 1.0f);
        this.setMovementCost(Terrain.HILL, 1.25f);
        this.setMovementCost(Terrain.WATER, 2.5f); // Foxes can swim but not their preference
        this.setMovementCost(Terrain.MOUNTAIN, 1.75f);

        Animal.logger.log(Level.CONFIG, "Fox instance created: ID {0}, Age: {1}/{2}, Cell: [{3},{4}]",
                   new Object[]{this.getId(), this.getCurrAge(), this.getMaxAge(), initialCell.getX(), initialCell.getY()});
    }

    // --- Method Overrides and Interface Method Usage ---

    // AgilePredator's attack is primary.
    @Override
    public void attack(Animal attacker, Animal defender) {
        if (!attacker.equals(this)) {
            Animal.logger.log(Level.WARNING, "Fox.attack() called with an attacker (" + attacker.getId() + ") that is not this Fox instance (" + this.getId() + ").");
        }
        AgilePredator.super.attack(attacker, defender);
    }

    // Scavenger's eatDead is primary for corpses.
    @Override
    public void eatDead(Animal animal, Corpse corpse) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Fox.eatDead() called with an animal (" + animal.getId() + ") that is not this Fox instance (" + this.getId() + ").");
        }
        Scavenger.super.eatDead(animal, corpse);
    }


    @Override
    public void hide(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Fox.hide() called with an animal (" + animal.getId() + ") that is not this Fox instance (" + this.getId() + ").");
        }
        HidingPrey.super.hide(animal);
    }

    // Carnivore methods (eatAlive, eatPlant) are inherited.
    // eatPlant will be highly unusual.
    @Override
    public void eatAlive(Animal animal, Animal prey) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Fox.eatAlive() called with an animal (" + animal.getId() + ") that is not this Fox instance (" + this.getId() + ").");
        }
        Carnivore.super.eatAlive(animal, prey);
    }

    @Override
    public void eatPlant(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Fox.eatPlant() called with an animal (" + animal.getId() + ") that is not this Fox instance (" + this.getId() + ").");
        }
        Animal.logger.log(Level.INFO, "Fox " + this.getId() + " is attempting to eat plants. This is highly unusual.");
        Carnivore.super.eatPlant(animal);
    }


    // --- Public methods to expose inherited default behaviors from interfaces ---

    // Social behaviors
    public void applyPackTacticsBonus() {
        Social.super.packTactics(this);
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

    // Basic behaviors (inherited via AgilePredator/HidingPrey)
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

    public void performFocusOnSurroundings() {
        HidingPrey.super.focusOnSurroundings(this);
    }

    public void performRun() {
        HidingPrey.super.run(this);
    }
}