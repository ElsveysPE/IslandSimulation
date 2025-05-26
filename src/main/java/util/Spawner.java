package util;
import Map.MapStructure;
import Map.Terrain;
import Organisms.Animals.Animal;
import Organisms.Animals.Species.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static Map.MapGeneration.getIslandMap;
import static util.SimManager.getAllAnimals;

public class Spawner {

    private static final Random random = new Random();
    private static final Logger logger = Logger.getLogger(Spawner.class.getName());

    private static final int RABBIT_LITTER_SIZE = 5;
    private static final int IGUANA_LITTER_SIZE = 5;
    private static final int SOCIAL_ANIMAL_GROUP_SIZE = 3; // For Wolves, Deer, Foxes initial groups
    private static final int DEFAULT_SPAWN_COUNT = 10; // Default count for each species (or group/litter type)


    public static void spawnDefaultInitialPopulation( ) {
        logger.log(Level.INFO, "Spawning default initial population with " + DEFAULT_SPAWN_COUNT + " instances/groups/litters per species type.");
        spawnInitialPopulation(
                DEFAULT_SPAWN_COUNT, // wolfGroupCount
                DEFAULT_SPAWN_COUNT, // rabbitLitterCount
                DEFAULT_SPAWN_COUNT, // bearCount
                DEFAULT_SPAWN_COUNT, // tigerCount
                DEFAULT_SPAWN_COUNT, // deerGroupCount
                DEFAULT_SPAWN_COUNT, // foxGroupCount
                DEFAULT_SPAWN_COUNT  // iguanaLitterCount
        );
    }

    public static void spawnInitialPopulation(
                                              int wolfGroupCount,
                                              int rabbitLitterCount,
                                              int bearCount,
                                              int tigerCount,
                                              int deerGroupCount,
                                              int foxGroupCount,
                                              int iguanaLitterCount) {
        logger.log(Level.INFO, "Spawning initial population: {0} Wolf groups (of {1}), {2} Rabbit litters (of {3}), {4} Bears, {5} Tigers, {6} Deer groups (of {1}), {7} Fox groups (of {1}), {8} Iguana litters (of {9}).",
                new Object[]{wolfGroupCount, SOCIAL_ANIMAL_GROUP_SIZE, rabbitLitterCount, RABBIT_LITTER_SIZE, bearCount, tigerCount, deerGroupCount, foxGroupCount, iguanaLitterCount, IGUANA_LITTER_SIZE});

        MapStructure.Cell startCell;
        int individualWolvesSpawned = 0;
        for (int i = 0; i < wolfGroupCount; i++) {
            startCell = findRandomValidCell(getIslandMap());
            if (startCell != null) {
                List<Wolf> group = spawnWolfGroup(startCell);
                if (!group.isEmpty()) { individualWolvesSpawned += group.size(); }
            } else {
                logger.log(Level.WARNING, "Failed to find valid cell for Wolf group {0} of {1}.", new Object[]{i + 1, wolfGroupCount});
            }
        }

        int individualRabbitsSpawned = 0;
        for (int i = 0; i < rabbitLitterCount; i++) {
            startCell = findRandomValidCell(getIslandMap());
            if (startCell != null) {
                List<Rabbit> litter = spawnRabbitLitter(startCell);
                if (!litter.isEmpty()) { individualRabbitsSpawned += litter.size(); }
            } else {
                logger.log(Level.WARNING, "Failed to find valid cell for Rabbit litter {0} of {1}.", new Object[]{i + 1, rabbitLitterCount});
            }
        }

        int bearsSpawned = 0;
        for (int i = 0; i < bearCount; i++) {
            startCell = findRandomValidCell(getIslandMap());
            if (startCell != null) {
                Animal bear = spawnBear(startCell);
                if (bear != null) { bearsSpawned++; }
            } else {
                logger.log(Level.WARNING, "Failed to find valid cell for Bear {0} of {1}.", new Object[]{i + 1, bearCount});
            }
        }

        int tigersSpawned = 0;
        for (int i = 0; i < tigerCount; i++) {
            startCell = findRandomValidCell(getIslandMap());
            if (startCell != null) {
                Animal tiger = spawnTiger(startCell);
                if (tiger != null) { tigersSpawned++; }
            } else {
                logger.log(Level.WARNING, "Failed to find valid cell for Tiger {0} of {1}.", new Object[]{i + 1, tigerCount});
            }
        }

        int individualDeerSpawned = 0;
        for (int i = 0; i < deerGroupCount; i++) {
            startCell = findRandomValidCell(getIslandMap());
            if (startCell != null) {
                List<Deer> group = spawnDeerGroup(startCell);
                if (!group.isEmpty()) { individualDeerSpawned += group.size(); }
            } else {
                logger.log(Level.WARNING, "Failed to find valid cell for Deer group {0} of {1}.", new Object[]{i + 1, deerGroupCount});
            }
        }

        int individualFoxesSpawned = 0;
        for (int i = 0; i < foxGroupCount; i++) {
            startCell = findRandomValidCell(getIslandMap());
            if (startCell != null) {
                List<Fox> group = spawnFoxGroup(startCell);
                if (!group.isEmpty()) { individualFoxesSpawned += group.size(); }
            } else {
                logger.log(Level.WARNING, "Failed to find valid cell for Fox group {0} of {1}.", new Object[]{i + 1, foxGroupCount});
            }
        }

        int individualIguanasSpawned = 0;
        for (int i = 0; i < iguanaLitterCount; i++) {
            startCell = findRandomValidCell(getIslandMap());
            if (startCell != null) {
                List<Iguana> litter = spawnIguanaLitter(startCell);
                if (!litter.isEmpty()) {individualIguanasSpawned += litter.size(); }
            } else {
                logger.log(Level.WARNING, "Failed to find valid cell for Iguana litter {0} of {1}.", new Object[]{i + 1, iguanaLitterCount});
            }
        }

        logger.log(Level.INFO, "Finished spawning initial population. Spawned: {0} Wolves, {1} Rabbits, {2} Bears, {3} Tigers, {4} Deer, {5} Foxes, {6} Iguanas. Total animals: {7}",
                new Object[]{individualWolvesSpawned, individualRabbitsSpawned, bearsSpawned, tigersSpawned, individualDeerSpawned, individualFoxesSpawned, individualIguanasSpawned, getAllAnimals().size()});
    }

