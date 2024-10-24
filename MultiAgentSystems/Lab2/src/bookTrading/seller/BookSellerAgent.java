package bookTrading.seller;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import java.util.*;

public class BookSellerAgent extends Agent{

    private Hashtable catalogue;

    private BookSellerGui myGui;

    protected void setup(){
        catalogue = new Hashtable();

        myGui = new BookSellerGui(this);
        myGui.show();
        
    }
}
