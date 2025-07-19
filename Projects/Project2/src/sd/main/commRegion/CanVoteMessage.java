package sd.main.commRegion;

public class CanVoteMessage extends Message {
    public CanVoteMessage(boolean canVote) {
        this.type = MessageType.CAN_VOTE_RESPONSE;
        this.args.put("response", String.valueOf(canVote));
    }

    public boolean canVote() {
        return Boolean.parseBoolean(args.get("response"));
    }
}
