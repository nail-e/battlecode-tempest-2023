package Jimmy;

import java.util.ArrayList;
import java.util.Map;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Launcher extends MovableRobot {
    RobotType robotType = RobotType.LAUNCHER;
    MapLocation initialTarget;
    ArrayList<MapLocation> clouds = new ArrayList<MapLocation>();
    MapInfo currentLocationInfo;
    Boolean injured = false;
    static final int actionRadius = 16;
    static final int startingHP = 200;
    MapLocation wellTarget;

    Launcher() throws GameActionException{
        super();
    }


    @Override
    public void initialize() throws GameActionException {
        super.initialize();
    }

    void getLatestCommunications() throws GameActionException{
        Communication.getEnemies();
        Communication.getIslandInfos();
    }

    @Override
    public void act() throws GameActionException {
        super.act();
        getLatestCommunications();


        if(!injured){
            if(rc.getHealth() < (startingHP /3)){
                injured = true;
            }
        }

        /**
         * kill nearby
         */

        if (rc.isActionReady()) {
            killNearbyRobots();
        }
        currentLocationInfo = rc.senseMapInfo(Robot.location);
        if(currentLocationInfo.hasCloud()){
            clouds.add(Robot.location);
        }
    }





    private void killNearbyRobots() throws GameActionException {

        RobotInfo targetRobot = null;
        loop: for (int i = 0; i < Robot.nearbyRobots.length; i++) {
            RobotInfo nearbyRobot = nearbyRobots[i];
            if (nearbyRobot.team == myTeam)
                continue;
            switch (nearbyRobot.type) {
                case LAUNCHER:
                    if (rc.canAttack(nearbyRobot.location)) {
                        targetRobot = nearbyRobot;
                        break loop;
                    }
                case HEADQUARTERS:
                    break;
                case DESTABILIZER:
                    if (rc.canAttack(nearbyRobot.location)) {
                        targetRobot = nearbyRobot;
                    }
                    break;
                case BOOSTER:
                    if (targetRobot == null || targetRobot.type != RobotType.DESTABILIZER) {
                        if (rc.canAttack(nearbyRobot.location)) {
                            targetRobot = nearbyRobot;

                        }
                    }
                    break;
                case AMPLIFIER:
                    if (targetRobot == null
                            || (targetRobot.type != RobotType.DESTABILIZER && targetRobot.type != RobotType.BOOSTER)) {
                        if (rc.canAttack(nearbyRobot.location)) {
                            targetRobot = nearbyRobot;
                        }
                    }
                    break;
                case CARRIER:
                    if (targetRobot == null) {
                        if (rc.canAttack(nearbyRobot.location)) {
                            targetRobot = nearbyRobot;
                        }
                    }
                    break;
                default:
                    break;
            }

        }
        if (targetRobot != null) {
            rc.attack(targetRobot.location);
            initialTarget = null;
        }
    }


    @Override
    public void move() throws GameActionException {

        MapLocation closestIsland = Utils.getClosestEnemyIsland(Communication.islandInfos, Robot.location);
        if(closestIsland != null){
            initialTarget = closestIsland;
        }


        if(initialTarget == null){
            initialTarget = Utils.getClosestEnemyRobot();
        }

        if(initialTarget == null){
            initialTarget = Communication.amplifierLocation;
        }







        if(initialTarget == null){
            initialTarget = new MapLocation(Utils.mapWidth / 2, Utils.mapHeight / 2);
        }



        if(injured){
            if(currentLocationInfo.hasCloud()){
                return;
            } else {
                if(clouds.size() > 0){
                    MapLocation[] _clouds = clouds.toArray(new MapLocation[clouds.size()]);
                    MapLocation closestCloud = Utils.getClosestMapLocationFromArray(_clouds, Robot.location);
                    Pathing.moveTowards(  closestCloud);
                } else {
                    if(initialTarget != null){
                        Pathing.moveTowards( initialTarget);
                        return;
                    }
                }

            };
        } else  {
            // Direction targetDirection = null;

            // for (int i = 0; i < nearbyRobots.length; i++) {
            //     RobotInfo nearbyRobot = nearbyRobots[i];
            //     // ignore my teams robots
            //     if (nearbyRobot.team == myTeam) continue;


            //     Direction directionToRobot = Robot.location.directionTo(nearbyRobot.location);
            //     if(nearbyRobot.type != RobotType.LAUNCHER && nearbyRobot.type != RobotType.HEADQUARTERS){
            //         if (rc.canMove(directionToRobot)) {
            //             targetDirection = directionToRobot;
            //         }
            //         moved = true;
            //     }

            // }

            // if (targetDirection != null && moved == false) {
            //     rc.move(targetDirection);
            //     moved = true;
            //     return;
            // }

            if(initialTarget != null){
                Pathing.moveTowards(  initialTarget);
                return;
            }
        }




    }

}

