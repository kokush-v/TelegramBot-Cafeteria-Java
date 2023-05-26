package uzhnu.bot.myclasses;

import java.util.ArrayList;

public class Order {
    private String status;
    private Long userId;
    private Long orderId;
    private ArrayList<ShopMenu> orderItems = new ArrayList<>();
    private String reason = "";
    private Double price = 0.0;

    public Order() {
    }

    public Order(String status, Long userId, Long orderId, ArrayList<ShopMenu> orderItems, String reason,
            Double price) {
        this.status = status;
        this.userId = userId;
        this.orderId = orderId;
        this.orderItems = orderItems;
        this.reason = reason;
        this.price = price;
    }

    public void clear() {
        this.orderItems.clear();
        this.price = 0.0;
        this.status = null;
        this.userId = null;
        this.orderId = null;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Double getPrice() {

        return this.price;
    }

    public void setPrice(Double price) {
        for (var x : orderItems) {
            this.price += x.getItem().getItemPrice() * x.getAmount();
        }
    }

    public ArrayList<ShopMenu> getOrderItems() {
        return this.orderItems;
    }

    public void addOrderItems(ShopMenu item) {
        this.orderItems.add(item);
    }

}
