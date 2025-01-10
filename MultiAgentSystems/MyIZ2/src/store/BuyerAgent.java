package store;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.util.Random;

public class BuyerAgent extends Agent {
    private AID[] cashierAgents;
    private Random rand = new Random();

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " запускается.");

        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                // Найти кассиров
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("cashier-agent");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    cashierAgents = new AID[result.length];
                    for (int i = 0; i < result.length; i++) {
                        cashierAgents[i] = result[i].getName();
                    }
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }

                if (cashierAgents != null && cashierAgents.length > 0) {
                    addBehaviour(new OneShotBehaviour() {
                        @Override
                        public void action() {
                            // Сгенерировать корзину
                            String cart = generateCart();
                            System.out.println("Корзина: " + cart);

                            // Выбрать случайного кассира
                            AID selectedCashier = cashierAgents[rand.nextInt(cashierAgents.length)];

                            // Отправить корзину выбранному кассиру
                            ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                            msg.addReceiver(selectedCashier);
                            msg.setContent(cart);
                            send(msg);
                            System.out.println(getLocalName() + " отправил корзину кассиру: " + selectedCashier.getLocalName());
                        }
                    });

                    // Обработка сообщений оплаты
                    addBehaviour(new CyclicBehaviour() {
                        @Override
                        public void action() {
                            ACLMessage msg = receive();
                            if (msg != null) {
                                switch (msg.getPerformative()) {
                                    case ACLMessage.REQUEST:
                                        // Получить запрос на оплату
                                        String paymentDetails = msg.getContent();
                                        System.out.println(getLocalName() + " получил запрос на оплату: " + paymentDetails);

                                        // Подтвердить оплату
                                        ACLMessage paymentConfirmation = new ACLMessage(ACLMessage.CONFIRM);
                                        paymentConfirmation.addReceiver(msg.getSender());
                                        paymentConfirmation.setContent("Оплата подтверждена");
                                        send(paymentConfirmation);
                                        System.out.println(getLocalName() + " подтвердил оплату.");

                                        // Завершить работу агента
                                        System.out.println(getLocalName() + " завершает работу.");
                                        doDelete();
                                        break;

                                    default:
                                        System.out.println("Неизвестный тип сообщения получен покупателем.");
                                }
                            } else {
                                block();
                            }
                        }
                    });
                } else {
                    System.out.println("Кассиры не найдены. Покупатель будет ожидать.");
                }
            }
        });
    }

    private String generateCart() {
        StringBuilder cart = new StringBuilder();
        int n = rand.nextInt(5) + 1;

        for (int i = 0; i < n; i++) {
            String product = "product_" + (rand.nextInt(15) + 1);
            int quantity = rand.nextInt(5) + 1;
            cart.append(product).append(":").append(quantity);
            if (i < n - 1) {
                cart.append(";");
            }
        }
        return cart.toString();
    }
}
