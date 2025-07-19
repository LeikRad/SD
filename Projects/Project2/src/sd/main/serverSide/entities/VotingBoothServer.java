package sd.main.serverSide.entities;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import sd.main.commRegion.*;

public class VotingBoothServer {

    private int _port;
    private static final Object _voteLock = new Object();

    private static final Object _voterMapLock = new Object();
    private static final Map<Integer, ConnectionHandler> _voterMap = new HashMap<>();

    private static Map<String, Integer> _voteCounts = new HashMap<>();

    private static class ConnectionHandler {
        Socket _socket;
        int _clientID;
        ObjectInputStream _objIn;
        ObjectOutputStream _objOut;

        ConnectionHandler(Socket socket, ObjectInputStream objIn, int clientID) {
            this._socket = socket;
            this._objIn = objIn;
        }

        public void closeConnection() {
            try {
                _objIn.close();
                _socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static {
        _voteCounts.put("DRP", 0);
        _voteCounts.put("NKDP", 0);
    }

    public VotingBoothServer(int port) {
        this._port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(_port)) {
            System.out.println("Voting Booth Server started on port " + _port);
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try {
            ObjectInputStream objIn = new ObjectInputStream(clientSocket.getInputStream());
            Message msg = (Message) objIn.readObject();
            System.out.println("Voting Both received message: " + msg);

            if (msg.type == MessageType.CONNECT) {
                ConnectMessage connectMsg = (ConnectMessage) msg;
                if ("VOTER".equalsIgnoreCase(msg.args.get("client_type"))) {
                    ConnectionHandler voter = new ConnectionHandler(clientSocket, objIn,
                            connectMsg.getSenderId());
                    handleVoter(voter);
                } else {
                    System.out.println("Unknown client type: " + msg.args.get("senderType"));
                }
            } else {
                System.out.println("Unknown message type: " + msg.type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleVoter(ConnectionHandler voter) {
        Message msg = null;
        try {
            msg = (Message) voter._objIn.readObject();
            voter.closeConnection();
            if (msg.type == MessageType.VOTE_REQUEST) {
                VoteMessage voteMsg = (VoteMessage) msg;
                String candidate = voteMsg.getVote();
                synchronized (_voteLock) {
                    _voteCounts.put(candidate, _voteCounts.get(candidate) + 1);
                }
                System.out.println("Vote received for candidate: " + candidate);
            }
        } catch (EOFException e) {
            System.out.println("Voter disconnected: " + voter._clientID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 5001;
        VotingBoothServer server = new VotingBoothServer(port);
        server.startServer();
    }
}