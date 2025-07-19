package sd.main.commRegion;

public class InterviewRequestMessage extends Message {

    public InterviewRequestMessage(int voterID) {
        this.type = MessageType.INTERVIEW_REQUEST;
        this.args.put("id", String.valueOf(voterID));
    }

    public int getVoterId() {
        return Integer.parseInt(args.get("id"));
    }
}
