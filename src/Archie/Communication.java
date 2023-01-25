package Archie;
import java.util.ArrayList;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.*;
import java.awt.List;

class Message {
    public int idx;
    public int value;
    public int turnAdded;

    Message (int idx, int value, int turnAdded) {
        this.idx = idx;
        this.value = value;
        this.turnAdded = turnAdded;
    }
}
class Communication {
    //sexy bit stuff 2: communication thing boogaloo
    private static final int OUTDATED_TURNS_AMOUNT = 30;
    private static final int AREA_RADIUS = RobotType.CARRIER.visionRadiusSquared;

    //Maybe we should change this based on exact amounts of which we can get on turn 1 -Shaun
    static final int STARTING_ISLAND_IDX = GameConstants.MAX_STARTING_HEADQUARTERS;
    private static final int STARTING_ENEMY_IDX = GameConstants.MAX_NUMBER_ISLANDS + GameConstants.MAX_STARTING_HEADQUARTERS;

    private static final int TOTAL_BITS = 16;
    private static final int MAPLOC_BITS = 12;
    private static final int TEAM_BITS = 1;
    private static final int HEALTH_BITS = 3;
    private static final int HEALTH_SIZE = (int) Math.ceil(Anchor.ACCELERATING.totalHealth / 8.0);

    private static List<Message> messagesQueue = new ArrayList<>();
    private static MapLocation[] headquarterLocs = new MapLocation[GameConstants.MAX_STARTING_HEADQUARTERS];


    static void addHeadquarter(RobotController rc) throws GameActionException {
        MapLocation me = rc.getLocation();
        for (int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++) {
            if (rc.readSharedArray(i) == 0) {
                rc.writeSharedArray(i, locationToInt(rc, me));
                break;
            }
        }
    }

    static void updateHeadquarterInfo(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 2) {
            for (int i = 0; i < GameConstants.MAX_STARTING_HEADQUARTERS; i++) {
                headquarterLocs[i] = (intToLocation(rc, rc.readSharedArray(i)));
                if (rc.readSharedArray(i) == 0) {
                    break;
                }
            }
        }
    }

    static void tryWriteMessages(RobotController rc) throws GameActionException {
        messagesQueue.removeIf(msg -> msg.turnAdded + OUTDATED_TURNS_AMOUNT < RobotPlayer.turnCount);
        // Can always write (0, 0), so just checks if we are in range to write
        if (rc.canWriteSharedArray(0, 0)) {
            while (messagesQueue.size() > 0) {
                Message msg = messagesQueue.remove(0); // taking from the front or back?
                if (rc.canWriteSharedArray(msg.idx, msg.value)) {
                    rc.writeSharedArray(msg.idx, msg.value);
                }
            }
        }
    }

    static void updateIslandInfo(RobotController rc, int id) throws GameActionException {
        MapLocation [] islandLocations = rc.senseNearbyIslandLocations(null, -1, id);
        if (islandLocations.length > 0) {
            int idx_to_write = id + STARTING_ISLAND_IDX;
            int value = locationToInt(rc, islandLocations[0]);
            if (value != rc.readSharedArray(idx_to_write)) {
                rc.canWriteSharedArray(idx_to_write, value);
            }
        }
    }

    static int BitPackIslandInfo(RobotController rc, int islandId, MapLocation closestLoc) {
        int intIsland = locationToInt(rc, closestLoc);
        intIsland = intIsland << (TOTAL_BITS - MAPLOC_BITS);
        try {
            Team team = rc.senseTeamOccupyingIsland(islandId);
            intIsland += team.ordinal() << HEALTH_BITS;
            int health = rc.senseAnchorPlantedHealth(islandId);
            intIsland += (int) Math.round((float) health / HEALTH_SIZE);
            return intIsland;
        } catch (GameActionException e) {return intIsland;}

    }

    static Team readTeamHoldingIsland(RobotController rc, int islandId) {
        try {
            islandId = islandId + STARTING_ISLAND_IDX;
            int islandInt = rc.readSharedArray(islandId);
            int healthMask = 0b111;
            int health = islandInt & healthMask;
            int team = (islandInt >> HEALTH_BITS) & 0b1;
            if (health > 0) {
                return Team.values()[team];
            }
        } catch (GameActionException e) {}
        return Team.NEUTRAL;
    }

    static MapLocation readIslandLocation(RobotController rc, int islandId) {
        try {
            islandId = islandId + STARTING_ISLAND_IDX;
            int islandInt = rc.readSharedArray(islandId);
            int islandLocIdx = islandInt >> (HEALTH_BITS + TEAM_BITS);
            return intToLocation(rc, islandLocIdx);
        } catch (GameActionException e) {return null;}
    }
    static int readMaxIslandHealth(RobotController rc, int islandId) {
        try {
            islandId = islandId + STARTING_ISLAND_IDX;
            int islandInt = rc.readSharedArray(islandId);
            int healthMask = 0b111;
            int health = islandInt & healthMask;
            return health;
        } catch (GameActionException e) {return -1;}
    }

    // static void clearObsoleteEnemies(RobotController rc) throws GameActionException {
        // for (int i = STARTING_ENEMY_IDX; i < GameConstants.SHARED_ARRAY_LENGTH; i++) {
            // MapLocation enemyLoc = intToLocation(rc.readSharedArray(i));
            // if (enemyLoc == null)
                // continue;
            // if (!rc.canSenseLocation(enemyLoc))
                // continue; //placeholder code
            // }
        // }

    static void reportEnemy(RobotController rc, MapLocation enemy) {

    }

    static void getClosestEnemy(RobotController rc) {

    }

    private static int locationToInt(RobotController rc, MapLocation m) {
        if (m == null) {
            return 0;
        }
        return 1 + m.x + m.y * rc.getMapWidth();
    }

    private static MapLocation intToLocation(RobotController rc, int m) {
        if (m == 0) {
            return null;
        }
        m--;
        return new MapLocation(m % rc.getMapWidth(), m / rc.getMapWidth()); //FIX THIS ELIAN
        //NVM I FIXED IT WOO
        //fn: make sure u fuckin put the instatiation in the same class
    }
}
