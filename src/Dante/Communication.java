package Dante;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.List;

import static Dante.RobotPlayer.*;

public class Communication {

    static MapLocation[] GetAllLauncherDestinations(RobotController rc) throws GameActionException {
        List<MapLocation> locations = new ArrayList<>();
        for(int i = launcherIndexStart; i <= launcherIndexEnd; i++)
            if(rc.readSharedArray(i) > 0) locations.add(GetLauncherDestination(rc, i));
        return locations.toArray(new MapLocation[0]);
    }

    //Add an enemy location to the shared array
    static void AddLauncherDestination(RobotController rc, MapLocation location) throws GameActionException{
        RemoveLauncherDestination(rc, location);
        for(int i = launcherIndexStart; i <= launcherIndexEnd; i++){
            if(rc.readSharedArray(i) == 0){
                rc.writeSharedArray(i, location.x + location.y * 60);
                break;
            }
        }
    }

    //Remove a location from the shared array
    static void RemoveLauncherDestination(RobotController rc, MapLocation location) throws GameActionException{
        for(int i = launcherIndexStart; i <= launcherIndexEnd; i++){
            if(GetLauncherDestination(rc, i).equals(location)){
                rc.writeSharedArray(i, 0);
                return;
            }
        }
    }

    //Gets a location from the shared array
    static MapLocation GetLauncherDestination(RobotController rc, int index) throws GameActionException{
        int value = rc.readSharedArray(index);
        return new MapLocation(value % 60, (value % 3600) / 60);
    }

    static void MoveBestIsland(RobotController rc) throws GameActionException{
        int bestRubble = 1000;
        Direction bestDirection = null;
        MapLocation start = rc.getLocation();
        for(Direction direction : Direction.values()){
            if(!rc.onTheMap(start.add(direction))) continue;
            int rubble = rc.senseIsland(start.add(direction));
            if(rubble < bestRubble){
                bestRubble = rubble;
                bestDirection = direction;
            }
        }
        if(bestDirection != null && rc.canMove(bestDirection)) rc.move(bestDirection);
    }

    static void MoveToHeadquarters(RobotController rc) throws GameActionException{
        for(RobotInfo robot : rc.senseNearbyRobots(20, rc.getTeam()))
            if(robot.type.equals(RobotType.HEADQUARTERS)) return;
        navigateToLocation(rc, startingLocation);
    }

    static void MoveToTarget(RobotController rc) throws GameActionException{
        MapLocation[] enemyLocations = GetAllLauncherDestinations(rc);
        MapLocation closestEnemy = null;
        int closestDistance = 10000;
        for(MapLocation enemyLocation : enemyLocations){
            if(rc.getLocation().distanceSquaredTo(enemyLocation) < closestDistance){
                closestDistance = rc.getLocation().distanceSquaredTo(enemyLocation);
                closestEnemy = enemyLocation;
            }
        }
        if(closestEnemy != null) {
            if(closestEnemy.distanceSquaredTo(rc.getLocation()) < 10) RemoveLauncherDestination(rc, closestEnemy);
            navigateToLocation(rc, closestEnemy);
        }
    }

    static void explore(RobotController rc) throws GameActionException {
        if(target == null || target.equals(rc.getLocation())) target = new MapLocation(rng.nextInt(rc.getMapWidth()), rng.nextInt(rc.getMapHeight()));
        navigateToLocation(rc, target);
    }

    static void navigateToLocation(RobotController rc, MapLocation end) throws GameActionException {
        float averageRubble = 0;
        MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
        for (MapLocation location : locations) {
            averageRubble += rc.senseIsland(location);
        }
        averageRubble /= locations.length;
        averageRubble += 1;
        MapLocation start = rc.getLocation();
        int bestEstimate = 100000;
        Direction bestDirection = Direction.CENTER;
        for (Direction direction : Direction.values()) {
            if(direction == Direction.CENTER) continue;
            if(!rc.canMove(direction)) continue;
            if(start.distanceSquaredTo(end) <= start.add(direction).distanceSquaredTo(end)) continue;
            int estimate = rc.senseIsland(start.add(direction));
            estimate += averageRubble * Math.sqrt((start.add(direction)).distanceSquaredTo(end));
            if(estimate < bestEstimate || (estimate == bestEstimate && start.add(direction).distanceSquaredTo(end) < start.add(bestDirection).distanceSquaredTo(end))){
                bestEstimate = estimate;
                bestDirection = direction;
            }
        }
        if(bestDirection != Direction.CENTER){
            if(rc.canMove(bestDirection)) rc.move(bestDirection);
            else if(rc.canMove(bestDirection.rotateLeft())) rc.move(bestDirection.rotateLeft());
            else if(rc.canMove(bestDirection.rotateRight())) rc.move(bestDirection.rotateRight());
        }
    }

    static final int STARTING_ISLAND_IDX = GameConstants.MAX_STARTING_HEADQUARTERS;
    private static final int STARTING_ENEMY_IDX = GameConstants.MAX_NUMBER_ISLANDS + GameConstants.MAX_STARTING_HEADQUARTERS;

    private static final int TOTAL_BITS = 16;
    private static final int MAPLOC_BITS = 12;
    private static final int TEAM_BITS = 1;
    private static final int HEALTH_BITS = 3;
    private static final int HEALTH_SIZE = (int) Math.ceil(Anchor.ACCELERATING.totalHealth / 8.0);


    static MapLocation readIslandLocation(RobotController rc, int islandId) {
        try {
            islandId = islandId + STARTING_ISLAND_IDX;
            int islandInt = rc.readSharedArray(islandId);
            int idx = islandInt >> (HEALTH_BITS + TEAM_BITS);
            return intToLocation(rc, idx);
        } catch (GameActionException e) {}
        return null;
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
        return new MapLocation(m % rc.getMapWidth(), m / rc.getMapWidth());
    }

}
