package sd.main.commRegion;

public class CheckIDResponseMessage extends Message{
    
    public CheckIDResponseMessage(int voterID, boolean status) {
        this.args.put("voter_id", String.valueOf(voterID));
        this.args.put("status", String.valueOf(status));
        this.type = MessageType.CHECK_ID_RESPONSE;
    }

    public int getVoterID() {
        return Integer.parseInt(args.get("voter_id"));
    }

    public boolean getStatus() {
        return Boolean.parseBoolean(args.get("status"));
    }
}
