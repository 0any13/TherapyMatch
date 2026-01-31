package therapyMatch.ontology;

import java.io.Serializable;

public class MatchRequest implements Serializable {
    public ClientProfile client;

    public MatchRequest(ClientProfile client) {
        this.client = client;
    }
}
