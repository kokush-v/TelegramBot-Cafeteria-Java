package uzhnu.bot.myclasses;

public class User {
    private Long userId;
    private String userName;
    private String userPhone;
    int numberOfOrders;

    public User() {
    }

    public User(Long userId, String userName, String userPhone, int numberOfOrders) {
        this.userId = userId;
        this.userName = userName;
        this.userPhone = userPhone;
        this.numberOfOrders = numberOfOrders;
    }

    public int getNumberOfOrders() {
        return numberOfOrders;
    }

    public void setNumberOfOrders(int numberOfOrders) {
        this.numberOfOrders += numberOfOrders;

        if (this.numberOfOrders < 0) {
            this.numberOfOrders = 0;
        }
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

}
