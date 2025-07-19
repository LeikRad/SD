package sd.main.commRegion;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    public MessageType type;

    public HashMap<String, String> args = new HashMap<>();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (String key : args.keySet()) {
            sb.append(key).append(": ").append(args.get(key)).append(", ");
        }
        sb.append("}");
        return "Message{" +
                "type=" + type +
                ", args=" + sb.toString() +
                '}';
    }
}