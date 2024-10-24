package bookTrading.buyer;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;

public class BookBuyerAgent extends Agent {

    private String targetBookTitle;

    private AID[] sellerAgents = {new AID("seller1", AID.ISLOCALNAME),
    new AID("seller2", AID.ISLOCALNAME)};

    protected void setup() {

        System.out.println("Hello! Buyer-agent " + getAID().getName() + "is ready.");

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            targetBookTitle = (String) args[0];
            System.out.println("Trying to buy " + targetBookTitle);

            addBehaviour(new TickerBehaviour(this, 1000) {
                @Override
                protected void onTick() {
                    myAgent.addBehaviour(new RequestPerformer());
                }
            });
        }
        else {
            System.out.println("No book title specified.");
            doDelete();
        }
    }

    protected void takeDown() {
        System.out.println("Buyer-agent " + getAID().getName() + "terminated.");
    }
}