    // --- Individual Spawning Methods (for breeding or specific placement) ---

    public static Wolf spawnWolf(MapStructure.Cell startCell) {
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Wolf: startCell is null!"); return null; }
        int size = getRandomInRange(5, 10);
        int agility = getRandomInRange(8, 15);
        int constitution = getRandomInRange(8, 15);
        int strength = getRandomInRange(7, 12);
        int speed = getRandomInRange(8, 15);
        int perception = getRandomInRange(10, 18);
        int stealth = getRandomInRange(10, 20);
        int minDamage = getRandomInRange(2, 5);
        float physRes = 1;
        int attackAdv = 0; int stealthAdv = 0; int perceptionAdv = 0; int evasionAdv = 0;
        int battleActionPoints = 2; int reactionPoints = 1;
        float initialEnergy = 40.0f + random.nextFloat() * 20f;
        int initialFat = getRandomInRange(10, 20);
        Wolf newWolf = new Wolf(startCell, size, agility, constitution, strength, speed, perception, stealth, minDamage, physRes, attackAdv, stealthAdv, perceptionAdv, evasionAdv, battleActionPoints, reactionPoints, initialEnergy, initialFat);
        getAllAnimals().add(newWolf);
        logger.log(Level.FINE, "Spawned individual Wolf {0} (Age: {1}/{2}) at cell [{3},{4}].", new Object[]{newWolf.getId(), newWolf.getCurrAge(), newWolf.getMaxAge(), startCell.getX(), startCell.getY()});
        return newWolf;
    }

    public static Deer spawnDeer(MapStructure.Cell startCell) {
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Deer: startCell is null!"); return null; }
        int size = getRandomInRange(4, 8); int agility = getRandomInRange(12, 20); int constitution = getRandomInRange(7, 14);
        int strength = getRandomInRange(3, 7); int speed = getRandomInRange(10, 18); int perception = getRandomInRange(10, 18);
        int stealth = getRandomInRange(12, 20); int minDamage = getRandomInRange(1, 3); float physRes = random.nextFloat() * 0.01f;
        int attackAdv = 0; int stealthAdv = 0; int perceptionAdv = 0; int evasionAdv = 1;
        int battleActionPoints = 1; int reactionPoints = 2;
        float initialEnergy = 30.0f + random.nextFloat() * 15f; int initialFat = getRandomInRange(10, 20);
        Deer newDeer = new Deer(startCell, size, agility, constitution, strength, speed, perception, stealth, minDamage, physRes, attackAdv, stealthAdv, perceptionAdv, evasionAdv, battleActionPoints, reactionPoints, initialEnergy, initialFat);
        getAllAnimals().add(newDeer);
        logger.log(Level.FINE, "Spawned individual Deer {0} (Age: {1}/{2}) at cell [{3},{4}].", new Object[]{newDeer.getId(), newDeer.getCurrAge(), newDeer.getMaxAge(), startCell.getX(), startCell.getY()});
        return newDeer;
    }

