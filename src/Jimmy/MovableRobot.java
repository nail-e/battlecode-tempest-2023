package Jimmy;

import battlecode.common.Direction;
import battlecode.common.GameActionException;

public abstract class MovableRobot extends Robot{


    MovableRobot() throws GameActionException{
        super();
        Pathing.rc = Robot.rc;
    }


    @Override
    void initialize() throws GameActionException {
        super.initialize();
    }

    @Override
    public void run() throws GameActionException{
        // temporaryObstacles = getTemporaryObstacles();
        super.run();
        // priorityMove();
        move();
        if(rc.isActionReady()){
            act();
        }
    }


    static void handleMove(Direction dir) throws GameActionException{
        try{
            rc.move(dir);
        }catch(GameActionException e){
            System.out.println("failed to move "+dir);
            throw e;
        }
        Robot.location = rc.getLocation();
    }

    abstract void move() throws GameActionException;

}
