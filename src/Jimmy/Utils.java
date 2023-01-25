package Jimmy;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.WellInfo;

public class Utils {

    // HP
    static final int LAUNCHER_STARTING_HEALTH = 20;
    static final int CARRIER_STARTING_HEALTH = 15;
    static final int DESTABILIZER_STARTING_HEALTH = 30;
    static final int BOOSTER_STARTING_HEALTH = 40;


    // misc
    static final int TEST_VALUE = 65535;

    static RobotController rc;
    static RobotType robotType;
    static int mapWidth;
    static int mapHeight;
    private int updateNumber = 0;
    static Random random = new Random();

    static Team myTeam;
    static Team enemyTeam;




    public static double getDistanceBetweenTwoPoints(MapLocation locationOne, MapLocation locationTwo) {
        return Math.sqrt(Math.pow(locationTwo.x - locationOne.x, 2) + Math.pow(locationTwo.y - locationOne.y, 2));
    }

    public static MapLocation getClosestMapLocationFromArray(MapLocation[] locations, MapLocation startLocation) {
        double minDistance = Double.MAX_VALUE;
        MapLocation closest = null;
        for (MapLocation loc : locations) {
            if (loc == null)
                continue;
            double distance = getDistanceBetweenTwoPoints(loc, startLocation);
            if (distance < minDistance) {
                minDistance = distance;
                closest = loc;
            }
        }
        return closest;
    }



    public static MapLocation getFarthestMapLocationFromArray(MapLocation[] locations, MapLocation startLocation) {
        double maxDistance = Double.MIN_VALUE;
        MapLocation closest = null;
        for (MapLocation loc : locations) {
            if (loc == null)
                continue;
            double distance = getDistanceBetweenTwoPoints(loc, startLocation);
            if (distance > maxDistance) {
                maxDistance = distance;
                closest = loc;
            }
        }
        return closest;
    }

    public static MapLocation getRandomMapLocationFromArray(MapLocation[] locations) {
        ArrayList<MapLocation> nonNullMapLocations = new ArrayList<MapLocation>();
        for (int i = 0; i < locations.length; i++) {
            if (locations[i] != null) {
                nonNullMapLocations.add(locations[i]);
            }
        }



        if(nonNullMapLocations.size() < 1) return null;

        return nonNullMapLocations.get(random.nextInt(nonNullMapLocations.size()));
    }


    public static Boolean buildRobotAtLocation(MapLocation location, RobotType robotType) throws GameActionException {
        Direction[] directions = Direction.allDirections();
        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
            MapLocation testLoc = Robot.location.add(direction);
            if (rc.canBuildRobot(robotType, testLoc)) {
                rc.buildRobot(robotType, testLoc);
                return true;
            }
        }

