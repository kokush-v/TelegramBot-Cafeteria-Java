package uzhnu.bot.myclasses;

public class ShopMenu {
    private ShopItem item;
    private int amount;

    public ShopMenu(ShopItem item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    public ShopMenu() {
    }

    public ShopItem getItem() {
        return item;
    }

    public void setItem(ShopItem item) {
        this.item = item;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}
