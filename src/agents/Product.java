package agents;

public class Product {
	private String name;
	private int price;
	private int public_price;
	private int stock;
	private int order_time;
	private int season;



	public Product(String name, int price, int stock,int season,  int order_time) {
		this.name = name;
		this.price = price;
		this.stock = stock;
		this.order_time = order_time;
		this.season = season;

	}

	public int getPublic_price() {
		return public_price;
	}

	public void setPublic_price(int public_price) {
		this.public_price = public_price;
	}


	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}


	/**
	 * @return the order_time
	 */
	public int getOrder_time() {
		return order_time;
	}
	/**
	 * @param order_time the order_time to set
	 */
	public void setOrder_time(int order_time) {
		this.order_time = order_time;
	}
	/**
	 * @return the stock
	 */
	public int getStock() {
		return stock;
	}
	/**
	 * @param stock the stock to set
	 */
	public void setStock(int stock) {
		this.stock = stock;
	}
	/**
	 * @return the price
	 */
	public int getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public void setPrice(int price) {
		this.price = price;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 *
	 * @param size
	 * @return
	 */
	public int decreasStock(int size){this.stock-=size; return this.stock;}

}
