package B;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

public class BClass extends Agent {

    protected void setup() {
        System.out.println("Привет! агент "+getAID().getName()+" готов.");
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(" - "+myAgent.getLocalName()+
                            " received: " + msg.getContent());
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Pong");
                    send(reply);
                }
                block();
            }
        });
    }
}
