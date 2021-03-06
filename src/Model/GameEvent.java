package Model;

/**
 * The Model.GameEvent class is responsible for handling the events that are generated once a Model.Player reinforces, attacks or fortifies.
 * @author Ahmad El-Sammak
 */
public class GameEvent {

    private final Player player;
    private String result;
    private String attackingRolls;
    private String defendingRolls;
    private boolean attackerWon;
    private final static int ZERO_TROOPS = 0;

    /**
     * Constructor for Model.GameEvent class where a Model.GameEvent should be initiated by a Model.Player.
     * @param player Model.Player that initiates the Model.GameEvent
     */
    public GameEvent(Player player) {
        this.player = player;
        attackingRolls = "";
        defendingRolls = "";
        attackerWon = false;
    }

    /**
     * This method is used to add troops to a territory specified by the Model.Player.
     * @param territory territory that troops will be added too.
     * @param troops the number of troops to add.
     */
    public void reinforce(Territory territory, int troops) {
        if(territory.getOccupant().equals(player) && troops <= player.getDeployableTroops() && troops > ZERO_TROOPS) {
            int originalTroopCount = territory.getTroops();
            player.incrementTroops(territory, troops);
            player.subtractDeployableTroops(troops);
            System.out.println("Reinforced territory: " + territory.getTerritoryName());
            System.out.println("Original Troop count:" + originalTroopCount + ", With reinforce: " + territory.getTroops());
            System.out.println("you have " + player.getDeployableTroops() + " LEFT to deploy");
        }else if(troops < ZERO_TROOPS){
            System.out.println("Nice try! No negative troops!");
        }else {
            System.out.println("Ensure that the territory that you are trying to reinforce belongs to you");
            System.out.println("and you have a valid number of troops to deploy");
            System.out.println("you have " + player.getDeployableTroops() + " to deploy");
        }
    }

    /**
     * This method is used to create a temporary Dice object.
     * @return Dice the temp dice
     */
    protected Dice createTempDice()
    {
        return new Dice();
    }

    /**
     * This method is used by the Model.Player when he/she wants to attack a neighbouring territory that is owned by another player.
     * This method will remove troops from either side (attacking/defending) based on the attack result.
     * @param attacking the attacking territory.
     * @param defending the defending territory.
     * @param numDice the number of dice the attacker wants to roll with.
     */
    public void attack(Territory attacking, Territory defending, int numDice) {

        if(attacking.getOccupant().equals(player) && !defending.getOccupant().equals(player) && attacking.isNeighbour(defending)) {
            try {
                Dice temp = createTempDice();
                Dice attackingDice = temp.setUpAttackingDice(attacking.getTroops(), numDice);
                Dice defendingDice = temp.setUpDefendingDice(defending.getTroops());

                AttackResult outcome = temp.attackResult(attackingDice.getRoll(), defendingDice.getRoll());

                switch (outcome) {
                    case D2 -> {
                        result = "Defender loses two troops!";
                        defending.removeTroops((-2));
                    }
                    case A2 -> {
                        result = "Attacker loses two troops!";
                        attacking.removeTroops((-2));
                    }
                    case A1D1 -> {
                        result = "Attacker & Defender lose ONE troop!";
                        attacking.removeTroops((-1));
                        defending.removeTroops((-1));
                    }
                    case D1 -> {
                        result = "Defender loses one troop!";
                        defending.removeTroops((-1));
                    }
                    case A1 -> {
                        result = "Attacker loses one troop!";
                        attacking.removeTroops((-1));
                    }
                }

                setResultRolls(attackingDice, defendingDice);
                winningMove(attacking, defending);

            } catch (NullPointerException e) {
                System.out.println("Null pointer exception!");
            }
        }else{
            System.out.println("You cannot attack your own territory!");
        }
    }

    /**
     * This method is used to store the attacker's and defender's rolls in a string.
     * @param attackingDice the attacker's rolls
     * @param defendingDice the defender's rolls
     */
    private void setResultRolls(Dice attackingDice, Dice defendingDice) {
        attackingRolls = "";
        defendingRolls = "";

        for(int x : attackingDice.getRoll()){
            attackingRolls += " || " + x + " || ";
        }

        for(int y : defendingDice.getRoll()){
            defendingRolls += " || " + y + " || ";
        }
    }

    /**
     * This method is used to check if the attacking territory beat the defending territory after an attack.
     * If the attacker won, then one troop will automatically be transferred from the attacking territory into the newly won defending territory.
     * @param attacking the attacker's territory
     * @param defending the defender's territory
     */
    private void winningMove(Territory attacking, Territory defending) {
        if (defending.getTroops() == ZERO_TROOPS) {
            attackerWon = true;
            (attacking.getOccupant()).addTerritory(defending.getTerritoryName(), defending);
            (defending.getOccupant()).removeTerritory(defending.getTerritoryName());
            defending.setOccupant(attacking.getOccupant());
            defending.setTroops(1);
            attacking.setTroops(attacking.getTroops() - 1);
        }
    }

    /**
     * Gets the result of an attack.
     * @return String
     */
    public String getResult() { return result; }

    /**
     * Gets the rolls of the attacker.
     * @return String
     */
    public String getAttackerRolls() { return attackingRolls; }

    /**
     * Gets the rolls of the defender.
     * @return String
     */
    public String getDefendingRolls() { return defendingRolls; }

    /**
     * Gets if the attacker defeated the defending territory or not.
     * @return boolean true if the attacker won, else false.
     */
    public boolean getAttackerWon() { return attackerWon; }

    /**
     * This method is used by the player to fortify by moving troops from one territory to another.
     * A player can move 1 to (territory1.getTroops() - 1) from one (and only one) of their territories into one (and only one) of their adjacent territories.
     * @param territory1 the territory to move troops FROM.
     * @param territory2 the territory to move troops INTO.
     * @param troops the number of troops to move from territory1 to territory2.
     */
    public void fortify(Territory territory1, Territory territory2, int troops) {
        if(territory1.getOccupant() == territory2.getOccupant() && territory1.getOccupant().equals(player) && territory1.getLinkedNeighbours().contains(territory2) && player.getFortifyStatus()) {
            if(troops < territory1.getTroops() && troops > ZERO_TROOPS) {
                player.decrementTroops(territory1, troops);
                player.incrementTroops(territory2, troops);
                player.setFortifyStatus(false);
                System.out.println("You have moved " + troops + " from " + territory1.getTerritoryName() + " to " + territory2.getTerritoryName());
            } else if (troops <= ZERO_TROOPS) {
                System.out.println("No troops will be moved.");
            } else {
                System.out.println("You cannot move more than " + (territory1.getTroops() - 1));
            }
        }
    }
}
