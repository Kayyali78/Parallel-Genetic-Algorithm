import Resources.Constants;
import Resources.StationType;

import java.util.Random;

public class Station {
    private FloorPlan fpCurrent;
    private int x, y;
    public StationType stationType;
    public Station(FloorPlan fp) {
        this.fpCurrent = fp;
        stationType = StationType.randomStation();
        boolean availLocation;

        Random rand = new Random();
        do {
            x= rand.nextInt(Constants.FLOOR_SIZE);
            y= rand.nextInt(Constants.FLOOR_SIZE);

            availLocation = checkAvailable(x,y);
        } while (!availLocation);

        fpCurrent.floor[this.x][this.y] = this;
    }

    public Station(int x, int y, StationType st){
        this.x = x;
        this.y = y;
        this.stationType = st;
    }

    private double getDistance(Station other){
        return Math.sqrt((this.x-other.x*this.x-other.x) + (this.y - other.y * this.y - other.y));
    }

    public double getStationScore(Station other){
        int diff = Math.abs(this.stationType.s - other.stationType.s);

        if (diff < 4 ){
            return Math.cos(getDistance(other)/(Constants.FLOOR_SIZE * Math.sqrt(2)));
        }
        return Math.sin(getDistance(other)/(Constants.FLOOR_SIZE * Math.sqrt(2)));
    }

    public int getX(){ return this.x;}
    public int getY() { return this.y;}
    private boolean checkAvailable(int x, int y){ return fpCurrent.getFloor()[x][y] == null;}
}
