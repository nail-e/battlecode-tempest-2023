package Dante;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static Dante.Communication.*;
import static Dante.RobotPlayer.attackingTypes;
import static Dante.RobotPlayer.maxCarriersPerArea;

public class Carrier {

    static MapLocation wellLoc;
    static MapLocation hqLoc;
    static MapLocation islandLoc;

    static boolean anchorMode = false;

    static void runCarrier(RobotController rc) throws GameActionException {
        if (wellLoc == null) scanWells(rc);
        if (wellLoc != null && rc.canCollectResource(wellLoc, -1)) rc.collectResource(wellLoc, -1);
        scanIslands(rc);

        if (anchorMode) {
            if (islandLoc == null) {
                for (int i = Communication.STARTING_ISLAND_IDX; i < Communication.STARTING_ISLAND_IDX + GameConstants.MAX_NUMBER_ISLANDS; i++) {
                    MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
                    if (islandNearestLoc != null) {
                        islandLoc = islandNearestLoc;
                        break;
                    }
                }
            } else RobotPlayer.moveTowards(rc, islandLoc);

            if (rc.canPlaceAnchor() && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) == Team.NEUTRAL) {
                rc.placeAnchor();
                anchorMode = false;
            }
        } else {
            int total = getTotalResources(rc);
            if (total == 0) {
                //move towards well or search for well
                if (wellLoc == null) RobotPlayer.moveRandom(rc);
                else if (!rc.getLocation().isAdjacentTo(wellLoc)) RobotPlayer.moveTowards(rc, wellLoc);
            }
            if (total == GameConstants.CARRIER_CAPACITY) {
                //move towards HQ
                RobotPlayer.moveTowards(rc, hqLoc);
            }
        }

        depositResource(rc, ResourceType.ADAMANTIUM);
        depositResource(rc, ResourceType.MANA);

        boolean enemyHeadquarters = false;
        boolean headquarters = false;
        for (RobotInfo robot : rc.senseNearbyRobots(20, rc.getTeam()))
            if (robot.getType().equals(RobotType.HEADQUARTERS)) {
                headquarters = true;
                break;
            }
        for (RobotInfo robot : rc.senseNearbyRobots(20, rc.getTeam().opponent()))
            if (robot.getType().equals(RobotType.HEADQUARTERS)) {
                enemyHeadquarters = true;
                break;
            }
        if (rc.getHealth() < 10 && !headquarters) MoveToHeadquarters(rc);
        if (rc.senseNearbyRobots().length > maxCarriersPerArea) explore(rc);
        for (RobotInfo robot : rc.senseNearbyRobots(20, rc.getTeam().opponent())) {
            AddLauncherDestination(rc, robot.getLocation());
            if (Arrays.asList(attackingTypes).contains(robot.type) && rc.canMove(rc.getLocation().directionTo(robot.getLocation()).opposite()))
                rc.move(rc.getLocation().directionTo(robot.getLocation()).opposite());
        }
    }

    static void scanWells(RobotController rc) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        if (wells.length > 0) wellLoc = wells[ 0 ].getMapLocation();
    }

    static void depositResource(RobotController rc, ResourceType type) throws GameActionException {
        int amount = rc.getResourceAmount(type);
        if (amount > 0) {
            if (rc.canTransferResource(hqLoc, type, amount)) rc.transferResource(hqLoc, type, amount);
        }
    }

    static int getTotalResources(RobotController rc) {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM)
                + rc.getResourceAmount(ResourceType.MANA)
                + rc.getResourceAmount(ResourceType.ELIXIR);
    }

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for (int id : ids) {
            if (rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL) {
                MapLocation[] locs = rc.senseNearbyIslandLocations(id);
                if (locs.length > 0) {
                    islandLoc = locs[ 0 ];
                    break;
                }
            }
        }
    }
}
