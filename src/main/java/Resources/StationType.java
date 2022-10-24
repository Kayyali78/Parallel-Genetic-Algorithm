package Resources;

import java.awt.*;
import java.util.Random;

public enum StationType {
    St1( 5, Color.blue),
    St2( 15, Color.cyan),
    St3( 25, Color.gray),
    St4( 35, Color.white)
    ;

    // x is the units wide
    private int x;

    // y is the units deep
    private int y;

    // Station color on display
    private Color color;

    // s is the station ID number
    public int s;

    // creating a Random for use in returning a random station
    private static final Random STTY = new Random();

    // array of StationTypes for use in random selection
    private static final StationType[] stationTypes = values();

    //return a random station type for station constructor
    public static StationType randomStation(){
        return stationTypes[STTY.nextInt(stationTypes.length)];
    }

    StationType(int s, Color color) {
        this.s = s;
        this.color = color;
    }

    public Color getColor() {return color;}
}
