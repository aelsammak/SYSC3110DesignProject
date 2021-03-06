package Model;

import Event.TerritoryEvent;
import JSONModels.JSONTerritory;
import Listener.TerritoryView;
import org.json.simple.JSONObject;
import java.awt.*;
import java.util.*;

/**
 * The Model.Territory class is responsible for containing all the important attributes of a territory in the game of Risk.
 *
 * @author Ahmad El-Sammak
 * @author Erik Iuhas
 */
public class Territory {

    private Player occupant;
    private int troops;
    private final HashMap<String, Territory> neighbours;
    private final String territoryName;
    private final ArrayList<TerritoryView> territoryViews;
    private String continentName;
    private Color color;
    private Color neighbourColor;
    private ArrayList<Territory> linkedNeighbours;
    private Timer blinking_yours;
    private Timer blinking_theirs;

    /**
     * Class constructor for the Model.Territory class. Sets the player who occupies the territory
     * @param territoryName the name of the territory.
     */
    public Territory(String territoryName) {
        this.territoryName = territoryName;
        troops = 0;
        neighbours = new HashMap<>();
        territoryViews = new ArrayList<>();
        linkedNeighbours = new ArrayList<>();
        blinking_yours = new Timer("flash_yours");
        blinking_theirs = new Timer("flash_theirs");
    }

    /**
     * Constructor for the Territory class. This constructor is used to create the Territory from the JSON file.
     * @param territory the JSONObject
     * @param old_territory the old Territory Object
     */
    public Territory(JSONObject territory,Territory old_territory){
        territoryViews = new ArrayList<>();
        JSONTerritory territory_json = new JSONTerritory(territory);
        territoryName = territory_json.getTerritoryName();
        troops = territory_json.getTroops();
        neighbours = new HashMap<>();
        continentName = old_territory.getContinentName();
        territoryViews.addAll(old_territory.removeTerritoryViews());
        linkedNeighbours = new ArrayList<>();
        blinking_yours = new Timer("flash_yours");
        blinking_theirs = new Timer("flash_theirs");

    }

    /**
     * Sets the color of the neighbouring territory.
     * @param color the color to set
     */
    public void setNeighbourColor(Color color){
        this.neighbourColor = color;
    }

    /**
     * Adds a color.
     * @param color color to be added
     */
    public void addColor(Color color) {
        this.color = color;
        updateView();
    }

    /**
     * Getter for the color of the Territory
     * @return Color the color
     */
    public Color getColor() { return color; }

    /**
     * Getter for the neighbouring Territory color.
     * @return Color the color
     */
    public Color getNeighbourColor() { return neighbourColor; }

    /**
     * This method is used to addTerritoryView listeners of the Model to update the view.
     * @param territoryView the listener to add
     */
    public void addTerritoryView(TerritoryView territoryView) { territoryViews.add(territoryView); }

    /**
     * This method is used to store all TerritoryViews, remove them from this Territory object and return a list of them.
     * @return ArrayList<TerritoryView> a list of the TerritoryViews
     */
    public ArrayList<TerritoryView> removeTerritoryViews(){
        ArrayList<TerritoryView> temp_views = new ArrayList<>(territoryViews);
        territoryViews.clear();
        return temp_views;
    }

    /**
     * Sets the continent name
     * @param name the continent name
     */
    public void setContinentName(String name){
        continentName = name;
    }

    /**
     * Gets the continent name.
     * @return String continent name
     */
    public String getContinentName(){
        return continentName;
    }

    /**
     * Gets the name of this Model.Territory.
     * @return String the Model.Territory name.
     */
    public String getTerritoryName() { return territoryName; }

    /**
     * Sets the number of troops in this Model.Territory.
     * @param troops the number of troops.
     */
    public void setTroops(int troops) {
        this.troops = troops;
        updateView();
    }

    /**
     * Gets the number of troops in this Model.Territory.
     * @return int the number of troops.
     */
    public int getTroops() {
        return troops;
    }

    /**
     * This method is used to remove troops and update the JLabel on the Model.Territory object as a result of an attack.
     * @param value the value to remove
     */
    public void removeTroops(int value) {
        troops += (value);
        occupant.addTotal(value);
        updateView();
    }

    /**
     * Adds a Model.Territory as a neighbour of this Model.Territory into a HashMap.
     * @param neighbour the neighbouring Model.Territory.
     */
    public void addNeighbour(Territory neighbour){ neighbours.put(neighbour.getTerritoryName() ,neighbour); }

    /**
     * Gets the HashMap of neighbouring Territories for this Model.Territory.
     * @return HashMap<String,Model.Territory> the HashMap of neighbours.
     */
    public HashMap<String,Territory> getNeighbours() {
        return this.neighbours;
    }

    /**
     * Sets a new Model.Player to occupy this Model.Territory.
     * @param occupant the new Model.Player to Occupy.
     */
    public void setOccupant(Player occupant) {
        this.occupant = occupant;
        addColor(occupant.getPlayer_color());
    }

