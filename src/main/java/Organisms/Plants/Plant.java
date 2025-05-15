package Organisms.Plants;

import Map.MapStructure;
import Map.Terrain;
import Organisms.HealthStatus;
import Organisms.Organism;

import java.util.List;

public abstract class Plant extends Organism {
    private float startingEnergyPoints;
    private float maxEnergyPoints = startingEnergyPoints;
    private float currEnergyPoints = startingEnergyPoints;
    private int averageSeedEnergyPoints;
    public int getAverageSeedEnergyPoints() {
        return averageSeedEnergyPoints;
    }
    private int size;

    public int getSize() {
        return size;
    }

    private float growthPoints=0;
    private float storedEnergyPoints=0;
    private int MAX_Age; //if 0 then plant can`t die of old age
    private int currAge=0;

    public void setCurrAge(int currAge) {
        this.currAge = currAge;
    }

    private LifeStage lifeStage;
    private boolean canSprout;
    private boolean hasSeeds;

    private boolean isDead = false;

    public boolean isDead() {
        return isDead;
    }

    private List<Terrain> preferableTerrain;

    public List<Terrain> getPreferableTerrain() {
        return preferableTerrain;
    }

    private List<Terrain> tolerableTerrain;

    public List<Terrain> getTolerableTerrain() {
        return tolerableTerrain;
    }

    private HealthStatus healthStatus;

    private MapStructure.Cell cell;

    public MapStructure.Cell getCell() {
        return cell;
    }

    public double energyMod(){
        if (getPreferableTerrain().contains(cell.getTerrain())) return 1;
        else if (getTolerableTerrain().contains(cell.getTerrain())) return 0.75;
        else return 0;
    }
    private void injuring(){
        if(maxEnergyPoints *0.2>=currEnergyPoints) healthStatus=HealthStatus.GRAVELY_INJURED;
        else if(maxEnergyPoints *0.4>=currEnergyPoints) healthStatus=HealthStatus.SEVERELY_INJURED;
        else if(maxEnergyPoints *0.6>=currEnergyPoints) healthStatus=HealthStatus.INJURED;
        else if(maxEnergyPoints *0.8>=currEnergyPoints) healthStatus=HealthStatus.SLIGHTLY_INJURED;
        else healthStatus=HealthStatus.HEALTHY;

    }
    private void healing(){
        float neededEnergy = maxEnergyPoints - currEnergyPoints;
        if(storedEnergyPoints>0 && neededEnergy!=0){
            float energyToUse = Math.min(storedEnergyPoints, neededEnergy);
            currEnergyPoints += energyToUse;
            storedEnergyPoints -= energyToUse;
        }
    }
    private void energyToGrowthConv() {
        switch (healthStatus) {
            case HEALTHY -> {
                float usableEnergy = storedEnergyPoints - (maxEnergyPoints / 50);
                if (usableEnergy > 0) {
                    storedEnergyPoints -= usableEnergy;
                    growthPoints += usableEnergy;
                }
            }
            case SLIGHTLY_INJURED -> {
                float usableEnergy = storedEnergyPoints - (maxEnergyPoints / 25);
                if (usableEnergy > 0) {
                    storedEnergyPoints -= usableEnergy;
                    growthPoints += usableEnergy;
                }
            }
            case INJURED -> {
                float usableEnergy = storedEnergyPoints - (maxEnergyPoints / 10);
                if (usableEnergy > 0) {
                    storedEnergyPoints -= usableEnergy;
                    growthPoints += usableEnergy;
                }
            }
            case SEVERELY_INJURED -> {
                float usableEnergy = storedEnergyPoints - (maxEnergyPoints / 2);
                if (usableEnergy > 0) {
                    storedEnergyPoints -= usableEnergy;
                    growthPoints += usableEnergy;
                }
            }
            case GRAVELY_INJURED -> {
                float usableEnergy = storedEnergyPoints - maxEnergyPoints;
                if (usableEnergy > 0) {
                    storedEnergyPoints -= usableEnergy;
                    growthPoints += usableEnergy;
                }
            }
        }
    }

    private void growth() {
        if (growthPoints > 1) {
            switch (lifeStage) {
                case SPROUT -> {
                    maxEnergyPoints += growthPoints;
                    growthPoints = 0;
                    if (maxEnergyPoints > 2 * startingEnergyPoints) lifeStage = LifeStage.YOUNG;
                }
                case YOUNG -> {
                    maxEnergyPoints += 0.5 * growthPoints;
                    growthPoints = 0;
                    if (maxEnergyPoints > 3 * startingEnergyPoints) lifeStage = LifeStage.MATURE;
                }
                case MATURE -> {
                    if (MAX_Age != 0 && currAge > MAX_Age) lifeStage = LifeStage.WITHERING;
                    if (MAX_Age == 0 || currAge < MAX_Age){
                        maxEnergyPoints += 0.25 * growthPoints;
                        growthPoints = 0;
                    }
                }
            }
        }
    }
    private void withering(){
        if(lifeStage==LifeStage.WITHERING) maxEnergyPoints -= startingEnergyPoints;
    }
    private void death(){
        if(maxEnergyPoints <=0) isDead=true;

    }

    public float getCurrEnergyPoints() {
        return currEnergyPoints;
    }

    public void plantStuff(){
        injuring();
        energyToGrowthConv();
        withering();
        growth();
        healing();
        death();
    }
}
