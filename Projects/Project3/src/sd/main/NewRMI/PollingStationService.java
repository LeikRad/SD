package sd.main.NewRMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PollingStationService extends Remote {
    boolean enterPollingStation(int voterID) throws RemoteException;
    void exitPollingStation(int voterID) throws RemoteException;
}