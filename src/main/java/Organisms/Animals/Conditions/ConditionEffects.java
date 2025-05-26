package Organisms.Animals.Conditions;

import Organisms.Animals.Animal;

//how each condition affects the animal
public class ConditionEffects {
    //proposed system: list with all animals with certain effects, adding and removal from that list adds/removes effect
    public static void mildIllnessStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.MILDLY_ILL, "strength", animal.getStrength()/10);
        ValueShenanigans.affectIntStat(animal, Conditions.MILDLY_ILL, "agility", animal.getAgility()/10);
        ValueShenanigans.affectIntStat(animal, Conditions.MILDLY_ILL, "constitution", animal.getConstitution()/10);
        ValueShenanigans.affectIntStat(animal, Conditions.MILDLY_ILL, "speed", animal.getSpeed()/10);
    }
    public static void mildIllnessEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.MILDLY_ILL);

    }
    public static void severeIllnessStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.SEVERELY_ILL, "strength", animal.getStrength()/5);
        ValueShenanigans.affectIntStat(animal, Conditions.SEVERELY_ILL, "agility", animal.getAgility()/5);
        ValueShenanigans.affectIntStat(animal, Conditions.SEVERELY_ILL, "constitution", animal.getConstitution()/5);
        ValueShenanigans.affectIntStat(animal, Conditions.SEVERELY_ILL, "speed", animal.getSpeed()/5);
        animal.setStealthAdv(animal.getStealthAdv()-1);

    }
    public static void severeIllnessEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.SEVERELY_ILL);
    }
    public static void fatiguedStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.FATIGUED, "strength", animal.getStrength()/15);
        ValueShenanigans.affectIntStat(animal, Conditions.FATIGUED, "agility", animal.getAgility()/15);
        ValueShenanigans.affectIntStat(animal, Conditions.FATIGUED, "perception", animal.getPerception()/15);
        ValueShenanigans.affectIntStat(animal, Conditions.FATIGUED, "speed", animal.getSpeed()/15);
    }
    public static void fatiguedEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.FATIGUED);
    }
    public static void exhaustedStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.EXHAUSTED, "strength", animal.getStrength()/8);
        ValueShenanigans.affectIntStat(animal, Conditions.EXHAUSTED, "agility", animal.getAgility()/8);
        ValueShenanigans.affectIntStat(animal, Conditions.EXHAUSTED, "perception", animal.getPerception()/8);
        ValueShenanigans.affectIntStat(animal, Conditions.EXHAUSTED, "speed", animal.getSpeed()/10);
        animal.setStealthAdv(animal.getStealthAdv()-1);

    }
    public static void exhaustedEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.EXHAUSTED);
        animal.setStealthAdv(animal.getStealthAdv()+1);
    }
    public static void poisonStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.POISONED, "strength", animal.getStrength()/5);
        ValueShenanigans.affectIntStat(animal, Conditions.POISONED, "agility", animal.getAgility()/5);
        ValueShenanigans.affectIntStat(animal, Conditions.POISONED, "perception", animal.getPerception()/5);
        ValueShenanigans.affectIntStat(animal, Conditions.POISONED, "constitution", animal.getConstitution()/5);

    }
    public static void poisonEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.POISONED);
    }
    public static void hobbledStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.HOBBLED, "speed", animal.getSpeed()/3);
        ValueShenanigans.affectIntStat(animal, Conditions.HOBBLED, "agility", animal.getAgility()/5);
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
    }
    public static void hobbledEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.HOBBLED);
        animal.setEvasionAdv(animal.getEvasionAdv()+1);
    }
    public static void proneStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.PRONE, "speed", animal.getSpeed());
        ValueShenanigans.affectIntStat(animal, Conditions.PRONE, "agility", animal.getAgility()/3);
        animal.setEvasionAdv(animal.getEvasionAdv()-2);
    }
    public static void proneEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.PRONE);
        animal.setEvasionAdv(animal.getEvasionAdv()+2);
    }
    public static void grappleStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.GRAPPLED, "speed", animal.getSpeed());
        ValueShenanigans.affectIntStat(animal, Conditions.GRAPPLED, "agility", animal.getAgility()/2);
        animal.setEvasionAdv(animal.getEvasionAdv()-4);
    }
    public static void grappleEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.GRAPPLED);
        animal.setEvasionAdv(animal.getEvasionAdv()+4);
    }
    public static void stressStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.STRESSED, "speed", (animal.getSpeed()/7*(-1)));
        ValueShenanigans.affectIntStat(animal, Conditions.STRESSED, "agility", (animal.getAgility()/7*(-1)));
        ValueShenanigans.affectIntStat(animal, Conditions.STRESSED, "strength", (animal.getAgility()/7*(-1)));
        animal.setEvasionAdv(animal.getEvasionAdv()+1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public static void stressEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.STRESSED);
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public static void panicStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.PANICKING, "speed", (animal.getSpeed()/2*(-1)));
        ValueShenanigans.affectIntStat(animal, Conditions.PANICKING, "agility", (int) (animal.getAgility()*0.1));
        ValueShenanigans.affectIntStat(animal, Conditions.PANICKING, "strength", (animal.getAgility()/3*(-1)));
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public static void panicEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.PANICKING);
        animal.setEvasionAdv(animal.getEvasionAdv()+1);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public static void angerStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.ANGRY, "strength", (animal.getStrength()/3*(-1)));
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public static  void angerEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.ANGRY);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public static void bleedingStart(Animal animal){
        animal.setStealthAdv(animal.getStealthAdv()-1);
    }
    public static void bleedingEnd(Animal animal){
        animal.setStealthAdv(animal.getStealthAdv()+1);;
    }
    public static void severelyBleedingStart(Animal animal){
        animal.setStealthAdv(animal.getStealthAdv()-2);

    }
    public static void severelyBleedingEnd(Animal animal){
        animal.setStealthAdv(animal.getStealthAdv()+2);
        animal.setEvasionAdv(animal.getEvasionAdv()+1);
    }
    public static void disorientedStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.DISORIENTED, "agility", (int) (animal.getAgility()*0.25));
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public static void disorientedEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.DISORIENTED);
    }
    public static void dazedStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.DAZED, "agility", (int) (animal.getAgility()*0.5));
        ValueShenanigans.affectIntStat(animal, Conditions.DAZED, "perception",(int) (animal.getPerception()*0.25));
        animal.setPerceptionAdv(animal.getPerceptionAdv()-2);
        animal.setEvasionAdv(animal.getEvasionAdv()-1);
    }
    public static void dazedEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.DAZED);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+2);
    }
    public static void hungerStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.STARVING, "strength",((int) (animal.getStrength()*0.1)));
        ValueShenanigans.affectIntStat(animal, Conditions.STARVING, "agility", ((int)(animal.getAgility()*0.1)));
        ValueShenanigans.affectIntStat(animal, Conditions.STARVING, "speed", ((int)(animal.getSpeed()*0.1)));
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public static void hungerEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.HUNGRY);
    }
    public static void starvationStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.STARVING, "strength",((int) (animal.getStrength()*0.25)));
        ValueShenanigans.affectIntStat(animal, Conditions.STARVING, "agility", ((int)(animal.getAgility()*0.25)));
        ValueShenanigans.affectIntStat(animal, Conditions.STARVING, "speed", ((int)(animal.getSpeed()*0.25)));
        ValueShenanigans.affectIntStat(animal, Conditions.STARVING, "perception", (animal.getPerception()/5*(-1)));
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public static void starvationEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.STARVING);
    }
    public static void superStarvationStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.SUPER_STARVING, "strength", (animal.getStrength()/3*(-1)));
        ValueShenanigans.affectIntStat(animal, Conditions.SUPER_STARVING, "agility", (animal.getAgility()/3*(-1)));
        ValueShenanigans.affectIntStat(animal, Conditions.SUPER_STARVING, "speed", (animal.getSpeed()/3*(-1)));
        ValueShenanigans.affectIntStat(animal, Conditions.SUPER_STARVING, "perception", (animal.getPerception()/3*(-1)));
        animal.setEvasionAdv(animal.getEvasionAdv()+2);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+2);

    }
    public static void superStarvationEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.SUPER_STARVING);
        animal.setEvasionAdv(animal.getEvasionAdv()-2);
        animal.setPerceptionAdv(animal.getPerceptionAdv()-2);
    }
    public static void incapacitationStart(Animal animal){
        ValueShenanigans.affectIntStat(animal, Conditions.INCAPACITATED, "speed", animal.getSpeed());
        ValueShenanigans.affectIntStat(animal, Conditions.INCAPACITATED, "agility", animal.getAgility());
        ValueShenanigans.affectIntStat(animal, Conditions.INCAPACITATED, "strength", animal.getStrength());
        ValueShenanigans.affectIntStat(animal, Conditions.INCAPACITATED, "stealth", animal.getStealth());
    }
    public static void incapacitationEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.INCAPACITATED);
    }
    public static void restStart(Animal animal){
        animal.setPerceptionAdv(animal.getPerceptionAdv()-1);
    }
    public static void restEnd(Animal animal){
        ValueShenanigans.reverseIntChanges(animal, Conditions.RESTING);
        animal.setPerceptionAdv(animal.getPerceptionAdv()+1);
    }
    public static void deepRestStart(Animal animal){
        animal.setPerceptionAdv(animal.getPerceptionAdv()-2);

    }
    public static void deepRestEnd(Animal animal){
        animal.setPerceptionAdv(animal.getPerceptionAdv()+2);
    }

}
