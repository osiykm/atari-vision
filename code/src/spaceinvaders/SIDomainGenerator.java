package spaceinvaders;

import ale.burlap.ALEDomainConstants;
import ale.burlap.ALEDomainGenerator;
import ale.burlap.propositionalfunction.PFVertAlign;
import spaceinvaders.sarsa.MultiObjectTiling;
import spaceinvaders.sarsa.ObjectTiling;
import spaceinvaders.cv.SpriteFinder;
import ale.io.Actions;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.common.NullAction;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.StaticPainter;
import burlap.oomdp.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * Space Invaders Domain Generator
 *
 * Created by MelRod on 3/12/16.
 */
public class SIDomainGenerator extends ALEDomainGenerator {

    public static int objectWidth = 8;

    SpriteFinder spriteFinder;


    @Override
    public Domain generateDomain() {

        // adds all ALE Actions
        Domain domain = super.generateDomain();

        // initialize SpriteFinder
        spriteFinder = new SpriteFinder();

        // Attributes
        Attribute xAtt = new Attribute(domain, ALEDomainConstants.XATTNAME, Attribute.AttributeType.INT);
        Attribute yAtt = new Attribute(domain, ALEDomainConstants.YATTNAME, Attribute.AttributeType.INT);
        Attribute vxAtt = new Attribute(domain, ALEDomainConstants.VXATTNAME, Attribute.AttributeType.INT);
        Attribute vyAtt = new Attribute(domain, ALEDomainConstants.VYATTNAME, Attribute.AttributeType.INT);
        Attribute agentCenteredX = new Attribute(domain, ALEDomainConstants.AGENT_CENT_XATTNAME, Attribute.AttributeType.INT);
        Attribute agentCenteredY = new Attribute(domain, ALEDomainConstants.AGENT_CENT_YATTNAME, Attribute.AttributeType.INT);

        // Object classes
        // agent
        ObjectClass agentClass = new ObjectClass(domain, ALEDomainConstants.CLASSAGENT);
        agentClass.addAttribute(xAtt);
        agentClass.addAttribute(yAtt);
        agentClass.addAttribute(vxAtt);
        // alien
        ObjectClass alienClass = new ObjectClass(domain, ALEDomainConstants.CLASSALIEN);
        alienClass.addAttribute(xAtt);
        alienClass.addAttribute(yAtt);
        alienClass.addAttribute(vxAtt);
        alienClass.addAttribute(agentCenteredX);
        alienClass.addAttribute(agentCenteredY);
        // bomb
        String[] bombClassNames = {ALEDomainConstants.CLASS_BOMB_UNKNOWN, ALEDomainConstants.CLASS_BOMB_AGENT, ALEDomainConstants.CLASS_BOMB_ALIEN};
        for (String bombClassName : bombClassNames) {
            ObjectClass bombClass = new ObjectClass(domain, bombClassName);
            bombClass.addAttribute(xAtt);
            bombClass.addAttribute(yAtt);
            bombClass.addAttribute(vyAtt);
            bombClass.addAttribute(agentCenteredX);
            bombClass.addAttribute(agentCenteredY);
        }

        // Propositional Functions
        new PFVertAlign(
                ALEDomainConstants.PFVertAlign,
                domain,
                new String[]{ALEDomainConstants.CLASSAGENT, ALEDomainConstants.CLASSALIEN},
                objectWidth);
        new PFVertAlign(
                ALEDomainConstants.PFVertAlign,
                domain,
                new String[]{ALEDomainConstants.CLASSAGENT, ALEDomainConstants.CLASS_BOMB_ALIEN},
                objectWidth);

        return domain;
    }

