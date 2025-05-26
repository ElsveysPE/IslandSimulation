package Organisms.Animals.Species; // Assumed package

import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Defensive.HidingPrey;
import Organisms.Animals.Behaviours.Nutrition.Herbivore;
import Map.MapStructure;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Tags;


import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class Iguana extends Animal implements HidingPrey, Herbivore {
    private static final int IGUANA_MAX_AGE = 900;
    private static final int IGUANA_INITIAL_CURR_AGE = 90;

    public Iguana(MapStructure.Cell initialCell,
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
                        Tags.DIURNAL, Tags.STEALTH, Tags.HERBY
                )),
                IGUANA_MAX_AGE, // Use species-specific constant for maxAge
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

        this.setCurrAge(IGUANA_INITIAL_CURR_AGE);

        // Set Iguana-specific movement costs
        this.setMovementCost(Terrain.PLAIN, 1.0f);
        this.setMovementCost(Terrain.HILL, 1.25f);
        this.setMovementCost(Terrain.WATER, 3.5f); // Not strong swimmers
        this.setMovementCost(Terrain.MOUNTAIN, 1.75f);

        Animal.logger.log(Level.CONFIG, "Iguana instance created: ID {0}, Age: {1}/{2}, Cell: [{3},{4}]",
                  new Object[]{this.getId(), this.getCurrAge(), this.getMaxAge(), initialCell.getX(), initialCell.getY()});
    }

    // --- Method Overrides and Interface Method Usage ---

    // HidingPrey extends Basic, so hide() is available.
    // This explicit override calls the HidingPrey's specific hide logic.
    @Override
    public void hide(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Iguana.hide() called with an animal (" + animal.getId() + ") that is not this Iguana instance (" + this.getId() + ").");
        }
        HidingPrey.super.hide(animal);
    }

    // Herbivore methods
    @Override
    public void eatPlant(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Iguana.eatPlant() called with an animal (" + animal.getId() + ") that is not this Iguana instance (" + this.getId() + ").");
        }
        Herbivore.super.eatPlant(animal);
    }

    @Override
    public void eatAlive(Animal animal, Animal prey) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Iguana.eatAlive() called with an animal (" + animal.getId() + ") that is not this Iguana instance (" + this.getId() + ").");
        }
        Animal.logger.log(Level.INFO, "Iguana " + this.getId() + " is attempting to 'eat alive' another animal. This is highly unusual for a herbivore.");
        Herbivore.super.eatAlive(animal, prey);
    }

    @Override
    public void eatDead(Animal animal, Corpse corpse) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Iguana.eatDead() called with an animal (" + animal.getId() + ") that is not this Iguana instance (" + this.getId() + ").");
        }
        Animal.logger.log(Level.INFO, "Iguana " + this.getId() + " is attempting to eat a corpse. This is highly unusual for a herbivore.");
        Herbivore.super.eatDead(animal, corpse);
    }


    // --- Public methods to expose inherited default behaviors from Basic (via HidingPrey) ---
    public void performAttack(Animal defender) {
        Animal.logger.log(Level.INFO, "Iguana " + this.getId() + " is attempting to attack defensively.");
        HidingPrey.super.attack(this, defender);
    }

    public void performGoReckless() {
        HidingPrey.super.goReckless(this);
    }

    public void performResting() {
        HidingPrey.super.resting(this);
    }

    public void performDeepResting() {
        HidingPrey.super.deepResting(this);
    }

    public void performDisengage() {
        HidingPrey.super.disengage(this);
    }

    public void performFocusOnSurroundings() {
        HidingPrey.super.focusOnSurroundings(this);
    }

    public void performRun() {
        HidingPrey.super.run(this);
    }
}