package Model;

import JSONModels.JSONAIPlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The AIPlayer class is used to make the decisions made by the "AI" player in the game of Risk.
 *
 * @author Ahmad El-Sammak
 * @author Erik Iuhas
 * @author Nikolas Paterson
 */
public class AIPlayer extends Player {

    private final GameModel gameModel;
    private final GameEvent gameEvent;
    private boolean attacking;

    /**
     * Class constructor for the AIPlayer class. Sets the name of the AI and passes in a reference to the gameView to simply control
     * which move the AI will be controlling.
     *
     * @param name the name of the AIPlayer
     * @param gameModel a reference to the gameView
     */
    public AIPlayer(String name, GameModel gameModel) {
        super(name);
        this.gameModel = gameModel;
        gameEvent = new GameEvent(this);
        attacking = true;
    }

    /**
     * Class constructor for the AIPlayer class. This constructor is used to create the AIPlayer from a JSON file.
     * @param player the player in JSONObject form
     * @param currentMap the map
     * @param gameModel the game model
     */
    public AIPlayer(JSONObject player, HashMap<String,Territory> currentMap,GameModel gameModel){
        super(player,currentMap);
        this.gameModel = gameModel;
        this.gameEvent = new GameEvent(this);
        JSONAIPlayer player_json = new JSONAIPlayer(player);
        setName(player_json.getName());
        setTotal_troops(player_json.getTotal_troops());
        setDeployableTroops(player_json.getDeployableTroops());
        setFortifyStatus(player_json.isFortifyStatus());
        JSONArray list_territories = player_json.getTerritories();
        setTerritoriesOccupied(new HashMap<>());
        setPlayer_color(player_json.getColor());
        HashMap<String,Territory> ref_map = getTerritoriesOccupied();
        for(Object territoryObj : list_territories){
            String territoryName = (String) territoryObj;
            ref_map.put(territoryName, currentMap.get(territoryName));
            currentMap.get(territoryName).setOccupant(this);
        }
        setPlayerNumber(player_json.getPlayerNumber());
        setPlayerListeners(new ArrayList<>());
        setInGame(player_json.isInGame());
        attacking = player_json.isAttacking();
        if(getFilePath() != null) {
            setPlayer_icon(scaleImage(player_json.getFilePath()));
        }
    }

    /**
     * This method is used to find all the neighbours around the given territory and return a ratio of how many of those neighbouring territories are the enemy.
     *
     * @param territory the territory to check
     * @return float the enemy ratio
     */
    public float enemyNeighbourRatio(Territory territory) {
        int enemyNeighbour = 0;
        for (Territory neighbour : territory.getNeighbours().values()) {
            if (!(neighbour.getOccupant().equals(this))) {
                enemyNeighbour++;
            }
        }
        return ((float) enemyNeighbour/territory.getNeighbours().size());
    }

    /**
     * This method is used to return a priority integer based on the enemy ratio that is passed in (higher priority = greater int).
     *
     * @param enemyNeighbourRatio the enemy ratio
     * @return int the priority
     */
    public int leastEnemySurrounded(float enemyNeighbourRatio) {
        if(enemyNeighbourRatio <= 0.25 && enemyNeighbourRatio > 0) {
            return 6;
        } else if(enemyNeighbourRatio > 0.25 && enemyNeighbourRatio <= 0.50) {
            return 4;
        } else if(enemyNeighbourRatio > 0.50 && enemyNeighbourRatio <= 0.75) {
            return 2;
        } else {
            return -100;
        }
    }

    /**
     * This method is used to return a priority integer based on how many troops the territory has.
     *
     * @param territory the territory to check
     * @return int the priority
     */
    public int lowestTroopTerritory(Territory territory) {
        if(territory.getTroops() == 1) {
            return 3;
        } else if(territory.getTroops() == 2) {
            return 2;
        } else {
            return 1;
        }
    }

    /**
     * This method is used to return the best territory to reinforce based on which one has the highest priority.
     *
     * @param troopBonus the troop bonus
     * @return territory the best territory to reinforce
     */
    public Territory bestReinforceTerritory(int troopBonus) {
        Territory bestTerritory = null;
        int value = 0;
        for (Territory curr : getTerritoriesOccupied().values()) {
            if (bestTerritory == null) {
                bestTerritory = curr;
                value = reinforcePriorityValue(bestTerritory, troopBonus);
            } else {
                int challengerValue = reinforcePriorityValue(curr, troopBonus);
                if (value < challengerValue) {
                    value = challengerValue;
                    bestTerritory = curr;
                }
            }
        }
        return bestTerritory;
    }

