package uzhnu.bot.myclasses;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import uzhnu.bot.methods.db;

public class UserSession {
    private Long userId;
    private int registerStep;
    private int editChoice;
    private ArrayList<ShopMenu> selectedItem = new ArrayList<>();
    private Order newOrder = new Order();

    public UserSession() {
    }

    public UserSession(Long userId, int registerStep, int editChoice) {
        this.userId = userId;
        this.registerStep = registerStep;
        this.editChoice = editChoice;

    }

    public CompletableFuture<UserSession> initUserSessionItems() {
        CompletableFuture<UserSession> future = new CompletableFuture<>();

        db.getShopItemFromDb().thenAccept(resp -> {
            if (resp != null) {
                for (var e : resp) {
                    System.out.println(e.getId());
                    selectedItem.add(new ShopMenu(e, 0));
                }
                future.complete(this);
            }
        }).exceptionally(ex -> {
            System.out.println("Error: " + ex.getMessage());
            return null;
        });

        return future;
    }

    public Order getNewOrder() {
        return newOrder;
    }

    public void setNewOrder(Order newOrder) {
        this.newOrder = newOrder;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getRegisterStep() {
        return registerStep;
    }

    public void setRegisterStep(int registerStep) {
        this.registerStep = registerStep;
    }

    public int getEditChoice() {
        return editChoice;
    }

    public void setEditChoice(int editChoice) {
        this.editChoice = editChoice;
    }

    public ArrayList<ShopMenu> getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(ArrayList<ShopMenu> selectedItem) {
        this.selectedItem = selectedItem;
    }

}