    public StateRenderLayer getStateRenderLayer(){
        StateRenderLayer rl = new StateRenderLayer();


        //create tilings
        int numTilesX = 14;
        int numTilesY = 20;
        ArrayList<ObjectTiling> tilings = new ArrayList<>();


        // Add Agent tiling
        tilings.add(new ObjectTiling(
                ALEDomainConstants.CLASSAGENT,
                ALEDomainConstants.XATTNAME,
                ALEDomainConstants.YATTNAME,
                0, 0, ALEDomainConstants.ALEScreenWidth, ALEDomainConstants.ALEScreenHeight,
                numTilesX, 1
        ));

        // Add Alien tiling
        tilings.add(new ObjectTiling(
                ALEDomainConstants.CLASSALIEN,
                ALEDomainConstants.AGENT_CENT_XATTNAME,
                ALEDomainConstants.AGENT_CENT_YATTNAME,
                -ALEDomainConstants.ALEScreenWidth, -ALEDomainConstants.ALEScreenHeight,
                ALEDomainConstants.ALEScreenWidth, ALEDomainConstants.ALEScreenHeight,
                2*numTilesX, 2*numTilesY
        ));

        // Add Bomb tiling
        tilings.add(new ObjectTiling(
                ALEDomainConstants.CLASS_BOMB_ALIEN,
                ALEDomainConstants.AGENT_CENT_XATTNAME,
                ALEDomainConstants.AGENT_CENT_YATTNAME,
                -ALEDomainConstants.ALEScreenWidth,  -ALEDomainConstants.ALEScreenHeight,
                ALEDomainConstants.ALEScreenWidth, ALEDomainConstants.ALEScreenHeight,
                2*numTilesX, 2*numTilesY
        ));

        rl.addStaticPainter(new TilingPainter(new MultiObjectTiling(tilings)));

        rl.addObjectClassPainter(ALEDomainConstants.CLASSAGENT, new SIPainter(Color.GREEN));
        rl.addObjectClassPainter(ALEDomainConstants.CLASSALIEN, new SIPainter(Color.YELLOW));
        rl.addObjectClassPainter(ALEDomainConstants.CLASS_BOMB_AGENT, new SIPainter(Color.ORANGE));
        rl.addObjectClassPainter(ALEDomainConstants.CLASS_BOMB_ALIEN, new SIPainter(Color.RED));
        rl.addObjectClassPainter(ALEDomainConstants.CLASS_BOMB_UNKNOWN, new SIPainter(Color.GRAY));

        return rl;
    }

    public Visualizer getVisualizer(){
        return new Visualizer(this.getStateRenderLayer());
    }

    public class SIPainter implements ObjectPainter{

        Color color;

        public SIPainter(Color color) {
            super();

            this.color = color;
        }

        @Override
        public void paintObject(Graphics2D g2, State s, ObjectInstance ob,
                                float cWidth, float cHeight) {

            //agent will be filled in gray
            g2.setColor(color);

            //set up floats for the width and height of our domain
            float fWidth = ALEDomainConstants.ALEScreenWidth;
            float fHeight = ALEDomainConstants.ALEScreenHeight;

            //determine the width of a single atari pixel on our canvas
            //such that the whole map can be painted
            float pixelWidth = (cWidth / fWidth);
            float pixelHeight = (cHeight / fHeight);

            float ax = ob.getIntValForAttribute(ALEDomainConstants.XATTNAME);
            float ay = fHeight - ob.getIntValForAttribute(ALEDomainConstants.YATTNAME);

            //left coordinate of cell on our canvas
            float rx = ax*pixelWidth;

            //top coordinate of cell on our canvas
            //coordinate system adjustment because the java canvas
            //origin is in the top left instead of the bottom right
            float ry = cHeight - ay*pixelHeight;

            //paint the rectangle
            g2.fill(new Ellipse2D.Float(rx - objectWidth/2, ry - objectWidth/2, objectWidth, objectWidth));
        }
    }

    public class TilingPainter implements StaticPainter{

        MultiObjectTiling multiTiling;

        public TilingPainter(MultiObjectTiling multiTiling) {
            this.multiTiling = multiTiling;
        }

