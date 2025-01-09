package hiringDepartment;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.sniffer.Message;

public class CandidateAgent extends Agent {

    private AID[] jobAgents;
    private int rating;
    private int age;
    private boolean assigned = false;

    @Override
    protected void setup() {
        System.out.println("Agent " + getAID().getName() + " is ready.");

        // Ожидание для сниффера
        doWait(30000);

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            rating = Integer.parseInt(args[0].toString());
            age = Integer.parseInt(args[1].toString());
            System.out.println(getLocalName() + " started with arguments: rating: " + rating + " age: " + age);

            addBehaviour(new TickerBehaviour(this, 5000) {
                @Override
                protected void onTick() {
                    System.out.println("Candidate agent " + getLocalName() + " on tick...");
                    // Обновляем список агентов работы
                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("job-agent");
                    template.addServices(sd);
                    try {
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following job agents:");
                        jobAgents = new AID[result.length];
                        for (int i = 0; i < result.length; i++) {
                            jobAgents[i] = result[i].getName();
                            System.out.println(jobAgents[i].getName());
                        }
                    } catch (FIPAException fe) {
                        fe.printStackTrace();
                    }

                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
        } else {
            System.out.println("Invalid arguments. Expecting: rating, maxAge, salary");
            doDelete();
        }
    }

    @Override
    protected void takeDown() {
        System.out.println(getAID().getName() + " is terminated.");
    }

    private class RequestPerformer extends Behaviour {
        private static final String CONVERSATION_ID = "job-hiring";

        private AID bestJob; // Лучшая подходящая вакансия
        private int bestSalary = 0; // Наивысшая зарплата
        private int repliesCount = 0; // Количество полученных ответов
        private MessageTemplate msgTemplate; // Шаблон для получения ответов
        private int step = 0;

        @Override
        public void action() {
            System.out.println("RequestPerformer.action " + getLocalName() + ": step: " + step);
            switch (step) {
                case 0:
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (AID jobAgent : jobAgents) {
                        cfp.addReceiver(jobAgent);
                    }
                    cfp.setContent(rating + "," + age);
                    cfp.setConversationId(CONVERSATION_ID);
                    cfp.setReplyWith("cfp" + System.currentTimeMillis());
                    myAgent.send(cfp);

                    msgTemplate = MessageTemplate.and(
                            MessageTemplate.MatchConversationId(CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    step = 1;
                    break;
                case 1:
                    ACLMessage reply = myAgent.receive(msgTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {
                            int salary = Integer.parseInt(reply.getContent());
                            if (bestJob == null || salary > bestSalary) {
                                bestSalary = salary;
                                bestJob = reply.getSender();
                            }
                        }
                        repliesCount++;
                        if (repliesCount >= jobAgents.length) {
                            step = 2;
                        }
                    } else {
                        block();
                    }
                    break;
                case 2:
                    ACLMessage offer = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    offer.addReceiver(bestJob);
                    offer.setContent(rating + "," + age);
                    offer.setConversationId(CONVERSATION_ID);
                    offer.setReplyWith("order" + System.currentTimeMillis());
                    myAgent.send(offer);
                    msgTemplate = MessageTemplate.and(
                            MessageTemplate.MatchConversationId(CONVERSATION_ID),
                            MessageTemplate.MatchInReplyTo(offer.getReplyWith()));
                    step = 3;
                    break;
                case 3:
                    reply = myAgent.receive(msgTemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.INFORM) {
                            System.out.println(getLocalName() + " assigned to job " + bestJob.getName());
                            myAgent.doDelete();
                        }
                        step = 4;
                    }
                    else {
                        block();
                    }
                    break;
            }
        }

        @Override
        public boolean done() {
            return ((step == 2 && bestJob == null) || step == 4);
        }
    }
}
