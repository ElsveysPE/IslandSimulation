package Organisms.Animals.Behaviours.BehaviourLogic.MovementLogic;

import Map.MapStructure;
import Organisms.Animals.Animal;
import Organisms.Animals.Corpses.Corpse;

import java.util.HashSet;

public class CellAnalysed {
        private MapStructure.Cell cell;
        public int currPlantCapacity;
        public static CellAnalysed formCellAnalysed(MapStructure.Cell cell) {
                CellAnalysed cellPA = new CellAnalysed();
                cellPA.cell= cell;
                cellPA.currPlantCapacity = cell.getCurrPlantCapacity();
                cellPA.resetDynamicFields();
                return cellPA;
        }
        public HashSet<Animal> sensedAnimals;
        public HashSet<Animal> dangerousAnimals;
        public HashSet<Animal> weakAnimals;
        public HashSet<Corpse> sensedCorpses;
        public int currAnimalCapacity;
        public int currDangerousAnimalCapacity;
        public int currWeakAnimalCapacity;
        public int cellDanger;
        public int cellPotentialDanger;
        public int cellFoodAvailability;
        public int cellHospitality;
        public int cellImportance;
        public void resetDynamicFields() {
                sensedAnimals = new HashSet<Animal>();
                sensedCorpses = new HashSet<Corpse>();
                weakAnimals = new HashSet<Animal>();
                dangerousAnimals = new HashSet<Animal>();
                cellDanger = 0;
                cellPotentialDanger = 0;
                cellFoodAvailability = 0;
                cellHospitality = 0;
                cellImportance = 0;
                currAnimalCapacity = 0;
                currWeakAnimalCapacity = 0;
                currDangerousAnimalCapacity = 0;
        }

        public MapStructure.Cell getCell() {
                return cell;
        }
}
