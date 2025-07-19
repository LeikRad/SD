package sd.main.commRegion;

public class DisconnectMessage extends Message {
    public DisconnectMessage(int senderId) {
        this.args.put("id", String.valueOf(senderId));
        this.type = MessageType.DISCONNECT;
    }

    public int getSenderId() {
        return Integer.parseInt(args.get("id"));
    }

}
