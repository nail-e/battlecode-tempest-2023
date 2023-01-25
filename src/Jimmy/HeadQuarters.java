package Jimmy;

import battlecode.common.Anchor;
import battlecode.common.GameActionException;
import battlecode.common.ResourceType;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class HeadQuarters extends Robot {
    RobotType robotType = RobotType.HEADQUARTERS;
    int turnsSinceAnchor = 0;
    static final int LAUNCHER_COST = 60;
    static final int CARRIER_COST = 50;
    static final int ANCHOR_COST = 100;
    int AMPLIFIER_COST = 40;
    int turnsSinceAmplifier = 50;
    int anchorGate = 500;
    int amplifierGate = 50;
    Boolean buildsAmplifier = null;

    HeadQuarters() throws GameActionException{
        super();
    }


    @Override
    public void initialize() throws GameActionException {
        super.initialize();
        Communication.canWriteSharedArray = true;
        Communication.addHeadquarter();

    }

    @Override
    public void act() throws GameActionException {

        // Communication.getIslandInfos();
        Communication.getWells();
        // Communication.senseEnemies();
        // Communication.clearObsoleteEnemies();
        int mana = rc.getResourceAmount(ResourceType.MANA);
        int adamantium = rc.getResourceAmount(ResourceType.ADAMANTIUM);

        RobotInfo[] nearbyRobots = rc.senseNearbyRobots();
        int myLauncherCount = 0;
        int enemyLauncherCount = 0;
        for(int i = 0; i <nearbyRobots.length;i++){
            RobotInfo robotInfo = nearbyRobots[i];
            if(robotInfo.type == RobotType.LAUNCHER){
                if(robotInfo.team == Robot.myTeam) myLauncherCount++;
                if(robotInfo.team == Robot.enemyTeam) enemyLauncherCount++;
            }
        }

        if(enemyLauncherCount > myLauncherCount){
            if(mana >= LAUNCHER_COST){
                Utils.buildRobotAtLocation(Robot.location, RobotType.LAUNCHER);
            }
        }

        int launcherCost = LAUNCHER_COST;
        int carrierCost = CARRIER_COST;
        int amplifierCost = AMPLIFIER_COST;
        int numAnchors = rc.getNumAnchors(Anchor.STANDARD);

        Communication.getAmplifierLocation();

        boolean shouldBuildAnchor = turnsSinceAnchor > anchorGate && numAnchors < 1 && (Utils.getClosestOpenIsland(Communication.islandInfos, Robot.location) != null);
        boolean shouldBuildAmplifier = turnsSinceAmplifier > amplifierGate && Communication.amplifierLocation == null;

        if(shouldBuildAnchor){
            launcherCost += ANCHOR_COST;
            carrierCost += ANCHOR_COST;
            amplifierCost += ANCHOR_COST;
        }
        if(shouldBuildAmplifier){
            launcherCost += amplifierCost;
            carrierCost += amplifierCost;
        }
        // if(shouldBuildAnchor && mana >= ANCHOR_COST && adamantium >= ANCHOR_COST){
        //     rc.buildAnchor(Anchor.STANDARD);
        //     turnsSinceAnchor = 0;
        //     if(anchorGate > 100){
        //         anchorGate -= 100;
        //     }
        //     if(anchorGate < 100) anchorGate = 100;
        // } else

        if(shouldBuildAmplifier && mana>=AMPLIFIER_COST && adamantium>=AMPLIFIER_COST){
            Utils.buildRobotAtLocation(Robot.location, RobotType.AMPLIFIER);
            AMPLIFIER_COST = AMPLIFIER_COST * 2;
            turnsSinceAmplifier = 0;
            amplifierGate += 200;
        }

        // else if(mana >= launcherCost && adamantium >= carrierCost){
        //     Utils.buildRobotAtLocation(Robot.location, RobotType.LAUNCHER);
        // }else if(mana >= launcherCost){
        //     Utils.buildRobotAtLocation(Robot.location, RobotType.LAUNCHER);
        // }

        else if(adamantium >= carrierCost){
            Utils.buildRobotAtLocation(Robot.location, RobotType.CARRIER);
        }
        turnsSinceAnchor++;
        turnsSinceAmplifier++;
    }

}
