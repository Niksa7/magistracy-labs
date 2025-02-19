package store;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import org.w3c.dom.ls.LSOutput;

import java.util.HashMap;
import java.util.Map;

public class DatabaseAgent extends Agent {

    private Map<String, Product> inventory = new HashMap<>();
    private int cash;

    @Override
    protected void setup() {
        System.out.println("[DB: " + getLocalName() + "] запускается.");

        doWait(20000);

        initDB();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("database");
        sd.setName(getLocalName());
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    switch (msg.getPerformative()) {
                        case ACLMessage.REQUEST:
                            String cart = msg.getContent();
                            System.out.println("База данных получила запрос на обработку корзины: " + cart);

                            String totalCost = processCart(cart);

                            // Вернуть итоговую стоимость кассиру
                            ACLMessage reply = msg.createReply();
                            reply.setPerformative(ACLMessage.INFORM_REF);
                            reply.setContent(String.valueOf(totalCost));
                            send(reply);
                            System.out.println("База данных отправила ответ.");
                            break;
                        case ACLMessage.INFORM:
                            System.out.println("База данных получила подтверждение платежа");
                        default:
//                            System.out.println("Неизвестный тип сообщения получен базой данных.");
                    }
                } else {
                    block();
                }
            }
        });
    }

    private void initDB() {
        for (int i = 1; i <= 15; i++) {
            String product = "product_" + i;
            int quantity = (int) (Math.random() * 50 + 20);
            int price = (int) (Math.random() * 100 + 50);
            inventory.put(product, new Product(quantity, price));
        }

        System.out.println("\n[DB] База данных инициализирована:\n");
        inventory.forEach((product, details) -> System.out.println(product + ": " + details));
    }

    private String processCart(String cart) {
        StringBuilder receiptDetails = new StringBuilder();
        String[] items = cart.split(";");
        int totalCost = 0;

        receiptDetails.append("\nЧек:\n");

        for (String item : items) {
            String[] productInfo = item.split(":");
            String product = productInfo[0];
            int quantity = Integer.parseInt(productInfo[1]);

            if (inventory.containsKey(product) && inventory.get(product).getQuantity() >= quantity) {
                Product itemDetails = inventory.get(product);
                int itemCost = itemDetails.getPrice() * quantity;
                totalCost += itemCost;

                // Обновить количество товара
                itemDetails.setQuantity(itemDetails.getQuantity() - quantity);

                // Добавить информацию о товаре в чек
                receiptDetails.append(product)
                        .append(", количество: ").append(quantity)
                        .append(", цена за единицу: ").append(itemDetails.getPrice())
                        .append(", стоимость: ").append(itemCost).append(" руб.\n");
            } else {
                receiptDetails.append(product)
                        .append(": недостаточное количество на складе.\n");
            }
        }

        receiptDetails.append("\nИтоговая стоимость: ").append(totalCost).append(" руб.");
        return receiptDetails.toString();
    }


    private static class Product {
        private int quantity;
        private int price;

        public Product(int quantity, int price) {
            this.quantity = quantity;
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getPrice() {
            return price;
        }

        @Override
        public String toString() {
            return "[quantity=" + quantity + ", price=" + price + "]";
        }
    }
}