    public static Fox spawnFox(MapStructure.Cell startCell) {
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Fox: startCell is null!"); return null; }
        int size = getRandomInRange(3, 6); int agility = getRandomInRange(12, 22); int constitution = getRandomInRange(6, 12);
        int strength = getRandomInRange(4, 8); int speed = getRandomInRange(10, 20); int perception = getRandomInRange(11, 20);
        int stealth = getRandomInRange(14, 24); int minDamage = getRandomInRange(2, 5); float physRes =1;
        int attackAdv = 0; int stealthAdv = 1; int perceptionAdv = 0; int evasionAdv = 1;
        int battleActionPoints = 2; int reactionPoints = 2;
        float initialEnergy = 35.0f + random.nextFloat() * 15f; int initialFat = getRandomInRange(8, 18);
        Fox newFox = new Fox(startCell, size, agility, constitution, strength, speed, perception, stealth, minDamage, physRes, attackAdv, stealthAdv, perceptionAdv, evasionAdv, battleActionPoints, reactionPoints, initialEnergy, initialFat);
        getAllAnimals().add(newFox);
        logger.log(Level.FINE, "Spawned individual Fox {0} (Age: {1}/{2}) at cell [{3},{4}].", new Object[]{newFox.getId(), newFox.getCurrAge(), newFox.getMaxAge(), startCell.getX(), startCell.getY()});
        return newFox;
    }

    // --- Group/Litter Spawning Methods (for initial population) ---

    public static List<Wolf> spawnWolfGroup(MapStructure.Cell startCell) {
        List<Wolf> group = new ArrayList<>();
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Wolf group: startCell is null!"); return group; }
        for (int i = 0; i < SOCIAL_ANIMAL_GROUP_SIZE; i++) {
            Wolf newWolf = new Wolf(startCell, getRandomInRange(15, 25), getRandomInRange(10, 20), getRandomInRange(12, 22), getRandomInRange(12, 22), getRandomInRange(10, 20), getRandomInRange(11, 21), getRandomInRange(8, 18), getRandomInRange(5, 10), 1, 0, 0, 0, 0, 3, 2, 80.0f + random.nextFloat() * 40f, getRandomInRange(20, 40));
            group.add(newWolf);
            getAllAnimals().add(newWolf);
            logger.log(Level.FINE, "Spawned Wolf (member {0}/{1} of group) {2} (Age: {3}/{4}) at cell [{5},{6}].", new Object[]{i+1, SOCIAL_ANIMAL_GROUP_SIZE, newWolf.getId(), newWolf.getCurrAge(), newWolf.getMaxAge(), startCell.getX(), startCell.getY()});
        }
        if (!group.isEmpty()) { logger.log(Level.INFO, "Spawned a group of {0} Wolves at cell [{1},{2}].", new Object[]{group.size(), startCell.getX(), startCell.getY()});}
        return group;
    }

    public static Bear spawnBear(MapStructure.Cell startCell) {
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Bear: startCell is null!"); return null; }
        Bear newBear = new Bear(startCell, getRandomInRange(25, 40), getRandomInRange(8, 15), getRandomInRange(18, 28), getRandomInRange(18, 28), getRandomInRange(7, 14), getRandomInRange(10, 18), getRandomInRange(5, 12), getRandomInRange(8, 15), 1, 0, 0, 0, 0, 3, 1, 100.0f + random.nextFloat() * 50f, getRandomInRange(30, 60));
        logger.log(Level.FINE, "Spawned Bear {0} (Age: {1}/{2}) at cell [{3},{4}].", new Object[]{newBear.getId(), newBear.getCurrAge(), newBear.getMaxAge(), startCell.getX(), startCell.getY()});
        getAllAnimals().add(newBear);
        return newBear;
    }

