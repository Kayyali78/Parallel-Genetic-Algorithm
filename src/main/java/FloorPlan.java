import Resources.*;
import com.google.common.util.concurrent.AtomicDouble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class FloorPlan extends Thread implements Runnable, Comparable {
  
    public double affinityScore;
    final private static Exchanger<Station[][]> exchanger = new Exchanger<>();
    protected Station[][] floor;
    private long tId,id;
    private volatile List<Station> stationList = new ArrayList<>();

    public FloorPlan() {
        createFP(Constants.STATIONS);
    }

    public FloorPlan(List<Station> stations, Station[][] floor) {
        this.stationList = stations;
        stationList.forEach(station -> floor[station.getX()][station.getY()] = station);
        affinityScore = getScore();
    }

    private void createFP(int stations) {
        floor = new Station[Constants.FLOOR_SIZE][Constants.FLOOR_SIZE];
        tId = Thread.currentThread().getId();
        createStations(stations);
        affinityScore = getScore();
    }

    private void createStations(int stations) {
        for (int i = 0; i < Constants.FLOOR_SIZE; i++) {
            for (int j = 0; j < Constants.FLOOR_SIZE; j++) {
                floor[i][j] = null;
            }
        }
        for (int i = 0; i < stations; i++) {
            stationList.add(new Station(this));
        }
    }

    @Override
    public int compareTo(Object o) {
        if (this.affinityScore > ((FloorPlan) o).affinityScore){
            return 1;
        } else {
            return 0;
        }
    }

    /* Station mutation takes in a station to determine if any mutation is performed. A copy of the station is made
       and a randomFloat is returned (between 0.00 and 1.00). If the returned value is less than the mutation rate set in
       the Constants file, the station is mutated by changing the stationType on the newStation and returning the new Station
       to the original call.
    */

    public Station mutation(Station station) {
        Station newStation = station;
        try {
            float chanceOfMutation = ThreadLocalRandom.current().nextFloat();

            if (chanceOfMutation > Constants.MUTATION_RATE) {
                return newStation;
            }
            newStation.stationType = StationType.randomStation();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newStation;
    }

    public Station[][] getFloor() {
        return floor;
    }

    public long getThreadId() {
        return tId;
    }

    public long getId() {return id;}

    public List<Station> getStationList() {
        return stationList;
    }

    protected void crossover() {
        try {
            Station[][] sentSeg = this.getSegment();
            Station[][] recSeg = exchanger.exchange(sentSeg, 10000, TimeUnit.MILLISECONDS);

            if (recSeg != null) {
                FloorPlan newFloor = new FloorPlan(new ArrayList<>(stationList), this.floor.clone());
                newFloor.updateSegment(recSeg);

                System.out.println("Current segment affinity score is: "+affinityScore);
                System.out.println("Updated segment affinity score is: "+newFloor.affinityScore);
                
                if (newFloor.getScore() > this.getScore()){
                    this.stationList = new ArrayList<>(newFloor.stationList);
                    this.floor = newFloor.floor.clone();
                    this.affinityScore = newFloor.getScore();
                    
                    if (this.affinityScore > Main.topFloor.affinityScore){
                        Main.topFloor = this;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private double getScore() {
        HashMap<Long,Double> scores = new HashMap<>();
        AtomicDouble total = new AtomicDouble(0);
        for (Station station_N : stationList){
            for (Station station_M : stationList){
                if (station_N.hashCode() == station_M.hashCode()){
                    continue;
                }
                long hashCode = (long) station_N.hashCode() * station_M.hashCode();

                if (! scores.containsKey(hashCode)){
                    double stationScore = station_N.getStationScore(station_M);

                    if (!Double.isNaN(stationScore)) {
                        scores.put(hashCode, stationScore);
                    }
                }
            }
        }
        scores.forEach( ( hashcode, score) -> total.getAndAdd(score) );
        return total.get()/scores.size();
    }

    private void updateSegment(Station[][] recSeg){
        floor = recSeg;

        List<Station> newList = new ArrayList<>();
        int available = Constants.STATIONS;

        for (int i=0;i<Constants.FLOOR_SIZE;i++){
            for (int j=0;j<Constants.FLOOR_SIZE;j++){
                if (floor[i][j] != null){
                    Station station = floor[i][j];
                    Station temp = new Station(station.getX(), station.getY(),station.stationType);
                    newList.add(temp);
                    floor[i][j] = temp;
                    available --;
                }
            }
        }
        while (available > 0){
            boolean filledStation = false;

            while (!filledStation){
                Station fill = stationList.get(new Random().nextInt(stationList.size()));
                Station temp = new Station(fill.getX(),fill.getY(),fill.stationType);
                if (floor[fill.getX()][fill.getY()] == null){
                    filledStation = true;
                    newList.add(temp);
                    stationList.remove(fill);
                }
            }
            available --;
        }
        stationList = newList;
    }

    private Station[][] getSegment() {
        Station[][] segment = new Station[Constants.FLOOR_SIZE][Constants.FLOOR_SIZE];
        float cross = new Random().nextFloat();
        int segSize = (int) (0.50 * Constants.FLOOR_SIZE);

        if (cross <= 0.25f){
            for (int i=0;i<segSize;i++){
                System.arraycopy(floor[i], 0, segment[i], 0, segSize);
            }
        } else if (cross > 0.25f && cross <= 0.5f){
            for (int i=segSize;i<Constants.FLOOR_SIZE;i++){
                System.arraycopy(floor[i], 0, segment[i], 0, segSize);
            }
        } else if (cross > 0.5f && cross <= 0.75f){
            for (int i=0;i<segSize;i++){
                System.arraycopy(floor[i], segSize, segment[i], segSize, Constants.FLOOR_SIZE - segSize);
            }
        } else if (cross > 0.75f && cross <= 1.0f){
            for (int i=segSize;i<Constants.FLOOR_SIZE;i++){
                System.arraycopy(floor[i], segSize, segment[i], segSize, Constants.FLOOR_SIZE - segSize);
            }
        }
        return segment;
    }

    @Override
    public void run(){
        this.crossover();
        mutation(this.stationList.get(new Random().nextInt(stationList.size())));
        addToHashMap();
    }

    synchronized void addToHashMap(){
        Main.floorPlans.put(this.getThreadId(),this);
        for (long tId : Main.floorPlans.keySet()){
            if (Main.topFloor == null){
                Main.topFloor = Main.floorPlans.get(tId);
            } else if (Main.topFloor.affinityScore <= this.affinityScore){
                Main.topFloor = this;
            }
        }
    }
}