package sd.main.clientSide.entities;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import sd.main.commRegion.*;
import sd.main.VoterIDGen;

public class VoterClient {

    private String _host;
    private int _port;
    private int _voterID;
    private static int _sharedID = 1;

    private String _serverHost;
    private int _clerkPort = 5000; // ID Check Server.
    private int _pollsterPort = 5003; // Pollster Server.
    private static int _pollingStationPort = 6000; // Polling Station (ENTER/EXIT).
    private int _votingBoothPort = 5001; // Voting Booth.

    private Socket _socket;

    private String lastVote = "DRP";

    public VoterClient(String host, int port) {
        this._host = host;
        this._port = port;
        this._voterID = VoterIDGen.generateID();
    }

    public VoterClient(int port) {
        this("localhost", port);
    }

    public void run() {
        while (true) {
            try {
                try {
                    _socket = new Socket(_host, _port);
                } catch (Exception e) {
                    System.out.println("Error connecting to IDCheckServer: " + e.getMessage());
                    continue;
                }
                ObjectOutputStream outPolling = new ObjectOutputStream(_socket.getOutputStream());
                ObjectInputStream inPolling = new ObjectInputStream(_socket.getInputStream());

                ConnectMessage joinMsg = new ConnectMessage("VOTER", _voterID);
                outPolling.writeObject(joinMsg);
                Message response = (Message) inPolling.readObject();
                if (response.type == MessageType.ALLOW_ENTRANCE) {

                    boolean canVote = handleClerk(joinMsg);

                    DisconnectMessage disconnectMsg = new DisconnectMessage(_voterID);
                    outPolling.writeObject(disconnectMsg);

                    inPolling.close();
                    outPolling.close();
                    _socket.close();

                    if (canVote && (Math.random() < 0.6)) {
                        handlePollster(joinMsg, disconnectMsg);
                    }
                    _voterID = VoterIDGen.generateID();
                } else {
                    inPolling.close();
                    outPolling.close();
                    _socket.close();
                }
            } catch (Exception e) {
                System.out.println("Error connecting to Polling Station: " + e.getMessage());
                continue;
            }
        }
    }

    private boolean handleClerk(ConnectMessage joinMsg) throws Exception {
        Socket clerkSocket = new Socket(_host, _clerkPort);
        clerkSocket.setSoTimeout(0);
        ObjectOutputStream outIDCheck = new ObjectOutputStream(clerkSocket.getOutputStream());
        ObjectInputStream inIDCheck = new ObjectInputStream(clerkSocket.getInputStream());

        outIDCheck.writeObject(joinMsg);
        Message response = (Message) inIDCheck.readObject();
        boolean canVote = false;
        if (response.type == MessageType.CAN_VOTE_RESPONSE) {
            CanVoteMessage msg = (CanVoteMessage) response;
            canVote = msg.canVote();
        }
        outIDCheck.close();
        inIDCheck.close();
        clerkSocket.close();

        if (canVote) {
            handleVotingBooth(joinMsg);
        }
        return canVote;
    }

    private void handleVotingBooth(ConnectMessage joinMsg) throws Exception {
        int delay = (int) (Math.random() * 10 + 5);
        Thread.sleep(delay);
        System.out.println("Voter " + _voterID + " accepted by Clerk. Proceeding to Voting Booth.");

        Socket votingSocket = new Socket(_host, _votingBoothPort);
        ObjectOutputStream outVotingBooth = new ObjectOutputStream(votingSocket.getOutputStream());
        outVotingBooth.writeObject(joinMsg);
        outVotingBooth.flush();

        VoteMessage voteMsg = new VoteMessage(lastVote);
        delay = (int) (Math.random() * 15);
        Thread.sleep(delay);
        outVotingBooth.writeObject(voteMsg);
        System.out.println("Voter " + _voterID + " voted for " + lastVote);

        outVotingBooth.close();
        votingSocket.close();
    }

    private void handlePollster(ConnectMessage joinMsg, DisconnectMessage disconnectMsg) throws Exception {
        Socket pollsterSocket = new Socket(_host, _pollsterPort);
        ObjectOutputStream outPollster = new ObjectOutputStream(pollsterSocket.getOutputStream());
        ObjectInputStream inPollster = new ObjectInputStream(pollsterSocket.getInputStream());

        outPollster.writeObject(joinMsg);
        Message pollMsg = (Message) inPollster.readObject();

        if (pollMsg.type == MessageType.POLL_REQUEST) {
            PollRequestMessage pollRequest = (PollRequestMessage) pollMsg;
            boolean accepted = pollRequest.isAccepted();
            if (accepted) {
                System.out.println("Voter " + _voterID + " accepted by Pollster.");
                if (Math.random() < 0.2) {
                    lastVote = lastVote.equals("DRP") ? "NKDP" : "DRP";
                }
                PollResponseMessage pollResponse = new PollResponseMessage(lastVote, _voterID);
                int delay = (int) (Math.random() * 10 + 5);
                Thread.sleep(delay);
                outPollster.writeObject(pollResponse);
                System.out.println("Voter " + _voterID + " voted for " + lastVote);
            }
            outPollster.writeObject(disconnectMsg);
            System.out.println("Voter " + _voterID + " disconnected from Pollster.");
        }
        inPollster.close();
        outPollster.close();
        pollsterSocket.close();
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 3; i++) {
            VoterClient voterClient = new VoterClient(_pollingStationPort);
            new Thread(() -> voterClient.run()).start();
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        }
    }
}