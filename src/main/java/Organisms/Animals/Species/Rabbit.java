package Organisms.Animals.Species; // Example package

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Defensive.HidingPrey;
import Organisms.Animals.Behaviours.Nutrition.Herbivore;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Tags;
// Import other necessary classes/enums if needed by constructor signature (unlikely here)
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
public class Rabbit extends Animal implements HidingPrey, Herbivore {

    private static final int RABBIT_MAX_AGE = 300;
    private static final int RABBIT_INITIAL_CURR_AGE = 60;

    public Rabbit(MapStructure.Cell initialCell,
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
                        Tags.HERBY, Tags.STEALTH, Tags.DIURNAL
                )),
                RABBIT_MAX_AGE,
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

        this.setCurrAge(RABBIT_INITIAL_CURR_AGE);

        this.setMovementCost(Terrain.PLAIN, 1.0f);
        this.setMovementCost(Terrain.HILL, 1.25f);
        this.setMovementCost(Terrain.WATER, 3.0f);
        this.setMovementCost(Terrain.MOUNTAIN, 2.5f);

        Animal.logger.log(Level.CONFIG, "Rabbit instance created: ID {0}, Age: {1}/{2}, Cell: [{3},{4}]",
                new Object[]{this.getId(), this.getCurrAge(), this.getMaxAge(), initialCell.getX(), initialCell.getY()});
    }

    // --- Method Overrides and Interface Method Usage ---

    @Override
    public void hide(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Rabbit.hide() called with an animal (" + animal.getId() + ") that is not this Rabbit instance (" + this.getId() + ").");
        }
        HidingPrey.super.hide(animal);
    }

    // Herbivore methods
    @Override
    public void eatPlant(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Rabbit.eatPlant() called with an animal (" + animal.getId() + ") that is not this Rabbit instance (" + this.getId() + ").");
        }
        Herbivore.super.eatPlant(animal);
    }

    @Override
    public void eatAlive(Animal animal, Animal prey) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Rabbit.eatAlive() called with an animal (" + animal.getId() + ") that is not this Rabbit instance (" + this.getId() + ").");
        }
        Animal.logger.log(Level.INFO, "Rabbit " + this.getId() + " is attempting to 'eat alive' another animal. This is highly unusual for a herbivore.");
        Herbivore.super.eatAlive(animal, prey);
    }

    @Override
    public void eatDead(Animal animal, Corpse corpse) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Rabbit.eatDead() called with an animal (" + animal.getId() + ") that is not this Rabbit instance (" + this.getId() + ").");
        }
        Animal.logger.log(Level.INFO, "Rabbit " + this.getId() + " is attempting to eat a corpse. This is highly unusual for a herbivore.");
        Herbivore.super.eatDead(animal, corpse);
    }


    // --- Public methods to expose inherited default behaviors from Basic (via HidingPrey) ---
    public void performAttack(Animal defender) {
        HidingPrey.super.attack(this, defender);
        Animal.logger.log(Level.INFO, "Rabbit " + this.getId() + " is attempting to attack. This is unusual.");
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