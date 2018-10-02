package backend;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import frontend.IClient;

public class RemoteGame extends UnicastRemoteObject implements IRemoteGame {

  private final ArrayList<IClient> clients;
  private static int client_count = 0;
  private static int vote_count = 0;
  private static int count_pass_player = 0;
  private int index_current_player = 0;

  RemoteGame() throws RemoteException {
    clients = new ArrayList<>();
  }

  private String getNextPlayerName() throws RemoteException {
    index_current_player++;
    if (index_current_player >= clients.size())
      index_current_player = 0;
    return clients.get(index_current_player).getUniqueName();
  }

  public synchronized String registerGameClient(IClient client) throws RemoteException {
    // TODO Auto-generated method stub
    String response = "success";

    this.clients.add(client);
    client_count++;

    return response;
  }

  public String broadcastWord(String json) {
    // TODO Auto-generated method stub
    String response = "success";

    try {
      int i = 0;
      String jsonVoting = "voting kuy" + json;

      // tell others about voting system
      while (i < clients.size()) {
        clients.get(i++).getVotingSystem(json);
      }
    } catch (RemoteException e) {
      e.printStackTrace();
      response = "error";
    }

    return response;
  }

  public String disconnectClient() throws RemoteException {
    // TODO Auto-generated method stub
    client_count--;
    return "Success";
  }

  @Override
  public String broadcastVote(boolean accept, String word) throws RemoteException {
    // TODO Auto-generated method stub
    int i = 0;

    if (accept) {
      vote_count++;
    }

    // tell others about voting in the board
    while (i < clients.size()) {
      clients.get(i++).getVote(accept);
    }

    // tell others to update the board
    if (vote_count >= client_count) {
      String nextPlayerName = getNextPlayerName();
      while (i < clients.size()) {
        clients.get(i++).getWord(word, nextPlayerName);
      }
      // @todo construct new Json + new word + coordinates
      String jsonCoordinates = "new json";
      while (i < clients.size()) {
        clients.get(i++).getBoard(jsonCoordinates);
      }
    }

    return "success";
  }

  @Override
  public synchronized String broadcastPass(String playerName) throws RemoteException {
    // TODO Auto-generated method stub
    String passMessage = playerName + " has pass";
    count_pass_player++;

    // tell others about pass message
    int i = 0;
    while (i < clients.size()) {
      clients.get(i++).getPass(playerName);
    }

    // tell others to update the board
    // @todo back to the board, get current old json coordinates
    String jsonCoordinates = "new json";
    while (i < clients.size()) {
      clients.get(i++).getBoard(jsonCoordinates);
    }

    return "success";
  }

  @Override
  public String broadcastGeneralMessage(String message) throws RemoteException {
    // TODO Auto-generated method stub
    int i = 0;
    while (i < clients.size()) {
      clients.get(i++).getGeneralMessage(message);
    }

    return "success";
  }
  
  @Override
  public ArrayList<String> broadcastPlayerList() throws RemoteException {
	  ArrayList<String> nameArray = new ArrayList<String>();
	  for (int i=0; i< clients.size(); i++) {
		  nameArray.add(clients.get(i).getUniqueName());
	  }
	  
	  return nameArray;
  }
}
