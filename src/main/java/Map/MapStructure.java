package Map;

import Organisms.Animals.Animal;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Organism;
import Organisms.Plants.Plant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MapStructure {
    public static class Cell {
        private static final Logger logger = Logger.getLogger(Cell.class.getName());
        private int x;
        private int y;
        private final int plantCapacity = 100;

        public int getPlantCapacity() {
            return plantCapacity;
        }

        private int currPlantCapacity;

        public int getCurrPlantCapacity() {
            return currPlantCapacity;
        }
        private HashSet<Plant> plants;
        public HashSet<Plant> getPlants(){
            return plants;
        }

        public void addPlant(Plant plant) {
            if (currPlantCapacity + plant.getSize() <= plantCapacity) {
                if (this.plants.add(plant)) {
                    currPlantCapacity += plant.getSize();
                    logger.log(Level.FINER, "Plant[{0}] successfully added to cell [{0},{1}]", new Object[]{x, y, plant.getId()});
                }
                else  logger.log(Level.WARNING, "Couldn`t add plant [{0}] to cell [{1},{2}] it already existed",
                        new Object[]{plant.getId(), x, y});
            } else {
                logger.log(Level.FINE, "Plant capacity reached in cell [{0},{1}], cannot add {2}", new Object[]{x, y, plant.getId()});
            }
        }

        public void removePlant(Plant plant) {
            if (this.plants.remove(plant)) {
                currPlantCapacity -= plant.getSize();
                if (currPlantCapacity < 0) currPlantCapacity = 0;
                logger.log(Level.FINER,"Plant[{0}] successfully removed from cell [{0},{1}]", new Object[]{x, y, plant.getId()});
            }
            else  logger.log(Level.WARNING, "Couldn`t remove plant [{0}] from cell [{1},{2}] it didn`t exist",
                    new Object[]{plant.getId(), x, y});
        }

        private int currAnimalCapacity;

        public int getCurrAnimalCapacity() {
            return currAnimalCapacity;
        }

        private HashSet<Animal> animals;
        private HashSet<Corpse> corpses;
        public HashSet<Corpse> getCorpses() {
            return corpses;
        }

        public void addCorpse(Corpse corpse) {
            if (this.corpses.add(corpse)) {
                currCorpseCapacity += corpse.getSize();
                logger.log(Level.FINER,
                        "Corpse {0} added to cell [{1},{2}]. Current corpse capacity: {3}",
                        new Object[]{corpse, this.x, this.y, currCorpseCapacity});
            } else {
                logger.log(Level.WARNING,
                        "Attempted to add Corpse {0} to cell [{1},{2}], but it already exists.",
                        new Object[]{corpse, this.x, this.y});
            }
        }

        public void removeCorpse(Corpse corpse) {
            if (this.corpses.remove(corpse)) {
                currCorpseCapacity -= corpse.getSize();
                if (currCorpseCapacity < 0) {
                    logger.log(Level.WARNING, "currCorpseCapacity fell below zero in cell [{0},{1}], resetting to 0.", new Object[]{this.x, this.y});
                    currCorpseCapacity = 0;
                }
                logger.log(Level.FINER,
                        "Corpse {0} removed from cell [{1},{2}]. Current corpse capacity: {3}",
                        new Object[]{corpse, this.x, this.y, currCorpseCapacity});
            } else {
                logger.log(Level.WARNING,
                        "Attempted to remove Corpse {0} from cell [{1},{2}], but it was not present.",
                        new Object[]{corpse, this.x, this.y});
            }
        }
        private int currCorpseCapacity;
        public int getCurrCorpseCapacity() {
            return currCorpseCapacity;
        }

        public void addAnimal(Animal animal) {
            if (this.animals.add(animal)) {
                currAnimalCapacity += animal.getSize();
                logger.log(Level.FINER,
                        "Animal {0} added to cell [{1},{2}]. Current animal capacity: {3}",
                        new Object[]{animal.getId(), this.x, this.y, currAnimalCapacity});
            } else {
                logger.log(Level.WARNING,
                        "Attempted to add Animal {0} to cell [{1},{2}], but it already exists.",
                        new Object[]{animal.getId(), this.x, this.y});
            }
        }

        public void removeAnimal(Animal animal) {
            if (this.animals.remove(animal)) {
                currAnimalCapacity -= animal.getSize();
                if (currAnimalCapacity < 0) {
                    logger.log(Level.WARNING, "currAnimalCapacity fell below zero in cell [{0},{1}], resetting to 0.", new Object[]{this.x, this.y});
                    currAnimalCapacity = 0;
                }
                logger.log(Level.FINER,
                        "Animal {0} removed from cell [{1},{2}]. Current animal capacity: {3}",
                        new Object[]{animal.getId(), this.x, this.y, currAnimalCapacity});
            } else {
                logger.log(Level.WARNING,
                        "Attempted to remove Animal {0} from cell [{1},{2}], but it was not present.",
                        new Object[]{animal.getId(), this.x, this.y});
            }
        }


        public HashSet<Animal> getAnimals(){
            return animals;
        }
        private Terrain terrain;

        public Cell(int x, int y, Terrain terrain) {
            this.x = x;
            this.y = y;
            this.terrain = terrain;

            this.plants = new HashSet<>();
            this.animals = new HashSet<>();
            this.corpses = new HashSet<>();

            this.currPlantCapacity = 0;
            this.currAnimalCapacity = 0;
            this.currCorpseCapacity = 0;

        }

        public int getX() {
            return this.x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return this.y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Terrain getTerrain() {
            return this.terrain;
        }

        public void setTerrain(Terrain terrain) {
            this.terrain = terrain;
        }
    }
}


