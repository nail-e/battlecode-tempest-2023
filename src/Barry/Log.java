package Barry;

import battlecode.common.*;

public class Log {
    public static RobotController rc;

    public static void println(String string) {
        System.out.println(rc.getLocation() + " " + string);
    }

    public static void println(String string, int id) {
        if (rc.getID() != id) {
            return;
        }

        println(string);
    }
}
