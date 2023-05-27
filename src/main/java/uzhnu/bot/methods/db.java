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
    static final String botChannel = "src/main/java/uzhnu/bot/database/bot_channel.json";
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
                            log.info("user was configurated");
                            future.complete(0);
                        } else {
                            log.info("Error: " + error.getMessage());
                            future.complete(1);
                        }

                    }
                });

        return future;

    }

    public static CompletableFuture<ArrayList<User>> getUsersFromDb(Long userId) {
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
                    log.info("Error: " + error.getMessage());
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
                            log.info("Error: " + error.getMessage());
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
                log.info("Error: " + error.getMessage());
            }

        });

        return future;
    }

    public static CompletableFuture<ArrayList<Order>> getUserOrdersFromDb(Long orderUserId) {
        CompletableFuture<ArrayList<Order>> future = new CompletableFuture<>();

        ArrayList<Order> orders = new ArrayList<>();
        db.database.child(String.format("orders/%s", orderUserId))
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            for (DataSnapshot eSnapshot : snapshot.getChildren()) {
                                orders.add(eSnapshot.getValue(Order.class));
                            }
                        }
                        future.complete(orders);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        log.info("Error: " + error.getMessage());
                    }

                });

        return future;
    }

    public static CompletableFuture<Integer> addOrderToDb(Order order) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        db.database.child(String.format("orders/%s/%s", order.getUserId(), order.getOrderId())).setValue(order,
                new CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            log.info("order was configurated");
                            future.complete(0);
                        } else {
                            log.info("Error: " + error.getMessage());
                            future.complete(1);
                        }
                    }
                });

        return future;
    }

    public static CompletableFuture<Integer> editOrderStatus(long orderId, long userId, int status, String reason) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        String statuString = "Схваленно ✅";

        if (status == 1) {
            statuString = "Відхилено ❌";
        }

        db.database.child(String.format("orders/%s/%s/status", userId, orderId)).setValue(statuString,
                new CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            log.info("status was configurated");
                            future.complete(0);
                        } else {
                            log.info("Error: " + error.getMessage());
                            future.complete(1);
                        }
                    }
                });
        log.info(reason, null);

        db.database.child(String.format("orders/%s/%s/reason", userId, orderId)).setValue(reason,
                new CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            log.info("reason added was configurated");
                            future.complete(0);
                        } else {
                            log.info("Error: " + error.getMessage());
                            future.complete(1);
                        }
                    }
                });

        return future;

    }

    public static CompletableFuture<Integer> removeOrderFromDb(Long orderId, Long userId) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        db.database.child(String.format("orders/%s/%s", userId, orderId))
                .removeValue(new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError error, DatabaseReference ref) {
                        if (error == null) {
                            log.info("order deleted successfully");
                            future.complete(0);
                        } else {
                            log.info("Failed to delete order: " + error.getMessage());
                            future.complete(1);
                        }
                    }
                });

        return future;
    }

    public static void addUserSessionToDb(long userId) {

        ArrayList<UserSession> allSession = getUserSessionFromDb(null);
        var u = new UserSession(userId, 1, 0);

        u.initUserSessionItems().thenAccept(res -> {
            allSession.add(res);
            try {
                FileWriter fileWriter = new FileWriter(sessionDbPath);
                fileWriter.write(H_GSON.toJson(allSession));
                fileWriter.close();
                log.info("session added");
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        });
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
            log.info("session was edited");

        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    public static ArrayList<Message> getMessagesFromDbBotChannel(int messageId) {
        try (Reader reader = new FileReader(botChannel)) {
            Message[] messagesArr = H_GSON.fromJson(reader, Message[].class);

            ArrayList<Message> messages = new ArrayList<Message>();

            for (Message m : messagesArr) {
                messages.add(m);
                if (messageId != 0)
                    if (m.getMessageId() == messageId) {
                        messages.clear();
                        messages.add(m);
                        break;
                    } else
                        messages.clear();
            }

            if (messageId != 0 && messages.isEmpty()) {
                return null;
            } else {
                return messages;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addMessagesToDbBotChannel(Message message) {
        try {
            ArrayList<Message> allMessages = getMessagesFromDbBotChannel(0);

            allMessages.add(message);

            FileWriter fileWriter = new FileWriter(botChannel);
            fileWriter.write(H_GSON.toJson(allMessages));
            fileWriter.close();
            log.info("message added");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

    }

    public static void editMessagesFromDbBotChannel(Message message) {
        try {
            ArrayList<Message> allMessages = getMessagesFromDbBotChannel(0);

            for (int i = 0; i < allMessages.size(); i++) {
                if (allMessages.get(i).getMessageId() == message.getMessageId()) {
                    allMessages.set(i, message);
                    break;
                }
            }

            FileWriter fileWriter = new FileWriter(botChannel);
            fileWriter.write(H_GSON.toJson(allMessages));
            fileWriter.close();
            log.info("message was edited");

        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    public static void removeMessagesFromDbBotChannel(int messageId) {
        try {
            ArrayList<Message> allMessages = getMessagesFromDbBotChannel(0);

            for (int m = 0; m < allMessages.size(); m++) {
                if (allMessages.get(m).getMessageId() == messageId) {
                    allMessages.remove(m);
                }
            }

            FileWriter fileWriter = new FileWriter(botChannel);
            fileWriter.write(H_GSON.toJson(allMessages));
            fileWriter.close();
            log.info("message was removed from database");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

    }

    public static ArrayList<Message> getOrderById(Long orderId) {
        try (Reader reader = new FileReader(botChannel)) {
            Message[] messagesArr = H_GSON.fromJson(reader, Message[].class);
            Long mesId = 0l;
            ArrayList<Message> messages = new ArrayList<Message>();

            for (Message m : messagesArr) {
                messages.add(m);
                if (orderId != null)
                    mesId = Long.parseLong(m.getText().split("\n")[0].split(" ")[1]);

                if (mesId.compareTo(orderId) == 0) {
                    messages.clear();
                    messages.add(m);
                    break;
                } else
                    messages.clear();
            }

            if (orderId != 0 && messages.isEmpty()) {
                return null;
            } else {
                return messages;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}