        @Override
        public void paint(Graphics2D g2, State state, float cWidth, float cHeight) {

            // get agent coordinates
            java.util.List<ObjectInstance> agentInstances = state.getObjectsOfClass(ALEDomainConstants.CLASSAGENT);
            float agentX;
            float agentY;
            if (agentInstances.size() > 0) {
                ObjectInstance agentInstance = agentInstances.get(0);
                agentX = ((float)agentInstance.getIntValForAttribute(ALEDomainConstants.XATTNAME)/(float)ALEDomainConstants.ALEScreenWidth)*cWidth;
                agentY = ((float)agentInstance.getIntValForAttribute(ALEDomainConstants.YATTNAME)/(float)ALEDomainConstants.ALEScreenHeight)*cHeight;
            } else {
                agentX = 0;
                agentY = 0;
            }

            // update tiling for state
            multiTiling.evaluate(state, (new NullAction(Actions.actionNames[0])).getAssociatedGroundedAction());

//            // make grid
//            g2.setColor(Color.black);
//            g2.setStroke(new BasicStroke(2));
//            for (int x = 1; x < tiling.numTilesX; x++) {
//                g2.drawLine((int)(x*tileWidth), 0, (int)(x*tileWidth), (int)cHeight);
//            }
//            for (int y = 1; y < tiling.numTilesY; y++) {
//                g2.drawLine(0, (int)(y*tileHeight), (int)cWidth, (int)(y*tileHeight));
//            }

            for (ObjectTiling tiling : multiTiling.objectTilings) {

                if (tiling.objectClass.equals(ALEDomainConstants.CLASSAGENT)) {
                    g2.setColor(Color.cyan);
                } else if (tiling.objectClass.equals(ALEDomainConstants.CLASSALIEN)) {
                    g2.setColor(Color.red);
                } else if (tiling.objectClass.equals(ALEDomainConstants.CLASS_BOMB_ALIEN)) {
                    g2.setColor(Color.black);
                } else {
                    g2.setColor(Color.black);
                }

                // tile sizes
                float tileWidth;
                float tileHeight;
                if (tiling.xAttributeName.equals(ALEDomainConstants.AGENT_CENT_XATTNAME)) {
                    tileWidth = cWidth/(tiling.numTilesX/2);
                    tileHeight = cHeight/(tiling.numTilesY/2);
                } else {
                    tileWidth = cWidth/tiling.numTilesX;
                    tileHeight = cHeight/tiling.numTilesY;
                }

                // make fill
                for (int i = 0; i < tiling.numTiles; i++) {
                    if (tiling.bitSet.get(i)) {
                        int x = (i % tiling.numTilesX);
                        int y = (i / tiling.numTilesX);

                        if (tiling.xAttributeName.equals(ALEDomainConstants.AGENT_CENT_XATTNAME)) {
                            g2.fill(new Rectangle2D.Float(x * tileWidth - cWidth + agentX, y * tileHeight - cHeight + agentY, tileWidth, tileHeight));
                        } else {
                            g2.fill(new Rectangle2D.Float(x * tileWidth, y * tileHeight, tileWidth, tileHeight));
                        }
                    }
                }

//                if (tiling.objectClass.equals(ALEDomainConstants.CLASSALIEN)) {
//                    // print grid
//                    System.err.println();
//                    for (int i = 0; i < tiling.numTiles; i++) {
//                        if (i % tiling.numTilesX == 0) {
//                            System.err.println();
//                        }
//
//                        if (tiling.bitSet.get(i)) {
//                            System.err.print("1 ");
//                        } else {
//                            System.err.print("0 ");
//                        }
//                    }
//                    System.err.println();
//                }
            }
        }
    }


    public Visualizer getParamVisualizer(ObjectTiling tiling, int actionID){
        StateRenderLayer rl = new StateRenderLayer();

        rl.addStaticPainter(new TilingParamPainter(tiling, actionID));

        return new Visualizer(rl);
    }
    public class TilingParamPainter implements StaticPainter{

        ObjectTiling tiling;
        int actionID;

        public TilingParamPainter(ObjectTiling tiling, int actionID) {
            this.tiling = tiling;
            this.actionID = actionID;
        }

        @Override
        public void paint(Graphics2D g2, State state, float cWidth, float cHeight) {

            int indexOffset = actionID * tiling.numTiles;

            // tile sizes
            float tileWidth = cWidth/tiling.numTilesX;
            float tileHeight = cHeight/tiling.numTilesY;

            double maxParam = 0;

            // find max
            for (int i = 0; i < tiling.numTiles; i++) {

                double param = tiling.params[i + indexOffset];
                if (param > maxParam) {
                    maxParam = param;
                } else if (-param > maxParam) {
                    maxParam = -param;
                }
            }
//            System.err.println("MAX PARAM: " + maxParam);

            // make fill
            for (int i = 0; i < tiling.numTiles; i++) {

                double param = tiling.params[i + indexOffset];
                int colorVal = (int)((Math.abs(param)/maxParam)*255.0);

                Color color;
                if (param > 0) {
                    color = new Color(0, colorVal, 0);
                } else {
                    color = new Color(colorVal, 0, 0);
                }
                g2.setColor(color);


                int x = (i % tiling.numTilesX);
                int y = (i / tiling.numTilesX);

                g2.fill(new Rectangle2D.Float(x * tileWidth, y * tileHeight, tileWidth, tileHeight));
            }

            // draw agent
            g2.setColor(Color.cyan);
            g2.fill(new Ellipse2D.Float(cWidth/2-5, cHeight/2-5, 10, 10));
        }
    }
}
