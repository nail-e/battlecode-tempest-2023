package examplefuncsplayer2;

import battlecode.common.*;

public class CarrierStrategy {

    static MapLocation hqLoc;
    static MapLocation wellLoc;
    static MapLocation islandLoc;

    static boolean anchorMode = false;

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        if (hqLoc == null) scanHQ(rc);
        if (wellLoc == null) scanWells(rc);

        // Collects from the well if it is close and our inventory is not full
        if(wellLoc != null && rc.canCollectResource(wellLoc,-1)) rc.collectResource(wellLoc, -1);

        //Transfers resources to headquarters
        depositResource(rc, ResourceType.ADAMANTIUM);
        depositResource(rc, ResourceType.MANA);

        int total = getTotalResources(rc);

        if (rc.canTakeAnchor(hqLoc, Anchor.STANDARD)) {
            rc.takeAnchor(hqLoc, Anchor.STANDARD);
            anchorMode = true;
        }

        //If carrier has no resources, it will look for well
        if(anchorMode) {
            if(islandLoc == null) RobotPlayer.moveRandom(rc);
            else RobotPlayer.moveTowards(rc, islandLoc);

            if(rc.canPlaceAnchor()) rc.placeAnchor();
        }
        else {
            if (total == 0) {
                if (wellLoc != null) {
                    MapLocation me = rc.getLocation();
                    if(!me.isAdjacentTo(wellLoc)) RobotPlayer.moveTowards(rc, wellLoc);
                }
                else {
                    RobotPlayer.moveRandom(rc);
                }
            }
            if(total == GameConstants.CARRIER_CAPACITY) {
                // will move towards HQ
                RobotPlayer.moveTowards(rc, hqLoc);
            }
        }
    }

    static void scanHQ(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots) {
            if(robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.HEADQUARTERS) {
                hqLoc = robot.getLocation();
                break;
            }
        }
    }

    static void scanWells(RobotController rc) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 0) wellLoc = wells[0].getMapLocation();
    }
    static void depositResource(RobotController rc, ResourceType type) throws GameActionException {
        int amount = rc.getResourceAmount(type);
        if (amount > 0) {
            if (rc.canTransferResource(hqLoc, type, amount)) rc.transferResource(hqLoc, type, amount);
        }
    }

    static int getTotalResources(RobotController rc) {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM) + rc.getResourceAmount(ResourceType.MANA);
    }

    static void scanIslands(RobotController rc) throws GameActionException {
            int[] ids = rc.senseNearbyIslands();
            for (int id : ids) {
                if(rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL) {
                    MapLocation[] locs = rc.senseNearbyIslandLocations(id);
                    if(locs.length > 0) {
                        islandLoc = locs[0];
                        break;
                    }
                }
            }
        }

}
