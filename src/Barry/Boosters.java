package Barry;

import battlecode.common.*;
import Barry.*;

// Boosters Code
public class Booster {
  public static void run(RobotController rc) throws GameActionException {

    // Get current location
    MapLocation myLocation = rc.getLocation();

    // Get robot type
    RobotType myType = rc.getType();

    // Get enemy location
    RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
    MapLocation enemyLocation = null;
    if (enemies.length > 0) {
      enemyLocation = enemies[0].getLocation();
    }

    // Get friendly buildings
    RobotInfo[] friends = rc.senseNearbyRobots(-1, rc.getTeam());
    MapLocation friendLocation = null;
    if (friends.length > 0) {
      friendLocation = friends[0].getLocation();
    }

    // Get nearest friendly buildings
    RobotInfo[] nearestFriends = rc.senseNearbyRobots(-1, rc.getTeam());
    MapLocation nearestFriendLocation = null;
    if (nearestFriends.length > 0) {
      nearestFriendLocation = nearestFriends[0].getLocation();
    }

    // Get nearest enemy buildings
    RobotInfo[] nearestEnemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
    MapLocation nearestEnemyLocation = null;
    if (nearestEnemies.length > 0) {
      nearestEnemyLocation = nearestEnemies[0].getLocation();
    }

    // Move towards nearest friendly building
    Direction towardFriend = myLocation.directionTo(nearestFriendLocation);
    if (rc.canMove(towardFriend)) {
      rc.move(towardFriend);
    }

    // Boost
    if (rc.canBoost()) {
      rc.boost();
    }
  }
}