    /**
     * Gets the Model.Player that occupies this Model.Territory.
     * @return Model.Player the occupant.
     */
    public Player getOccupant() {
        return occupant;
    }

    /**
     * This method is used to check if this Model.Territory is neighbours with the @param territoryToCheck.
     * @param territoryToCheck the territory to check.
     * @return boolean true if it is a neighbour, false if not.
     */
    public boolean isNeighbour(Territory territoryToCheck) {
        String terrToCheck = territoryToCheck.getTerritoryName();
        return neighbours.containsKey(terrToCheck);
    }

    /**
     * Combines the name of the Model.Territory and lists all the neighbouring Territories.
     * Prints String combination of the information above.
     */
    public void print_info(){
        System.out.println("Model.Territory Name: " + territoryName );
        System.out.println("Neighbours: " + neighbours.keySet().toString());
        System.out.println("Owner: " + occupant.getName());
        System.out.println("Troop Count: " + troops);
        System.out.println("================================================");
    }

    /**
     * Creates a String that displays the territory name as well as its neighbouring territories
     * @return String - The string in the description
     */
    public String toString() {
        String output = "------>Territory Name: " + this.territoryName + "<------\n";
        output += "======Neighbouring Territories======\n";

        for(String str : neighbours.keySet()) {
            output += "              " + neighbours.get(str).getTerritoryName() + "\n";
        }
        output += "==================";
        return output;
    }

    /**
     * Getter for all neighbours that are linked.
     * @return ArrayList<Territory> list of neighbours that are linked
     */
    public ArrayList<Territory> getLinkedNeighbours(){
        linkedNeighbours = new ArrayList<>(linkNeighbours(getOccupant(),new HashSet<>()));
        return linkedNeighbours;
    }

    /**
     * Used to link neighbours and put it into a set.
     * @param owner the player
     * @param val to compare
     * @return Set<Territory> the set of linked neighbours
     */
    public Set<Territory> linkNeighbours(Player owner,Set<Territory> val){
        for(Territory neighbour : neighbours.values()){
            if(neighbour.getOccupant().equals(owner) && !val.contains(neighbour)){
                val.add(neighbour);
                val.addAll(neighbour.linkNeighbours(owner,val));
            }
        }
        return val;
    }

    /**
     * Initializes the debugLinkNeighbours as an empty hashset and stores it as an arraylist.
     * @return ArrayList<Territory> the array list
     */
    public ArrayList<Territory> debugLink(){
        linkedNeighbours = new ArrayList<>(debugLinkNeighbours(new HashSet<>()));
        return linkedNeighbours;
    }

    /**
     * Linking method is used in MapSelect to test if map is the proper format.
     * ie. Each territory must be able to access other territories anywhere on the map
     * and there shouldn't be any separated island/territories that can't be reached.
     * @param val the linked neighbours
     * @return Set<Territory> the set of neighbours
     */
    public Set<Territory> debugLinkNeighbours(Set<Territory> val){
        for(Territory neighbour : neighbours.values()){
            if(!val.contains(neighbour)){
                val.add(neighbour);
                val.addAll(neighbour.debugLinkNeighbours(val));
            }
        }
        return val;
    }

    /**
     * This method is used to stop the timer and stop the flashing of the valid territories that the player can attack.
     */
    public void cancel_timer(){
        blinking_yours.cancel();
        blinking_theirs.cancel();
        blinking_yours = new Timer();
        blinking_theirs = new Timer();
        for(Territory temp : neighbours.values()){
            temp.addColor(temp.getNeighbourColor());
        }
    }

    /**
     * This method creates a timer object which is used to flash the valid territory that the player can attack.
     */
    public void activateTimer(){
        blinking_yours.scheduleAtFixedRate(new FlashTimerTask(getColor(),getNeighbours(),0),0,1000);
        blinking_theirs.scheduleAtFixedRate(new FlashTimerTask(getColor(),getNeighbours(),1),500,1000);
    }

    /**
     * Updates all the necessary labels and backgrounds that the view needs to change after an event.
     */
    public void updateView(){
        for(TerritoryView territoryView : territoryViews){
            territoryView.handleTerritoryUpdate(new TerritoryEvent(this, occupant, troops, color));
        }
    }

    /**
     * Saves the Territory to the JSONPlayer
     * @return JSONObject Territory
     */
    public JSONObject saveJSON(){
        JSONTerritory territory_json = new JSONTerritory();
        territory_json.setTerritoryName(territoryName);
        territory_json.setTroops(troops);
        return territory_json.getTerritory_json();
    }

    /**
     * This method is used to update the links between this Territory and it's neighbours.
     * @param neighbours the set of neighbouring territories
     * @param map the current map
     */
    public void updateLink(Set<String> neighbours, HashMap<String,Territory> map){
        for (String territory_names : neighbours){
            this.neighbours.put(territory_names,map.get(territory_names));
        }
    }

}
