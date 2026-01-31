package therapyMatch.ontology;

import java.io.Serializable;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class PsychiatristProfile implements Serializable {
    public List<String> specialties;
    public int maxClients;
    public int currentClients;
    public boolean onlineCapable;
    public HashMap<String, List<String>> availability; // day ->list of time slots
    private int totalSlotsBooked;

    public PsychiatristProfile(List<String> specialties, int maxClients, boolean onlineCapable) {
        this.specialties = specialties;
        this.maxClients = maxClients;
        this.currentClients = 0;
        this.onlineCapable = onlineCapable;
        this.availability = new HashMap<>();
        this.totalSlotsBooked = 0;
        initializeAvailability();
    }

    private void initializeAvailability() {
        //initialize with availability for the week
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        for (String day : days) {
            List<String> slots = new ArrayList<>();
            slots.add("09:00");
            slots.add("11:00");
            slots.add("14:00");
            slots.add("16:00");
            slots.add("18:00");
            availability.put(day, slots);
        }
    }

    public boolean hasCapacity() {
        //check for available time slots across the entire week
        return hasAvailableSlots();
    }

    private boolean hasAvailableSlots() {
        for (List<String> slots : availability.values()) {
            if (!slots.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSpecialty(String symptom) {
        return specialties.contains(symptom);
    }

    public void addClient() {
        currentClients++;
        totalSlotsBooked++;
    }


    public String[] getAndBookNextSlot() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for (String day : days) {
            List<String> slots = availability.get(day);
            if (slots != null && !slots.isEmpty()) {
                //get and remove the first available slot
                String time = slots.remove(0);
                return new String[]{day, time};
            }
        }

        //no slots available
        return null;
    }

     //peek at the next available slot without booking it

    public String[] peekNextSlot() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};

        for (String day : days) {
            List<String> slots = availability.get(day);
            if (slots != null && !slots.isEmpty()) {
                return new String[]{day, slots.get(0)};
            }
        }

        return null;
    }


     //get total number of remaining slots across all days

    public int getRemainingSlots() {
        int count = 0;
        for (List<String> slots : availability.values()) {
            count += slots.size();
        }
        return count;
    }

    public double calculateMatchScore(ClientProfile client) {
        double score = 0.0;

        //specialty matching (0-50 points)
        int matchingSymptoms = 0;
        for (String symptom : client.symptoms) {
            if (hasSpecialty(symptom)) {
                matchingSymptoms++;
            }
        }
        if (!client.symptoms.isEmpty()) {
            score += (matchingSymptoms / (double) client.symptoms.size()) * 50.0;
        }

        //capacity check based on remaining time slots (0-30 points)
        int remainingSlots = getRemainingSlots();
        if (remainingSlots > 0) {
            //more remaining slots== higher score
            double slotRatio = Math.min(remainingSlots / 10.0, 1.0); // Cap at 10 slots
            score += slotRatio * 30.0;
        }

        //preference matching (0-20 points)
        if (client.preference.equals("online") && onlineCapable) {
            score += 20.0;
        } else if (client.preference.equals("in-person") && !onlineCapable) {
            score += 20.0;
        } else if (client.preference.equals("in-person") && onlineCapable) {
            score += 10.0; //can accommodate in-person even if online capable
        }

        return score;
    }
}