package Organisms.Animals.Species;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Defensive.DefensivePrey;
import Organisms.Animals.Behaviours.Miscellaneous.Social;
import Organisms.Animals.Behaviours.Nutrition.Herbivore;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Animals.Tags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;

public class Deer extends Animal implements Herbivore, DefensivePrey, Social {

    private static final int DEER_MAX_AGE = 720;
    private static final int DEER_INITIAL_CURR_AGE = 150;

    public Deer(MapStructure.Cell initialCell,
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
                        Tags.FIGHT, Tags.SOCIAL, Tags.MATUTINAL, Tags.HERBY
                )),
                DEER_MAX_AGE,
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

        this.setCurrAge(DEER_INITIAL_CURR_AGE);

        // Set Deer-specific movement costs
        this.setMovementCost(Terrain.PLAIN, 1.0f);
        this.setMovementCost(Terrain.HILL, 1.25f);
        this.setMovementCost(Terrain.WATER, 2.0f);
        this.setMovementCost(Terrain.MOUNTAIN, 1.75f);

        Animal.logger.log(Level.CONFIG, "Deer instance created: ID {0}, Age: {1}/{2}, Cell: [{3},{4}]",
                new Object[]{this.getId(), this.getCurrAge(), this.getMaxAge(), initialCell.getX(), initialCell.getY()});
    }

    // --- Method Overrides and Interface Method Usage ---

    // DefensivePrey extends Basic. Deer will use DefensivePrey's attack.
    @Override
    public void attack(Animal attacker, Animal defender) {
        if (!attacker.equals(this)) {
            Animal.logger.log(Level.WARNING, "Deer.attack() called with an attacker (" + attacker.getId() + ") that is not this Deer instance (" + this.getId() + ").");
        }
        DefensivePrey.super.attack(attacker, defender);
    }

    // Herbivore methods
    @Override
    public void eatPlant(Animal animal) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Deer.eatPlant() called with an animal (" + animal.getId() + ") that is not this Deer instance (" + this.getId() + ").");
        }
        Herbivore.super.eatPlant(animal);
    }

    @Override
    public void eatAlive(Animal animal, Animal prey) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Deer.eatAlive() called with an animal (" + animal.getId() + ") that is not this Deer instance (" + this.getId() + ").");
        }
        Animal.logger.log(Level.INFO, "Deer " + this.getId() + " is attempting to 'eat alive' another animal. This is highly unusual for a herbivore.");
        Herbivore.super.eatAlive(animal, prey);
    }

    @Override
    public void eatDead(Animal animal, Corpse corpse) {
        if (!animal.equals(this)) {
            Animal.logger.log(Level.WARNING, "Deer.eatDead() called with an animal (" + animal.getId() + ") that is not this Deer instance (" + this.getId() + ").");
        }
        Animal.logger.log(Level.INFO, "Deer " + this.getId() + " is attempting to eat a corpse. This is highly unusual for a herbivore.");
        Herbivore.super.eatDead(animal, corpse);
    }

    // --- Public methods to expose inherited default behaviors from interfaces ---

    // Social behaviors
    public void applyPackTacticsBonus() {
        Social.super.packTactics(this);
    }

    // DefensivePrey behaviors
    public void performDefensiveAttack(Animal defender) {
        this.attack(this, defender);
    }

    public void performGetDown(Animal target) {
        DefensivePrey.super.getDown(this, target);
    }

    public void performHeadStrike(Animal target) {
        DefensivePrey.super.headStrike(this, target);
    }

    // Basic behaviors (inherited via DefensivePrey)
    public void performHide() {
        DefensivePrey.super.hide(this);
    }

    public void performGoReckless() {
        DefensivePrey.super.goReckless(this);
    }

    public void performResting() {
        DefensivePrey.super.resting(this);
    }

    public void performDeepResting() {
        DefensivePrey.super.deepResting(this);
    }

    public void performDisengage() {
        DefensivePrey.super.disengage(this);
    }

    public void performFocusOnSurroundings() {
        DefensivePrey.super.focusOnSurroundings(this);
    }

    public void performRun() {
        DefensivePrey.super.run(this);
    }
}
