package sd.main.commRegion;

public class ConnectMessage extends Message {
    public ConnectMessage(String senderType, int senderId) {
        this.args.put("client_type", senderType);
        this.args.put("id", String.valueOf(senderId));
        this.type = MessageType.CONNECT;
    }

    public String getSenderType() {
        return args.get("client_type");
    }
    
    public int getSenderId() {
        return Integer.parseInt(args.get("id"));
    }
}
