package therapyMatch.agents;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;
import jade.core.AID;

import therapyMatch.ontology.*;

import java.io.IOException;
import java.util.Arrays;

public class ClientAgent extends Agent {

    protected void setup() {
        System.out.println(getLocalName() + " started");

        //sending match request
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                // defaults or arguments if provided
                Object[] args = getArguments();
                ClientProfile profile;

                if (args != null && args.length >= 3) {
                    String[] symptoms = ((String) args[0]).split(",");
                    int urgency = Integer.parseInt((String) args[1]);
                    String preference = (String) args[2];

                    profile = new ClientProfile(
                            Arrays.asList(symptoms),
                            urgency,
                            preference
                    );
                } else {
                    //default profile
                    profile = new ClientProfile(
                            Arrays.asList("anxiety", "insomnia"),
                            3,
                            "online"
                    );
                }

                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(new AID("coordinator", AID.ISLOCALNAME));

                try {
                    msg.setContentObject(new MatchRequest(profile));
                    send(msg);
                    System.out.println(getLocalName() + " sent match request for symptoms: " +
                            profile.symptoms + " with urgency: " + profile.urgency);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //wait for response from coordinator
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {

                    if (msg.getPerformative() == ACLMessage.INFORM) {
                        //successful match
                        try {
                            AppointmentSchedule appointment = (AppointmentSchedule) msg.getContentObject();
                            System.out.println("\n" + getLocalName() + " MATCHED!");
                            System.out.println("  Psychiatrist: " + appointment.psychiatristName);
                            System.out.println("  Day: " + appointment.day);
                            System.out.println("  Time: " + appointment.timeSlot);
                            System.out.println("  Type: " + appointment.sessionType);
                            System.out.println();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    else if (msg.getPerformative() == ACLMessage.FAILURE) {
                        // no match
                        System.out.println("\n" + getLocalName() + " could not be matched: " +
                                msg.getContent());
                        System.out.println();
                    }

                } else {
                    block();
                }
            }
        });
    }
}
