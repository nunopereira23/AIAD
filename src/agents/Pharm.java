package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.UnreadableException;
import com.opencsv.CSVWriter;

import java.io.File;
import java.lang.*;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import java.util.*;
import java.util.stream.IntStream;

public class Pharm extends Agent {
	// The catalogue of medicines for sale (maps the title of a medicine to its price)
	private Hashtable catalogue;
	// The GUI by means of which the user can add medicines in the catalogue
	private PharmGui myGui;

	private Point pharm_coordinates;
	private File log_file;

	// Put agent initializations here
	protected void setup() {
		// Create the catalogue
		catalogue = new Hashtable();

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			pharm_coordinates = new Point((Integer)args[0],(Integer)args[1]);
			System.out.println("Pharm coordinates" + pharm_coordinates);
			String[] products = (String[])args[2];
			generateProducts(products);
			System.out.println("PRODS ADDED");
		}
		else {
			// Make the agent terminate immediately
			System.out.println("No coordinates defined");
			doDelete();
		}

//		// Create and show the GUI
//		myGui = new PharmGui(this);
//		myGui.showGui();


		// Register the medicine-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("medicine-selling");
		sd.setName(getLocalName()+"-drugs-selling");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Add the behaviour serving queries from buyer agents
		addBehaviour(new OfferRequestsServer());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new PurchaseOrdersServer());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Close the GUI
