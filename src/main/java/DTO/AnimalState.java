package DTO;

public class AnimalState {
        public long id;
        public int x;
        public int y;
        public String speciesType;
        public AnimalState(long id, int x, int y, String speciesType) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.speciesType = speciesType;
        }
        public AnimalState() {}
    }

