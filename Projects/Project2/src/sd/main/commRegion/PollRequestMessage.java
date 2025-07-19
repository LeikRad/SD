package sd.main.commRegion;

public class PollRequestMessage extends Message {

    public PollRequestMessage(boolean accepted, int voterID) {
        this.type = MessageType.POLL_REQUEST;
        this.args.put("id", String.valueOf(voterID));
        this.args.put("accept", String.valueOf(accepted));
    }

    public int getVoterId() {
        return Integer.parseInt(args.get("id"));
    }

    public boolean isAccepted() {
        return Boolean.parseBoolean(args.get("accept"));
    }
}
