package sd.main.commRegion;

public class PollResponseMessage extends Message {

    public PollResponseMessage(String vote, int voterID) {
        this.args.put("vote", vote);
        this.args.put("id", String.valueOf(voterID));
        this.type = MessageType.POLL_RESPONSE;
    }

    public int getVoterId() {
        return Integer.parseInt(args.get("id"));
    }

    public String getVote() {
        return args.get("vote");
    }
}
