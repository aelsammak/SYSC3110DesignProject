import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ReinforcePopUpController implements ActionListener {

    private ReinforcePopUp popup;
    private GameEvent ge;

    public ReinforcePopUpController(ReinforcePopUp reinforcepopup){
        this.popup = reinforcepopup;
        ge = new GameEvent(popup.getPlayer());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int x = Integer.valueOf(popup.getTroops().getText());
        int deployableTroops = popup.getPlayer().getDeployableTroops();

        if(e.getSource().equals(popup.getPlus())){
            if(x < deployableTroops){
                x++;
            }
            popup.setTroops(x);
        }else if(e.getSource().equals(popup.getMinus())){
            if(x > 0){
                x--;
            }
            popup.setTroops(x);
        }else if(e.getSource().equals(popup.getDeployButton())){
            ge.reinforce(popup.getTerritory(), Integer.parseInt(popup.getTroops().getText()));
            popup.setVisible(false);
        }
    }
}