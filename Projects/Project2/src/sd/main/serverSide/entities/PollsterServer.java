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

public class PollsterServer {

    private int _port;

    private static final Object _pollsterMapLock = new Object();
    private static final Object _voterMapLock = new Object();

    private static final Map<Integer, ConnectionHandler> _pollsterMap = new HashMap<>();
    private static final Map<Integer, ConnectionHandler> _voterMap = new HashMap<>();

    private static final Object _voterQueueLock = new Object();

    private static final LinkedList<Integer> _voterQueue = new LinkedList<>();

    // Map to track interview responses: key = voter id, value = answer
    private static Map<String, Integer> _voteCounts = new HashMap<>();

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

    static {
        _voteCounts.put("DRP", 0);
        _voteCounts.put("NKDP", 0);
    }

    public PollsterServer(int port) {
        this._port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(_port)) {
            System.out.println("[PollsterServer] Started on port " + _port);
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
            System.out.println("[PollsterServer] Received: " + msg);

            if (msg.type == MessageType.CONNECT) {
                ConnectMessage connectMsg = (ConnectMessage) msg;
                if ("POLLSTER".equalsIgnoreCase(msg.args.get("client_type"))) {
                    ConnectionHandler pollster = new ConnectionHandler(clientSocket, objIn, objOut,
                            connectMsg.getSenderId());
                    synchronized (_pollsterMapLock) {
                        _pollsterMap.put(connectMsg.getSenderId(), pollster);
                        System.out.println("Pollster connected. Total pollsters: " + _pollsterMap.size());
                    }
                    handlePollster(pollster);
                } else if ("VOTER".equalsIgnoreCase(msg.args.get("client_type"))) {
                    ConnectionHandler voter = new ConnectionHandler(clientSocket, objIn, objOut,
                            connectMsg.getSenderId());
                    synchronized (_voterMapLock) {
                        _voterMap.put(connectMsg.getSenderId(), voter);
                        _voterQueue.add(connectMsg.getSenderId());
                        System.out.println("Voter connected. Total voters: " + _voterMap.size());
                    }
                    handleVoter(voter);
                } else {
                    System.out.println("Unknown client type: " + msg.args.get("senderType"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlePollster(ConnectionHandler pollster) {
        while (true) {
            Message msg = null;
            try {
                msg = (Message) pollster._objIn.readObject();
                int voterID = -1;
                if (msg.type == MessageType.ID_REQUEST) {
                    synchronized (_pollsterMapLock) {
                        System.out.println("Pollster waiting for voter...");
                        while (_voterQueue.isEmpty()) {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        voterID = _voterQueue.removeFirst();
                    }
                    Message req = new InterviewRequestMessage(voterID);
                    pollster._objOut.writeObject(req);
                    System.out.println("Pollster received interview request");
                }

                msg = (Message) pollster._objIn.readObject();

                if (msg.type == MessageType.INTERVIEW_RESPONSE) {
                    InterviewResponseMessage interviewMsg = (InterviewResponseMessage) msg;
                    boolean accepted = interviewMsg.isAccepted();
                    System.out.println("Server received interview response from pollster. Accepted?: " + accepted);
                    voterID = interviewMsg.getVoterId();
                    Message resp = new PollRequestMessage(accepted, interviewMsg.getVoterId());
                    ConnectionHandler voter = _voterMap.get(voterID);
                    voter._objOut.writeObject(resp);
                    System.out
                            .println("Sent interview request to voter: " + interviewMsg.getVoterId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void handleVoter(ConnectionHandler voter) {
        Message msg = null;
        try {
            while (true) {
                msg = (Message) voter._objIn.readObject();
                if (msg.type == MessageType.POLL_RESPONSE) {
                    PollResponseMessage pollResponseMsg = (PollResponseMessage) msg;
                    String response = pollResponseMsg.getVote();
                    synchronized (_voteCounts) {
                        _voteCounts.put(response, _voteCounts.get(response) + 1);
                    }

                } else if (msg.type == MessageType.DISCONNECT) {
                    synchronized (_voterMapLock) {
                        _voterMap.remove(voter._clientID);
                        System.out.println("Voter disconnected. Total voters: " + _voterMap.size());
                    }
                    voter.closeConnection();
                    break;
                } else {
                    System.out.println("Unknown message type from voter: " + msg.type);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 5003;
        PollsterServer server = new PollsterServer(port);
        server.startServer();
    }
}