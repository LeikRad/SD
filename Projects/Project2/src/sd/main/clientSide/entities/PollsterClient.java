package sd.main.clientSide.entities;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import sd.main.commRegion.*;

public class PollsterClient {
    private static int _sharedID = 1;
    private String _host;
    private int _port; // IDCheckServer port
    private int _pollsterID;
    private Socket _socket;
    private static boolean _running = true;

    public PollsterClient(String host, int port) {
        this._host = host;
        this._port = port;
        this._pollsterID = _sharedID++;
    }

    public PollsterClient() {
        this("localhost", 5003);
    }

    public void run() {
        try {
            _socket = new Socket(_host, _port);
        } catch (Exception e) {
            System.out.println("Error connecting to Pollster Server: " + e.getMessage());
            return;
        }

        try {
            ObjectInputStream in = new ObjectInputStream(_socket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(_socket.getOutputStream());

            ConnectMessage connectMsg = new ConnectMessage("Pollster", _pollsterID);
            out.writeObject(connectMsg);

            while (_running) {
                IDRequestMessage idRequest = new IDRequestMessage(_pollsterID);
                out.writeObject(idRequest);
                System.out.println("Pollster " + _pollsterID + " sent ID request");

                Message pollRequest = receivMessage(in);

                if (pollRequest.type == MessageType.INTERVIEW_REQUEST) {
                    this.handlePollRequest(pollRequest, out);
                } else {
                    System.out.println("Unexpected message type: " + pollRequest.type);
                }
            }

        } catch (Exception e) {
            System.out.println("Error writing: " + e.getMessage());
            return;
        }
    }

    private void handlePollRequest(Message msg, ObjectOutputStream out) {
        System.out.println("Pollster " + _pollsterID + " received poll request");
        InterviewRequestMessage pollRequest = (InterviewRequestMessage) msg;
        InterviewResponseMessage pollResponse = null;
        if (Math.random() < 0.1) {
            System.out.println("Pollster " + _pollsterID + " accepted interview request");
            pollResponse = new InterviewResponseMessage(true, pollRequest.getVoterId());
        } else {
            System.out.println("Pollster " + _pollsterID + " declined interview request");
            pollResponse = new InterviewResponseMessage(false, pollRequest.getVoterId());
        }

        try {
            out.writeObject(pollResponse);
            System.out.println("Pollster " + _pollsterID + " sent poll response");
        } catch (Exception e) {
            System.out.println("Error sending poll response: " + e.getMessage());
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

    public static void main(String[] args) {
        PollsterClient client = new PollsterClient();
        client.run();
    }
}