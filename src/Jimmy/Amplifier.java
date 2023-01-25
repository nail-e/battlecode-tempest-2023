package Jimmy;


import java.util.ArrayList;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Amplifier extends MovableRobot {
    RobotType robotType = RobotType.AMPLIFIER;
    MapLocation targetHeadquarters;
    SearchType searchType = SearchType.WELLS;
    final int VISION_RADIUS = 3;
    enum SearchType {
        WELLS,
        ISLANDS
    }
    boolean isMainAmplifier = false;
    ArrayList<MapLocation> edgeLocations;



    Amplifier() throws GameActionException{

    }

    @Override
    public void act() throws GameActionException {
        // TODO Auto-generated method stub
        super.act();
        Robot.senseNearbyIslands();
        Communication.senseNearbyIslands();
    }


    @Override
    public void initialize() throws GameActionException{
        super.initialize();
        Communication.canWriteSharedArray = true;
        Communication.getAmplifierLocation();
        if(Communication.amplifierLocation == null){
            Communication.updateAmplifierLocation(Robot.location);
            isMainAmplifier = true;
        }
        edgeLocations =  new ArrayList<MapLocation>();
        MapLocation BOTTOMLEFT = new MapLocation(VISION_RADIUS, VISION_RADIUS);
        MapLocation LEFT = new MapLocation(VISION_RADIUS, Utils.mapHeight / 2 );
        MapLocation TOPLEFT = new MapLocation(VISION_RADIUS, Utils.mapHeight - VISION_RADIUS);
        MapLocation TOP = new MapLocation(Utils.mapWidth / 2 , Utils.mapHeight  - VISION_RADIUS);
        MapLocation TOPRIGHT = new MapLocation(Utils.mapWidth - VISION_RADIUS, Utils.mapHeight - VISION_RADIUS);
        MapLocation RIGHT = new MapLocation(Utils.mapWidth - VISION_RADIUS, Utils.mapHeight / 2);
        MapLocation BOTTOMRIGHT = new MapLocation(Utils.mapWidth - VISION_RADIUS, VISION_RADIUS);
        MapLocation CENTER = new MapLocation(Utils.mapWidth / 2, Utils.mapHeight /2);
        this.edgeLocations.add(BOTTOMLEFT);
        this.edgeLocations.add(LEFT);
        this.edgeLocations.add(TOPLEFT);
        this.edgeLocations.add(TOP);
        this.edgeLocations.add(TOPRIGHT);
        this.edgeLocations.add(RIGHT);
        this.edgeLocations.add(BOTTOMRIGHT);
        this.edgeLocations.add(CENTER);
    }


    @Override
    void move() throws GameActionException {


        edgeLocations.removeIf(location -> {
            if(Math.abs(location.x - Robot.location.x) < VISION_RADIUS && Math.abs(location.y - Robot.location.y) < VISION_RADIUS){
                return true;
            }
            return false;
        });

        // MapLocation closestEnemy = Utils.getClosestEnemyRobot();
        // if(closestEnemy != null ){
        //     Pathing.moveTowards(closestEnemy);
        //     rc.setIndicatorString("target enemy" + closestEnemy);
        // } else

        if(edgeLocations.size() > 0){
            MapLocation[] unSearchedMapLocations = edgeLocations.toArray(new MapLocation[edgeLocations.size()]);
            MapLocation closestUnsearchedLocation = Utils.getClosestMapLocationFromArray(unSearchedMapLocations, Robot.location);
            Pathing.moveTowards(  closestUnsearchedLocation);
        } else {
            Pathing.moveTowards(new MapLocation(Utils.mapWidth / 2, Utils.mapHeight /2));
        }

        if(rc.getHealth() < 120 && isMainAmplifier){
            Communication.clearAmplifierLocation();
        }
        Communication.updateAmplifierLocation(Robot.location);
        Communication.clearObsoleteEnemies();

    }


}


