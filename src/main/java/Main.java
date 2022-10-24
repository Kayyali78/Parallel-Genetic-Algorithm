import Resources.Constants;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Phaser;


/*
* Written by Scarlett Weeks @ SUNY Oswego for CSC 375, October 2022
 */
public class Main {
    static FloorPlan topFloor;
    static ConcurrentHashMap<Long,FloorPlan> floorPlans = new ConcurrentHashMap<Long, FloorPlan>();
    static volatile boolean visible = false;

    public static void main( String[] args) {
        int processors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: " + processors);

        if (topFloor == null) {
            FloorPlan fp = new FloorPlan();
            topFloor = fp;
        }

        GUI gui = new GUI(topFloor);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame("Optimized Floor Plan");
                frame.setSize(Constants.WINDOW_SIZE,Constants.WINDOW_SIZE);
                frame.setResizable(true);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().setBackground(new Color(0,0,0));

                frame.add(gui);

                frame.setVisible(true);
                visible = true;
            }
        });

        new Timer(1, event -> gui.repaint()).start();

        while (!visible) {Thread.onSpinWait();}

        final Phaser phaser = new Phaser() {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                System.out.println("Phase/Generation: " +phase);
                return phase >= Constants.MAX_GENS || registeredParties == 0;
            }
        };

        phaser.register();

        ForkJoinPool newWorkStealingPool = new ForkJoinPool(processors,ForkJoinPool.defaultForkJoinWorkerThreadFactory,null,true);
        for (int i=0;i<processors;i++){
            phaser.register();
            newWorkStealingPool.submit(new Thread() {
                FloorPlan fp = new FloorPlan();
                    public void run() {
                        do {
                            fp.run();
                            phaser.arriveAndAwaitAdvance();
                        } while (!phaser.isTerminated());
                    }
            });
        }
        phaser.arriveAndDeregister();
        System.out.println("Final Affinity Score is: "+topFloor.affinityScore);
        System.out.println("Number of threads invoking: "+newWorkStealingPool.getActiveThreadCount());
    }
}

