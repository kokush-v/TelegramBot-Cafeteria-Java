package uzhnu.bot.methods;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.objects.Message;

import com.google.gson.Gson;

import uzhnu.bot.myclasses.Order;
import uzhnu.bot.myclasses.ShopItem;
import uzhnu.bot.myclasses.User;
import uzhnu.bot.myclasses.UserSession;

public class db {

    static final Gson H_GSON = new Gson();
    static final Logger log = Logger.getLogger(db.class);
    static String userDbPath = "src/main/java/uzhnu/bot/database/users.json";
    static String shopItemDbPath = "src/main/java/uzhnu/bot/database/shop.json";
    static String orderDbPath = "src/main/java/uzhnu/bot/database/orders.json";
    static String sessionDbPath = "src/main/java/uzhnu/bot/database/user_sessions.json";
    static String botChannelDbPath = "src/main/java/uzhnu/bot/database/bot_channel.json";

    public static void addUserToDb(User user) {
        try {
            ArrayList<User> allUsers = getUsersFromDb(null);

            allUsers.add(user);

            FileWriter fileWriter = new FileWriter(userDbPath);
            fileWriter.write(H_GSON.toJson(allUsers));
            fileWriter.close();
            log.info("User was added to database");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

    }

    public static void editUserFromDb(User user) {
        try {
            ArrayList<User> allUsers = getUsersFromDb(null);

            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).getUserId().compareTo(user.getUserId()) == 0) {
                    allUsers.set(i, user);
                    break;
                }
            }

            FileWriter fileWriter = new FileWriter(userDbPath);
            fileWriter.write(H_GSON.toJson(allUsers));
            fileWriter.close();
            log.info("User was edited");

        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    public static ArrayList<User> getUsersFromDb(Long userId) {
        try (Reader reader = new FileReader(userDbPath)) {
            User[] usersArr = H_GSON.fromJson(reader, User[].class);

            ArrayList<User> users = new ArrayList<User>();

            for (User u : usersArr) {
                users.add(u);
                if (userId != null)
                    if (u.getUserId().compareTo(userId) == 0) {
                        users.clear();
                        users.add(u);
                        break;
                    } else
                        users.clear();
            }

            if (userId != null && users.isEmpty()) {
                return null;
            } else {
                return users;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<ShopItem> getShopItemFromDb(Long itemId) {
        try (Reader reader = new FileReader(shopItemDbPath)) {
            ShopItem[] itemsArr = H_GSON.fromJson(reader, ShopItem[].class);

            ArrayList<ShopItem> items = new ArrayList<ShopItem>();

            for (ShopItem i : itemsArr) {
                items.add(i);
                if (itemId != null)
                    if (i.getId().compareTo(itemId) == 0) {
                        items.clear();
                        items.add(i);
                        break;
                    } else
                        items.clear();
            }

            if (itemId != null && items.isEmpty()) {
                return null;
            } else {
                return items;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<Order> getOrdersFromDb(Long orderId) {
        try (Reader reader = new FileReader(orderDbPath)) {
            Order[] ordersArr = H_GSON.fromJson(reader, Order[].class);

            ArrayList<Order> orders = new ArrayList<Order>();

            for (Order o : ordersArr) {
                orders.add(o);
                if (orderId != null)
                    if (o.getOrderId().compareTo(orderId) == 0) {
                        orders.clear();
                        orders.add(o);
                        break;
                    } else
                        orders.clear();
            }

            if (orderId != null && orders.isEmpty()) {
                return null;
            } else {
                return orders;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addOrderToDb(Order order) {
        try {
            ArrayList<Order> allOrders = getOrdersFromDb(null);

            order.setOrderId(allOrders.size() + 1);
            order.setStatus("Опрацьовується 🔃");

            allOrders.add(order);

            FileWriter fileWriter = new FileWriter(orderDbPath);
            fileWriter.write(H_GSON.toJson(allOrders));
            fileWriter.close();
            log.info("Order was added to database");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

    }

    public static void editOrderStatus(long orderId, int status, String reason) {
        try {
            ArrayList<Order> allOrders = getOrdersFromDb(null);

            for (int i = 0; i < allOrders.size(); i++) {
                if (allOrders.get(i).getOrderId().compareTo(orderId) == 0) {
                    if (status == 0) {
                        allOrders.get(i).setStatus("Схвалено ✅");
                    } else {
                        allOrders.get(i).setStatus("Відхилино ❌");
                        if (!reason.isEmpty()) {
                            allOrders.get(i).setReason(reason);
                        }
                    }

                    allOrders.set(i, allOrders.get(i));
                    break;
                }
            }

            FileWriter fileWriter = new FileWriter(orderDbPath);
            fileWriter.write(H_GSON.toJson(allOrders));
            fileWriter.close();
            log.info("Order status was edited");

        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    public static void removeOrderFromDb(Long orderId) {
        try {
            ArrayList<Order> allOrders = getOrdersFromDb(null);

            for (int o = 0; o < allOrders.size(); o++) {
                if (allOrders.get(o).getOrderId().compareTo(orderId) == 0) {
                    allOrders.remove(o);
                }
            }

            FileWriter fileWriter = new FileWriter(orderDbPath);
            fileWriter.write(H_GSON.toJson(allOrders));
            fileWriter.close();
            log.info("Order was removed from database");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

    }

    public static ArrayList<Order> getUserOrdersFromDb(Long userId) {
        try (Reader reader = new FileReader(orderDbPath)) {
            Order[] ordersArr = H_GSON.fromJson(reader, Order[].class);

            ArrayList<Order> orders = new ArrayList<Order>();

            for (Order o : ordersArr) {
                if (userId != null)
                    if (o.getUserId().compareTo(userId) == 0) {
                        orders.add(o);
                    }
            }

            if (userId != null && orders.isEmpty()) {
                return null;
            } else {
                return orders;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    public static ArrayList<Message> getOrderById(Long orderId) {
        try (Reader reader = new FileReader(botChannelDbPath)) {
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

    public static ArrayList<Message> getMessagesFromDbBotChannel(int messageId) {
        try (Reader reader = new FileReader(botChannelDbPath)) {
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

            FileWriter fileWriter = new FileWriter(botChannelDbPath);
            fileWriter.write(H_GSON.toJson(allMessages));
            fileWriter.close();
            log.info("Message added");
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

            FileWriter fileWriter = new FileWriter(botChannelDbPath);
            fileWriter.write(H_GSON.toJson(allMessages));
            fileWriter.close();
            log.info("Message was edited");

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

            FileWriter fileWriter = new FileWriter(botChannelDbPath);
            fileWriter.write(H_GSON.toJson(allMessages));
            fileWriter.close();
            log.info("Message was removed from database");
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }

    }

}
