package Jimmy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.WellInfo;

public abstract class Robot {
    /**
     * We will use this variable to count the number of turns this robot has been
     * alive.
     * You can use static variables like this to save any information you want. Keep
     * in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between
     * your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided
     * by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant
     * number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very
     * useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static RobotController rc;
    static Team myTeam;
    static Team enemyTeam;
    static RobotType robotType;
    static MapLocation location;
    static Utils utils;
    static RobotInfo[] nearbyRobots;
    static MapInfo[] nearbyMapInfos;
    static WellInfo[] nearbyWells;
    static int[] nearbyIslands;

    Robot() throws GameActionException{

        initialize();
    }

    static void getLocation(){
        Robot.location = rc.getLocation();
    }

    static void senseNearbyRobots(){
        Robot.nearbyRobots = rc.senseNearbyRobots();
    }

    static void senseNearbyMapInfo(){
        Robot.nearbyMapInfos = rc.senseNearbyMapInfos();
    }

    static void senseNearbyIslands(){
        Robot.nearbyIslands = rc.senseNearbyIslands();
    }

    static void senseNearbyWells(){
        Robot.nearbyWells = rc.senseNearbyWells();
    }

    static void senseNearby() throws GameActionException{

        // sense locally
        senseNearbyRobots();
        // senseNearbyMapInfo();
        senseNearbyIslands();
        senseNearbyWells();

        // add stuff to comm array
        // Communication.senseEnemies();
        Communication.senseNearbyIslands();
        Communication.senseNearbyWells();
    }

    static void clearNearby(){
        nearbyRobots = null;
        nearbyMapInfos = null;
        nearbyRobots = null;
        nearbyWells = null;
    }

    void initialize() throws GameActionException{
        getLocation();
        myTeam = rc.getTeam();
        enemyTeam = myTeam.opponent();
        Utils.myTeam = myTeam;
        Utils.enemyTeam = enemyTeam;
        Utils.mapWidth = rc.getMapWidth();
        Utils.mapHeight = rc.getMapHeight();
        senseNearby();
    };

    public void run() throws GameActionException {
        // Communication.getAmplifierLocation();
        Communication.getIslandInfos();
        act();
        Robot.turnCount++;
    };



    /**
     * handle how each robot acts
     */
    public void act() throws GameActionException{
        getLocation();
        clearNearby();
        senseNearby();
    };
}

