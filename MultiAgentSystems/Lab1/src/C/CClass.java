package C;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

public class CClass extends Agent {

    protected void setup() {
        System.out.println("Привет! агент "+getAID().getName()+" готов.");
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    System.out.println(" - " + myAgent.getLocalName()+ " received: "+ msg.getContent() + " from " + msg.getSender().getLocalName());
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent("Nice to meet you "+ msg.getSender().getLocalName() +"!");
                    send(reply);
                }
                block();
            }
        });

        AMSAgentDescription [] agents = null;
        try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults(Long.valueOf(-1));
            agents = AMSService.search(this, new AMSAgentDescription(), c);
        }
        catch (Exception e) {
            System.out.println("Problem searching AMS: " + e);
            e.printStackTrace();
        }
    }
}