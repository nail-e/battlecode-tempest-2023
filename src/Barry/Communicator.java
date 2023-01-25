package Barry;

import battlecode.common.*;
import java.util.*;
import Barry.*;

// Communicators can send and receive messages
public interface Communicator {
  public boolean sendMessage(Message message, RobotController rc) throws GameActionException;
  public List<Message> receiveMessages(MessageType messageType, RobotController rc) throws GameActionException;

  public static Communicator newCommunicator() {
    return new BasicCommunicator();
  }
}