    public static Tiger spawnTiger(MapStructure.Cell startCell) {
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Tiger: startCell is null!"); return null; }
        Tiger newTiger = new Tiger(startCell, getRandomInRange(20, 35), getRandomInRange(15, 25), getRandomInRange(10, 20), getRandomInRange(15, 25), getRandomInRange(12, 22), getRandomInRange(12, 22), getRandomInRange(15, 25), getRandomInRange(7, 14), 1, 0, 1, 0, 0, 3, 2, 90.0f + random.nextFloat() * 45f, getRandomInRange(25, 50));
        logger.log(Level.FINE, "Spawned Tiger {0} (Age: {1}/{2}) at cell [{3},{4}].", new Object[]{newTiger.getId(), newTiger.getCurrAge(), newTiger.getMaxAge(), startCell.getX(), startCell.getY()});
        getAllAnimals().add(newTiger);
        return newTiger;
    }

    public static List<Rabbit> spawnRabbitLitter(MapStructure.Cell startCell) {
        List<Rabbit> litter = new ArrayList<>();
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Rabbit litter: startCell is null!"); return litter; }
        for (int i = 0; i < RABBIT_LITTER_SIZE; i++) {
            Rabbit newRabbit = new Rabbit(startCell, getRandomInRange(3, 6), getRandomInRange(15, 25), getRandomInRange(6, 12), getRandomInRange(2, 5), getRandomInRange(14, 24), getRandomInRange(10, 20), getRandomInRange(14, 24), getRandomInRange(1, 2), 1, 0, 1, 0, 1, 2, 3, 30.0f + random.nextFloat() * 20f, getRandomInRange(5, 15));
            litter.add(newRabbit);
            getAllAnimals().add(newRabbit);
            logger.log(Level.FINE, "Spawned Rabbit (member {0}/{1} of litter) {2} (Age: {3}/{4}) at cell [{5},{6}].", new Object[]{i+1, RABBIT_LITTER_SIZE, newRabbit.getId(), newRabbit.getCurrAge(), newRabbit.getMaxAge(), startCell.getX(), startCell.getY()});
        }
        if (!litter.isEmpty()) { logger.log(Level.INFO, "Spawned a litter of {0} Rabbits at cell [{1},{2}].", new Object[]{litter.size(), startCell.getX(), startCell.getY()});}
        return litter;
    }

    public static List<Deer> spawnDeerGroup(MapStructure.Cell startCell) {
        List<Deer> group = new ArrayList<>();
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Deer group: startCell is null!"); return group; }
        for (int i = 0; i < SOCIAL_ANIMAL_GROUP_SIZE; i++) {
            Deer newDeer = new Deer(startCell, getRandomInRange(12, 20), getRandomInRange(14, 24), getRandomInRange(10, 18), getRandomInRange(8, 16), getRandomInRange(12, 22), getRandomInRange(12, 22), getRandomInRange(10, 20), getRandomInRange(3, 7), 1, 0, 0, 0, 1, 2, 2, 70.0f + random.nextFloat() * 30f, getRandomInRange(15, 30));
            group.add(newDeer);
            getAllAnimals().add(newDeer);
            logger.log(Level.FINE, "Spawned Deer (member {0}/{1} of group) {2} (Age: {3}/{4}) at cell [{5},{6}].", new Object[]{i+1, SOCIAL_ANIMAL_GROUP_SIZE, newDeer.getId(), newDeer.getCurrAge(), newDeer.getMaxAge(), startCell.getX(), startCell.getY()});
        }
        if(!group.isEmpty()){ logger.log(Level.INFO, "Spawned a group of {0} Deer at cell [{1},{2}].", new Object[]{group.size(), startCell.getX(), startCell.getY()});}
        return group;
    }

