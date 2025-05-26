package Organisms.Animals.Management.RestStuff;

import Organisms.Animals.Animal;
import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LevelUp {
    private static final Logger logger = Logger.getLogger(LevelUp.class.getName());

    public static void increaseFatStorage(Animal animal, int amount) {
        animal.setFatStorage((animal.getFatStorage() + amount / (animal.getFatStorage()/10)));
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints() - amount);
    }

    public static void increaseAttribute(Animal animal, String stat, int amount) {
        try {
            Field preferredStat;
            preferredStat = Animal.class.getDeclaredField(stat);
            preferredStat.setAccessible(true);
            preferredStat.setInt(animal, preferredStat.getInt(animal) + amount / preferredStat.getInt(animal));
            animal.setStoredEnergyPoints(animal.getStoredEnergyPoints() - amount);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to modify " + stat + " for " + animal, e);
        }
    }
    public static void increaseMaxHealth(Animal animal, int amount){
        animal.setMaxHealth(animal.getMaxHealth() + amount / animal.getSize());
        animal.setStoredEnergyPoints(animal.getStoredEnergyPoints() - amount);
    }
}
