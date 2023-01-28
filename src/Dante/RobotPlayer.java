package Dante;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public strictfp class RobotPlayer {

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        Robot robot;
        switch(rc.getType()) {
            case HEADQUARTERS:
                robot = new Headquarters(rc);
                break;
            case CARRIER:
                robot = new Carrier(rc);
                break;
            case BUILDER:
                robot = new Builder(rc);
                break;
            case LAUNCHER:
                robot = new Launcher(rc);
                break;
            case DESTABILIZER:
                robot = new Destabilizer(rc);
                break;
            case LABORATORY:
                robot = new Lab(rc);
                break;
            default:
                robot = new Tower(rc);
                break;
        }

        while(true){
            robot.initTurn();
            robot.play();
            robot.endTurn();
            Clock.yield();
        }
    }
}
