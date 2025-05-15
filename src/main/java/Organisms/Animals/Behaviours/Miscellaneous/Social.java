package Organisms.Animals.Behaviours.Miscellaneous;

import Map.MapStructure;
import Organisms.Animals.Animal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public interface Social {
    HashSet<Animal> group = new HashSet<>();
    public static void expandPack(Animal animal){
        group.add(animal);
    }
    public static void shrinkPack(Animal animal){
        group.remove(animal);
    }
    public static HashSet<Animal> getGroup(){
        return group;
    }
    public static int containsFriends(Animal animal, MapStructure.Cell cell){
        return 0;
    }
    default void packTactics(Animal animal){
        if(animal.getCell().getAnimals().contains(group)){
            animal.setAttackAdv(animal.getAttackAdv()+1);
        }
    }
}
