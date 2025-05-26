package Organisms.Animals.Behaviours.Miscellaneous;

import Map.MapStructure;
import Organisms.Animals.Animal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Territorial {
    static final Logger TERRITORIAL_LOGGER = Logger.getLogger(Territorial.class.getName());

    default void expandTerritory(Animal animal, MapStructure.Cell cell) {
        if (animal == null) {
            TERRITORIAL_LOGGER.log(Level.WARNING, "Cannot expand territory: animal instance is null.");
            return;
        }
        if (cell == null) {
            TERRITORIAL_LOGGER.log(Level.WARNING, "Animal " + animal.getId() + " cannot expand territory: cell is null.");
            return;
        }
        if (animal.getTerritories() == null) {
            TERRITORIAL_LOGGER.log(Level.WARNING, "Animal " + animal.getId() + " has null territories set. Cannot expand.");
            animal.setTerritories(new HashSet<>());
            return;
        }

        if (animal.getTerritories().add(cell)) {
            TERRITORIAL_LOGGER.log(Level.INFO, "Animal " + animal.getId() + " expanded territory to include cell: [" + cell.getX() + "," + cell.getY() + "].");
        } else {
            TERRITORIAL_LOGGER.log(Level.FINE, "Animal " + animal.getId() + " territory already included cell: [" + cell.getX() + "," + cell.getY() + "]. No change.");
        }
    }

    default void shrinkTerritory(Animal animal, MapStructure.Cell cell) {
        if (animal == null) {
            TERRITORIAL_LOGGER.log(Level.WARNING, "Cannot shrink territory: animal instance is null.");
            return;
        }
        if (cell == null) {
            TERRITORIAL_LOGGER.log(Level.WARNING, "Animal " + animal.getId() + " cannot shrink territory: cell is null.");
            return;
        }
        if (animal.getTerritories() == null) {
            TERRITORIAL_LOGGER.log(Level.WARNING, "Animal " + animal.getId() + " has null territories set. Cannot shrink.");
            return;
        }

        if (animal.getTerritories().remove(cell)) {
            TERRITORIAL_LOGGER.log(Level.INFO, "Animal " + animal.getId() + " shrunk territory, removing cell: [" + cell.getX() + "," + cell.getY() + "].");
        } else {
            TERRITORIAL_LOGGER.log(Level.FINE, "Animal " + animal.getId() + " territory did not include cell: [" + cell.getX() + "," + cell.getY() + "]. No change.");
        }
    }

    default HashSet<MapStructure.Cell> getAnimalTerritories(Animal animalInstance) {
        if (animalInstance != null && animalInstance.getTerritories() != null) {
            return animalInstance.getTerritories();
        }
        if (animalInstance == null) {
            TERRITORIAL_LOGGER.log(Level.WARNING, "Attempted to get territories for a null animal instance.");
        } else {
            TERRITORIAL_LOGGER.log(Level.WARNING, "Animal " + animalInstance.getId() + " has null territories set.");
        }
        return new HashSet<>();
    }
}

