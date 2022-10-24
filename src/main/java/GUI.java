import Resources.Constants;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

public class GUI extends JPanel {
    FloorPlan floor;
    int space, PAD = 150;
    public GUI (FloorPlan fp){
        super();
        space = (Constants.WINDOW_SIZE - PAD)/(Constants.FLOOR_SIZE);
        this.setBounds(600,600,Constants.WINDOW_SIZE,Constants.WINDOW_SIZE);
        this.setOpaque(true);
        this.setSize(1000,1000);

        this.floor = fp;
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        FloorPlan fp = Main.topFloor;
        Graphics2D g2 = (Graphics2D) g;

        fp.getStationList().forEach(station -> {
            if (station != null) {
                g2.setColor(station.stationType.getColor());
                g2.fillRect((station.getX() * space) + PAD / 2, (station.getY() * space) + PAD / 2, space, space);
                g2.setColor(Color.BLACK);
                g2.drawString("("+station.getX()+", "+station.getY()+")", (station.getX()*space+(space/2)) + PAD/2-15, (station.getY()*space+(space/2))+PAD/2);
            }
        });
        DecimalFormat df = new DecimalFormat("###.###############");
        g2.drawString("Affinity Score: "+ df.format(Main.topFloor.affinityScore),Constants.WINDOW_SIZE-400,Constants.WINDOW_SIZE-50);
    }
}
