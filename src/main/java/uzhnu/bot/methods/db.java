package uzhnu.bot.methods;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.gson.Gson;

import uzhnu.bot.myclasses.*;

public class db {

    static final Logger log = Logger.getLogger(db.class);
    static final Gson H_GSON = new Gson();

    static final String sessionDbPath = "src/main/java/uzhnu/bot/database/sessionBot.json";

    static DatabaseReference database;

    public static void init() throws Exception {
        FileInputStream serviceAccount = new FileInputStream(
                "src/main/java/uzhnu/bot/database/tokenFirebase.json");

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl("https://cafeteriabotdatabase-default-rtdb.europe-west1.firebasedatabase.app")
                .build();

        FirebaseApp.initializeApp(options);

        db.database = FirebaseDatabase.getInstance().getReference();
    }

    public static CompletableFuture<Integer> addUserToDb(User user) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        db.database.child(String.format("users/%s", user.getUserId().toString())).setValue(user,
                new CompletionListener() {

                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            System.out.println("user was configurated");
                            future.complete(0);
                        } else {
                            System.out.println("Error: " + error.getMessage());
                            future.complete(1);
                        }

                    }
                });

        return future;

    }

    public static ArrayList<User> getUsersFromDb(Long userId) {
        ArrayList<User> usersArr = new ArrayList<>();
        if (userId == null) {
            db.database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            usersArr.add(userSnapshot.getValue(User.class));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("Error: " + error.getMessage());
                }

            });
        } else {
            db.database.child(("users/" + userId).toString())
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                usersArr.add(snapshot.getValue(User.class));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            System.out.println("Error: " + error.getMessage());
                        }

                    });
        }

        if (usersArr.isEmpty())
            return null;

        return usersArr;
    }

    public static CompletableFuture<ArrayList<User>> getUsersFromDb1(Long userId) {
        CompletableFuture<ArrayList<User>> future = new CompletableFuture<>();

        ArrayList<User> usersArr = new ArrayList<>();
        if (userId == null) {
            db.database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {

                        for (DataSnapshot eSnapshot : snapshot.getChildren()) {
                            usersArr.add(eSnapshot.getValue(User.class));
                        }
                        future.complete(usersArr);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("Error: " + error.getMessage());
                }

            });
        } else {
            db.database.child(("users/" + userId).toString())
                    .addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                usersArr.add(snapshot.getValue(User.class));
                            }

                            future.complete(usersArr);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            System.out.println("Error: " + error.getMessage());
                        }

                    });
        }

        return future;

    }

    public static CompletableFuture<ArrayList<ShopItem>> getShopItemFromDb() {
        CompletableFuture<ArrayList<ShopItem>> future = new CompletableFuture<>();

        ArrayList<ShopItem> itemsArr = new ArrayList<>();
        db.database.child("shopitems").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot eSnapshot : snapshot.getChildren()) {
                        itemsArr.add(eSnapshot.getValue(ShopItem.class));
                    }
                    future.complete(itemsArr);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Error: " + error.getMessage());
            }

        });

        return future;
    }

    public static CompletableFuture<ArrayList<Order>> getUserOrdersFromDb1(Long orderUserId) {
        CompletableFuture<ArrayList<Order>> future = new CompletableFuture<>();

        ArrayList<Order> orders = new ArrayList<>();

        db.database.child("orders").orderByChild("userId").startAt(orderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            for (DataSnapshot eSnapshot : snapshot.getChildren()) {
                                orders.add(eSnapshot.getValue(Order.class));
                            }
                            future.complete(orders);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.out.println("Error: " + error.getMessage());
                    }

                });

        return future;
    }

    public static CompletableFuture<Integer> addOrderToDb(Order order) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        db.database.child(String.format("orders/%s", order.getOrderId().toString())).setValue(order,
                new CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            System.out.println("order was configurated");
                            future.complete(0);
                        } else {
                            System.out.println("Error: " + error.getMessage());
                            future.complete(1);
                        }
                    }
                });

        return future;
    }

    public static CompletableFuture<Integer> editOrderStatus(long orderId, int status) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String statuString = "Схваленно";

        if (status == 1) {
            statuString = "Відхилено";
        }

        db.database.child("orders/" + orderId + "/status").setValue(statuString,
                new CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            System.out.println("status was configurated");
                            future.complete(0);
                        } else {
                            System.out.println("Error: " + error.getMessage());
                            future.complete(1);
                        }
                    }
                });

        return future;
    }

    public static CompletableFuture<Integer> removeOrderFromDb(Long orderId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        db.database.child("orders/" + orderId).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError error, DatabaseReference ref) {
                if (error == null) {
                    System.out.println("order deleted successfully");
                    future.complete(0);
                } else {
                    System.out.println("Failed to delete order: " + error.getMessage());
                    future.complete(1);
                }
            }
        });

        return future;
    }

    public static void addUserSessionToDb(long userId) {
        try {
            ArrayList<UserSession> allSession = getUserSessionFromDb(null);

            allSession.add(new UserSession(userId, 1, 0));

            FileWriter fileWriter = new FileWriter(sessionDbPath);
            fileWriter.write(H_GSON.toJson(allSession));
            fileWriter.close();
            log.info("Session added");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

    }

    public static ArrayList<UserSession> getUserSessionFromDb(Long userId) {
        try (Reader reader = new FileReader(sessionDbPath)) {
            UserSession[] uSessionArr = H_GSON.fromJson(reader, UserSession[].class);

            ArrayList<UserSession> uSession = new ArrayList<UserSession>();

            for (UserSession u : uSessionArr) {
                uSession.add(u);
                if (userId != null)
                    if (u.getUserId().compareTo(userId) == 0) {
                        uSession.clear();
                        uSession.add(u);
                        break;
                    } else
                        uSession.clear();
            }

            if (userId != null && uSession.isEmpty()) {
                return null;
            } else {
                return uSession;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void editUserSessionFromDb(UserSession userSession) {
        try {
            ArrayList<UserSession> allSession = getUserSessionFromDb(null);

            for (int i = 0; i < allSession.size(); i++) {
                if (allSession.get(i).getUserId().compareTo(userSession.getUserId()) == 0) {
                    allSession.set(i, userSession);
                    break;
                }
            }

            FileWriter fileWriter = new FileWriter(sessionDbPath);
            fileWriter.write(H_GSON.toJson(allSession));
            fileWriter.close();
            log.info("Session was edited");

        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    public static ArrayList<Message> getMessagesFromDbBotChannel(int messageId) {
        return null;

    }

    public static void addMessagesToDbBotChannel(Message message) {

    }

    public static void editMessagesFromDbBotChannel(Message message) {

    }

    public static void removeMessagesFromDbBotChannel(int messageId) {

    }

}
