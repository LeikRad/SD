package sd.main.commRegion;

public class IDRequestResp extends Message {
    public IDRequestResp(int id) {
        this.type = MessageType.ID_REQUEST_RESP;
        this.args.put("voter_id", String.valueOf(id));
    }

    public int getVoterID() {
        return Integer.parseInt(this.args.get("voter_id"));
    }
}
