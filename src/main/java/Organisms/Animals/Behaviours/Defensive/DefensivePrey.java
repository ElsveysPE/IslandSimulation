package Organisms.Animals.Behaviours.Defensive;

import Organisms.Animals.Animal;
import Organisms.Animals.BasicChecks;
import Organisms.Animals.Behaviours.Basic;
import Organisms.Animals.Conditions.Condition;
import util.ImportantMethods;

public interface DefensivePrey extends Basic {
    default void attack(Animal attacker, Animal defender) {
        int chanceToHit;
        int damage;
        int chanceToEvade;
        boolean advantage = false;
        if (defender.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(defender.getAgility());
        else if (defender.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(defender.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(defender.getAgility());
        if (BasicChecks.isHidden(attacker, defender)) {
            chanceToHit=ImportantMethods.d100rollAdv(attacker.getStrength());
            damage = (int) (ImportantMethods.d20rollAdv(attacker.getMinDamage())*Math.min(1.0, defender.getPhysRes() + 0.3));
        } else {
            if (attacker.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(attacker.getStrength());
            else if (attacker.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(attacker.getStrength());
            else chanceToHit = ImportantMethods.d100roll(attacker.getStrength());
            damage = (int) (ImportantMethods.d20roll(attacker.getMinDamage()) * Math.min(1.0, defender.getPhysRes() + 0.2));
        }
        if (chanceToHit>95) {
            damage=damage*2;
            advantage = true;
        }
        if (chanceToHit>chanceToEvade) {
            defender.setHealthPoints(defender.getHealthPoints() - damage);
            if (advantage && ImportantMethods.d100rollAdv(attacker.getAgility()) >= 85)
                defender.conditions.add(Condition.BLEEDING);
            if (!advantage && ImportantMethods.d100roll(attacker.getAgility()) >= 85)
                defender.conditions.add(Condition.BLEEDING);
        }
        attacker.battleActionPoints--;
    }
    default void getDown(Animal attacker, Animal defender){
        //triggers prone or hobbled if successful
        int chanceToHit;
        int damage;
        int chanceToEvade;
        boolean advantage = false;
        Condition affliction = Condition.HOBBLED;
        if (defender.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(defender.getAgility());
        else if (defender.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(defender.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(defender.getAgility());
        if (BasicChecks.isHidden(attacker, defender)) {
            chanceToHit=ImportantMethods.d100rollAdv(attacker.getStrength());
            damage = (int) (ImportantMethods.d20rollAdv(attacker.getMinDamage())* Math.min(1.0, defender.getPhysRes() + 0.3));
        } else {
            if (attacker.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(attacker.getStrength());
            else if (attacker.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(attacker.getStrength());
            else chanceToHit = ImportantMethods.d100roll(attacker.getStrength());
            damage = (int) (ImportantMethods.d20roll(attacker.getMinDamage()) *  Math.min(1.0, defender.getPhysRes() + 0.2));
        }
        if (chanceToHit > 95) {
            damage = damage * 2;
            affliction = Condition.PRONE;
            advantage = true;
        }
        if (chanceToHit>chanceToEvade){
            defender.setHealthPoints(defender.getHealthPoints()-damage);
            if(advantage && ImportantMethods.d100rollAdv(attacker.getAgility())>=95) defender.conditions.add(Condition.BLEEDING);
            if(advantage && ImportantMethods.d100rollAdv(attacker.getAgility())>=40) defender.conditions.add(affliction);
            if(!advantage && ImportantMethods.d100roll(attacker.getAgility())>=95) defender.conditions.add(Condition.BLEEDING);
            if(!advantage && ImportantMethods.d100roll(attacker.getAgility())>=40) defender.conditions.add(affliction);
        }
        attacker.battleActionPoints--;
    }
    default void headStrike(Animal attacker, Animal defender){
        //as in strikes the head, not with the head
        //triggers disoriented or dazed if successful
        int chanceToHit;
        int damage;
        int chanceToEvade;
        boolean advantage = false;
        Condition affliction = Condition.DISORIENTED;
        if (defender.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(defender.getAgility());
        else if (defender.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(defender.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(defender.getAgility());
        if (BasicChecks.isHidden(attacker, defender)) {
            chanceToHit=ImportantMethods.d100rollAdv(attacker.getStrength());
            damage = (int) (ImportantMethods.d20rollAdv(attacker.getMinDamage())* Math.min(1.0, defender.getPhysRes() + 0.4));
        } else {
            if (attacker.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(attacker.getStrength());
            else if (attacker.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(attacker.getStrength());
            else chanceToHit = ImportantMethods.d100roll(attacker.getStrength());
            damage = (int) (ImportantMethods.d20roll(attacker.getMinDamage()) *  Math.min(1.0, defender.getPhysRes() + 0.3));
        }
        if (chanceToHit > 90) {
            damage = damage * 2;
            affliction = Condition.DAZED;
            advantage = true;
        }
        if (chanceToHit>chanceToEvade){
            defender.setHealthPoints(defender.getHealthPoints()-damage);
            if(advantage && ImportantMethods.d100rollAdv(attacker.getStrength())>=90) defender.conditions.add(Condition.BLEEDING);
            if(advantage && ImportantMethods.d100rollAdv(attacker.getStrength())>=40) defender.conditions.add(affliction);
            if(!advantage && ImportantMethods.d100roll(attacker.getStrength())>=90) defender.conditions.add(Condition.BLEEDING);
            if(!advantage && ImportantMethods.d100roll(attacker.getStrength())>=40) defender.conditions.add(affliction);
        }
        attacker.battleActionPoints--;
    }
}
