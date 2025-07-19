package sd.main.NewRMI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PollsterServiceImpl extends UnicastRemoteObject implements PollsterService {
    
    private static final Map<Integer, Boolean> _pollsters = new HashMap<>();
    private static final Map<Integer, Boolean> _voters = new HashMap<>();
    private static final LinkedList<Integer> _voterQueue = new LinkedList<>();
    private static Map<String, Integer> _voteCounts = new HashMap<>();
    private static final Map<Integer, Boolean> _interviewStatus = new HashMap<>();
    
    static {
        _voteCounts.put("DRP", 0);
        _voteCounts.put("NKDP", 0);
    }
    
    public PollsterServiceImpl() throws RemoteException {
        super();
    }
    
    @Override
    public boolean registerVoter(int voterID) throws RemoteException {
        synchronized (_voterQueue) {
            _voters.put(voterID, true);
            _voterQueue.add(voterID);
            _interviewStatus.put(voterID, null);
            System.out.println("Voter " + voterID + " registered with pollster. Total voters: " + _voters.size());
            _voterQueue.notifyAll();
            return true;
        }
    }

    @Override
    public Boolean checkInterviewStatus(int voterID) throws RemoteException {
        return _interviewStatus.get(voterID);
    }

    @Override
    public boolean registerPollster(int pollsterID) throws RemoteException {
        synchronized (_pollsters) {
            _pollsters.put(pollsterID, true);
            System.out.println("Pollster " + pollsterID + " registered. Total pollsters: " + _pollsters.size());
            return true;
        }
    }
    
    @Override
    public int getNextVoter(int pollsterID) throws RemoteException {
        synchronized (_voterQueue) {
            while (_voterQueue.isEmpty()) {
                try {
                    System.out.println("Pollster " + pollsterID + " waiting for voter...");
                    _voterQueue.wait(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            int voterID = _voterQueue.removeFirst();
            System.out.println("Pollster " + pollsterID + " received interview request for voter " + voterID);
            return voterID;
        }
    }
    
    @Override
    public void respondToInterview(int pollsterID, int voterID, boolean accepted) throws RemoteException {
        _interviewStatus.put(voterID, accepted);
        System.out.println("Pollster " + pollsterID + (accepted ? " accepted " : " declined ") + 
                        "interview for voter " + voterID);
    }
    
    @Override
    public Map<String, Integer> getPollCounts() throws RemoteException {
        synchronized (_voteCounts) {
            return new HashMap<>(_voteCounts);
        }
    }

    @Override
    public void submitPoll(int voterID, String vote) throws RemoteException {
        Boolean accepted = _interviewStatus.get(voterID);
        
        if (accepted != null && accepted) {
            synchronized (_voteCounts) {
                _voteCounts.put(vote, _voteCounts.get(vote) + 1);
                System.out.println("Poll received from voter " + voterID + " for: " + vote + 
                                " (Total " + vote + " votes: " + _voteCounts.get(vote) + ")");
            }
        } else {
            System.out.println("Poll rejected for voter " + voterID + " - not accepted for interview");
        }
    }
    
    @Override
    public void disconnectVoter(int voterID) throws RemoteException {
        synchronized (_voters) {
            _voters.remove(voterID);
            _interviewStatus.remove(voterID);
            System.out.println("Voter " + voterID + " disconnected from pollster. Total voters: " + _voters.size());
        }
    }
}