        return false;
    }

    public static MapLocation[] getSurroundingLocations(MapLocation location) {
        // Create an array to store the surrounding locations
        MapLocation[] surroundingLocations = new MapLocation[9];

        // Add the 8 surrounding locations to the array
        surroundingLocations[0] = location;
        surroundingLocations[1] = location.translate(1, 0);
        surroundingLocations[2] = location.translate(1, 1);
        surroundingLocations[3] = location.translate(0, 1);
        surroundingLocations[4] = location.translate(-1, 1);
        surroundingLocations[5] = location.translate(-1, 0);
        surroundingLocations[6] = location.translate(-1, -1);
        surroundingLocations[7] = location.translate(0, -1);
        surroundingLocations[8] = location.translate(1, -1);

        return surroundingLocations;
    }


    public static MyWellInfo getClosestOpenWell(MyWellInfo[] wells, MapLocation startLocation){
        double minDistanceClosestOpenWell = Double.MAX_VALUE;
        MyWellInfo closestOpenWell = null;

        for (MyWellInfo well : wells) {
            if (well == null) continue;

            double distance = getDistanceBetweenTwoPoints(well.location, startLocation);
        }
        return closestOpenWell;

    }


    public static MyIslandInfo getClosestOpenIsland(MyIslandInfo[] islands, MapLocation startLocation){
        double minDistance = Double.MAX_VALUE;
        MyIslandInfo closest = null;
        for (MyIslandInfo island : islands) {
            if (island == null || island.team == Robot.myTeam) continue;
            double distance = getDistanceBetweenTwoPoints(island.location, startLocation);
            if (distance < minDistance) {
                minDistance = distance;
                closest = island;
            }
        }
        return closest;
    }


    public static MapLocation getClosestEnemyIsland(MyIslandInfo[] islands, MapLocation startLocation){
        double minDistance = Double.MAX_VALUE;
        MapLocation closest = null;
        for (MyIslandInfo island : islands) {
            if (island == null || island.team != Robot.enemyTeam) continue;
            double distance = getDistanceBetweenTwoPoints(island.location, startLocation);
            if (distance < minDistance) {
                minDistance = distance;
                closest = island.location;
            }
        }
        return closest;
    }


    public static double getSquaredEuclideanDistance(MapLocation location1, MapLocation location2) {
        double xDiff = location2.x - location1.x;
        double yDiff = location2.y - location1.y;
        return xDiff * xDiff + yDiff * yDiff;
    }


    public static RobotInfo[] getEnemyRobots(){
        ArrayList<RobotInfo> enemyRobots = new ArrayList<RobotInfo>();
        for(int i = 0; i<Robot.nearbyRobots.length;i++){
            RobotInfo robot = Robot.nearbyRobots[i];
            if(robot.team.equals(Robot.enemyTeam)){
                enemyRobots.add(robot);
            }
        }

        return enemyRobots.toArray(new RobotInfo[enemyRobots.size()]);
    }

    public static boolean canSenseLocation(MapLocation location){
        for(int i = 0; i<Robot.nearbyMapInfos.length;i++){
            MapInfo nearbyMapInfo = Robot.nearbyMapInfos[i];
            if(nearbyMapInfo.getMapLocation().equals(location)){
                return true;
            }
        }
        return false;
    }

    public static RobotInfo senseRobotAtLocation(MapLocation location){
        for(int i = 0; i<Robot.nearbyRobots.length;i++){
            RobotInfo nearbyRobot = Robot.nearbyRobots[i];
            if(nearbyRobot.location.equals(location)){
                return nearbyRobot;
            }
        }
        return null;
    }


    public static MapLocation getClosestWell(){
        MyWellInfo[] commWells = Communication.wellInfos;
        WellInfo[] localWells = Robot.nearbyWells;
        double minDistance = Double.MAX_VALUE;
        MapLocation closest = null;

        for(int i = 0; i<commWells.length;i++){
            MyWellInfo commWell = commWells[i];
            if(commWell == null) continue;
            double distance = getDistanceBetweenTwoPoints(commWell.location, Robot.location);
            if(distance < minDistance){
                minDistance = distance;
                closest = commWell.location;
            }
        }

        for(int i = 0; i<localWells.length;i++){
            WellInfo localWell = localWells[i];
            MapLocation location = localWell.getMapLocation();
            double distance = getDistanceBetweenTwoPoints(location, Robot.location);
            if(distance < minDistance){
                minDistance = distance;
                closest = location;
            }
        }

        return closest;
    }

    /**
     *  prio launchers
     */
    public static MapLocation getClosestEnemyRobot(){
        MyRobotInfo[] commRobots = Communication.enemyRobots;
        RobotInfo[] localRobots = Robot.nearbyRobots;
        double minDistance = Double.MAX_VALUE;
        MapLocation closest = null;
        RobotType targetType = RobotType.HEADQUARTERS;

        for(int i = 0; i<commRobots.length;i++){
            MyRobotInfo commRobot = commRobots[i];
            if(commRobot == null) continue;
            double distance = getDistanceBetweenTwoPoints(commRobot.location, Robot.location);
            if(targetType == commRobot.robotType && distance < minDistance){
                minDistance = distance;
                closest = commRobot.location;
                targetType = commRobot.robotType;
            } else {
                if(commRobot.robotType == RobotType.DESTABILIZER){
                    minDistance = distance;
                    closest = commRobot.location;
                    targetType = commRobot.robotType;
                } else if(commRobot.robotType == RobotType.BOOSTER){
                    if(targetType != RobotType.DESTABILIZER){
                        minDistance = distance;
                        closest = commRobot.location;
                        targetType = commRobot.robotType;
                    }
                } else if(commRobot.robotType == RobotType.LAUNCHER){
                    if(targetType != RobotType.DESTABILIZER && targetType != RobotType.BOOSTER){
                        minDistance = distance;
                        closest = commRobot.location;
                        targetType = commRobot.robotType;
                    }
                } else if(commRobot.robotType == RobotType.AMPLIFIER){
                    if(targetType != RobotType.DESTABILIZER && targetType != RobotType.BOOSTER && targetType != RobotType.LAUNCHER){
                        minDistance = distance;
                        closest = commRobot.location;
                        targetType = commRobot.robotType;
                    }
                } else if(commRobot.robotType == RobotType.CARRIER){
                    if(targetType != RobotType.DESTABILIZER && targetType != RobotType.BOOSTER && targetType != RobotType.LAUNCHER && targetType != RobotType.AMPLIFIER){
                        minDistance = distance;
                        closest = commRobot.location;
                        targetType = commRobot.robotType;
                    }
                }
            }
        }

        for(int i = 0; i<localRobots.length;i++){
            RobotInfo localRobot = localRobots[i];
            if(localRobot.team == Robot.myTeam) continue;
            MapLocation location = localRobot.location;
            double distance = getDistanceBetweenTwoPoints(location, Robot.location);
            if(targetType == localRobot.type && distance < minDistance){
                minDistance = distance;
                closest = localRobot.location;
                targetType = localRobot.type;
            } else {
                if(localRobot.type == RobotType.DESTABILIZER){
                    minDistance = distance;
                    closest = localRobot.location;
                    targetType = localRobot.type;
                } else if(localRobot.type == RobotType.BOOSTER){
                    if(targetType != RobotType.DESTABILIZER){
                        minDistance = distance;
                        closest = localRobot.location;
                        targetType = localRobot.type;
                    }
                } else if(localRobot.type == RobotType.LAUNCHER){
                    if(targetType != RobotType.DESTABILIZER && targetType != RobotType.BOOSTER){
                        minDistance = distance;
                        closest = localRobot.location;
                        targetType = localRobot.type;
                    }
                } else if(localRobot.type == RobotType.AMPLIFIER){
                    if(targetType != RobotType.DESTABILIZER && targetType != RobotType.BOOSTER && targetType != RobotType.LAUNCHER){
                        minDistance = distance;
                        closest = localRobot.location;
                        targetType = localRobot.type;
                    }
                } else if(localRobot.type == RobotType.CARRIER){
                    if(targetType != RobotType.DESTABILIZER && targetType != RobotType.BOOSTER && targetType != RobotType.LAUNCHER && targetType != RobotType.AMPLIFIER){
                        minDistance = distance;
                        closest = localRobot.location;
                        targetType = localRobot.type;
                    }
                }
            }
        }

        return closest;
    }

    public static boolean isWithinMapBounds(MapLocation location){
        return (location.x > 0 && location.y > 0 && location.x < Utils.mapWidth  && location.y < Utils.mapHeight );
    }

    public static boolean isAtEdge(MapLocation location){
        return (location.x == 0 || location.y == 0 || location.x == Utils.mapWidth -1  || location.y == Utils.mapHeight - 1 );
    }

    public static List<MapLocation> getAllCoordinatesBetween(MapLocation start, MapLocation end) {
        List<MapLocation> coordinates = new ArrayList<MapLocation>();
        int x1 = start.x;
        int y1 = start.y;
        int x2 = end.x;
        int y2 = end.y;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            coordinates.add(new MapLocation(x1, y1));

            if (x1 == x2 && y1 == y2) {
                break;
            }

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
        return coordinates;
    }

    public static boolean isStillAValidIslandTarget(MapLocation location){
        for(int i = 0; i<Communication.islandInfos.length;i++){
            MyIslandInfo island = Communication.islandInfos[i];
            if(island == null) continue;
            if(island.location.equals(location)) return true;
        }

        return false;
    }

    public static boolean isInRangeOfEnemy(MapLocation location){

        for(int i = 0; i<Robot.nearbyRobots.length;i++){
            RobotInfo nearbyRobot = Robot.nearbyRobots[i];
            Double distance = Utils.getSquaredEuclideanDistance(location, nearbyRobot.location);

            if(nearbyRobot.team == Robot.myTeam){
                continue;
            };

            switch(nearbyRobot.type){
                case HEADQUARTERS:
                    if(distance < 11) {
                        return true;
                    }
                case CARRIER:
                    if(distance < 10) return true;
                case LAUNCHER:
                    if(distance < 17) return true;
                case DESTABILIZER:
                    if(distance < 14) return true;
                default:
                    continue;
            }
        }

        for(int i = 0; i<Communication.enemyRobots.length;i++){
            MyRobotInfo nearbyRobot = Communication.enemyRobots[i];
            if(nearbyRobot == null) continue;
            Double distance = Utils.getSquaredEuclideanDistance(location, nearbyRobot.location);

            switch(nearbyRobot.robotType){
                case HEADQUARTERS:
                    if(distance < 11) {
                        return true;
                    }
                case CARRIER:
                    if(distance < 10) return true;
                case LAUNCHER:
                    if(distance < 17) return true;
                case DESTABILIZER:
                    if(distance < 14) return true;
                default:
                    continue;
            }
        }
        return false;
    }




}
