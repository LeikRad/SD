package sd.main.serverSide.entities;

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

public class IDCheckServer {

    private int _port;

    // Global set of voter IDs seen (across clerks).
    private static final Set<Integer> _seenVoterIDs = new HashSet<>();

    // List to store clerk connections.
    private static final Object _clerkMapLock = new Object();
    private static final Object _voterMapLock = new Object();

    // private static final List<ClerkHandler> _clerkList = new LinkedList<>();
    private static final Map<Integer, ConnectionHandler> _clerkMap = new HashMap<>();
    private static final Map<Integer, ConnectionHandler> _voterMap = new HashMap<>();

    // Round-robin index to distribute requests among clerks.
    private static int _clerkRoundRobinIndex = 0;

    // FIFO ordering for voter requests.
    private static final Object _voterQueueLock = new Object();

    private static final LinkedList<Integer> _voterQueue = new LinkedList<>();

    // Inner class for clerk connections.
    private static class ConnectionHandler {
        Socket _socket;
        int _clientID;
        ObjectInputStream _objIn;
        ObjectOutputStream _objOut;

        ConnectionHandler(Socket socket, ObjectInputStream objIn, ObjectOutputStream objOut, int clientID) {
            this._socket = socket;
            this._objIn = objIn;
            this._objOut = objOut;
            this._clientID = clientID;
        }

        public void closeConnection() {
            try {
                _objIn.close();
                _objOut.close();
                _socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public IDCheckServer(int port) {
        this._port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(_port)) {
            System.out.println("ID Check Server started on port " + _port);
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
            ObjectOutputStream objOut = new ObjectOutputStream(clientSocket.getOutputStream());
            ObjectInputStream objIn = new ObjectInputStream(clientSocket.getInputStream());

            Message msg = (Message) objIn.readObject();
            System.out.println("ID Check Server received request: " + msg);

            if (msg.type == MessageType.CONNECT) {
                ConnectMessage connectMsg = (ConnectMessage) msg;
                if ("CLERK".equalsIgnoreCase(msg.args.get("client_type"))) {
                    ConnectionHandler clerk = new ConnectionHandler(clientSocket, objIn, objOut,
                            connectMsg.getSenderId());
                    synchronized (_clerkMapLock) {
                        _clerkMap.put(connectMsg.getSenderId(), clerk);
                        System.out.println("Clerk connected. Total clerks: " + _clerkMap.size());
                    }
                    handleClerk(clerk);
                } else if ("VOTER".equalsIgnoreCase(msg.args.get("client_type"))) {
                    ConnectionHandler voter = new ConnectionHandler(clientSocket, objIn, objOut,
                            connectMsg.getSenderId());
                    synchronized (_voterMapLock) {
                        _voterMap.put(connectMsg.getSenderId(), voter);
                        _voterQueue.add(connectMsg.getSenderId());
                        System.out.println("adding voter " + connectMsg.getSenderId() + " to queue");
                        System.out.println("Voter connected. Total voters: " + _voterMap.size());
                    }
                    while (true) {
                    }
                } else {
                    System.out.println("Unknown client type: " + msg.args.get("senderType"));
                }
            } else {
                System.out.println("ID Check Server received invalid request: " + msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClerk(ConnectionHandler clerk) {
        while (true) {
            Message msg = null;
            try {
                msg = (Message) clerk._objIn.readObject();

                if (msg.type == MessageType.ID_REQUEST) {
                    synchronized (_voterQueue) {
                        System.out.println("Clerk is waiting for voter requests...");
                        while (_voterQueue.isEmpty()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        int voterID = _voterQueue.removeFirst();
                        Message resp = new IDRequestResp(voterID);
                        clerk._objOut.writeObject(resp);
                        System.out.println("Clerk " + clerk._clientID + " processing voter " + voterID);
                    }
                } else {
                    System.out.println("Unknown message type from clerk: " + msg.type);
                }
                msg = (Message) clerk._objIn.readObject();

                if (msg.type == MessageType.CHECK_ID_REQUEST) {
                    CheckIDRequesteMessage checkIDReq = (CheckIDRequesteMessage) msg;
                    int voterID = checkIDReq.getVoterId();
                    Message resp = null;
                    synchronized (_seenVoterIDs) {
                        if (_seenVoterIDs.contains(voterID)) {
                            resp = new CheckIDResponseMessage(voterID, false);
                            System.out.println(
                                    "Clerk " + clerk._clientID + " rejected voter " + voterID);
                        } else {
                            _seenVoterIDs.add(voterID);
                            resp = new CheckIDResponseMessage(voterID, true);
                            System.out.println(
                                    "Clerk " + clerk._clientID + " accepted voter " + voterID);
                        }
                    }
                    clerk._objOut.writeObject(resp);
                }

                msg = (Message) clerk._objIn.readObject();

                if (msg.type == MessageType.RESPOND_VOTER_REQUEST) {
                    RespondVoterRequestMessage req = (RespondVoterRequestMessage) msg;
                    int voterID = req.getVoterId();
                    boolean status = req.getStatus();

                    ConnectionHandler voter = _voterMap.get(voterID);
                    Message resp = new CanVoteMessage(status);
                    voter._objOut.writeObject(resp);
                    System.out.println(
                            "Server " + clerk._clientID + " sent response to voter " + voterID + ": " + status);

                    synchronized (_voterMapLock) {
                        voter.closeConnection();
                        _voterMap.remove(voterID);
                        System.out.println("Voter " + voterID + " disconnected. Total voters: " + _voterMap.size());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 5000;
        IDCheckServer server = new IDCheckServer(port);
        server.startServer();
    }
}