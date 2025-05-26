package Map;

public class plantCheck {
    public static void growthCheck(){
        for(MapStructure.Cell cell :Map.MapGeneration.getMapInCells()){
            if(cell.getCurrPlantCapacity()<=100 && cell.getCurrPlantCapacity()>80){
                cell.setGrowth(Growth.RAMPANT);
            }
            else if(cell.getCurrPlantCapacity()<=80 && cell.getCurrPlantCapacity()>60){
                cell.setGrowth(Growth.SEVERE);
            }
            else if(cell.getCurrPlantCapacity()<=60 && cell.getCurrPlantCapacity()>40){
                cell.setGrowth(Growth.NOTICEABLE);
            }
            else if(cell.getCurrPlantCapacity()<=40 && cell.getCurrPlantCapacity()>20){
                cell.setGrowth(Growth.NEGLIGENT);
            }
            else {
                cell.setGrowth(Growth.NONE);
            }
        }
    }
    public static void plantsGrowing(){
        for(MapStructure.Cell cell :Map.MapGeneration.getMapInCells()){
            cell.setCurrPlantCapacity(cell.getCurrPlantCapacity()+ cell.getCurrPlantCapacity()/10);
        }
    }
    public static void plantCheck(){
        plantsGrowing();
        growthCheck();
    }
}
