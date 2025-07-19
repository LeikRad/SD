package sd.main.commRegion;

public class InterviewResponseMessage extends Message {

    public InterviewResponseMessage(boolean accepted, int voterID) {
        this.type = MessageType.INTERVIEW_RESPONSE;
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
