package therapyMatch.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.core.AID;

import therapyMatch.ontology.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorAgent extends Agent {
    private List<MatchResponse> receivedProposals;
    private ACLMessage currentClientRequest;
    private int expectedResponses;
    private int receivedResponses;

    protected void setup() {
        System.out.println("Coordinator ready");
        receivedProposals = new ArrayList<>();

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {

                    //handle new match request from client
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        handleClientRequest(msg);
                    }
                    //handle proposals from psychiatrists
                    else if (msg.getPerformative() == ACLMessage.PROPOSE ||
                            msg.getPerformative() == ACLMessage.REFUSE) {
                        handlePsychiatristResponse(msg);
                    }

                } else {
                    block();
                }
            }

            private void handleClientRequest(ACLMessage msg) {
                try {
                    MatchRequest req = (MatchRequest) msg.getContentObject();
                    System.out.println("Coordinator received request from " + msg.getSender().getLocalName() +
                            " with urgency: " + req.client.urgency);

                    //store original request
                    currentClientRequest = msg;
                    receivedProposals.clear();
                    receivedResponses = 0;

                    //forward to all 8 psychiatrists
                    ACLMessage forward = new ACLMessage(ACLMessage.REQUEST);
                    forward.addReceiver(new AID("psy1", AID.ISLOCALNAME));
                    forward.addReceiver(new AID("psy2", AID.ISLOCALNAME));
                    forward.addReceiver(new AID("psy3", AID.ISLOCALNAME));
                    forward.addReceiver(new AID("psy4", AID.ISLOCALNAME));
                    forward.addReceiver(new AID("psy5", AID.ISLOCALNAME));
                    forward.addReceiver(new AID("psy6", AID.ISLOCALNAME));
                    forward.addReceiver(new AID("psy7", AID.ISLOCALNAME));
                    forward.addReceiver(new AID("psy8", AID.ISLOCALNAME));
                    forward.setContentObject(msg.getContentObject());

                    expectedResponses = 8;
                    send(forward);

                    System.out.println("Coordinator forwarded request to all psychiatrists");

                } catch (UnreadableException | IOException e) {
                    e.printStackTrace();
                }
            }

            private void handlePsychiatristResponse(ACLMessage msg) {
                try {
                    receivedResponses++;

                    if (msg.getPerformative() == ACLMessage.PROPOSE) {
                        MatchResponse response = (MatchResponse) msg.getContentObject();
                        receivedProposals.add(response);
                        System.out.println("Coordinator received proposal from " +
                                response.psychiatristName + " with score: " +
                                String.format("%.2f", response.matchScore));
                    } else if (msg.getPerformative() == ACLMessage.REFUSE) {
                        System.out.println("Coordinator received refusal from " +
                                msg.getSender().getLocalName());
                    }

                    //select best match when all responses received
                    if (receivedResponses >= expectedResponses) {
                        selectBestMatch();
                    }

                } catch (UnreadableException e) {
                    e.printStackTrace();
                }
            }

            private void selectBestMatch() {
                if (receivedProposals.isEmpty()) {
                    //no psych available
                    System.out.println("Coordinator: No psychiatrist available for this client");
                    notifyClient(null, false);
                    return;
                }

                //find highest score for best match
                MatchResponse bestMatch = receivedProposals.get(0);
                for (MatchResponse proposal : receivedProposals) {
                    if (proposal.matchScore > bestMatch.matchScore) {
                        bestMatch = proposal;
                    }
                }

                System.out.println("Coordinator selected best match: " + bestMatch.psychiatristName +
                        " with score: " + String.format("%.2f", bestMatch.matchScore));

                //confirm with selected psych
                ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                accept.addReceiver(new AID(bestMatch.psychiatristName, AID.ISLOCALNAME));
                try {
                    MatchRequest originalReq = (MatchRequest) currentClientRequest.getContentObject();
                    accept.setContentObject(originalReq);
                    send(accept);

                    //notify client of successful match
                    notifyClient(bestMatch, true);

                } catch (UnreadableException | IOException e) {
                    e.printStackTrace();
                }

                //reject other proposals
                for (MatchResponse proposal : receivedProposals) {
                    if (!proposal.psychiatristName.equals(bestMatch.psychiatristName)) {
                        ACLMessage reject = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                        reject.addReceiver(new AID(proposal.psychiatristName, AID.ISLOCALNAME));
                        send(reject);
                    }
                }
            }

            private void notifyClient(MatchResponse match, boolean success) {
                ACLMessage reply = currentClientRequest.createReply();

                if (success && match != null) {
                    reply.setPerformative(ACLMessage.INFORM);

                    //create appointment
                    try {
                        MatchRequest req = (MatchRequest) currentClientRequest.getContentObject();
                        AppointmentSchedule appointment = new AppointmentSchedule(
                                match.psychiatristName,
                                currentClientRequest.getSender().getLocalName(),
                                match.availableDay,
                                match.availableTime,
                                req.client.preference
                        );
                        reply.setContentObject(appointment);
                        System.out.println("Coordinator created: " + appointment);
                    } catch (UnreadableException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("No available psychiatrist found for your requirements");
                }

                send(reply);
            }
        });
    }
}