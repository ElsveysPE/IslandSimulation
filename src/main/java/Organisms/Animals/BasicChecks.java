package Organisms.Animals;

import Organisms.Animals.Conditions.Conditions;
import Organisms.HealthStatus;

public class BasicChecks {
    public static boolean hidden(Animal hider, Animal looker){
        return hider.getCurrStealth() > looker.getCurrPerception();
    }
    public static int perceptionCheck(Animal animal){
        int per = animal.getPerception();
        int perAdv = animal.getPerceptionAdv();
        if(perAdv>0) return util.ImportantMethods.d100rollAdv(per);
        else if(perAdv<0) return util.ImportantMethods.d100rollDisAdv(per);
        else return util.ImportantMethods.d100roll(per);
    }
    public static int stealthCheck(Animal animal){
        int st = animal.getStealth();
        int stAdv = animal.getStealthAdv();
        if(stAdv>0) return util.ImportantMethods.d100rollAdv(st);
        else if(stAdv<0) return util.ImportantMethods.d100rollDisAdv(st);
        else return util.ImportantMethods.d100roll(st);
    }
    public static int initiativeCheck(Animal animal){
        int advantage = calculateInitiativeAdv(animal);
        if(advantage>0)  return util.ImportantMethods.d100rollAdv((int)Math.round((double) animal.getAgility()/4*3 + (double) animal.getSpeed()/4));
        else if(advantage<0) return util.ImportantMethods.d100rollDisAdv((int)Math.round((double) animal.getAgility()/4*3 + (double) animal.getSpeed()/4));
        else return util.ImportantMethods.d100roll((int)Math.round((double) animal.getAgility()/4*3 + (double) animal.getSpeed()/4));
    }
    private static int calculateInitiativeAdv(Animal animal) {
        int advantage = 0;
        if (animal.conditions.contains(Conditions.HOBBLED)) advantage--;
        if (animal.conditions.contains(Conditions.PRONE)) advantage -= 2;
        if (animal.conditions.contains(Conditions.STARVING)) advantage--;
        if (animal.conditions.contains(Conditions.EXHAUSTED)) advantage--;
        if (animal.conditions.contains(Conditions.DAZED)) advantage -= 3;
        if (animal.conditions.contains(Conditions.DISORIENTED)) advantage--;
        if (animal.conditions.contains(Conditions.SEVERELY_ILL)) advantage--;
        if (animal.conditions.contains(Conditions.POISONED)) advantage--;
        if (animal.conditions.contains(Conditions.RESTING)) advantage--;
        if (animal.conditions.contains(Conditions.DEEP_RESTING)) advantage-=2;
        if (animal.getHealthStatus().equals(HealthStatus.GRAVELY_INJURED)) advantage -= 3;
        if (animal.getHealthStatus().equals(HealthStatus.SEVERELY_INJURED)) advantage--;
        if (animal.conditions.contains(Conditions.ANGRY)) advantage++;
        if (animal.conditions.contains(Conditions.PANICKING)) advantage+=2;
        if (animal.conditions.contains(Conditions.STRESSED)) advantage+=2;
        if (animal.conditions.contains(Conditions.SUPER_STARVING)) advantage+=3;
        if (animal.getTags().contains(Tags.VOLANT) || animal.getTags().contains(Tags.STEALTH))
            switch (animal.getVerticalPosition()) {
                case A_LITTLE_HIGH -> advantage++;
                case HIGH -> advantage += 2;
                case VERY_HIGH -> advantage += 3;
            }
        return advantage;
    }
}
