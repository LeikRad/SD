package sd.main.NewRMI;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class RMIElectionServer {
    public static void main(String[] args) {
        try {
            // Create the RMI registry on port 1099
            LocateRegistry.createRegistry(1099);
            
            // Create and bind services
            IDCheckService idCheckService = new IDCheckServiceImpl();
            PollingStationService pollingStationService = new PollingStationServiceImpl();
            VotingBoothService votingBoothService = new VotingBoothServiceImpl();
            PollsterService pollsterService = new PollsterServiceImpl();
            
            // Bind services to the registry
            Naming.rebind("IDCheckService", idCheckService);
            Naming.rebind("PollingStationService", pollingStationService);
            Naming.rebind("VotingBoothService", votingBoothService);
            Naming.rebind("PollsterService", pollsterService);
            
            System.out.println("RMI Election Server is running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}