package Controller;

import View.MapSelectScreen;
import View.StartUpView;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * Controller for the start screen.
 * @author nikolaspaterson
 */
public class StartUpController implements ActionListener {

    private final StartUpView startUpView;

    /**
     * controller constructor.
     * @param startUp the view
     */
    public StartUpController(StartUpView startUp){
        this.startUpView = startUp;
    }

    /**
     * Performs an action based on which button triggered the event
     * @param e - ActionEvent
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(JButtonActionCommands.HOW_TO_PLAY.getCommand())){

            //Create frame and panel
            JFrame popUpFrame = new JFrame();
            JPanel howtoPanel = new JPanel();
            howtoPanel.setBorder(BorderFactory.createEmptyBorder(100,100,300,300));
            howtoPanel.setLayout(new BoxLayout(howtoPanel, BoxLayout.Y_AXIS));
            popUpFrame.add(howtoPanel, BorderLayout.CENTER);

            //add labels to panel
            JLabel temp = new JLabel("<html>" +
                    "<div style='text-align: center;'><h3>Are you ready to conquer the world?</h3></div>" +
                    "<div style='text-align: left;'><p>RISK can be played with 2 to 6 players</p>" +
                    "<p>Each players turn is composed of 3 steps,</p>" +
                    "<ul><li>Reinforce - Click on a territory you own and draft more troops to join your army. You can reinforce several different territories per turn.</li>" +
                    "<li>Attack - Click on a territory you own then click on a neighbouring enemy territory. You can only attack if you have at least 2 troops." +
                    " You can attack several times per turn.</li>" +
                    "<li>Fortify - Click on a territory you own and click on another territory you own to relocate troops from the first territory to the second.This can only be done ONCE per turn!</li></ul>" +
                    "</div></html>");
            howtoPanel.add(temp);

            popUpFrame.setTitle("Tutorial");
            popUpFrame.pack();
            popUpFrame.setVisible(true);

            //hide panel if not in focus
            popUpFrame.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    popUpFrame.setVisible(true);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    popUpFrame.dispose();
                }
            });
        }else {
            startUpView.dispose();
            new MapSelectScreen();
        }
    }
}
