package sd.main.commRegion;

public class IDRequestMessage extends Message{
    public IDRequestMessage(int senderId) {
        this.args.put("id", String.valueOf(senderId));
        this.type = MessageType.ID_REQUEST;
    }

    public int getSenderId() {
        return Integer.parseInt(args.get("id"));
    }
}
