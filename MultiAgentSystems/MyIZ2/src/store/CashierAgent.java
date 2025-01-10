package store;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

public class CashierAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println(getLocalName() + " запускается.");

        doWait(21000);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("cashier-agent");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {
            private String buyerCart; // Корзина покупателя
            private ACLMessage buyerMsg; // Сообщение от покупателя

            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    switch (msg.getPerformative()) {
                        case ACLMessage.REQUEST:
                            // Получить корзину от клиента
                            buyerCart = msg.getContent();
                            buyerMsg = msg;
                            System.out.println("Кассир получил корзину от " + msg.getSender().getLocalName() + ": " + buyerCart);

                            // Отправить корзину в базу данных
                            ACLMessage dbMsg = new ACLMessage(ACLMessage.REQUEST);
                            dbMsg.addReceiver(getAID("db"));
                            dbMsg.setContent(buyerCart);
                            send(dbMsg);
                            System.out.println("Кассир отправил корзину в базу данных.");
                            break;

                        case ACLMessage.INFORM_REF:
                            // Получить итоговую стоимость корзины
                            String totalCost = msg.getContent();
                            System.out.println("Кассир получил итоговую стоимость корзины: " + totalCost + " руб.");

                            // Отправить запрос на подтверждение оплаты покупателю
                            ACLMessage paymentRequest = new ACLMessage(ACLMessage.REQUEST);
                            paymentRequest.addReceiver(buyerMsg.getSender());
                            paymentRequest.setContent("Итоговая стоимость: " + totalCost + " руб. Подтвердите оплату.");
                            send(paymentRequest);
                            System.out.println("Кассир отправил запрос на подтверждение оплаты покупателю.");
                            break;

                        case ACLMessage.CONFIRM:
                            // Получить подтверждение оплаты от покупателя
                            System.out.println("Кассир получил подтверждение оплаты от " + msg.getSender().getLocalName());
                            System.out.println("Обработка завершена. Кассир ждет следующего покупателя.");
                            break;

                        default:
                            System.out.println("Неизвестный тип сообщения получен кассиром.");
                    }
                } else {
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        System.out.println("CashierAgent " + getAID().getName() + " завершает работу.");
    }
}
