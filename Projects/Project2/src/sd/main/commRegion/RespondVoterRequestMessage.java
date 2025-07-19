package sd.main.commRegion;

public class RespondVoterRequestMessage extends Message {
    public RespondVoterRequestMessage(int senderId, int voterID, boolean status) {
        this.args.put("id", String.valueOf(senderId));
        this.args.put("voter_id", String.valueOf(voterID));
        this.args.put("status", String.valueOf(status));
        this.type = MessageType.RESPOND_VOTER_REQUEST;
    }

    public int getSenderId() {
        return Integer.parseInt(args.get("id"));
    }

    public int getVoterId() {
        return Integer.parseInt(args.get("voter_id"));
    }

    public boolean getStatus() {
        return Boolean.parseBoolean(args.get("status"));
    }    
}
