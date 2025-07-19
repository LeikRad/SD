package sd.main.clientSide.entities;

import java.rmi.Naming;

import sd.main.NewRMI.*;

public class RMIClerkClient {
    private static int _sharedID = 1;
    private String _host;
    private int _clerkID;
    private static boolean _running = true;

    public RMIClerkClient(String host) {
        this._host = host;
        this._clerkID = _sharedID++;
    }

    public RMIClerkClient() {
        this("localhost");
    }

    public void run() {
        try {
            IDCheckService idCheckService = 
                (IDCheckService) Naming.lookup("rmi://" + _host + "/IDCheckService");
            
            idCheckService.registerClerk(_clerkID);
            System.out.println("Clerk " + _clerkID + " registered and ready to process voters");
            
            while (_running) {
                try {
                    // Get next voter to process
                    System.out.println("Clerk " + _clerkID + " waiting for next voter...");
                    int voterID = idCheckService.getNextVoter(_clerkID);
                    System.out.println("Clerk " + _clerkID + " processing voter " + voterID);
                    
                    // Check voter ID
                    boolean status = idCheckService.checkVoterID(_clerkID, voterID);
                    System.out.println("Clerk " + _clerkID + " checked voter " + voterID + ": " + 
                                      (status ? "APPROVED" : "REJECTED"));
                    
                    // Respond to voter
                    idCheckService.respondToVoter(_clerkID, voterID, status);
                    System.out.println("Clerk " + _clerkID + " responded to voter " + voterID);
                    
                    int delay = (int) (Math.random() * 6 + 5);
                    Thread.sleep(delay);
                } catch (Exception e) {
                    System.out.println("Error processing voter: " + e.getMessage());
                    e.printStackTrace();
                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            System.out.println("Error in clerk client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 1; i++) {
            RMIClerkClient clerkClient = new RMIClerkClient();
            new Thread(() -> clerkClient.run()).start();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }
    }
}