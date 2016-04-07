package ale.burlap;

import ale.burlap.propositionalfunction.PFVertAlign;
import ale.cv.SpriteFinder;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.State;
import burlap.oomdp.visualizer.ObjectPainter;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.Visualizer;

import java.awt.*;
import java.awt.geom.Ellipse2D;

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
        // bomb
        ObjectClass bombClass = new ObjectClass(domain, ALEDomainConstants.CLASSBOMB);
        bombClass.addAttribute(xAtt);
        bombClass.addAttribute(yAtt);
        bombClass.addAttribute(vyAtt);

        // Propositional Functions
        new PFVertAlign(
                ALEDomainConstants.PFVertAlign,
                domain,
                new String[]{ALEDomainConstants.CLASSAGENT, ALEDomainConstants.CLASSALIEN},
                objectWidth);
        new PFVertAlign(
                ALEDomainConstants.PFVertAlign,
                domain,
                new String[]{ALEDomainConstants.CLASSAGENT, ALEDomainConstants.CLASSBOMB},
                objectWidth);

        return domain;
    }

    public StateRenderLayer getStateRenderLayer(){
        StateRenderLayer rl = new StateRenderLayer();
        rl.addObjectClassPainter(ALEDomainConstants.CLASSAGENT, new SIPainter(Color.GREEN));
        rl.addObjectClassPainter(ALEDomainConstants.CLASSALIEN, new SIPainter(Color.YELLOW));

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
}
