package edu.brown.cs.atari_vision.ale.gui;

import burlap.mdp.singleagent.SADomain;
import burlap.mdp.singleagent.environment.Environment;
import burlap.shell.visual.VisualExplorer;
import burlap.visualizer.Visualizer;
import edu.brown.cs.atari_vision.ale.burlap.ALEAction;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainConstants;
import edu.brown.cs.atari_vision.ale.burlap.ALEDomainGenerator;
import edu.brown.cs.atari_vision.ale.burlap.ALEEnvironment;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by maroderi on 7/11/16.
 */
public class ALEVisualExplorer extends VisualExplorer {

    protected final int humanFPS = 60;
    protected static final float widthRatio = 6f;
    protected static final float heightRatio = 3f;

    protected KeyboardControl keyboardControl;

    public ALEVisualExplorer(SADomain domain, Environment env, Visualizer painter, boolean human) {
        super(domain, env, painter,
                (int)(ALEDomainConstants.ALEScreenWidth * widthRatio),
                (int)(ALEDomainConstants.ALEScreenHeight * heightRatio));

        if (human) {
            enableHumanInput();
        }
    }

    protected void enableHumanInput() {
        keyboardControl = new KeyboardControl();
        this.addKeyListener(keyboardControl);
        this.painter.addKeyListener(keyboardControl);
        this.propViewer.addKeyListener(keyboardControl);

        ActionListener execute = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                executeAction(new ALEAction(keyboardControl.toALEAction()));
            }
        };
        Timer timer = new Timer(1000/humanFPS, execute);
        timer.start();

        startLiveStatePolling(1000/humanFPS);
    }

    public static void main(String[] args) {

        String alePath = "/home/maroderi/projects/Arcade-Learning-Environment/ale";
        String romPath = "/home/maroderi/projects/atari_roms/pong.bin";

        ALEDomainGenerator domGen = new ALEDomainGenerator();
        SADomain domain = domGen.generateDomain();
        ALEEnvironment env = new ALEEnvironment(domain, alePath, romPath);
        ALEVisualizer visualizer = new ALEVisualizer();
        ALEVisualExplorer exp = new ALEVisualExplorer(domain, env, visualizer, true);

        exp.initGUI();
    }
}
