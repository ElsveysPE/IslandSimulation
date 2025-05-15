package Organisms.Animals.Conditions;

import Organisms.Animals.Animal;

//how each condition affects the animal
public class ConditionEffects {
    //proposed system: list with all animals with certain effects, adding and removal from that list adds/removes effect
    public void mildIllnessStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.MILDLY_ILL, "strength", animal.getStrength()/10);
        ValueShenanigans.affectIntStat(animal, Condition.MILDLY_ILL, "agility", animal.getAgility()/10);
        ValueShenanigans.affectIntStat(animal, Condition.MILDLY_ILL, "constitution", animal.getConstitution()/10);
        ValueShenanigans.affectIntStat(animal, Condition.MILDLY_ILL, "speed", animal.getSpeed()/10);
    }
    public void mildIllnessEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.MILDLY_ILL);

    }
    public void severeIllnessStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.SEVERELY_ILL, "strength", animal.getStrength()/5);
        ValueShenanigans.affectIntStat(animal, Condition.SEVERELY_ILL, "agility", animal.getAgility()/5);
        ValueShenanigans.affectIntStat(animal, Condition.SEVERELY_ILL, "constitution", animal.getConstitution()/5);
        ValueShenanigans.affectIntStat(animal, Condition.SEVERELY_ILL, "speed", animal.getSpeed()/5);
        animal.setStealthAdv(animal.getStealthAdv()-1);

    }
    public void severeIllnessEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.SEVERELY_ILL);
    }
    public void fatiguedStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.FATIGUED, "strength", animal.getStrength()/15);
        ValueShenanigans.affectIntStat(animal, Condition.FATIGUED, "agility", animal.getAgility()/15);
        ValueShenanigans.affectIntStat(animal, Condition.FATIGUED, "perception", animal.getPerception()/15);
        ValueShenanigans.affectIntStat(animal, Condition.FATIGUED, "speed", animal.getSpeed()/15);
    }
    public void fatiguedEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.FATIGUED);
    }
    public void exhaustedStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.EXHAUSTED, "strength", animal.getStrength()/8);
        ValueShenanigans.affectIntStat(animal, Condition.EXHAUSTED, "agility", animal.getAgility()/8);
        ValueShenanigans.affectIntStat(animal, Condition.EXHAUSTED, "perception", animal.getPerception()/8);
        ValueShenanigans.affectIntStat(animal, Condition.EXHAUSTED, "speed", animal.getSpeed()/10);
        animal.setStealthAdv(animal.getStealthAdv()-1);

    }
    public void exhaustedEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.EXHAUSTED);
        animal.setStealthAdv(animal.getStealthAdv()+1);
    }
    public void poisonStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.POISONED, "strength", animal.getStrength()/5);
        ValueShenanigans.affectIntStat(animal, Condition.POISONED, "agility", animal.getAgility()/5);
        ValueShenanigans.affectIntStat(animal, Condition.POISONED, "perception", animal.getPerception()/5);
        ValueShenanigans.affectIntStat(animal, Condition.POISONED, "constitution", animal.getConstitution()/5);

    }
    public void poisonEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.POISONED);
    }
    public void hobbledStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.HOBBLED, "speed", animal.getSpeed()/3);
        ValueShenanigans.affectIntStat(animal, Condition.HOBBLED, "agility", animal.getAgility()/5);
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
    }
    public void hobbledEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.HOBBLED);
        animal.setEvasionAdv(animal.getEvasionAdv()+1);
    }
    public void proneStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.PRONE, "speed", animal.getSpeed());
        ValueShenanigans.affectIntStat(animal, Condition.PRONE, "agility", animal.getAgility()/3);
        animal.setEvasionAdv(animal.getEvasionAdv()-2);
    }
    public void proneEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.PRONE);
        animal.setEvasionAdv(animal.getEvasionAdv()+2);
    }
    public void grappleStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.GRAPPLED, "speed", animal.getSpeed());
        ValueShenanigans.affectIntStat(animal, Condition.GRAPPLED, "agility", animal.getAgility()/2);
        animal.setEvasionAdv(animal.getEvasionAdv()-4);
    }
    public static void grappleEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.GRAPPLED);
        animal.setEvasionAdv(animal.getEvasionAdv()+4);
    }
    public void stressStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.STRESSED, "speed", (animal.getSpeed()/7*(-1)));
        ValueShenanigans.affectIntStat(animal, Condition.STRESSED, "agility", (animal.getAgility()/7*(-1)));
        ValueShenanigans.affectIntStat(animal, Condition.STRESSED, "strength", (animal.getAgility()/7*(-1)));
        animal.setEvasionAdv(animal.getEvasionAdv()+1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public void stressEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.STRESSED);
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public void panicStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.PANICKING, "speed", (animal.getSpeed()/2*(-1)));
        ValueShenanigans.affectIntStat(animal, Condition.PANICKING, "agility", (int) (animal.getAgility()*0.1));
        ValueShenanigans.affectIntStat(animal, Condition.PANICKING, "strength", (animal.getAgility()/3*(-1)));
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public void panicEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.PANICKING);
        animal.setEvasionAdv(animal.getEvasionAdv()+1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public void angerStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.ANGRY, "strength", (animal.getStrength()/3*(-1)));
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public void angerEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.ANGRY);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public void bleedingStart(Animal animal){
        //потрібен новий метод якийсь, по-звичайному тут нормально не зробиш, треба DOT method

    }
    public void bleedingEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.BLEEDING);
    }
    public void severelyBleedingStart(Animal animal){

    }
    public void severelyBleedingEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.SEVERELY_BLEEDING);
    }
    public void disorientedStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.DISORIENTED, "agility", (int) (animal.getAgility()*0.25));
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public void disorientedEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.DISORIENTED);
    }
    public void dazedStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.DAZED, "agility", (int) (animal.getAgility()*0.5));
        ValueShenanigans.affectIntStat(animal, Condition.DAZED, "perception",(int) (animal.getPerception()*0.25));
        animal.setPerceptionAdv(animal.getPerceptionAdv()-2);
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
    }
    public void dazedEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.DAZED);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+2);
    }
    public void hungerStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.STARVING, "strength",((int) (animal.getStrength()*0.1)));
        ValueShenanigans.affectIntStat(animal, Condition.STARVING, "agility", ((int)(animal.getAgility()*0.1)));
        ValueShenanigans.affectIntStat(animal, Condition.STARVING, "speed", ((int)(animal.getSpeed()*0.1)));
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public void hungerEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.HUNGRY);
    }
    public void starvationStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.STARVING, "strength",((int) (animal.getStrength()*0.25)));
        ValueShenanigans.affectIntStat(animal, Condition.STARVING, "agility", ((int)(animal.getAgility()*0.25)));
        ValueShenanigans.affectIntStat(animal, Condition.STARVING, "speed", ((int)(animal.getSpeed()*0.25)));
        ValueShenanigans.affectIntStat(animal, Condition.STARVING, "perception", (animal.getPerception()/5*(-1)));
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public void starvationEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.STARVING);
    }
    public void superStarvationStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.SUPER_STARVING, "strength", (animal.getStrength()/3*(-1)));
        ValueShenanigans.affectIntStat(animal, Condition.SUPER_STARVING, "agility", (animal.getAgility()/3*(-1)));
        ValueShenanigans.affectIntStat(animal, Condition.SUPER_STARVING, "speed", (animal.getSpeed()/3*(-1)));
        ValueShenanigans.affectIntStat(animal, Condition.SUPER_STARVING, "perception", (animal.getPerception()/3*(-1)));
        animal.setEvasionAdv(animal.getEvasionAdv()+2);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+2);

    }
    public void superStarvationEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.SUPER_STARVING);
        animal.setEvasionAdv(animal.getEvasionAdv()-2);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-2);
    }
    public void incapacitationStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Condition.INCAPACITATED, "speed", animal.getSpeed());
        ValueShenanigans.affectIntStat(animal, Condition.INCAPACITATED, "agility", animal.getAgility());
        ValueShenanigans.affectIntStat(animal, Condition.INCAPACITATED, "strength", animal.getStrength());
        ValueShenanigans.affectIntStat(animal, Condition.INCAPACITATED, "stealth", animal.getStealth());
    }
    public void incapacitationEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.INCAPACITATED);
    }
    public void restStart(Animal animal){
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public void restEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Condition.RESTING);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public void deepRestStart(Animal animal){
        animal.setPerceptionAdv(animal.getPerceptionAdv()-2);

    }
    public void deepRestEnd(Animal animal){
        animal.setPerceptionAdv(animal.getPerceptionAdv()+2);
    }

}
