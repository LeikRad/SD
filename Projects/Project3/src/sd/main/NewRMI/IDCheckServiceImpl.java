package sd.main.NewRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class IDCheckServiceImpl extends UnicastRemoteObject implements IDCheckService {
    
    private static final Set<Integer> _seenVoterIDs = new HashSet<>();
    private static final Map<Integer, Boolean> _clerks = new HashMap<>(); 
    private static final Map<Integer, Boolean> _voters = new HashMap<>();
    private static final LinkedList<Integer> _voterQueue = new LinkedList<>();
    
    // Map to track voter verification status
    private static final Map<Integer, Boolean> _voterStatus = new HashMap<>();
    
    public IDCheckServiceImpl() throws RemoteException {
        super();
    }
    
    @Override
    public boolean registerVoter(int voterID) throws RemoteException {
        synchronized (_voterQueue) {
            _voters.put(voterID, true);
            _voterQueue.add(voterID);
            _voterStatus.put(voterID, null);
            System.out.println("Voter " + voterID + " registered. Total voters: " + _voters.size());
            _voterQueue.notifyAll();
            return true;
        }
    }
    
    @Override
    public boolean registerClerk(int clerkID) throws RemoteException {
        synchronized (_clerks) {
            _clerks.put(clerkID, true);
            System.out.println("Clerk " + clerkID + " registered. Total clerks: " + _clerks.size());
            return true;
        }
    }
    
    @Override
    public int getNextVoter(int clerkID) throws RemoteException {
        synchronized (_voterQueue) {
            while (_voterQueue.isEmpty()) {
                try {
                    System.out.println("Clerk " + clerkID + " waiting for voters...");
                    _voterQueue.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int voterID = _voterQueue.removeFirst();
            System.out.println("Clerk " + clerkID + " processing voter " + voterID);
            return voterID;
        }
    }
    
    @Override
    public boolean checkVoterID(int clerkID, int voterID) throws RemoteException {
        synchronized (_seenVoterIDs) {
            boolean status;
            if (_seenVoterIDs.contains(voterID)) {
                status = false;
                System.out.println("Clerk " + clerkID + " rejected voter " + voterID);
            } else {
                _seenVoterIDs.add(voterID);
                status = true;
                System.out.println("Clerk " + clerkID + " accepted voter " + voterID);
            }
            
            _voterStatus.put(voterID, status);
            return status;
        }
    }
    
    @Override
    public void respondToVoter(int clerkID, int voterID, boolean status) throws RemoteException {
        System.out.println("Server " + clerkID + " sent response to voter " + voterID + ": " + status);
        
        _voterStatus.put(voterID, status);
        
        synchronized (_voters) {
            System.out.println("Voter " + voterID + " processed with status: " + status);
        }
    }
    
    @Override
    public Boolean checkVoterStatus(int voterID) throws RemoteException {
        Boolean status = _voterStatus.get(voterID);
        
        if (status != null) {
            synchronized (_voters) {
                _voters.remove(voterID);
                _voterStatus.remove(voterID);
                System.out.println("Voter " + voterID + " disconnected after checking status. Total voters: " + _voters.size());
            }
        }
        
        return status;
    }
}