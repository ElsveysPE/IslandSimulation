package util;
import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Species.Wolf; // Import concrete species
import Organisms.Animals.Species.Rabbit; // Import concrete species

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
public class Spawner {

    private static final Random random = new Random();
    // Logger for spawning events
    private static final Logger logger = Logger.getLogger(Spawner.class.getName());

    public static void spawnInitialPopulation(List<Animal> animalList, MapStructure.Cell[][] map, int wolfCount, int rabbitCount) {
        logger.log(Level.INFO, "Spawning initial population: {0} Wolves, {1} Rabbits.", new Object[]{wolfCount, rabbitCount});
        int wolvesSpawned = 0;
        for (int i = 0; i < wolfCount; i++) {
            Animal wolf = spawnWolf(map);
            if (wolf != null) {
                animalList.add(wolf);
                wolvesSpawned++;
            } else {
                logger.log(Level.WARNING, "Failed to spawn Wolf {0} of {1} (no valid cell found?).", new Object[]{i + 1, wolfCount});
            }
        }

        int rabbitsSpawned = 0;
        for (int i = 0; i < rabbitCount; i++) {
            Animal rabbit = spawnRabbit(map);
            if (rabbit != null) {
                animalList.add(rabbit);
                rabbitsSpawned++;
            } else {
                logger.log(Level.WARNING, "Failed to spawn Rabbit {0} of {1} (no valid cell found?).", new Object[]{i + 1, rabbitCount});
            }
        }
        logger.log(Level.INFO, "Finished spawning initial population. Spawned: {0} Wolves, {1} Rabbits. Total animals: {2}",
                new Object[]{wolvesSpawned, rabbitsSpawned, animalList.size()});
    }


    public static Wolf spawnWolf(MapStructure.Cell[][] map) {
        // 1. Find a random valid starting cell
        MapStructure.Cell startCell = findRandomValidCell(map);
        if (startCell == null) {
            logger.log(Level.SEVERE, "Could not find a valid cell to spawn Wolf!");
            return null;
        }


        int maxHealth = getRandomInRange(50, 100);
        int maxAge = getRandomInRange(10*4, 15*4);
        int size = getRandomInRange(15, 25);
        int agility = getRandomInRange(10, 20);
        int constitution = getRandomInRange(12, 22);
        int strength = getRandomInRange(12, 22);
        int speed = getRandomInRange(10, 20);
        int perception = getRandomInRange(11, 21);
        int stealth = getRandomInRange(8, 18);
        int minDamage = getRandomInRange(5, 10);
        float physRes = random.nextFloat() * 0.075f;
        int attackAdv = 0;
        int stealthAdv = 0;
        int perceptionAdv = 0;
        int evasionAdv = 0;
        int battleActionPoints = 3;
        int reactionPoints = 2;
        float initialEnergy = 80.0f + random.nextFloat() * 40f;
        int initialFat = getRandomInRange(20, 40);


        Wolf newWolf = new Wolf(startCell, maxHealth, maxAge, size, agility, constitution,
                strength, speed, perception, stealth, minDamage, physRes,
                attackAdv, stealthAdv, perceptionAdv, evasionAdv,
                battleActionPoints, reactionPoints, initialEnergy, initialFat);

        return newWolf;
    }

    public static Rabbit spawnRabbit(MapStructure.Cell[][] map) {
        MapStructure.Cell startCell = findRandomValidCell(map);
        if (startCell == null) {
            logger.log(Level.SEVERE, "Could not find a valid cell to spawn Rabbit!");
            return null;
        }

        int maxHealth = getRandomInRange(10, 35);
        int maxAge = getRandomInRange(5*4, 8*4);
        int size = getRandomInRange(4, 8);
        int agility = getRandomInRange(12, 22);
        int constitution = getRandomInRange(8, 18);
        int strength = getRandomInRange(4, 14);
        int speed = getRandomInRange(10, 20);
        int perception = getRandomInRange(12, 22);
        int stealth = getRandomInRange(12, 22);
        int minDamage = getRandomInRange(1, 2);
        float physRes = 0.0f;
        int attackAdv = 0;
        int stealthAdv = 0;
        int perceptionAdv = 0;
        int evasionAdv = 0;
        int battleActionPoints = 2;
        int reactionPoints = 3;
        float initialEnergy = 40.0f + random.nextFloat() * 20f;
        int initialFat = getRandomInRange(5, 10);


        Rabbit newRabbit = new Rabbit(startCell, maxHealth, maxAge, size, agility, constitution,
                strength, speed, perception, stealth, minDamage, physRes,
                attackAdv, stealthAdv, perceptionAdv, evasionAdv,
                battleActionPoints, reactionPoints, initialEnergy, initialFat);

        return newRabbit;
    }


    private static MapStructure.Cell findRandomValidCell(MapStructure.Cell[][] map) {
        if (map == null || map.length == 0 || map[0].length == 0) {
            logger.log(Level.SEVERE, "Map is null or empty in findRandomValidCell.");
            return null;
        }
        int width = map.length;
        int height = map[0].length;
        int attempts = 0;
        int maxAttempts = width * height * 3;

        while(attempts < maxAttempts) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            MapStructure.Cell cell = map[x][y];

            if (cell != null && cell.getTerrain() != Terrain.WATER && cell.getTerrain() != Terrain.PLACEHOLDER) {
                return cell;
            }
            attempts++;
        }
        logger.log(Level.SEVERE, "Could not find a valid spawn cell after {0} attempts.", maxAttempts);
        return null;
    }


    private static int getRandomInRange(int min, int max) {
        if (min >= max) {
            // Handle invalid range if necessary, or just return min
            // logger.log(Level.WARNING, "Invalid range in getRandomInRange: min({0}) >= max({1})", new Object[]{min, max});
            return min;
        }
        // random.nextInt(bound) generates 0 to bound-1, so add 1 to max-min
        return random.nextInt((max - min) + 1) + min;
    }
}