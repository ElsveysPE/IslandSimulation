package Organisms.Animals.Behaviours.Defensive;

import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Basic;
import Organisms.Animals.Behaviours.BattleConditions.BattleConditions;

public interface EvasivePrey extends Basic {
    default void focusOnEvasion(Animal animal){
        animal.setEvasionAdv(animal.getEvasionAdv()+1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
        animal.setAttackAdv(animal.getAttackAdv()-2);
        animal.battleActionPoints--;
    }
    default void run(Animal animal){

    }
    default void disengage(Animal runner){
        runner.battleConditions.add(BattleConditions.DISENGAGING);
        runner.battleActionPoints--;
    }
    default void nuhUh(Animal animal){
        //helps get out of grappling and level 1 afflictions
        //or reduce level 2 afflictions to level 1(dazed to disoriented, prone to hobbled)
        animal.reactionPoints--;
    }
}
