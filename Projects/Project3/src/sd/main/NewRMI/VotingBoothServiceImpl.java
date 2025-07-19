package sd.main.NewRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

public class VotingBoothServiceImpl extends UnicastRemoteObject implements VotingBoothService {
    
    private static final Object _voteLock = new Object();
    private static Map<String, Integer> _voteCounts = new HashMap<>();
    
    static {
        _voteCounts.put("DRP", 0);
        _voteCounts.put("NKDP", 0);
    }
    
    public VotingBoothServiceImpl() throws RemoteException {
        super();
    }
    
    @Override
    public void castVote(int voterID, String candidate) throws RemoteException {
        synchronized (_voteLock) {
            _voteCounts.put(candidate, _voteCounts.get(candidate) + 1);
        }
        System.out.println("Vote received from voter " + voterID + " for candidate: " + candidate);
    }
    
    @Override
    public Map<String, Integer> getVoteCounts() throws RemoteException {
        synchronized (_voteLock) {
            return new HashMap<>(_voteCounts);
        }
    }
    
    // Keep this for backward compatibility
    public static Map<String, Integer> getCurrentVoteCounts() throws RemoteException {
        synchronized (_voteLock) {
            return new HashMap<>(_voteCounts);
        }
    }
}