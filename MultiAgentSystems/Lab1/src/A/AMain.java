package A;

import jade.core.Agent;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.*;
import jade.core.behaviours.*;
import jade.lang.acl.*;

public class AMain extends Agent {

    public void setup() {
        System.out.println("Привет! агент "+getAID().getName()+" готов.");
        // Поведение агента исполняемое в цикле
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // Получение локального имени агента и его сообщения
                    System.out.println(" - " + myAgent.getLocalName()+ " received: "+ msg.getContent() + " from " + msg.getSender().getLocalName());
                    // Блок поведения, пока в очереди сообщ. не появиться хотя бы одно сообщ.
                    block();
                }
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

        for (int i=0; i < agents.length -1; i++) {
            AID agentID = agents[i].getName();
            if (!agentID.equals(this.getAID())) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(agentID); // id агента, которому отправлено сообщение
                msg.setLanguage("English");
                msg.setContent("Hello from " + getLocalName()+ " to " + agentID.getLocalName());
                send(msg);
            }
        }
    }
}