package Organisms.Animals.Behaviours;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Conditions.Condition;
import Organisms.Animals.Tags;

import static Organisms.Animals.Behaviours.Miscellaneous.CaresAboutProgeny.progenyLocation;
import static Organisms.Animals.Behaviours.Miscellaneous.Social.getGroup;
import static Organisms.Animals.Behaviours.Miscellaneous.Territorial.territories;

public interface TagHandlers {
        default void processDefense(CellPostAnalysis cellPA, Animal animal) {
            // Handle ARMOUR, SPIKY, EVADE, DEFEND
            if (cellPA.currPlantCapacity>=75) {
                cellPA.cellPotentialDanger+=40;
            }
            else if (cellPA.currPlantCapacity>=50) {
                cellPA.cellPotentialDanger+=20;
            }
            else if (cellPA.currPlantCapacity>=25){
                cellPA.cellPotentialDanger-=20;
            }
            else cellPA.cellPotentialDanger-=40;
            if(animal.getTags().contains(Tags.ARMOUR)) {
                cellPA.cellPotentialDanger*=1.5;
            }
            if(animal.getTags().contains(Tags.SPIKY)) {
                cellPA.cellPotentialDanger/=2;
            }
            if(animal.getTags().contains(Tags.EVADE)) {
                cellPA.cellPotentialDanger*=1.5;
            }
            if(animal.getTags().contains(Tags.DEFEND)) {
                cellPA.cellPotentialDanger/=2;
            }
        }

        default void processDiet(CellPostAnalysis cellPA, Animal animal) {
            // Handle CARNY, HERBY, OMNY
            if(animal.getTags().contains(Tags.CARNY) && !animal.getTags().contains(Tags.SCAVENGER)) {
                if (animal.conditions.contains(Condition.SUPER_STARVING)){
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity * 8;
                    cellPA.cellFoodAvailability += cellPA.currAnimalCapacity  - cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity * 4;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity;
                }
                else if (animal.conditions.contains(Condition.STARVING)) {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity * 4;
                    cellPA.cellFoodAvailability += (cellPA.currAnimalCapacity - cellPA.currWeakAnimalCapacity)/2;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity * 2;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity;
                } else if (animal.conditions.contains(Condition.HUNGRY)) {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity * 3;
                    cellPA.cellFoodAvailability += cellPA.currAnimalCapacity - cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity/2;
                }

                else {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity * 2;
                    cellPA.cellFoodAvailability += cellPA.currAnimalCapacity - cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity / 2;
                }
            }
            if(animal.getTags().contains(Tags.CARNY) && animal.getTags().contains(Tags.SCAVENGER)){
                if (animal.conditions.contains(Condition.SUPER_STARVING)){
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity * 4;
                    cellPA.cellFoodAvailability += cellPA.currAnimalCapacity  - cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity * 10;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity;
                }
                else if (animal.conditions.contains(Condition.STARVING)) {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity * 3;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity;
                } else if (animal.conditions.contains(Condition.HUNGRY)) {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity / 2;
                    cellPA.cellFoodAvailability += cellPA.currAnimalCapacity / 4;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity / 2;
                }

                else {
                    cellPA.cellFoodAvailability += cellPA.currAnimalCapacity / 2;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity;
                }
            }
            if(animal.getTags().contains(Tags.HERBY)) {
                if (animal.conditions.contains(Condition.SUPER_STARVING)){
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity*8;
                }
                else if (animal.conditions.contains(Condition.STARVING)) {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity / 4;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity / 2;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity*4;
                } else if (animal.conditions.contains(Condition.HUNGRY)) {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity / 2;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity / 4;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity*2;
                }
                else cellPA.cellFoodAvailability+=cellPA.currPlantCapacity;
            }
            if(animal.getTags().contains(Tags.OMNY)){
                if (animal.conditions.contains(Condition.SUPER_STARVING)){
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity * 6;
                    cellPA.cellFoodAvailability += (cellPA.currAnimalCapacity  - cellPA.currWeakAnimalCapacity)*2;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity * 4;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity*4;
                }
                else if (animal.conditions.contains(Condition.STARVING)) {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity * 5;
                    cellPA.cellFoodAvailability += (cellPA.currAnimalCapacity - cellPA.currWeakAnimalCapacity);
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity * 4;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity*4;
                } else if (animal.conditions.contains(Condition.HUNGRY)) {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity * 3;
                    cellPA.cellFoodAvailability += cellPA.currAnimalCapacity - cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity*2;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity*2;
                }

                else {
                    cellPA.cellFoodAvailability += cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currAnimalCapacity - cellPA.currWeakAnimalCapacity;
                    cellPA.cellFoodAvailability += cellPA.currPlantCapacity;
                    cellPA.cellFoodAvailability += cellPA.currCorpseCapacity;
                }
            }
        }

        default void processSocial(CellPostAnalysis cellPA, Animal animal) {
            // Handle SOCIAL, CARING, TERRITORIAL
            if (animal.getTags().contains(Tags.SOCIAL)) {
                for (Animal animal1 : getGroup())
                    if (cellPA.sensedAnimals.contains(animal1)) cellPA.cellHospitality = +10;
                    else cellPA.cellHospitality = -5;
            }
            if (animal.getTags().contains(Tags.CARING)) {
                if (cellPA.x == progenyLocation.getX() && cellPA.y == progenyLocation.getY())
                    cellPA.cellImportance = +10;
            }
            if (animal.getTags().contains(Tags.TERRITORIAL)) {
                for (MapStructure.Cell cell : territories) {
                    if (cellPA.x == cell.getX() && cellPA.y == cell.getY()) {
                        cellPA.cellHospitality = +10;
                        cellPA.cellImportance = +10;
                    } else {
                        cellPA.cellHospitality = -10;
                        cellPA.cellImportance = -10;
                    }
                }
            }
        }

        default void processHunting(CellPostAnalysis cellPA, Animal animal) {
            // Handle AGILE, FORAGER, SCAVENGER, STRONG
            if(animal.getTags().contains(Tags.AGILE)) {
                if (cellPA.currPlantCapacity>=75) {
                    cellPA.cellHospitality=+8;
                }
                else if (cellPA.currPlantCapacity>=50) {
                    cellPA.cellHospitality=+4;
                }
            }
            if(animal.getTags().contains(Tags.FORAGER)) {

            }
            if(animal.getTags().contains(Tags.SCAVENGER)) {
                //scavenger dietary preferences are handled in processDiet

            }
            if(animal.getTags().contains(Tags.STRONG)) {
                if (cellPA.currPlantCapacity>=75) {
                    cellPA.cellPotentialDanger=+2;
                }
                else if (cellPA.currPlantCapacity>=50) {
                    cellPA.cellPotentialDanger++;
                }
            }
        }
    }

