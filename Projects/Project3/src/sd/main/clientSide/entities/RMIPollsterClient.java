package sd.main.clientSide.entities;

import java.rmi.Naming;

import sd.main.NewRMI.*;

public class RMIPollsterClient {
    private static int _sharedID = 1;
    private String _host;
    private int _pollsterID;
    private static boolean _running = true;

    public RMIPollsterClient(String host) {
        this._host = host;
        this._pollsterID = _sharedID++;
    }

    public RMIPollsterClient() {
        this("localhost");
    }

    public void run() {
        try {
            PollsterService pollsterService = 
                (PollsterService) Naming.lookup("rmi://" + _host + "/PollsterService");
            
            pollsterService.registerPollster(_pollsterID);
            
            while (_running) {
                // Get next voter to interview
                int voterID = pollsterService.getNextVoter(_pollsterID);
                
                // Decide whether to accept the interview
                boolean accepted = Math.random() < 0.1;
                
                // Respond to the interview request
                pollsterService.respondToInterview(_pollsterID, voterID, accepted);
                System.out.println("Pollster " + _pollsterID + 
                                  (accepted ? " accepted " : " declined ") + 
                                  "interview for voter " + voterID);
                
                Thread.sleep(500);
            }
        } catch (Exception e) {
            System.out.println("Error in pollster client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 1; i++) {
            RMIPollsterClient pollsterClient = new RMIPollsterClient();
            new Thread(() -> pollsterClient.run()).start();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }
    }
}