    private int reinforcePriorityValue(Territory territory, int troopBonus) {
        int value = leastEnemySurrounded(enemyNeighbourRatio(territory)) + lowestTroopTerritory(territory) + continentValue(territory);
        for (Territory enemies : territory.getNeighbours().values()){
            if(!enemies.getOccupant().equals(this)){
                value += predictiveAttackProbability(territory, enemies, troopBonus);
            }
        }
        return value;
    }

    /**
     * This method is used split the amount of deployable troops in order to avoid the AI from placing all of its troops.
     *
     * @return int the amount of troops to deploy
     */
    public int splitDeployTroops() {
        int troops = this.getDeployableTroops();
        if(troops <= 3) {
            return troops;
        }
        return (troops/3);
    }

    /**
     * The AIPlayer's reinforce move.
     */
    public void reinforce() {
        int troopBonus = splitDeployTroops();
        Territory bestTerritory = bestReinforceTerritory(troopBonus);
        gameEvent.reinforce(bestTerritory,troopBonus);
    }

    /**
     * This method is used find the best territory to attack with and to attack.
     *
     * @return ArrayList<Territory> index 0 is the attacking territory, index 1 is the defending territory
     */
    public ArrayList<Territory> bestAttackTerritory() {
        ArrayList<Territory> weakest = new ArrayList<>();
        double highestValue = 2;
        double threshold = 2;
        for(Territory allTerritories : getTerritoriesOccupied().values()){
            for(Territory currentEnemy : allTerritories.getNeighbours().values()){
                if(!currentEnemy.getOccupant().equals(this)){
                    double newValue = successfulAttackProbability(allTerritories, currentEnemy, threshold);
                    if(weakest.size() == 0 && highestValue < newValue){
                        highestValue = newValue;
                        weakest.add(allTerritories);
                        weakest.add(currentEnemy);
                    }else if(highestValue < newValue){
                        highestValue = newValue;
                        weakest.set(0,allTerritories);
                        weakest.set(1,currentEnemy);
                    }
                }
            }
        }
        return weakest;
    }

    /**
     * This method is used for the AIPlayer's attack move.
     */
    public void attack() {
        ArrayList<Territory> terrAttack = bestAttackTerritory();
        if (terrAttack.size() != 0){
            attackingMove(terrAttack.get(0), terrAttack.get(1));
            winningAttackingMove(terrAttack.get(0), terrAttack.get(1));
        }else {
            attacking = false;
        }
    }

    /**
     * This method is used when an AIPlayer defeats the defending territory and gets to move troops into the newly won territory.
     * @param attacking the attacking territory
     * @param defending the defending territory
     */
    public void winningAttackingMove(Territory attacking, Territory defending) {
        if(gameEvent.getAttackerWon()) {
            int value = leastEnemySurrounded(enemyNeighbourRatio(attacking));
            if(value < 0) {
                gameEvent.fortify(attacking, defending, (attacking.getTroops()-1));
            } else {
                gameEvent.fortify(attacking, defending, (attacking.getTroops()/2));
            }
            this.setFortifyStatus(true);
        }
    }

    /**
     * This method decides the attacking move for the AIPlayer
     * @param attacker the attacking territory
     * @param defender the defending territory
     */
    public void attackingMove(Territory attacker, Territory defender) {
        if(attacker.getTroops() >= 4){
            gameEvent.attack(attacker, defender, 3);
        }else if(attacker.getTroops() == 3){
            gameEvent.attack(attacker, defender, 2);
        }else {
            gameEvent.attack(attacker, defender, 1);
        }
    }

    /**
     * Gets the state of game
     *
     * @return GameState state of the game
     */
    public GameState getState(){
        return gameModel.getCurrentState();
    }

    /**
     * Changes the state of the game. Reinforce --NEXT STATE--> Attack --NEXT STATE--> Fortify
     */
    public void nextState(){
        attacking = true;
        gameModel.nextState();
    }