    public static List<Fox> spawnFoxGroup(MapStructure.Cell startCell) {
        List<Fox> group = new ArrayList<>();
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Fox group: startCell is null!"); return group;}
        for (int i = 0; i < SOCIAL_ANIMAL_GROUP_SIZE; i++) {
            Fox newFox = new Fox(startCell, getRandomInRange(8, 14), getRandomInRange(14, 24), getRandomInRange(8, 16), getRandomInRange(7, 14), getRandomInRange(12, 22), getRandomInRange(12, 22), getRandomInRange(12, 22), getRandomInRange(3, 7), 1, 0, 1, 0, 1, 2, 2, 60.0f + random.nextFloat() * 30f, getRandomInRange(10, 25));
            group.add(newFox);
            getAllAnimals().add(newFox);
            logger.log(Level.FINE, "Spawned Fox (member {0}/{1} of group) {2} (Age: {3}/{4}) at cell [{5},{6}].", new Object[]{i+1, SOCIAL_ANIMAL_GROUP_SIZE, newFox.getId(), newFox.getCurrAge(), newFox.getMaxAge(), startCell.getX(), startCell.getY()});
        }
        if(!group.isEmpty()){ logger.log(Level.INFO, "Spawned a group of {0} Foxes at cell [{1},{2}].", new Object[]{group.size(), startCell.getX(), startCell.getY()});}
        return group;
    }

    public static List<Iguana> spawnIguanaLitter(MapStructure.Cell startCell) {
        List<Iguana> litter = new ArrayList<>();
        if (startCell == null) { logger.log(Level.SEVERE, "Cannot spawn Iguana litter: startCell is null!"); return litter;}
        for (int i = 0; i < IGUANA_LITTER_SIZE; i++) {
            Iguana newIguana = new Iguana(startCell, getRandomInRange(2, 5), getRandomInRange(10, 18), getRandomInRange(8, 15), getRandomInRange(2, 6), getRandomInRange(5, 12), getRandomInRange(12, 20), getRandomInRange(14, 22), getRandomInRange(1, 3), 1, 0, 1, 0, 1, 2, 2, 20.0f + random.nextFloat() * 15f, getRandomInRange(3, 10));
            litter.add(newIguana);
            getAllAnimals().add(newIguana);
            logger.log(Level.FINE, "Spawned Iguana (member {0}/{1} of litter) {2} (Age: {3}/{4}) at cell [{5},{6}].",
                    new Object[]{i+1, IGUANA_LITTER_SIZE, newIguana.getId(), newIguana.getCurrAge(), newIguana.getMaxAge(), startCell.getX(), startCell.getY()});
        }
        if(!litter.isEmpty()){ logger.log(Level.INFO, "Spawned a litter of {0} Iguanas at cell [{1},{2}].", new Object[]{litter.size(), startCell.getX(), startCell.getY()});}
        return litter;
    }

    // --- Utility Methods ---

    private static MapStructure.Cell findRandomValidCell(MapStructure.Cell[][] map) {
        if (map == null || map.length == 0 || map[0].length == 0) { logger.log(Level.SEVERE, "Map is null or empty in findRandomValidCell."); return null; }
        int width = map.length;
        int height = map[0].length;
        int attempts = 0;
        int maxAttempts = width * height * 3;
        while(attempts < maxAttempts) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            MapStructure.Cell cell = map[x][y];
            if (cell != null && cell.getTerrain() != Terrain.WATER && cell.getTerrain() != null ) { return cell; }
            attempts++;
        }
        logger.log(Level.SEVERE, "Could not find a valid spawn cell after {0} attempts.", maxAttempts);
        return null;
    }

    private static int getRandomInRange(int min, int max) {
        if (min > max) { logger.log(Level.WARNING, "Invalid range in getRandomInRange: min({0}) > max({1}). Returning min.", new Object[]{min, max}); return min; }
        if (min == max) return min;
        return random.nextInt((max - min) + 1) + min;
    }
    public static void spawnAnimal(Animal animal){
        if (animal.getClass().equals(Wolf.class)) {
            spawnWolf(animal.getCell());
        } else if (animal.getClass().equals(Rabbit.class)) {
            spawnRabbitLitter(animal.getCell());
        } else if (animal.getClass().equals(Deer.class)) {
            spawnDeer(animal.getCell());
        } else if (animal.getClass().equals(Bear.class)) {
            spawnBear(animal.getCell());
        } else if (animal.getClass().equals(Fox.class)) {
            spawnFoxGroup(animal.getCell());
        } else if (animal.getClass().equals(Tiger.class)) {
            spawnTiger(animal.getCell());
        } else if (animal.getClass().equals(Iguana.class)) {
            spawnIguanaLitter(animal.getCell());
        } else {
            logger.log(Level.SEVERE, "Couldn`t get the specie of animal{1}", animal);
        }
        animal.actionPoints--;
    }
}
