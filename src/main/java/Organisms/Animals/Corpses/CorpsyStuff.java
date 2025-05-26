package Organisms.Animals.Corpses;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Conditions.Conditions;
import Organisms.Animals.Conditions.ValueShenanigans;
import util.SimManager;

public class CorpsyStuff {
    public static Corpse generateCorpse(Animal animal){
        MapStructure.Cell cell = animal.getCell();
        Corpse corpse = new Corpse();
        corpse.energyPoints=animal.getStoredEnergyPoints()+ (float) animal.getMaxHealth()*0.9f + animal.getFatStorage();
        corpse.size=animal.getSize();
        corpse.decayStage=DecayStage.FRESH;
        corpse.cell=cell;
        if(animal.conditions.contains(Conditions.SEVERELY_BLEEDING)
                || animal.conditions.contains(Conditions.INCAPACITATED)
                || animal.conditions.contains(Conditions.DAZED)
                || animal.conditions.contains(Conditions.PRONE)) corpse.decaySpeed=DecaySpeed.FAST;
        else if(animal.conditions.contains(Conditions.STARVING)
                || animal.conditions.contains(Conditions.SUPER_STARVING)) corpse.decaySpeed=DecaySpeed.SLOW;
        else corpse.decaySpeed=DecaySpeed.MEDIUM;
        cell.removeAnimal(animal);
        ValueShenanigans.cleanRecords(animal);
        cell.addCorpse(corpse);
        animal=null;
        return corpse;

    }
    public static void decay(Corpse corpse){
        int timeSinceDeath = SimManager.getTick()-corpse.getDeathDate();
        MapStructure.Cell cell = corpse.cell;
        if(corpse.decaySpeed.equals(DecaySpeed.SLOW)){
            switch (timeSinceDeath) {
                case (8) -> {
                    corpse.decayStage = DecayStage.SLIGHTLY_DECAYED;
                    corpse.energyPoints *= 0.9;
                }
                case (16) -> {
                    corpse.decayStage = DecayStage.DECAYED;
                    corpse.energyPoints *= 0.67;
                }
                case (24) -> {
                    corpse.decayStage = DecayStage.SEVERELY_DECAYED;
                    corpse.energyPoints *= 0.5;
                }
                case (32) -> {
                    cell.removeCorpse(corpse);
                    corpse = null;
                }
            }
        }
        else if(corpse.decaySpeed.equals(DecaySpeed.MEDIUM)){
            switch (timeSinceDeath) {
                case (4) -> {
                    corpse.decayStage = DecayStage.SLIGHTLY_DECAYED;
                    corpse.energyPoints *= 0.9;
                }
                case (8) -> {
                    corpse.decayStage = DecayStage.DECAYED;
                    corpse.energyPoints *= 0.67;
                }
                case (12) -> {
                    corpse.decayStage = DecayStage.SEVERELY_DECAYED;
                    corpse.energyPoints *= 0.5;
                }
                case (16) -> {
                    cell.removeCorpse(corpse);
                    corpse = null;
                }
            }
        }
        else {
            switch (timeSinceDeath) {
                case (2) -> {
                    corpse.decayStage = DecayStage.SLIGHTLY_DECAYED;
                    corpse.energyPoints *= 0.9;
                }
                case (4) -> {
                    corpse.decayStage = DecayStage.DECAYED;
                    corpse.energyPoints *= 0.67;
                }
                case (6) -> {
                    corpse.decayStage = DecayStage.SEVERELY_DECAYED;
                    corpse.energyPoints *= 0.5;
                }
                case (8) -> {
                    cell.removeCorpse(corpse);
                    corpse = null;
                }
            }

        }
    }
}
