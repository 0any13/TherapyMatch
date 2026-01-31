package therapyMatch.main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.ContainerController;

public class Main {

    public static void main(String[] args) {
        try {
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            p.setParameter(Profile.GUI, "true");

            ContainerController cc = rt.createMainContainer(p);

            //create coordinator
            cc.createNewAgent("coordinator", "therapyMatch.agents.CoordinatorAgent", null).start();

            //create psychiatrists with different profiles

            Object[] psy1Args = new Object[] {"anxiety,stress", "5", "true"};
            cc.createNewAgent("psy1", "therapyMatch.agents.PsychiatristAgent", psy1Args).start();

            Object[] psy2Args = new Object[] {"depression,trauma", "3", "false"};
            cc.createNewAgent("psy2", "therapyMatch.agents.PsychiatristAgent", psy2Args).start();

            Object[] psy3Args = new Object[] {"anxiety,depression,insomnia", "4", "true"};
            cc.createNewAgent("psy3", "therapyMatch.agents.PsychiatristAgent", psy3Args).start();

            //wait for agents to initialize
            Thread.sleep(2000);

            //create clients with different needs

            Object[] client1Args = new Object[] {"anxiety,insomnia", "3", "online"};
            cc.createNewAgent("client1", "therapyMatch.agents.ClientAgent", client1Args).start();

            Thread.sleep(1000);

            Object[] client2Args = new Object[] {"depression", "5", "in-person"};
            cc.createNewAgent("client2", "therapyMatch.agents.ClientAgent", client2Args).start();

            Thread.sleep(1000);

            Object[] client3Args = new Object[] {"stress", "2", "online"};
            cc.createNewAgent("client3", "therapyMatch.agents.ClientAgent", client3Args).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
