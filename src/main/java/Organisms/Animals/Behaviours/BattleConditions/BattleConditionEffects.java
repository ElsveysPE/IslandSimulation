package Organisms.Animals.Behaviours.BattleConditions;

import Organisms.Animals.Animal;

public class BattleConditionEffects {
    public static void applyReckless(Animal animal){
        animal.battleConditions.add(BattleConditions.RECKLESS);
        animal.setAttackAdv(animal.getAttackAdv()+2);
        animal.setEvasionAdv(animal.getEvasionAdv()-2);
    }
    public static void disableReckless(Animal animal){
        animal.battleConditions.remove(BattleConditions.RECKLESS);
        animal.setAttackAdv(animal.getAttackAdv()-2);
        animal.setEvasionAdv(animal.getEvasionAdv()+2);
    }
    public static void applyRetreating(Animal animal){
        animal.battleConditions.add(BattleConditions.RETREATING);
        animal.setEvasionAdv(animal.getEvasionAdv()+2);
    }
    public static void disableRetreating(Animal animal){
        animal.battleConditions.remove(BattleConditions.RETREATING);
        animal.setEvasionAdv(animal.getEvasionAdv()-2);
    }
}
