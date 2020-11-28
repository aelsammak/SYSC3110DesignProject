package View;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;

/**
 * The View.PlayerSelectPanel class is used to store the information of each player in a JPanel.
 * A player has a Name, Icon and deciding if is an AI or Model.Player.
 *
 * @author Ahmad El-Sammak
 */
public class PlayerSelectPanel extends JPanel {

    private final JTextField playerName;
    private final JButton photo;
    private final JButton option;
    private String filePath;

    /**
     * Class constructor for View.PlayerSelectPanel class.
     */
    public PlayerSelectPanel() {
        super();
        setLayout(new FlowLayout(FlowLayout.CENTER, 15, 30));
        Border blackLine = BorderFactory.createLineBorder(Color.black);
        setBorder(blackLine);
        JLabel pn = new JLabel("Player Name:");
        pn.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        playerName = new JTextField(8);
        option = new JButton("PLAYER");
        option.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
        option.setBounds(new Rectangle(50,50));

        option.addActionListener(e -> {
            if (option.getText().equals("PLAYER")) {
                option.setText("AI");
            } else{
                option.setText("PLAYER");
            }
        });

        ArrayList<String> file = new ArrayList<>();
        file.add("/resources/Chizzy.png");
        file.add("/resources/TA.png");
        file.add("/resources/Captain.png");
        file.add("/resources/nik.png");
        file.add("/resources/passki.png");
        file.add("/resources/Bruce.png");

        ImageIcon chizzy = scaleImage(file.get(0));
        ImageIcon TA = scaleImage(file.get(1));
        ImageIcon chip = scaleImage((file.get(2)));
        ImageIcon nik = scaleImage((file.get(3)));
        ImageIcon pass = scaleImage((file.get(4)));
        ImageIcon bruce = scaleImage((file.get(5)));

        photo = new JButton();
        photo.setIcon(chizzy);



        photo.addActionListener(e -> {
            if(photo.getIcon().equals(chizzy)) {
                photo.setIcon(TA);
                filePath = "/resources/TA.png";
            } else if (photo.getIcon().equals(TA)) {
                photo.setIcon(chip);
                filePath = "/resources/Captain.png";
            } else if (photo.getIcon().equals(chip)) {
                photo.setIcon(nik);
                filePath = "/resources/nik.png";
            } else if (photo.getIcon().equals(nik)){
                photo.setIcon(pass);
                filePath = "/resources/passki.png";
            } else if (photo.getIcon().equals(pass)) {
                photo.setIcon(bruce);
                filePath = "/resources/Bruce.png";
            } else{
                photo.setIcon(chizzy);
                filePath = "/resources/Chizzy.png";
            }
        });

        add(photo);
        add(pn);
        add(playerName);
        add(option);

        setSize(600, 600);
        setVisible(true);
    }

    public String getFilePath() { return filePath; }

    /**
     * Getter for the player type.
     * @return String the player type
     */
    public String getPlayerType() { return option.getText(); }

    /**
     * Gets the name of the player.
     * @return String the player name.
     */
    public String getPlayerName(){
        return playerName.getText();
    }

    /**
     * Gets the Icon for the player.
     * @return Icon the player Icon
     */
    public Icon getImageIcon(){
        return photo.getIcon();
    }

    /**
     * Scales the Image to properly fit the JPanel.
     * @param filename the filepath
     * @return ImageIcon the scaled Icon
     */
    private ImageIcon scaleImage(String filename) {
        ImageIcon scaledImg = new ImageIcon(getClass().getResource(filename));
        Image img = scaledImg.getImage().getScaledInstance( 85, 85,  java.awt.Image.SCALE_SMOOTH );
        scaledImg = new ImageIcon(img);
        return scaledImg;
    }
}
