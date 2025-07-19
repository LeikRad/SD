package sd.main.commRegion;

public class CheckIDRequesteMessage extends Message {
    public CheckIDRequesteMessage(int senderId, int voterID) {
        this.args.put("id", String.valueOf(senderId));
        this.args.put("voter_id", String.valueOf(voterID));
        
        this.type = MessageType.CHECK_ID_REQUEST;
    }

    public int getSenderId() {
        return Integer.parseInt(args.get("id"));
    }

    public int getVoterId() {
        return Integer.parseInt(args.get("voter_id"));
    }
}
