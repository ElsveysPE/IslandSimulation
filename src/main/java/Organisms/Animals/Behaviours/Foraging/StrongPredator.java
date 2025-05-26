package Organisms.Animals.Behaviours.Foraging;

import Organisms.Animals.Animal;
import Organisms.Animals.BasicChecks;
import Organisms.Animals.Behaviours.Basic;
import Organisms.Animals.Conditions.Conditions;
import util.ImportantMethods;

public interface StrongPredator extends Basic {
    default void attack(Animal predator, Animal prey) {
        int chanceToHit;
        int damage;
        if (BasicChecks.hidden(predator, prey)) {
            chanceToHit=ImportantMethods.d100rollAdv(predator.getStrength());
            damage = (int) (ImportantMethods.d33rollAdv(predator.getMinDamage())*Math.min(1.0, prey.getPhysRes() + 0.4));
        } else {
            if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getStrength());
            else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getStrength());
            else chanceToHit = ImportantMethods.d100roll(predator.getStrength());
            damage = (int) (ImportantMethods.d33roll(predator.getMinDamage()) * Math.min(1.0, prey.getPhysRes() + 0.3));
        }
        int chanceToEvade;
        if (prey.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(prey.getAgility());
        else if (prey.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(prey.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(prey.getAgility());
        if (chanceToHit>95) damage=damage*2;
        if (chanceToHit>chanceToEvade) prey.setHealthPoints(prey.getHealthPoints()-damage);
       // predator.battleActionPoints--;
    }
    default void grapple(Animal predator, Animal prey){
        int chanceToHit;
        int damage;
        int chanceToEvade;
        boolean advantage = false;
        Conditions affliction = Conditions.GRAPPLED;
        if (prey.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(prey.getAgility());
        else if (prey.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(prey.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(prey.getAgility());
        if (BasicChecks.hidden(predator, prey)) {
            chanceToHit=ImportantMethods.d100rollAdv(predator.getStrength());
            damage = (int) (ImportantMethods.d15rollAdv(predator.getMinDamage())*Math.min(1.0, prey.getPhysRes() + 0.3));
        } else {
            if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getStrength());
            else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getStrength());
            else chanceToHit = ImportantMethods.d100roll(predator.getStrength());
            damage = (int) (ImportantMethods.d15roll(predator.getMinDamage()) *  Math.min(1.0, prey.getPhysRes() + 0.2));
        }
        if (chanceToHit > 95) {
            damage = damage * 2;
            advantage = true;
        }
        if (chanceToHit>chanceToEvade){
            prey.setHealthPoints(prey.getHealthPoints()-damage);
            if(advantage && ImportantMethods.d100rollAdv(predator.getAgility())>=40) prey.conditions.add(affliction);
            if(!advantage && ImportantMethods.d100roll(predator.getAgility())>=40) prey.conditions.add(affliction);
        }
      //  predator.battleActionPoints--;
    }
    default void getDown(Animal predator, Animal prey){
        //triggers prone or hobbled if successful
        int chanceToHit;
        int damage;
        int chanceToEvade;
        boolean advantage = false;
        Conditions affliction = Conditions.HOBBLED;
        if (prey.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(prey.getAgility());
        else if (prey.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(prey.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(prey.getAgility());
        if (BasicChecks.hidden(predator, prey)) {
            chanceToHit=ImportantMethods.d100rollAdv(predator.getStrength());
            damage = (int) (ImportantMethods.d20rollAdv(predator.getMinDamage())* Math.min(1.0, prey.getPhysRes() + 0.3));
        } else {
            if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getStrength());
            else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getStrength());
            else chanceToHit = ImportantMethods.d100roll(predator.getStrength());
            damage = (int) (ImportantMethods.d20roll(predator.getMinDamage()) *  Math.min(1.0, prey.getPhysRes() + 0.2));
        }
        if (chanceToHit > 95) {
            damage = damage * 2;
            affliction = Conditions.PRONE;
            advantage = true;
        }
        if (chanceToHit>chanceToEvade){
            prey.setHealthPoints(prey.getHealthPoints()-damage);
            if(advantage && ImportantMethods.d100rollAdv(predator.getStrength())>=95) prey.conditions.add(Conditions.BLEEDING);
            if(advantage && ImportantMethods.d100rollAdv(predator.getStrength())>=40) prey.conditions.add(affliction);
            if(!advantage && ImportantMethods.d100roll(predator.getStrength())>=95) prey.conditions.add(Conditions.BLEEDING);
            if(!advantage && ImportantMethods.d100roll(predator.getStrength())>=40) prey.conditions.add(affliction);
        }
       // predator.battleActionPoints--;
    }
    default void headStrike(Animal predator, Animal prey){
        //as in strikes the head, not with the head
        //triggers disoriented or dazed if successful
            int chanceToHit;
            int damage;
            int chanceToEvade;
            boolean advantage = false;
            Conditions affliction = Conditions.DISORIENTED;
            if (prey.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(prey.getAgility());
            else if (prey.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(prey.getAgility());
            else chanceToEvade = ImportantMethods.d100roll(prey.getAgility());
            if (BasicChecks.hidden(predator, prey)) {
                chanceToHit=ImportantMethods.d100rollAdv(predator.getStrength());
                damage = (int) (ImportantMethods.d20rollAdv(predator.getMinDamage())* Math.min(1.0, prey.getPhysRes() + 0.4));
            } else {
                if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getStrength());
                else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getStrength());
                else chanceToHit = ImportantMethods.d100roll(predator.getStrength());
                damage = (int) (ImportantMethods.d20roll(predator.getMinDamage()) *  Math.min(1.0, prey.getPhysRes() + 0.3));
            }
            if (chanceToHit > 90) {
                damage = damage * 2;
                affliction = Conditions.DAZED;
                advantage = true;
            }
            if (chanceToHit>chanceToEvade){
                prey.setHealthPoints(prey.getHealthPoints()-damage);
                if(advantage && ImportantMethods.d100rollAdv(predator.getStrength())>=90) prey.conditions.add(Conditions.BLEEDING);
                if(advantage && ImportantMethods.d100rollAdv(predator.getStrength())>=40) prey.conditions.add(affliction);
                if(!advantage && ImportantMethods.d100roll(predator.getStrength())>=90) prey.conditions.add(Conditions.BLEEDING);
                if(!advantage && ImportantMethods.d100roll(predator.getStrength())>=40) prey.conditions.add(affliction);
            }
           // predator.battleActionPoints--;
        }
        default void youAreStaying(Animal predator, Animal prey){
            int chanceToHit;
            int damage;
            int chanceToEvade;
            boolean advantage = false;
            Conditions affliction = Conditions.HOBBLED;
            if (prey.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(prey.getAgility());
            else if (prey.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(prey.getAgility());
            else chanceToEvade = ImportantMethods.d100roll(prey.getAgility());
            if (BasicChecks.hidden(predator, prey)) {
                chanceToHit=ImportantMethods.d100rollAdv(predator.getStrength());
                damage = (int) (ImportantMethods.d10rollAdv(predator.getMinDamage())* Math.min(1.0, prey.getPhysRes() + 0.3));
            } else {
                if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getStrength());
                else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getStrength());
                else chanceToHit = ImportantMethods.d100roll(predator.getStrength());
                damage = (int) (ImportantMethods.d10roll(predator.getMinDamage()) *  Math.min(1.0, prey.getPhysRes() + 0.2));
            }
            if (chanceToHit > 95) {
                damage = damage * 2;
                affliction = Conditions.PRONE;
                advantage = true;
            }
            if (chanceToHit>chanceToEvade){
                prey.setHealthPoints(prey.getHealthPoints()-damage);
                if(advantage && ImportantMethods.d100rollAdv(predator.getAgility())>=40) prey.conditions.add(affliction);
                if(!advantage && ImportantMethods.d100roll(predator.getAgility())>=40) prey.conditions.add(affliction);
            }
            predator.reactionPoints--;
    }
    }
