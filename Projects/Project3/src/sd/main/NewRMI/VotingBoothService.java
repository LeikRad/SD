package sd.main.NewRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface VotingBoothService extends Remote {
    void castVote(int voterID, String candidate) throws RemoteException;
    Map<String, Integer> getVoteCounts() throws RemoteException;
}