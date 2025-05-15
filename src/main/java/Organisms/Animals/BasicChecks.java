package Organisms.Animals;

public class BasicChecks {
    public static boolean isHidden(Animal hider, Animal looker){
        return hider.getCurrStealth() > looker.getCurrPerception();
    }
}
