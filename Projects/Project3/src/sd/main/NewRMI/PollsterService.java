package sd.main.NewRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface PollsterService extends Remote {
    boolean registerVoter(int voterID) throws RemoteException;
    boolean registerPollster(int pollsterID) throws RemoteException;
    int getNextVoter(int pollsterID) throws RemoteException;
    void respondToInterview(int pollsterID, int voterID, boolean accepted) throws RemoteException;
    void submitPoll(int voterID, String vote) throws RemoteException;
    void disconnectVoter(int voterID) throws RemoteException;
    Boolean checkInterviewStatus(int voterID) throws RemoteException;
    Map<String, Integer> getPollCounts() throws RemoteException;
}