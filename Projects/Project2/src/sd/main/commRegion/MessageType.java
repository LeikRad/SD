package sd.main.commRegion;

public enum MessageType {
    CONNECT,
    ALLOW_ENTRANCE,
    CAN_VOTE_RESPONSE,
    ID_REQUEST,
    CHECK_ID_REQUEST,
    RESPOND_VOTER_REQUEST,
    ID_REQUEST_RESP,
    CHECK_ID_RESPONSE,
    VOTE_REQUEST,
    POLL_REQUEST,
    POLL_RESPONSE,
    INTERVIEW_REQUEST,
    INTERVIEW_RESPONSE,
    DISCONNECT;

    private String description;

    MessageType() {
        this.description = this.name().toLowerCase();
    }

    public String getDescription() {
        return description;
    }
}