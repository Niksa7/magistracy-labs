package hiringDepartment;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import org.w3c.dom.ls.LSOutput;

import java.util.HashMap;
import java.util.Map;

public class JobAgent extends Agent {
    private int requiredRating;
    private int maxAge;
    private int salary;
    private boolean assigned = false;

    @Override
    protected void setup() {
        System.out.println("Agent " + getAID().getName() + " is ready.");

        // Ожидание для сниффера
        doWait(20000);

        Object[] args = getArguments();
        if (args != null && args.length > 0) {

            requiredRating = Integer.parseInt(args[0].toString());
            maxAge = Integer.parseInt(args[1].toString());
            salary = Integer.parseInt(args[2].toString());
            System.out.println(getLocalName() + " started with requirments: rating: " + requiredRating + " maxAge: " + maxAge + " salary: " + salary);

            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("job-agent");
            sd.setName("JADE-job-trading");
            dfd.addServices(sd);
            try {
                DFService.register(this, dfd);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            addBehaviour(new OfferRequestsServer());
            addBehaviour(new AssignCandidateServer());
        }
        else {
            System.out.println("Invalid arguments. Expecting: rating, maxAge, salary");
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println(getAID().getName() + " is terminated.");
    }

    /**
     * Поведение для обработки CFP запросов от кандидатов
     */
    private class OfferRequestsServer extends CyclicBehaviour {
        @Override
        public void action() {
            System.out.println("JobHiringAgent.OfferRequestsServer.action");
            MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage msg = myAgent.receive(msgTemplate);
            if (msg != null) {
                String[] content = msg.getContent().split(",");
                if (content.length != 2) {
                    throw new IllegalArgumentException("Invalid message format");
                }
                ACLMessage reply = msg.createReply();
                int rating = Integer.parseInt(content[0].trim());
                int age = Integer.parseInt(content[1].trim());
                if (rating >= requiredRating && age <= maxAge) {
                    reply.setPerformative(ACLMessage.PROPOSE);
                    reply.setContent(String.valueOf(salary));
                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                    reply.setContent("Not enough rating or age");
                }
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }

    /**
     * Поведение для записи кандидата на работу
     */
    private class AssignCandidateServer extends CyclicBehaviour {
        @Override
        public void action() {
            System.out.println("AssignCandidateServer.OfferRequestsServer.action");
            MessageTemplate msgTemplate = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
            ACLMessage msg = myAgent.receive(msgTemplate);
            if (msg != null) {
                String[] content = msg.getContent().split(",");
                if (content.length != 2) {
                    throw new IllegalArgumentException("Invalid message format");
                }
                int rating = Integer.parseInt(content[0].trim());
                int age = Integer.parseInt(content[1].trim());
                ACLMessage reply = msg.createReply();

                if(!assigned) {
                    reply.setPerformative(ACLMessage.INFORM);
                    System.out.println(getLocalName() + " is assign agent " + msg.getSender().getLocalName() + " with salary: " + salary);
                } else {
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Not available for this time...");
                }
                assigned = true;
                myAgent.send(reply);
            } else {
                block();
            }
        }
    }
}