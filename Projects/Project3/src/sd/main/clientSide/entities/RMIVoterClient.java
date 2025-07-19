package sd.main.clientSide.entities;

import java.rmi.Naming;
import sd.main.VoterIDGen;
import sd.main.NewRMI.*;

public class RMIVoterClient {
    private int _voterID;
    private String _host;
    private String lastVote = "DRP";

    public RMIVoterClient(String host) {
        this._host = host;
        this._voterID = VoterIDGen.generateID();
    }

    public RMIVoterClient() {
        this("localhost");
    }

    public void run() {
        while (true) {
            try {
                PollingStationService pollingStationService = 
                    (PollingStationService) Naming.lookup("rmi://" + _host + "/PollingStationService");
                
                boolean entered = pollingStationService.enterPollingStation(_voterID);
                System.out.println("Voter " + _voterID + " entering polling station: " + entered);
                
                if (entered) {
                    boolean canVote = handleClerk();
                    
                    // Exit polling station
                    pollingStationService.exitPollingStation(_voterID);
                    System.out.println("Voter " + _voterID + " exited polling station");
                    
                    // If allowed to vote and randomly decides to participate in poll
                    if (canVote && (Math.random() < 0.6)) {
                        handlePollster();
                    }
                    
                    _voterID = VoterIDGen.generateID();
                }
                
                Thread.sleep(1000);
                
            } catch (Exception e) {
                System.out.println("Error in voter client: " + e.getMessage());
                e.printStackTrace();
                try { Thread.sleep(3000); } catch (InterruptedException ie) {}
            }
        }
    }

    private boolean handleClerk() throws Exception {
        IDCheckService idCheckService = 
            (IDCheckService) Naming.lookup("rmi://" + _host + "/IDCheckService");
        
        System.out.println("Voter " + _voterID + " registering with ID Check Service");
        
        boolean canVote = idCheckService.registerVoter(_voterID);
        
        Thread.sleep(1000);
        
        int attempts = 0;
        Boolean status = null;
        while (attempts < 30) { 
            status = idCheckService.checkVoterStatus(_voterID);

            if (status != null) {
                canVote = status;
                System.out.println("Voter " + _voterID + " can vote: " + canVote);
                break;
            } else {
                System.out.println("Waiting for clerk to process voter " + _voterID);
                Thread.sleep(1000);
                attempts++;
            }
        }

        if (status == null) {
            canVote = false;
            System.out.println("Voter " + _voterID + " timed out waiting for clerk");
        }
        
        // Vote if allowed
        if (canVote) {
            VotingBoothService votingBoothService = 
                (VotingBoothService) Naming.lookup("rmi://" + _host + "/VotingBoothService");
            
            // Cast vote
            int delay = (int) (Math.random() * 10 + 5);
            Thread.sleep(delay);
            votingBoothService.castVote(_voterID, lastVote);
            System.out.println("Voter " + _voterID + " voted for " + lastVote);
        } else {
            System.out.println("Voter " + _voterID + " was not allowed to vote");
        }
        
        return canVote;
    }

    private void handlePollster() throws Exception {
        PollsterService pollsterService = 
            (PollsterService) Naming.lookup("rmi://" + _host + "/PollsterService");
        
        System.out.println("Voter " + _voterID + " registering with Pollster Service");
        pollsterService.registerVoter(_voterID);
        
        System.out.println("Voter " + _voterID + " waiting for pollster response...");
        boolean interviewAccepted = waitForPollsterResponse(pollsterService);
    
        if (interviewAccepted) {
            // If interview accepted, possibly change vote preference 
            if (Math.random() < 0.2) {
                lastVote = lastVote.equals("DRP") ? "NKDP" : "DRP";
            }
            
            // Submit poll response
            int delay = (int) (Math.random() * 10 + 5);
            Thread.sleep(delay);
            pollsterService.submitPoll(_voterID, lastVote);
            System.out.println("Voter " + _voterID + " participated in poll for " + lastVote);
        } else {
            System.out.println("Voter " + _voterID + " was not interviewed by pollster");
        }
        
        // Disconnect from pollster
        pollsterService.disconnectVoter(_voterID);
    }

    private boolean waitForPollsterResponse(PollsterService pollsterService) throws Exception {
        int attempts = 0;
        Boolean accepted = null;
        
        while (attempts < 10) {
            accepted = pollsterService.checkInterviewStatus(_voterID);
            
            if (accepted != null) {
                System.out.println("Pollster response for voter " + _voterID + ": " + 
                                (accepted ? "ACCEPTED" : "DECLINED"));
                return accepted;
            } else {
                System.out.println("Voter " + _voterID + " waiting for pollster decision...");
                Thread.sleep(500);
                attempts++;
            }
        }
        
        System.out.println("Voter " + _voterID + " timed out waiting for pollster");
        return false;
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 6; i++) {
            RMIVoterClient voterClient = new RMIVoterClient();
            new Thread(() -> voterClient.run()).start();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }
    }
}