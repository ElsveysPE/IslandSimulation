package Organisms.Animals.Behaviours;

import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Plants.Plant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CellPostAnalysis {
        public MapStructure.Cell cell;
        public int x;
        public int y;
        public Terrain terrain;
        public int currPlantCapacity;
        public HashSet<Plant> plants;
        public static CellPostAnalysis fromCell(MapStructure.Cell cell) {
                CellPostAnalysis cellPA = new CellPostAnalysis();
                cellPA.x = cell.getX();
                cellPA.y = cell.getY();
                cellPA.terrain = cell.getTerrain();
                cellPA.currPlantCapacity = cell.getCurrPlantCapacity();
                cellPA.plants = cell.getPlants();
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
        public int currCorpseCapacity;
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
        }
