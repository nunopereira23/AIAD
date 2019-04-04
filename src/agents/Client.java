package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.proto.ContractNetInitiator;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.DeflaterInputStream;

public class Client extends Agent {
	// The list of known seller agents
	private AID[] sellerAgents;
	// The GUI by means of which the user can add products to the order
	private ClientGui myGui;

	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hallo! Client "+getAID().getName()+" is ready.");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			String[] products = (String[])args[0];
			int the_chosen_one = ThreadLocalRandom.current().nextInt(0, products.length);
			buyProduct(products[the_chosen_one], "price");
		}
		else {
			// Make the agent terminate immediately
			doDelete();
		}
		
//		// Create and show the GUI
//				myGui = new ClientGui(this);
//				myGui.showGui();
		}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Close the GUI
//				myGui.dispose();
		// Printout a dismissal message
		System.out.println("Buyer-agent "+getAID().getName()+" terminating.");
	}
	
public void buyProduct(String t, String s_type) {
	System.out.println("Type: " + s_type);
	switch (s_type) {
    case "price":
    	addBehaviour(new PricePurchaseManager(this, t));
        break;
    case "time":
    	addBehaviour(new TimePurchaseManager(this, t));
        break;
    case "mixed":
    	addBehaviour(new MixedPurchaseManager(this, t));
        break;
    default:
        break;
}
}

//Mixed Method
private class MixedPurchaseManager extends TickerBehaviour {
    private String Medicine;
    
    private MixedPurchaseManager(Agent a, String t) {
      super(a, 10000); // every 10 secs
      Medicine = t;
    }

    public void onTick() {
		System.out.println("Trying to buy "+ Medicine);
		// Update the list of seller agents
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("medicine-selling");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(myAgent, template); 
			System.out.println("Found the following seller agents:");
			sellerAgents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				sellerAgents[i] = result[i].getName();
				System.out.println(sellerAgents[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Perform the request
		myAgent.addBehaviour(new RequestPerformer(Medicine));
    }
  }

//Price Method
private class PricePurchaseManager extends TickerBehaviour {
    private String Medicine;
    
    private PricePurchaseManager(Agent a, String t) {
      super(a, 10000); // every 10 secs
      Medicine = t;
    }

    public void onTick() {
		System.out.println("Trying to buy "+ Medicine);
		// Update the list of seller agents
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("medicine-selling");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(myAgent, template); 
			System.out.println("Found the following seller agents:");
			sellerAgents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				sellerAgents[i] = result[i].getName();
				System.out.println(sellerAgents[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Perform the request
		myAgent.addBehaviour(new RequestPerformer(Medicine));	
    }
  }

//Time Method
private class TimePurchaseManager extends TickerBehaviour {
    private String Medicine;
    
    private TimePurchaseManager(Agent a, String t) {
      super(a, 10000); // every 10 secs
      Medicine = t;
    }

    public void onTick() {
		System.out.println("Trying to buy "+ Medicine);
		// Update the list of seller agents
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("medicine-selling");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(myAgent, template); 
			System.out.println("Found the following seller agents:");
			sellerAgents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				sellerAgents[i] = result[i].getName();
				System.out.println(sellerAgents[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Perform the request
		myAgent.addBehaviour(new RequestPerformer(Medicine));	
    }
  }


	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by medicine-buyer agents to request seller 
	   agents the target medicine.
	 */
	private class RequestPerformer extends Behaviour {
		private AID bestSeller; // The agent who provides the best offer 
		private int bestPrice;  // The best offered price
		private int repliesCnt = 0; // The counter of replies from seller agents
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;
		private Coords coord =new Coords();

	    private RequestPerformer(String t) {
	        coord.setTitle(t);
	      }
	    
		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < sellerAgents.length; ++i) {
					cfp.addReceiver(sellerAgents[i]);
				} 
//				cfp.setContent(Medicine);
				cfp.setConversationId("medicine-trade");
				try {
					cfp.setContentObject(coord);
				} catch (IOException e) {
					e.printStackTrace();
				}
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("medicine-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer 
						int price = Integer.parseInt(reply.getContent());

						coord.setOffer_price(price);
						if (bestSeller == null || price < bestPrice) {
							// This is the best offer at present
							bestPrice = price;
							bestSeller = reply.getSender();
						}
					}
					repliesCnt++;
					if (repliesCnt >= sellerAgents.length) {
						// We received all replies
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2:
				// Send the purchase order to the seller that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				coord.setSeller_id(bestSeller.toString());

				try {
					order.setContentObject(coord);
					order.setConversationId("medicine-trade");
					order.setReplyWith("order"+System.currentTimeMillis());
				} catch (IOException e) {
					e.printStackTrace();
				}

				myAgent.send(order);

                ACLMessage DENIED = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                for(int i = 0 ; i < sellerAgents.length; i++){
                    if(sellerAgents[i] != bestSeller){
                      DENIED.addReceiver(sellerAgents[i]);
                    }
                }
				try {
					DENIED.setContentObject(coord);
					DENIED.setConversationId("medicine-trade");
					DENIED.setReplyWith("order"+System.currentTimeMillis());
				} catch (IOException e) {
					e.printStackTrace();
				}

                myAgent.send(DENIED);
				// Prepare the template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("medicine-trade"), MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
			case 3:      
				// Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// Purchase order reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate
						System.out.println(coord.title +" successfully purchased from agent "+reply.getSender().getName());
						System.out.println("Price = "+bestPrice);
						myAgent.doDelete();
					}
					else {
						System.out.println("Attempt failed: requested medicine already sold.");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
			}        
		}

		public boolean done() {
			if (step == 2 && bestSeller == null) {
				System.out.println("Attempt failed: "+ coord.title +" not available for sale");
			}
			return ((step == 2 && bestSeller == null) || step == 4);
		}
	}  // End of inner class RequestPerformer
}