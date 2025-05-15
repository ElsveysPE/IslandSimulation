package Organisms.Animals.Conditions;

import Organisms.Animals.Animal;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.logging.Level;

public class ValueShenanigans {
    private static final Logger logger = Logger.getLogger(ValueShenanigans.class.getName());
    public static HashMap<Animal, HashMap<Condition, HashMap<Field, Integer>>> originalIntValueStorage;
    public static HashMap<Animal, HashMap<Condition, HashMap<Field, Float>>> originalFloatValueStorage;
    static {
        originalIntValueStorage = new HashMap<>();
        originalFloatValueStorage = new HashMap<>();
    }
    public static void affectIntStat(Animal animal, Condition condition, String stat, int change) {
        try {
            Field field;
            field = Animal.class.getDeclaredField(stat);
            field.setAccessible(true);
            field.setInt(animal, field.getInt(animal) - change);
            if(originalIntValueStorage.containsKey(animal)) {
                if (originalIntValueStorage.get(animal).containsKey(condition)) {
                    originalIntValueStorage.get(animal).get(condition).put(field, change);
                } else {
                    originalIntValueStorage.get(animal).put(condition, new HashMap<>());
                    originalIntValueStorage.get(animal).get(condition).put(field, change);
                }
            }
            else {
                originalIntValueStorage.put(animal, new HashMap<>());
                originalIntValueStorage.get(animal).put(condition, new HashMap<>());
                originalIntValueStorage.get(animal).get(condition).put(field, change);
            }
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to modify " + stat + " for " + animal, e);
        }
    }
    public void affectFloatStat(Animal animal, Condition condition, String stat, float change){
        try {
            Field field;
            field = Animal.class.getDeclaredField(stat);
            field.setAccessible(true);
            field.setFloat(animal, field.getFloat(animal) - change);
            if(originalFloatValueStorage.containsKey(animal)) {
                if (originalFloatValueStorage.get(animal).containsKey(condition)) {
                    originalFloatValueStorage.get(animal).get(condition).put(field, change);
                } else {
                    originalFloatValueStorage.get(animal).put(condition, new HashMap<>());
                    originalFloatValueStorage.get(animal).get(condition).put(field, change);
                }
            }
            else {
                originalFloatValueStorage.put(animal, new HashMap<>());
                originalFloatValueStorage.get(animal).put(condition, new HashMap<>());
                originalFloatValueStorage.get(animal).get(condition).put(field, change);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.log(Level.SEVERE, "Failed to modify " + stat + " for " + animal, e);
        }
    }
    public static void reverseIntChanges(Animal animal, Condition condition) {
            if(originalIntValueStorage.containsKey(animal) && originalIntValueStorage.get(animal).containsKey(condition)){
                HashMap<Field, Integer> statChanges = originalIntValueStorage.get(animal).get(condition);
                    for (Entry<Field, Integer> entry : statChanges.entrySet()) {
                        Field key = entry.getKey();
                        int value = entry.getValue();

                        try {
                            key.setAccessible(true);
                            key.setInt(animal, key.getInt(animal) + value); // Reverse the change
                        } catch (IllegalAccessException e) {
                            logger.log(Level.SEVERE, "Failed to reverse changes for " + animal, e);
                        }
                    }
                originalIntValueStorage.get(animal).remove(condition);
                if (originalIntValueStorage.get(animal).isEmpty()) {
                    originalIntValueStorage.remove(animal);
                }
            }
            else logger.log(Level.WARNING, "Failed to find animal " + animal + " or condition " + condition);
        }

    public void reverseFloatChanges(Animal animal, Condition condition){
        HashMap<Field, Float> statChanges = new HashMap<>();
        if(originalFloatValueStorage.containsKey(animal) && originalFloatValueStorage.get(animal).containsKey(condition)){
            statChanges = originalFloatValueStorage.get(animal).get(condition);
            for (Entry<Field, Float> entry : statChanges.entrySet()) {
                Field key = entry.getKey();
                float value = entry.getValue();

                try {
                    key.setAccessible(true);
                    key.setFloat(animal, key.getFloat(animal) + value); // Reverse the change
                } catch (IllegalAccessException e) {
                    logger.log(Level.SEVERE, "Failed to reverse changes for " + animal, e);
                }
            }
            originalFloatValueStorage.get(animal).remove(condition);
            if (originalFloatValueStorage.get(animal).isEmpty()) {
                originalFloatValueStorage.remove(animal);
            }
        }
        else logger.log(Level.WARNING, "Failed to find animal " + animal + " or condition" + condition);
    }
    public static void cleanRecords(Animal animal){
        originalIntValueStorage.remove(animal);
        originalFloatValueStorage.remove(animal);
    }
}