    /**
     * Determines if the troops being deployed will greatly assist the territory in need during the attacking phase.
     *
     * @param allyTerritory belongs to the AI
     * @param enemyTerritory AI considers to attack
     * @param deployableBonus the amount of troops
     * @return int priority
     */
    private int predictiveAttackProbability(Territory allyTerritory, Territory enemyTerritory, int deployableBonus){
        int total_with = allyTerritory.getTroops() + deployableBonus + enemyTerritory.getTroops();
        float troopDifference_with = (float) (allyTerritory.getTroops() + deployableBonus - enemyTerritory.getTroops( ))/total_with;

        int total = allyTerritory.getTroops() + enemyTerritory.getTroops();
        float troopDifference = (float) (allyTerritory.getTroops() - enemyTerritory.getTroops())/total;

        if(troopDifference_with/troopDifference > 2) {
            return 3;
        } else if(troopDifference > 1.5){
            return 2;
        }else if(troopDifference > 1.25) {
            return 1;
        }else{
            return 0;
        }
    }

    /**
     * This method is used to calculate weather or not the player should attack,
     * while attempting to preserve a certain amount of troops in the territory. It uses a logarithmic function to calculate the risk of the outcome.
     *
     * @param allyTerritory belongs to the AI
     * @param enemyTerritory AI considers to attack
     * @param threshold the amount of troops
     * @return int priority
     */
    private double successfulAttackProbability(Territory allyTerritory, Territory enemyTerritory, double threshold){
        int difference = allyTerritory.getTroops() - enemyTerritory.getTroops();
        return Math.log((double) allyTerritory.getTroops()-threshold) + difference;
    }

    /**
     * Finds if the AIPlayer is still attacking.
     *
     * @return boolean true if attacking, else false.
     */
    public boolean stillAttacking(){
        return attacking;
    }

    /**
     * This method is used to find if the AIPlayer is close to controlling the continent or not.
     *
     * @param territory the territory to check
     * @return int priority
     */
    private int continentValue(Territory territory){
        Continent temp_continent = gameModel.getContinent(territory);
        int continentSize = temp_continent.getContinentTerritory().size();
        int ownedTerritories = 0;
        for(Territory c_terri : temp_continent.getContinentTerritory().values()){
            if(c_terri.getOccupant().equals(this)){
                ownedTerritories++;
            }
        }
        float continentRatio = (float) ownedTerritories/continentSize;
        if (continentRatio == 1){
            return -5;
        } else if(continentRatio >= 0.75){
            return 5;
        }else {
           return  0;
        }
    }

    /**
     * Creates a fortify event if a territory can benefit from fortifying
     */
    public void fortify(){
        Territory[] territories = bestFortifyTerritory();
        if(territories != null){
            gameEvent.fortify(territories[0], territories[1], availableTroopsToReceive(territories[0]));
        }
    }

    /**
     * Checks for the best territory to reinforce.
     * Territory will only be reinforced if there is a territory that can donate troops without increasing their own
     * threat level.
     * @return territory array, index 0 is territory to lose troops, index 1 is territory to gain troops.
     */
    private Territory[] bestFortifyTerritory(){
        Territory[] territories = new Territory[2];
        HashMap<Territory, Integer> threatMap = new HashMap<>();

        for(Territory t : this.getTerritoriesOccupied().values()){ //assign threat to each territory
            threatMap.put(t, assignThreatLevel(t, t.getTroops()));
        }

        Territory[] mostInDanger = getGreatestDangerLevel(threatMap); //array of territories in decreasing danger
        if (mostInDanger != null) {
            for (Territory territory : mostInDanger) { //check if territory can be helped
                for (Territory friendlyNeighbour : territory.getLinkedNeighbours()) {
                    if (assignThreatLevel(friendlyNeighbour, friendlyNeighbour.getTroops()) < assignThreatLevel(territory, territory.getTroops())) {
                        //friend is safer than you, so they can help
                        int newThreatLevelTerritory = assignThreatLevel(territory, territory.getTroops() + availableTroopsToReceive(friendlyNeighbour));
                        int newThreatLevelDonoTerritory = assignThreatLevel(friendlyNeighbour, friendlyNeighbour.getTroops() - availableTroopsToReceive(friendlyNeighbour));
                        if (newThreatLevelTerritory < threatMap.get(territory) && newThreatLevelDonoTerritory == assignThreatLevel(friendlyNeighbour, friendlyNeighbour.getTroops())) {
                            //friend helping you will not hurt them, so they will sent troops
                            territories[0] = friendlyNeighbour;
                            territories[1] = territory;
                            return territories;
                        }//else it will hurt friend so you will find some other friend
                    }
                }
            }
        }
        return null; //no good fortify move found
    }

