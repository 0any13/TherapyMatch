package therapyMatch.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import therapyMatch.ontology.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class PsychiatristAgent extends Agent {
    private PsychiatristProfile profile;

    protected void setup() {
        //get arguments passed
        Object[] args = getArguments();

        if (args != null && args.length > 0) {
            List<String> specialties = Arrays.asList(((String) args[0]).split(","));
            int maxClients = Integer.parseInt((String) args[1]);
            boolean onlineCapable = Boolean.parseBoolean((String) args[2]);

            profile = new PsychiatristProfile(specialties, maxClients, onlineCapable);
        } else {
            //default profile
            profile = new PsychiatristProfile(
                    Arrays.asList("anxiety", "depression"),
                    5,
                    true
            );
        }

        System.out.println(getLocalName() + " ready with specialties: " + profile.specialties +
                ", capacity: " + profile.maxClients + ", online: " + profile.onlineCapable);

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {

                    //handle coordinator confirm for psychiatrist for the client
                    if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
                        try {
                            //book  proposed slot
                            MatchRequest req = (MatchRequest) msg.getContentObject();
                            String[] slot = profile.getAndBookNextSlot();

                            if (slot != null) {
                                profile.addClient();
                                System.out.println(getLocalName() + " confirmed match. Current clients: " +
                                        profile.currentClients + "/" + profile.maxClients +
                                        " | Booked: " + slot[0] + " at " + slot[1] +
                                        " | Remaining slots: " + profile.getRemainingSlots());
                            } else {
                                System.out.println(getLocalName() + " ERROR: No slots available despite acceptance!");
                            }
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    // handle for coordinator chose another psychiatrist
                    else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                        System.out.println(getLocalName() + " proposal was rejected by coordinator");
                    }
                    //handle new match request from coordinator
                    else if (msg.getPerformative() == ACLMessage.REQUEST) {
                        try {
                            MatchRequest req = (MatchRequest) msg.getContentObject();
                            System.out.println(getLocalName() + " received request for client with symptoms: " +
                                    req.client.symptoms + ", urgency: " + req.client.urgency);

                            //calculate match score using profile
                            double matchScore = profile.calculateMatchScore(req.client);

                            //check if we have available slots
                            String[] nextSlot = profile.peekNextSlot();
                            boolean canAccept = profile.hasCapacity() &&
                                    matchScore > 20.0 &&
                                    nextSlot != null; //must have available time slots

                            //get available slot info
                            String availableDay = "";
                            String availableTime = "";
                            if (nextSlot != null) {
                                availableDay = nextSlot[0];
                                availableTime = nextSlot[1];
                            }

                            //create response
                            MatchResponse response = new MatchResponse(
                                    getLocalName(),
                                    matchScore,
                                    canAccept,
                                    availableDay,
                                    availableTime
                            );

                            ACLMessage reply = msg.createReply();
                            if (canAccept) {
                                reply.setPerformative(ACLMessage.PROPOSE);
                                System.out.println(getLocalName() + " proposing match with score: " +
                                        String.format("%.2f", matchScore) +
                                        " | Next slot: " + availableDay + " at " + availableTime +
                                        " | Remaining: " + profile.getRemainingSlots());
                            } else {
                                reply.setPerformative(ACLMessage.REFUSE);
                                String reason = !profile.hasCapacity() ? "no time slots" :
                                        (nextSlot == null ? "no time slots" : "low score");
                                System.out.println(getLocalName() + " refusing (score: " +
                                        String.format("%.2f", matchScore) +
                                        ", reason: " + reason +
                                        ", remaining slots: " + profile.getRemainingSlots() + ")");
                            }

                            reply.setContentObject(response);
                            send(reply);

                        } catch (UnreadableException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    block();
                }
            }
        });
    }
}