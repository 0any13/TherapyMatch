package therapyMatch.ontology;

import java.io.Serializable;

public class AppointmentSchedule implements Serializable {
    public String psychiatristName;
    public String clientName;
    public String day;
    public String timeSlot;
    public String sessionType; // online or in-person

    public AppointmentSchedule(String psychiatristName, String clientName,
                               String day, String timeSlot, String sessionType) {
        this.psychiatristName = psychiatristName;
        this.clientName = clientName;
        this.day = day;
        this.timeSlot = timeSlot;
        this.sessionType = sessionType;
    }

    @Override
    public String toString() {
        return "Appointment: " + clientName + " with " + psychiatristName +
                " on " + day + " at " + timeSlot + " (" + sessionType + ")";
    }
}
