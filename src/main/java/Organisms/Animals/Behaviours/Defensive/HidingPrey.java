package Organisms.Animals.Behaviours.Defensive;

import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.Basic;
import util.ImportantMethods;

public interface HidingPrey extends Basic {
    default void hide(Animal animal) {
        if (animal.getStealthAdv() >= 0) animal.setCurrStealth(ImportantMethods.d100rollAdv(animal.getStealth()));
        else if (animal.getStealthAdv() <= -2)
            animal.setCurrStealth(ImportantMethods.d100rollDisAdv(animal.getStealth()));
        else animal.setCurrStealth(ImportantMethods.d100roll(animal.getStealth()));
    }

}
