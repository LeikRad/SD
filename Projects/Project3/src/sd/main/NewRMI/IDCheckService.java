package sd.main.NewRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDCheckService extends Remote {
    boolean registerVoter(int voterID) throws RemoteException;
    boolean registerClerk(int clerkID) throws RemoteException;
    int getNextVoter(int clerkID) throws RemoteException;
    boolean checkVoterID(int clerkID, int voterID) throws RemoteException;
    void respondToVoter(int clerkID, int voterID, boolean status) throws RemoteException;
    Boolean checkVoterStatus(int voterID) throws RemoteException;
}