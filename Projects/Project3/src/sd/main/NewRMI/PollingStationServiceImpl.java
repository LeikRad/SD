package sd.main.NewRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.Set;

public class PollingStationServiceImpl extends UnicastRemoteObject implements PollingStationService {
    
    private static final int CAPACITY = 3; 
    private static final Set<Integer> inside = new HashSet<>();
    
    public PollingStationServiceImpl() throws RemoteException {
        super();
    }
    
    @Override
    public boolean enterPollingStation(int voterID) throws RemoteException {
        synchronized (inside) {
            System.out.println("Voter " + voterID + " waiting to enter polling station...");
            while (inside.size() >= CAPACITY) {
                try {
                    inside.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            inside.add(voterID);
            System.out.println("Voter " + voterID + " allowed to enter. Currently inside: " + inside.size());
            return true;
        }
    }
    
    @Override
    public void exitPollingStation(int voterID) throws RemoteException {
        synchronized (inside) {
            inside.remove(voterID);
            System.out.println("Voter " + voterID + " exited.");
            inside.notifyAll();
        }
    }
}