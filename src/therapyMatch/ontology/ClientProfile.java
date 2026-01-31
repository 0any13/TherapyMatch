package therapyMatch.ontology;

import java.io.Serializable;
import java.util.List;

public class ClientProfile implements Serializable {
    public List<String> symptoms;
    public int urgency; // 1–5
    public String preference; // online / in-person

    public ClientProfile(List<String> symptoms, int urgency, String preference) {
        this.symptoms = symptoms;
        this.urgency = urgency;
        this.preference = preference;
    }
}
