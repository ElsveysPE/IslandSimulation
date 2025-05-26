package Organisms.Animals.Behaviours.Foraging;

import Organisms.Animals.Animal;
import Organisms.Animals.BasicChecks;
import Organisms.Animals.Behaviours.Basic;
import Organisms.Animals.Behaviours.BattleConditions.BattleConditions;
import Organisms.Animals.Conditions.Conditions;
import Organisms.Animals.Conditions.ConditionEffects;
import util.ImportantMethods;

public interface AgilePredator extends Basic {
    default void attack(Animal predator, Animal prey) {
        int chanceToHit;
        int damage;
        int chanceToEvade;
        boolean advantage = false;
        if (prey.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(prey.getAgility());
        else if (prey.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(prey.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(prey.getAgility());
        if (BasicChecks.hidden(predator, prey)) {
            chanceToHit=ImportantMethods.d100rollAdv(predator.getAgility());
            damage = (int) (ImportantMethods.d20rollAdv(predator.getMinDamage())*Math.min(1.0, prey.getPhysRes() + 0.8));
            if (chanceToHit>85) {
                damage=damage*4;
                advantage=true;
            }
        } else {
            if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getAgility());
            else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getAgility());
            else chanceToHit = ImportantMethods.d100roll(predator.getAgility());
            damage = (int) (ImportantMethods.d20roll(predator.getMinDamage()) * prey.getPhysRes());
            if (chanceToHit > 95) {
                damage = damage * 2;
                advantage = true;
            }
        }
        if (chanceToHit>chanceToEvade) {
            prey.setHealthPoints(prey.getHealthPoints() - damage);
            if (advantage && ImportantMethods.d100rollAdv(predator.getAgility()) >= 90)
                prey.conditions.add(Conditions.BLEEDING);
            if (!advantage && ImportantMethods.d100roll(predator.getAgility()) >= 90)
                prey.conditions.add(Conditions.BLEEDING);
        }
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
            chanceToHit=ImportantMethods.d100rollAdv(predator.getAgility());
            damage = (int) (ImportantMethods.d15rollAdv(predator.getMinDamage())*Math.min(1.0, prey.getPhysRes() + 0.8));
            if (chanceToHit>85) {
                damage=damage*3;
                advantage = true;
            }
        } else {
            if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getAgility());
            else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getAgility());
            else chanceToHit = ImportantMethods.d100roll(predator.getAgility());
            damage = (int) (ImportantMethods.d15roll(predator.getMinDamage()) * prey.getPhysRes());
            if (chanceToHit > 95) {
                damage = damage * 2;
                advantage = true;
            }
        }
        if (chanceToHit>chanceToEvade){
            prey.setHealthPoints(prey.getHealthPoints()-damage);
            if(advantage && ImportantMethods.d100rollAdv(predator.getAgility())>=35) prey.conditions.add(affliction);
            if(!advantage && ImportantMethods.d100roll(predator.getAgility())>=35) prey.conditions.add(affliction);
        }
        // predator.battleActionPoints--;
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
            chanceToHit=ImportantMethods.d100rollAdv(predator.getAgility());
            damage = (int) (ImportantMethods.d10rollAdv(predator.getMinDamage())*Math.min(1.0, prey.getPhysRes() + 0.8));
            if (chanceToHit>85) {
                damage=damage*3;
                affliction = Conditions.PRONE;
                advantage = true;
            }
        } else {
            if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getAgility());
            else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getAgility());
            else chanceToHit = ImportantMethods.d100roll(predator.getAgility());
            damage = (int) (ImportantMethods.d10roll(predator.getMinDamage()) * prey.getPhysRes());
            if (chanceToHit > 95) {
                damage = damage * 2;
                affliction = Conditions.PRONE;
                advantage = true;
            }
        }
        if (chanceToHit>chanceToEvade){
            prey.setHealthPoints(prey.getHealthPoints()-damage);
            if(advantage && ImportantMethods.d100rollAdv(predator.getAgility())>=95) prey.conditions.add(Conditions.BLEEDING);
            if(advantage && ImportantMethods.d100rollAdv(predator.getAgility())>=40) prey.conditions.add(affliction);
            if(!advantage && ImportantMethods.d100roll(predator.getAgility())>=95) prey.conditions.add(Conditions.BLEEDING);
            if(!advantage && ImportantMethods.d100roll(predator.getAgility())>=40) prey.conditions.add(affliction);
        }
       // predator.battleActionPoints--;
    }
    default void arteryStrike(Animal predator, Animal prey){
            int chanceToHit;
            int damage;
            int chanceToEvade;
            boolean advantage = false;
            Conditions affliction = Conditions.BLEEDING;
            if (prey.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(prey.getAgility());
            else if (prey.getEvasionAdv() <= -1) chanceToEvade = ImportantMethods.d100rollDisAdv(prey.getAgility());
            else chanceToEvade = ImportantMethods.d100roll(prey.getAgility());
            if (BasicChecks.hidden(predator, prey)) {
                chanceToHit=ImportantMethods.d100rollAdv(predator.getAgility());
                damage = (int) (ImportantMethods.d10rollAdv(predator.getMinDamage())*Math.min(1.0, prey.getPhysRes() + 0.8));
                if (chanceToHit>85) {
                    damage=damage*3;
                    affliction = Conditions.SEVERELY_BLEEDING;
                    advantage = true;
                }
            } else {
                if (predator.getAttackAdv() >= 1) chanceToHit = ImportantMethods.d100rollAdv(predator.getAgility());
                else if (predator.getAttackAdv() <= -1) chanceToHit = ImportantMethods.d100rollDisAdv(predator.getAgility());
                else chanceToHit = ImportantMethods.d100roll(predator.getAgility());
                damage = (int) (ImportantMethods.d10roll(predator.getMinDamage()) * prey.getPhysRes());
                if (chanceToHit > 95) {
                    damage = damage * 2;
                    affliction = Conditions.SEVERELY_BLEEDING;
                    advantage = true;
                }
            }
            if (chanceToHit>chanceToEvade) {
                prey.setHealthPoints(prey.getHealthPoints() - damage);
                if(advantage && ImportantMethods.d100rollAdv(predator.getAgility())>=40) prey.conditions.add(affliction);
                if(!advantage && ImportantMethods.d100roll(predator.getAgility())>=40) prey.conditions.add(affliction);
            }
            // predator.battleActionPoints--;
        }
        default void getBackHere(Animal predator, Animal prey){
        prey.battleConditions.remove(BattleConditions.DISENGAGING);
        predator.reactionPoints--;
        }
        default void bugOff(Animal agile){
           ConditionEffects.grappleEnd(agile);
            agile.reactionPoints--;
    }
    }
