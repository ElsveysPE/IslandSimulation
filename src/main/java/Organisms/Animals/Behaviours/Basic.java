package Organisms.Animals.Behaviours;

import Organisms.Animals.*;
import Organisms.Animals.Behaviours.BattleConditions.BattleConditions;
import Organisms.Animals.Conditions.Conditions;
import util.ImportantMethods;

public interface Basic {


    default void hide(Animal animal) {
        if (animal.getStealthAdv() >= 1) animal.setCurrStealth(ImportantMethods.d100rollAdv(animal.getStealth()));
        else if (animal.getStealthAdv() <= -1)
            animal.setCurrStealth(ImportantMethods.d100rollDisAdv(animal.getStealth()));
        else animal.setCurrStealth(ImportantMethods.d100roll(animal.getStealth()));
    }

    default void focusOnSurroundings(Animal animal) {
        if (animal.getPerceptionAdv() >= 0)
            animal.setCurrPerception(util.ImportantMethods.d100rollAdv(animal.getPerception()));
        else if (animal.getPerceptionAdv() <= -2)
            animal.setCurrPerception(util.ImportantMethods.d100rollDisAdv(animal.getPerception()));
        else animal.setCurrPerception(util.ImportantMethods.d100roll(animal.getPerception()));
    }


    /* default void movement(Animal animal) {
        Terrain currTerrain = animal.getCell().getTerrain();
        int currX = animal.getCell().getX();
        int currY = animal.getCell().getY();
        int movPointsExp = 1;
        if (currTerrain.equals(Terrain.WATER)) movPointsExp = animal.getSwimSpeed();
        else if (currTerrain.equals(Terrain.WATER) && animal.getSwimSpeed() == 0) movPointsExp = 2;
        if (currTerrain.equals(Terrain.HILL)) movPointsExp = 2;
        if (currTerrain.equals(Terrain.MOUNTAIN)) movPointsExp = 3;
        /*
        if(x!=0) neighbours.add(IslandMap[x-1][y]);
        if(x!=Length-1) neighbours.add(IslandMap[x+1][y]);
        if(y!=0) neighbours.add(IslandMap[x][y-1]);
        if(y!=Height-1) neighbours.add(IslandMap[x][y+1]);
        animal.movementPoints = animal.movementPoints - movPointsExp;
    }
     */

    default void run(Animal animal) {

    }

    default void attack(Animal attacker, Animal defender) {
        int chanceToHit;
        int damage;
        int chanceToEvade;
        if (BasicChecks.hidden(attacker, defender)) {
            chanceToHit = ImportantMethods.d100rollAdv(Math.max(attacker.getAgility(), attacker.getStrength()));
            damage = (int) (ImportantMethods.d10rollAdv(attacker.getMinDamage()) * Math.min(1.0, defender.getPhysRes() + 0.1));
        } else {
            if (attacker.getAttackAdv() >= 1)
                chanceToHit = ImportantMethods.d100rollAdv(Math.max(attacker.getAgility(), attacker.getStrength()));
            else if (attacker.getAttackAdv() <= -1)
                chanceToHit = ImportantMethods.d100rollDisAdv(Math.max(attacker.getAgility(), attacker.getStrength()));
            else chanceToHit = ImportantMethods.d100roll(Math.max(attacker.getAgility(), attacker.getStrength()));
            damage = (int) (ImportantMethods.d10roll(attacker.getMinDamage()) * defender.getPhysRes());
        }
        if (chanceToHit > 95) damage = damage * 2;
        if (defender.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(defender.getAgility());
        else if (defender.getEvasionAdv() <= 1) chanceToEvade = ImportantMethods.d100rollDisAdv(defender.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(defender.getAgility());
        if (chanceToHit > chanceToEvade) defender.setHealthPoints(defender.getHealthPoints() - damage);
       // attacker.battleActionPoints--;
    }

    default void goReckless(Animal animal) {
        animal.battleConditions.add(BattleConditions.RECKLESS);
        animal.setAttackAdv(animal.getAttackAdv() + 1);
        animal.setEvasionAdv(animal.getEvasionAdv() - 1);
        animal.setStealthAdv(animal.getStealthAdv() - 1);
        animal.setPerceptionAdv(animal.getPerceptionAdv() - 1);
        animal.setPhysRes(Math.min(animal.getPhysRes() * 2, 1));
        // animal.battleActionPoints--;
    }

    default void resting(Animal animal) {
        animal.conditions.add(Conditions.RESTING);
        animal.conditions.remove(Conditions.FATIGUED);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
        animal.actionPoints--;
    }
    default void deepResting(Animal animal) {
        animal.conditions.add(Conditions.DEEP_RESTING);
        animal.conditions.remove(Conditions.FATIGUED);
        animal.conditions.remove(Conditions.EXHAUSTED);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-3);
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
        animal.actionPoints-=4;
    }

    default void disengage(Animal runner) {
        runner.battleConditions.add(BattleConditions.DISENGAGING);
        runner.battleActionPoints--;
    }
}

