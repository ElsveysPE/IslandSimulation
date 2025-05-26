package util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

public class ImportantMethods {
    public static <T> boolean isArrayFull(T[][] array) {
        for (T[] row : array) {
            for (T element : row) if (element==null){
                return false;
            }

        }
        return true;
    }
    public static int plusOrMinus(){
        return Math.random() < 0.5 ? -1 : 1;
    }
    public static int d10roll(int modifier){
        return (int) (Math.random()*10)+1+modifier;
    }
    public static int d4Straight(){
        return (int) (Math.random()*4);
    }
    public static int d8Straight(){
        return (int) (Math.random()*8);
    }
    public static int d10rollDisAdv(int modifier){
        int rollA = (int) (Math.random()*10)+1+modifier;
        int rollB = (int) (Math.random()*10)+1+modifier;
        return Math.min(rollA, rollB);
    }
    public static int d10rollAdv(int modifier){
        int rollA = (int) (Math.random()*10)+1+modifier;
        int rollB = (int) (Math.random()*10)+1+modifier;
        return Math.max(rollA, rollB);
    }
    public static int d15roll(int modifier){
        return (int) (Math.random()*15)+1+modifier;
    }
    public static int d15rollDisAdv(int modifier){
        int rollA = (int) (Math.random()*15)+1+modifier;
        int rollB = (int) (Math.random()*15)+1+modifier;
        return Math.min(rollA, rollB);
    }
    public static int d15rollAdv(int modifier){
        int rollA = (int) (Math.random()*15)+1+modifier;
        int rollB = (int) (Math.random()*15)+1+modifier;
        return Math.max(rollA, rollB);
    }
    public static int d20roll(int modifier){
        return (int) (Math.random()*20)+1+modifier;
    }
    public static int d20rollDisAdv(int modifier){
        int rollA = (int) (Math.random()*20)+1+modifier;
        int rollB = (int) (Math.random()*20)+1+modifier;
        return Math.min(rollA, rollB);
    }
    public static int d20rollAdv(int modifier){
        int rollA = (int) (Math.random()*20)+1+modifier;
        int rollB = (int) (Math.random()*20)+1+modifier;
        return Math.max(rollA, rollB);
    }
    public static int d33roll(int modifier){
        return (int) (Math.random()*33)+1+modifier;
    }
    public static int d33rollDisAdv(int modifier){
        int rollA = (int) (Math.random()*33)+1+modifier;
        int rollB = (int) (Math.random()*33)+1+modifier;
        return Math.min(rollA, rollB);
    }
    public static int d33rollAdv(int modifier){
        int rollA = (int) (Math.random()*33)+1+modifier;
        int rollB = (int) (Math.random()*33)+1+modifier;
        return Math.max(rollA, rollB);
    }
    public static int d45roll(int modifier){
        return (int) (Math.random()*46)+1+modifier;
    }
    public static int d45rollDisAdv(int modifier){
        int rollA = (int) (Math.random()*45)+1+modifier;
        int rollB = (int) (Math.random()*45)+1+modifier;
        return Math.min(rollA, rollB);
    }
    public static int d45rollAdv(int modifier){
        int rollA = (int) (Math.random()*45)+1+modifier;
        int rollB = (int) (Math.random()*45)+1+modifier;
        return Math.max(rollA, rollB);
    }
    public static int d100roll(int modifier){
      return (int) (Math.random()*100)+1+modifier;
    }
    public static int d100rollAdv(int modifier){
        int rollA = (int) (Math.random()*100)+1+modifier;
        int rollB = (int) (Math.random()*100)+1+modifier;
        return Math.max(rollA, rollB);
    }
    public static int d100rollDisAdv(int modifier){
        int rollA = (int) (Math.random()*100)+1+modifier;
        int rollB = (int) (Math.random()*100)+1+modifier;
        return Math.min(rollA, rollB);
    }
    public static <T> T HashSetRandomElement(HashSet<T> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(set.size());
        Iterator<T> iterator = set.iterator();
        for (int i = 0; i < randomIndex; i++) {
            iterator.next();
        }
        return iterator.next();
    }
}