    /**
     * Takes a hashmap of territories and their threat levels, returns a sorted array of territories decreasing based
     * on their threat levels.
     * @param map hashmap of territories and their threat levels
     * @return sorted Territory array
     */
    private Territory[] getGreatestDangerLevel(HashMap<Territory, Integer> map){
        if(map.isEmpty()){
            return null;
        }

        Territory[] sortedDanger = new Territory[map.size()];
        int i = 0;
        for(Territory t : map.keySet()){ //fill array with territories
            sortedDanger[i] = t;
            i++;
        }

        //sorts array based on territories threat level in hashmap
        for(int j = 0; j < sortedDanger.length; j++){
            for(int k = 0; k < sortedDanger.length; k++){
                if(map.get(sortedDanger[j]) < map.get(sortedDanger[k])){
                    Territory temp = sortedDanger[j];
                    sortedDanger[j] = sortedDanger[k];
                    sortedDanger[k] = temp;
                }
            }
        }
        return sortedDanger;
    }

    /**
     * Assigns threat level to a territory. level 0 if territory is safe, level 1 if territory borders and enemy
     * that has less troops, level 2 territory borders and enemy with more troops
     * @param t territory
     * @param troops number of troops
     * @return threat level
     */
    private int assignThreatLevel(Territory t, int troops){
        int threat = 0;
        if (!isSafe(t)) {
            for (Territory neighbourTerritory : t.getNeighbours().values()) {
                if (!neighbourTerritory.getOccupant().equals(t.getOccupant())) {
                    if (troops > neighbourTerritory.getTroops() && threat < 1) {
                        threat = 1;
                    } else {
                        threat = 2;
                    }
                }
            }
        }
        return threat;
    }

    /**
     * Returns the number of troops that a territory can donate and still be safe. If a territory is surrounded by
     * its own country it will donate all but 1 troop. If the territory has an enemy neighbour it will keep 2 more
     * troops than the enemy and donate the rest.
     * !! Could return 0 or negative troops !!
     * @param t Territory
     * @return int - The number of troops that territory t can donate
     */
    private int availableTroopsToReceive(Territory t){
        if(isSafe(t)){
            return t.getTroops()-1;
        }else{
            //Check for neighbouring enemy with highest number of troops (AKA Biggest threat)
            int enemyTroops = 0;
            for(Territory enemyTerritory : t.getNeighbours().values()){
                if(!enemyTerritory.getOccupant().equals(t.getOccupant())){
                    if(enemyTerritory.getTroops() > enemyTroops){
                        enemyTroops = enemyTerritory.getTroops();
                    }
                }
            }
            return t.getTroops() - (enemyTroops + 2);
        }
    }

    /**
     * Checks if a territory is surrounded by territories of the same owner
     * @param t Territory to check
     * @return true if all neighbours are owned by the owner of t
     */
    private boolean isSafe(Territory t){
        for(Territory neighbouringTerritory: t.getNeighbours().values()){
            if(!neighbouringTerritory.getOccupant().equals(this)){
                return false;
            }
        }
        return true;
    }

    /**
     * Sets all the attributes of the AIPlayer into the JSON file.
     * @return JSONObject holds the AIPlayer attributes
     */
    public JSONObject saveJSON(){
        JSONAIPlayer player_json = new JSONAIPlayer();
        player_json.setPlayer(getName());
        player_json.setPlayerIndex(getPlayerNumber());
        player_json.setColor(getPlayer_color());
        player_json.setDeployableTroops(getDeployableTroops());
        player_json.setFortify(getFortifyStatus());
        player_json.setTotalTroops(getTotal_troops());
        player_json.setType(getClass().getName());
        player_json.setInGame(isInGame());
        player_json.setOccupiedTerritories(getTerritoriesOccupied().keySet());
        player_json.setAttacking(attacking);
        player_json.setIconPath(getFilePath());
        return player_json.getAiPlayer();
    }

}
