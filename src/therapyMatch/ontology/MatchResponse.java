package therapyMatch.ontology;

import java.io.Serializable;

public class MatchResponse implements Serializable {
    public String psychiatristName;
    public double matchScore;
    public boolean canAccept;
    public String availableDay;
    public String availableTime;

    public MatchResponse(String psychiatristName, double matchScore,
                         boolean canAccept, String availableDay, String availableTime) {
        this.psychiatristName = psychiatristName;
        this.matchScore = matchScore;
        this.canAccept = canAccept;
        this.availableDay = availableDay;
        this.availableTime = availableTime;
    }

    @Override
    public String toString() {
        return psychiatristName + " (Score: " + String.format("%.2f", matchScore) +
                ", Available: " + (canAccept ? availableDay + " " + availableTime : "No") + ")";
    }
}