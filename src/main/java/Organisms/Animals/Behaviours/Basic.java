package Organisms.Animals.Behaviours;

import Map.MapGeneration;
import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.*;
import Organisms.Animals.Conditions.Condition;
import Organisms.HealthStatus;
import util.ImportantMethods;

import java.util.List;

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

    default WhereToMove whereToMove(Animal animal) {
        return null;
    }

    default void run(Animal animal) {

    }

    default void attack(Animal attacker, Animal defender) {
        int chanceToHit;
        int damage;
        int chanceToEvade;
        if (BasicChecks.isHidden(attacker, defender)) {
            chanceToHit = ImportantMethods.d100rollAdv(Math.max(attacker.getAgility(), attacker.getStrength()));
            damage = (int) (ImportantMethods.d10rollAdv(attacker.getMinDamage()) * Math.min(1.0, defender.getPhysRes() + 0.1));
        } else {
            if (attacker.getAttackAdv() >= 1)
                chanceToHit = ImportantMethods.d100rollAdv(Math.max(attacker.getAgility(), attacker.getStrength()));
            else if (attacker.getAttackAdv() <= 1)
                chanceToHit = ImportantMethods.d100rollDisAdv(Math.max(attacker.getAgility(), attacker.getStrength()));
            else chanceToHit = ImportantMethods.d100roll(Math.max(attacker.getAgility(), attacker.getStrength()));
            damage = (int) (ImportantMethods.d10roll(attacker.getMinDamage()) * defender.getPhysRes());
        }
        if (chanceToHit > 95) damage = damage * 2;
        if (defender.getEvasionAdv() >= 1) chanceToEvade = ImportantMethods.d100rollAdv(defender.getAgility());
        else if (defender.getEvasionAdv() <= 1) chanceToEvade = ImportantMethods.d100rollDisAdv(defender.getAgility());
        else chanceToEvade = ImportantMethods.d100roll(defender.getAgility());
        if (chanceToHit > chanceToEvade) defender.setHealthPoints(defender.getHealthPoints() - damage);
        attacker.battleActionPoints--;
    }

    default void goReckless(Animal animal) {
        animal.battleConditions.add(BattleConditions.RECKLESS);
        animal.setAttackAdv(animal.getAttackAdv() + 1);
        animal.setEvasionAdv(animal.getEvasionAdv() - 1);
        animal.setStealthAdv(animal.getStealthAdv() - 1);
        animal.setPerceptionAdv(animal.getPerceptionAdv() - 1);
        animal.setPhysRes(Math.min(animal.getPhysRes() * 2, 1));
        animal.battleActionPoints--;
    }

    default void resting(Animal animal) {
        animal.conditions.add(Condition.RESTING);
        animal.conditions.remove(Condition.FATIGUED);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
        animal.actionPoints--;
    }
    default void deepResting(Animal animal) {
        animal.conditions.add(Condition.DEEP_RESTING);
        animal.conditions.remove(Condition.FATIGUED);
        animal.conditions.remove(Condition.EXHAUSTED);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-3);
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
        animal.actionPoints-=4;
    }

    default void disengage(Animal runner) {
        List<MapStructure.Cell> possibleRoutes = MapGeneration.getNeighbours(runner.getCell());
        runner.battleConditions.add(BattleConditions.DISENGAGING);
        runner.battleActionPoints--;
    }

    default int initiativeRoll(Animal animal) {
        if (animal.conditions.contains(Condition.INCAPACITATED)) return 0;
        else if (hasDisadvantageOnInitiative(animal) - hasAdvantageOnInitiative(animal) > animal.getAgility() / 20)
            return ImportantMethods.d100rollDisAdv(animal.getAgility());
        else if (hasDisadvantageOnInitiative(animal) < hasAdvantageOnInitiative(animal))
            return ImportantMethods.d100rollAdv(animal.getAgility());
        else return ImportantMethods.d100roll(animal.getAgility());
    }

    default int hasDisadvantageOnInitiative(Animal animal) {
        int disadvantage = 0;
        if (animal.conditions.contains(Condition.HOBBLED)) disadvantage++;
        if (animal.conditions.contains(Condition.PRONE)) disadvantage += 2;
        if (animal.conditions.contains(Condition.STARVING)) disadvantage++;
        if (animal.conditions.contains(Condition.EXHAUSTED)) disadvantage++;
        if (animal.conditions.contains(Condition.DAZED)) disadvantage += 3;
        if (animal.conditions.contains(Condition.DISORIENTED)) disadvantage++;
        if (animal.conditions.contains(Condition.SEVERELY_ILL)) disadvantage++;
        if (animal.conditions.contains(Condition.POISONED)) disadvantage++;
        if (animal.conditions.contains(Condition.RESTING)) disadvantage++;
        if (animal.conditions.contains(Condition.DEEP_RESTING)) disadvantage+=2;
        if (animal.getHealthStatus().equals(HealthStatus.GRAVELY_INJURED)) disadvantage += 3;
        if (animal.getHealthStatus().equals(HealthStatus.SEVERELY_INJURED)) disadvantage++;
        return disadvantage;
    }

    default int hasAdvantageOnInitiative(Animal animal) {
        int advantage = 0;
        if (animal.conditions.contains(Condition.ANGRY)) advantage++;
        if (animal.conditions.contains(Condition.PANICKING)) advantage++;
        if (animal.conditions.contains(Condition.STRESSED)) advantage++;
        if (animal.conditions.contains(Condition.SUPER_STARVING)) advantage++;
        if (animal.getTags().contains(Tags.VOLANT) || animal.getTags().contains(Tags.AGILE))
            switch (animal.getVerticalPosition()) {
                case A_LITTLE_HIGH -> advantage++;
                case HIGH -> advantage += 2;
                case VERY_HIGH -> advantage += 3;
            }
        return advantage;
    }
}

