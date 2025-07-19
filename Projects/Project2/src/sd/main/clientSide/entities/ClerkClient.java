package sd.main.clientSide.entities;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import sd.main.commRegion.*;

public class ClerkClient {

    private static int _sharedID = 1;
    private String _host;
    private int _port; // IDCheckServer port
    private int _clerkID;
    private Socket _socket;
    private static boolean _running = true;

    public ClerkClient(String host, int port) {
        this._host = host;
        this._port = port;
        this._clerkID = _sharedID++;
    }

    public ClerkClient() {
        this("localhost", 5000);
    }

    public void run() {
        try {
            _socket = new Socket(_host, _port);
        } catch (Exception e) {
            System.out.println("Error connecting to IDCheckServer: " + e.getMessage());
            return;
        }

        try {
            ObjectOutputStream out = new ObjectOutputStream(_socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(_socket.getInputStream());

            ConnectMessage connectMsg = new ConnectMessage("CLERK", _clerkID);
            out.writeObject(connectMsg);
            out.flush();

            while (_running) {
                IDRequestMessage idRequest = new IDRequestMessage(_clerkID);
                out.writeObject(idRequest);
                out.flush();

                System.out.println("Clerk " + _clerkID + " sent ID request.");

                Message response = this.receivMessage(in);
                if (response.type == MessageType.ID_REQUEST_RESP) {
                    this.handleIDResp(response, out);
                } else {
                    System.out.println("Unexpected message type: " + response.type);
                }
                response = this.receivMessage(in);

                if (response.type == MessageType.CHECK_ID_RESPONSE) {
                    this.handleCheckIDResp(response, out);
                } else {
                    System.out.println("Unexpected message type: " + response.type);
                }
            }

        } catch (Exception e) {
            System.out.println("Error writting: " + e.getMessage());
            return;
        }
    }

    private Message receivMessage(ObjectInputStream in) {
        try {
            Object obj = in.readObject();
            if (obj instanceof Message) {
                return (Message) obj;
            } else {
                System.out.println("Unexpected object: " + obj);
            }
        } catch (Exception e) {
            System.out.println("Error receiving message: " + e.getMessage());
        }
        return null;
    }

    private void handleIDResp(Message response, ObjectOutputStream out) {
        IDRequestResp idResp = (IDRequestResp) response;
        int voterID = idResp.getVoterID();
        CheckIDRequesteMessage checkIDReq = new CheckIDRequesteMessage(_clerkID, voterID);
        try {
            out.writeObject(checkIDReq);
            System.out.println("Clerk " + _clerkID + " sent check ID request for voter " + voterID);
        } catch (Exception e) {
            System.out.println("Error sending check ID request: " + e.getMessage());
        }
    }

    private void handleCheckIDResp(Message response, ObjectOutputStream out) {
        CheckIDResponseMessage checkIDResp = (CheckIDResponseMessage) response;
        int voterID = checkIDResp.getVoterID();
        boolean accepted = checkIDResp.getStatus();
        RespondVoterRequestMessage respondVoterReq = new RespondVoterRequestMessage(_clerkID, voterID, accepted);
        if (accepted) {
            respondVoterReq = new RespondVoterRequestMessage(_clerkID, voterID, true);
        } else {
            System.out.println("Clerk " + _clerkID + " rejected voter " + voterID);
        }
        try {
            out.writeObject(respondVoterReq);
            System.out.println("Clerk " + _clerkID + " sent response to voter " + voterID);
        } catch (Exception e) {
            System.out.println("Error sending response to voter: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ClerkClient clerkClient = new ClerkClient();
        clerkClient.run();
    }
}