package sd.main.commRegion;

public class VoteMessage extends Message {
    public VoteMessage(String candidate) {
        this.args.put("vote", candidate);
        this.type = MessageType.VOTE_REQUEST;
    }

    public String getVote() {
        return args.get("vote");
    }
}
