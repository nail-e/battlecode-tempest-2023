package Jimmy;

import battlecode.common.Anchor;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.WellInfo;

public class Carrier extends MovableRobot {
    RobotType robotType = RobotType.AMPLIFIER;
    Boolean hasMaterial;
    int adamantium = 0;
    int mana = 0;
    int elixer = 0;

    Boolean wait = false;
    CarrierTarget target = CarrierTarget.WELL;
    MapLocation targetWell;
    MapLocation targetHeadQuarter;
    MapLocation targetIsland;
    Boolean collecting = false;
    WellInfo[] nearbyWells;
    int anchorCarrierIndex = 0;

    enum CarrierTarget {
        HEADQUARTERS,
        WELL,
        ISLAND
    }

    Carrier() throws GameActionException {
        super();
    }

    @Override
    public void initialize() throws GameActionException {
        super.initialize();
        getLatestCommunications();
    }

    void getLatestCommunications() throws GameActionException {
        Robot.senseNearbyIslands();
        Communication.getWells();
        Communication.getIslandInfos();
        Communication.getHeadquarterInfo();
    }

    @Override
    public void act() throws GameActionException {
        super.act();
        getLatestCommunications();



        // determind if we can write to the shared array;
        if(rc.canWriteSharedArray(0, 0)){
            Communication.canWriteSharedArray = true;
            Communication.tryWriteMessages();
        } else {
            Communication.canWriteSharedArray = false;
        }
        if(rc.getAnchor() != null){
            target = CarrierTarget.ISLAND;
        }else if (((this.mana + this.elixer + this.adamantium) > 0 && !collecting) || (targetHeadQuarter != null && rc.canTakeAnchor(targetHeadQuarter, Anchor.STANDARD))) {
            target = CarrierTarget.HEADQUARTERS;
        } else {
            target = CarrierTarget.WELL;
        }
        if(target != CarrierTarget.WELL) targetWell = null;
        if(target != CarrierTarget.HEADQUARTERS) targetHeadQuarter = null;
        if(target != CarrierTarget.ISLAND) targetIsland = null;

        if(targetHeadQuarter == null && target == CarrierTarget.HEADQUARTERS){
            targetHeadQuarter = Utils.getClosestMapLocationFromArray(Communication.headquarterLocs, Robot.location);
        } else if(targetWell == null && target == CarrierTarget.WELL){
            targetWell = Utils.getClosestWell();
        } else if(target == CarrierTarget.ISLAND && (targetIsland == null  || !Utils.isStillAValidIslandTarget(targetIsland))){
            MyIslandInfo _targetIsland = Utils.getClosestOpenIsland(Communication.islandInfos, Robot.location);
            if(_targetIsland != null){
                targetIsland = _targetIsland.location;
            }else {
                targetIsland = null;
            }

        }




        if (target == CarrierTarget.ISLAND) {
            if(rc.canPlaceAnchor()){
                for(int i = 0; i<Robot.nearbyIslands.length;i++){

                    int islandId = Robot.nearbyIslands[i];
                    Team team = rc.senseTeamOccupyingIsland(islandId);
                    if(!team.equals(Robot.myTeam)){
                        rc.setIndicatorString("Huzzah, placed anchor at" + rc.getLocation());
                        System.out.println("Huzzah, placed anchor at" + rc.getLocation());
                        rc.placeAnchor();
                        Communication.senseNearbyIslands();
                        target = CarrierTarget.WELL;
                        targetIsland = null;
                    }
                }
            }
            return;
        }


        /*
         * defend yourself if you can
         */
        if (adamantium > 0 || mana > 0 || elixer > 0) {
             if (Robot.nearbyRobots.length > 0) {
                 for (int i = 0; i < Robot.nearbyRobots.length; i++) {
                     RobotInfo nearbyRobot = Robot.nearbyRobots[i];
                     if (nearbyRobot.team != myTeam && robotType == RobotType.LAUNCHER) {
                         if (rc.canAttack(nearbyRobot.location)) {
                             rc.attack(nearbyRobot.location);
                             target = CarrierTarget.WELL;
                             return;
                         }
                     }
                     if(nearbyRobot.team == myTeam && robotType == RobotType.HEADQUARTERS){

                     }
                 }
             }
         }

        if (targetHeadQuarter != null) {
            if (rc.canTakeAnchor(targetHeadQuarter, Anchor.STANDARD)) {
                rc.takeAnchor(targetHeadQuarter, Anchor.STANDARD);
            }
        }







        if (targetHeadQuarter != null) {

            if (rc.canTransferResource(targetHeadQuarter, ResourceType.ADAMANTIUM, this.adamantium)) {
                rc.transferResource(targetHeadQuarter, ResourceType.ADAMANTIUM, this.adamantium);
                this.adamantium = 0;
            }

            if (rc.canTransferResource(targetHeadQuarter, ResourceType.MANA, this.mana)) {
                rc.transferResource(targetHeadQuarter, ResourceType.MANA, this.mana);
                this.mana = 0;
            }

            if (rc.canTransferResource(targetHeadQuarter, ResourceType.ELIXIR, this.elixer)) {
                rc.transferResource(targetHeadQuarter, ResourceType.ELIXIR, this.elixer);
                this.elixer = 0;
            }

            if (this.adamantium == 0 && this.mana == 0 && this.elixer == 0) {
                target = CarrierTarget.WELL;
                // targetWell = Utils.getClosestWell();
            }


        }

        /*
         * stop and collect if you can.
         */
        if((this.mana + this.elixer + this.adamantium) < 40){
            collecting = false;
            for(int i = 0; i<Robot.nearbyWells.length;i++){
                WellInfo nearbyWell = Robot.nearbyWells[i];
                MapLocation wellLocation = nearbyWell.getMapLocation();
                if (rc.canCollectResource(wellLocation, -1)) {
                    collecting = true;
                    rc.collectResource(wellLocation, -1);
                    this.mana = rc.getResourceAmount(ResourceType.MANA);
                    this.adamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);
                    this.elixer = rc.getResourceAmount(ResourceType.ELIXIR);
                    // targetHeadQuarter = utils.getRandomMapLocationFromArray(headquarters);
                }
            }
        } else {
            collecting = false;
        }

    }

    @Override
    public void move() throws GameActionException {
        if (collecting) return;
        if (target == CarrierTarget.HEADQUARTERS && targetHeadQuarter != null) {
            Pathing.moveTowards( targetHeadQuarter);
        } else if (targetWell != null && target == CarrierTarget.WELL) {
            Pathing.moveTowards( targetWell);
        } else if (targetIsland != null && target == CarrierTarget.ISLAND) {
            Pathing.moveTowards( targetIsland);
        }

    }

}
