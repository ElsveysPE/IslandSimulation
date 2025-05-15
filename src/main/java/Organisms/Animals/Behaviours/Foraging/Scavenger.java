package Organisms.Animals.Behaviours.Foraging;

import Organisms.Animals.Animal;
import Organisms.Animals.BasicChecks;
import Organisms.Animals.Behaviours.Basic;
import Organisms.Animals.Behaviours.Nutrition.Carnivore;
import Organisms.Animals.Corpses.Corpse;
import util.ImportantMethods;

public interface Scavenger extends Basic, Carnivore {
    default void attack(Animal predator, Animal prey) {
        int chanceToHit;
        int damage;
        int chanceToEvade;
        if (prey.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(prey.getAgility());
        else if (prey.getEvasionAdv() <= 1) chanceToEvade = ImportantMethods.d100rollDisAdv(prey.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(prey.getAgility());
        if (BasicChecks.isHidden(predator, prey)) {
            chanceToHit=ImportantMethods.d100rollAdv(predator.getAgility());
            damage = (int) (ImportantMethods.d20rollAdv(predator.getMinDamage())*Math.min(1.0, prey.getPhysRes() + 0.3));
        } else {
            if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getAgility());
            else if (predator.getAttackAdv() <= 1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getAgility());
            else chanceToHit = ImportantMethods.d100roll(predator.getAgility());
            damage = (int) (ImportantMethods.d20roll(predator.getMinDamage()) * Math.min(1.0, prey.getPhysRes() + 0.2));
        }
        if (chanceToHit>95) damage=damage*2;
        if (chanceToHit>chanceToEvade) prey.setHealthPoints(prey.getHealthPoints()-damage);
        predator.battleActionPoints--;
    }
    default void eatDead(Animal animal, Corpse corpse){

    }
}