//		myGui.dispose();
		// Printout a dismissal message
		System.out.println("Seller-agent "+getAID().getName()+" terminating.");
	}

	/**
     This is invoked by the GUI when the user adds a new medicine for sale
	 */
	public void updateCatalogue(final String title,final int price, final int stock, final int season, final int order_time) {
		addBehaviour(new OneShotBehaviour() {
			public void action() {
				Product to_insert = new Product(title,price,stock,season,order_time);
				catalogue.put(title, to_insert);
				System.out.println("Product " + to_insert.getName() + " inserted!");
				System.out.println("Price -> " + to_insert.getPrice());
				System.out.println("Order Time -> " + to_insert.getOrder_time());
				System.out.println("Stock -> " + to_insert.getStock());
				System.out.println("Season -> " + to_insert.getSeason());
			}
		} );
	}

	public void generateProducts(String[] products){
		int nrProducts = ThreadLocalRandom.current().nextInt(1, products.length + 1);

		ArrayList<Integer> chosen = new ArrayList<Integer>();
		for(int i = 0; i<products.length ; i++){
//			int the_chosen_one = ThreadLocalRandom.current().nextInt(0, products.length);
//			if (chosen.contains(the_chosen_one)){
//				i--;
//			}
//			else {
//				int price = ThreadLocalRandom.current().nextInt(100, 1000 + 1);
//				int stock = ThreadLocalRandom.current().nextInt(10, 150 + 1);
//				int season = ThreadLocalRandom.current().nextInt(1, 4 + 1);
//				chosen.add(the_chosen_one);
//				updateCatalogue(products[the_chosen_one], price, stock, season, 1);
//			}
			int price = ThreadLocalRandom.current().nextInt(1, 50 + 1);
			int stock = ThreadLocalRandom.current().nextInt(10, 150 + 1);
			int season = ThreadLocalRandom.current().nextInt(1, 4 + 1);
			updateCatalogue(products[i], price, stock, season, 1);
		}
	}

	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				System.out.println(title);
				Coords cli_coord = null;
				try {
					cli_coord = (Coords) msg.getContentObject();
					System.out.printf("Med: %s",cli_coord.getTitle());
					title = cli_coord.getTitle();
					System.out.printf("X: %d", cli_coord.getX());
					System.out.printf("Y: %d ",cli_coord.getY());
				} catch (UnreadableException e) {
					e.printStackTrace();
				}


				ACLMessage reply = msg.createReply();
				Product to_sell = (Product) catalogue.get(title);
				if (to_sell != null) {
					// The requested medicine is available for sale. Reply with the price
					Integer price = to_sell.getPrice();
					//TODO MATH On Price iwth coordinates and season and multiplier
					Point client_coords = new Point(cli_coord.getX(),cli_coord.getY());
					double distance = client_coords.distance(pharm_coordinates);
					Integer offerPrice  = (int) (Math.abs(0.1*(1/cli_coord.getActual_season())-0.1*(1/to_sell.getSeason())) *(-1) *price + 0.1 * price * distance)+price;
                    System.out.printf("\n Price : %d", price);
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(offerPrice.intValue()));

				}
				else {
					// The requested medicine is NOT available for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
	private class PurchaseOrdersServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL));
			ACLMessage msg = myAgent.receive(mt);
			log_file = new File("./logs/log_" + myAgent.getLocalName() + ".csv");
			if (msg!=null) {
				if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
					// ACCEPT_PROPOSAL Message received. Process it
					Coords coord = null;
					try {
						coord = (Coords) msg.getContentObject();
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
					//TODO guardar coord no csv
                    CSVWriter csvWriter = null;
					FileWriter fw = null;

					if(log_file.exists()){
						try {
							fw = new FileWriter(log_file,true);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					else{
						try {
							fw = new FileWriter(log_file);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					csvWriter = new CSVWriter(fw);
					List<String[]> rows = new LinkedList<String[]>();
                    String title = coord.getTitle();
					ACLMessage reply = msg.createReply();
					Product to_sell = (Product) catalogue.get(title);
					Integer price = to_sell.getPrice();
					Integer product_season = to_sell.getSeason();
					Integer actual_season = coord.getActual_season();
					Integer offer_price = coord.getOffer_price();
					Integer product_stock = to_sell.getStock();
					Point client_coords = new Point(coord.getX(),coord.getY());
					Integer distance = (int)client_coords.distance(pharm_coordinates);

					if (to_sell != null && to_sell.getStock() != 0) {
                        rows.add(new String[]{
                        		coord.getTitle(),
								String.valueOf(product_stock),
								product_season.toString(),
								actual_season.toString(),
								Integer.toString(coord.getX()),
								Integer.toString(coord.getY()),
								Integer.toString(pharm_coordinates.x),
								Integer.toString(pharm_coordinates.y),
								distance.toString(),
								Integer.toString(price),
								Integer.toString(offer_price),
								"sold"
                        });
                        csvWriter.writeAll(rows);
                        try {
                            csvWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
						reply.setPerformative(ACLMessage.INFORM);
						System.out.println(title + " sold to agent " + msg.getSender().getName());
						int quantity = 1 ;
						to_sell.decreasStock(quantity);
					} else {
						// The requested medicine has been sold to another buyer in the meanwhile .
						reply.setPerformative(ACLMessage.FAILURE);
						reply.setContent("not-available");
					}
					myAgent.send(reply);
				}
				else if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
                    Coords coord = null;
                    try {
                        coord = (Coords) msg.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    if(!(coord.getSeller_id().equals(myAgent.getAID().toString()))) {
						String title = coord.getTitle();
						Product to_sell = (Product) catalogue.get(title);
						Integer price = to_sell.getPrice();
						Integer product_season = to_sell.getSeason();
						Integer actual_season = coord.getActual_season();
						Integer product_stock = to_sell.getStock();
						Integer offer_price = coord.getOffer_price();
						Point client_coords = new Point(coord.getX(),coord.getY());
						Integer distance = (int)client_coords.distance(pharm_coordinates);

						CSVWriter csvWriter = null;
						FileWriter fw = null;
						if(log_file.exists()){
							try {
								fw = new FileWriter(log_file,true);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						else{
							try {
								fw = new FileWriter(log_file);
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
						csvWriter = new CSVWriter(fw);
						List<String[]> rows = new LinkedList<String[]>();
						rows.add(new String[]{
								coord.getTitle(),
								String.valueOf(product_stock),
								product_season.toString(),
								actual_season.toString(),
								Integer.toString(coord.getX()),
								Integer.toString(coord.getY()),
								Integer.toString(pharm_coordinates.x),
								Integer.toString(pharm_coordinates.y),
								distance.toString(),
								Integer.toString(price),
								Integer.toString(offer_price),
								"not_sold"
						});
						csvWriter.writeAll(rows);
						try {
							csvWriter.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer
}
