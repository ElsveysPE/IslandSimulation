package Map;

import Organisms.Animals.Animal;
import Organisms.Animals.Corpses.Corpse;
import Organisms.Organism;

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

        private Growth growth;

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
                if (currAnimalCapacity < 0) {
                    logger.log(Level.WARNING, "currAnimalCapacity fell below zero in cell [{0},{1}], resetting to 0.", new Object[]{this.x, this.y});
                    currAnimalCapacity = 0;
                } else {
                    currAnimalCapacity -= animal.getSize();
                    logger.log(Level.FINER,
                            "Animal {0} removed from cell [{1},{2}]. Current animal capacity: {3}",
                            new Object[]{animal.getId(), this.x, this.y, currAnimalCapacity});
                }
            }else {
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

            this.animals = new HashSet<>();
            this.corpses = new HashSet<>();

            this.currPlantCapacity = 0;
            this.currAnimalCapacity = 0;
            this.currCorpseCapacity = 0;
            this.growth = Growth.NONE;

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
        public void setCurrPlantCapacity(int currPlantCapacity){
            this.currAnimalCapacity= currPlantCapacity;
        }

        public Growth getGrowth() {
            return growth;
        }

        public void setGrowth(Growth growth) {
            this.growth = growth;
        }
    }
}


