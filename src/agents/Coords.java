package agents;
import java.util.concurrent.ThreadLocalRandom;
import java.io.Serializable;

public class Coords implements Serializable {
    public String title;
    public int x;
    public int y;
    public int best_price;
    public String seller_id;
    public String buyer_id = "";
    public int actual_season;
    public int offer_price;
    public int distance;


public Coords() {
    this.x = ThreadLocalRandom.current().nextInt(0, 100 + 1);
    this.y = ThreadLocalRandom.current().nextInt(0, 100 + 1);
    this.actual_season = ThreadLocalRandom.current().nextInt(1, 4 + 1);
    }

    public Coords(String title, int x, int y) {
        this.x = x;
        this.y = y;
        this.title = title;
    }


    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getOffer_price() {
        return offer_price;
    }

    public void setOffer_price(int offer_price) {
        this.offer_price = offer_price;
    }

    public int getActual_season() {
        return actual_season;
    }

    public void setActual_season(int actual_season) {
        this.actual_season = actual_season;
    }

    public String getBuyer_id() {
        return buyer_id;
    }

    public void setBuyer_id(String buyer_id) {
        this.buyer_id = buyer_id;
    }


    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public int getBest_price() {
        return best_price;
    }
    public void setPrice(int price) {
        this.best_price = price;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
