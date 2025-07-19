package sd.main.serverSide.entities;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import sd.main.commRegion.*;

public class PollingStationServer {

    private int _port;
    private static final int CAPACITY = 3; // max voters allowed inside
    private static final Object lock = new Object();
    private static Set<Integer> inside = new HashSet<>();

    // List of all the voters inside the polling station.
    private static final Object _voterMapLock = new Object();
    private static final Map<Integer, ConnectionHandler> _voterMap = new HashMap<>();

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

    public PollingStationServer(int port) {
        this._port = port;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(_port)) {
            System.out.println("Polling Station Server started on port " + _port);
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
            System.out.println("Polling Station Server received request: " + msg);
            if (msg.type.equals(MessageType.CONNECT)) {
                ConnectMessage connectMsg = (ConnectMessage) msg;
                if ("VOTER".equalsIgnoreCase(msg.args.get("client_type"))) {
                    ConnectionHandler voter = new ConnectionHandler(clientSocket, objIn, objOut,
                            connectMsg.getSenderId());
                    handleVoter(voter);
                } else {
                    System.out.println("Invalid client type: " + msg.args.get("client_type"));
                    objOut.writeObject("ERROR: Invalid client type");
                }
            } else {
                System.out.println("Invalid message type: " + msg.type);
                objOut.writeObject("ERROR: Invalid message type");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleVoter(ConnectionHandler voter) {
        int voterId = voter._clientID;

        synchronized (lock) {
            System.out.println("Voter waiting to enter polling station...");
            while (inside.size() >= CAPACITY) {
            }
            synchronized (inside) {
                inside.add(voterId);
            }
        }
        try {
            Message resp = new AllowEntranceMessage();
            voter._objOut.writeObject(resp);
            System.out.println("Voter " + voterId + " allowed to enter.");

            // This is to keep the connection alive while the voter is inside the staion
            while (true) {
                try {
                    Message msg = (Message) voter._objIn.readObject();
                    if (msg.type.equals(MessageType.DISCONNECT)) {
                        synchronized (inside) {
                            inside.remove(voterId);
                        }
                        System.out.println("Voter " + voterId + " exited.");
                        voter.closeConnection();
                        break;
                    } else {
                        System.out.println("Invalid message type from voter: " + msg.type);
                    }
                } catch (java.io.EOFException eof) {
                    synchronized (inside) {
                        inside.remove(voterId);
                    }
                    System.out.println("Voter " + voterId + " disconnected unexpectedly.");
                    voter.closeConnection();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 6000;
        PollingStationServer server = new PollingStationServer(port);
        server.startServer();
    }
}
