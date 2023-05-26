package uzhnu.bot.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import uzhnu.bot.configuration.BotConfig;
import uzhnu.bot.methods.db;
import uzhnu.bot.myclasses.Order;
import uzhnu.bot.myclasses.ReplyButton;
import uzhnu.bot.myclasses.ShopMenu;
import uzhnu.bot.myclasses.User;
import uzhnu.bot.myclasses.UserSession;

public class TelegramBot extends TelegramLongPollingBot {

  private final BotConfig config;
  private User user = new User();
  private ArrayList<Message> textMessageHistory = new ArrayList<>();
  static final Logger log = Logger.getLogger(TelegramBot.class);
  long botChannel = -1001928215363l;

  public TelegramBot(BotConfig config) {
    super(config.getBotToken());
    this.config = config;

    List<BotCommand> listOfCommands = new ArrayList<>();
    listOfCommands.add(new BotCommand("/start", "Перейти на початок"));
    listOfCommands.add(new BotCommand("/menu", "Відкриває меню кафетерію"));
    listOfCommands.add(new BotCommand("/profile", "Профіль користувача"));
    listOfCommands.add(new BotCommand("/register", "Зареєструйтесь щоб користуватись ботом"));

    try {
      this.execute(
          new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
    } catch (TelegramApiException e) {
      log.error(e.getMessage());
    }
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage()) {
      long chatId = update.getMessage().getChatId();
      Message messageUpdate = update.getMessage();
      String messageTextUpdate = messageUpdate.getText();
      Long userId = update.getMessage().getFrom().getId();

      addHistoryMessage(messageUpdate);

      if (messageUpdate.getChatId().compareTo(botChannel) == 0) {
        db.addMessagesToDbBotChannel(messageUpdate);
      }

      try {
        switch (messageTextUpdate) {
          case "/start" -> {
            sendMessage(
                chatId,
                String.format(
                    "Доброго дня %s, вас вітає %s. Я створенний для полегшеної взаємодії з кафетерієм.",
                    update.getMessage().getChat().getFirstName(),
                    this.getMe().getFirstName()),
                null);
            if (db.getUserSessionFromDb(userId) == null)
              db.addUserSessionToDb(userId);

            if (db.getUsersFromDb(userId) == null) {
              var messageNew = sendMessage(chatId, "Чи бажаєте провести реєстрацію? 😊", null);

              ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                  List.of(
                      new ReplyButton("Так", "REG"),
                      new ReplyButton("Пізніше", "NOT_REG")));
              setReplyButtonsOnMessage(replyButtons, chatId, messageNew.getMessageId(),
                  "Чи бажаєте провести реєстрацію? 😊");
            } else {
              showProfile(chatId, update, 0, userId);
              var s = db.getUserSessionFromDb(userId).get(0);
              if (s.getRegisterStep() != 4) {
                s.setRegisterStep(4);
                db.editUserSessionFromDb(s);
              }
            }

          }
          case "/register" -> {

            userReg(chatId, userId, messageUpdate.getMessageId());
          }

          case "/profile" -> {
            showProfile(chatId, update, 0, userId);
          }
          case "/menu" -> {
            if (db.getUsersFromDb(userId) != null)
              showMenu(chatId, userId);
            else {
              var m = sendMessage(chatId, "❗", null);

              ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                  Arrays.asList(new ReplyButton("Реєстрація", "REG")));

              setReplyButtonsOnMessage(replyButtons, chatId, m.getMessageId(), "❗Будь ласка пройдіть реєстрацію❗");
            }
          }
        }
      } catch (Exception e) {
        log.error(e.getMessage());
      }
    } else if (update.hasCallbackQuery()) {
      int messageId = (int) update.getCallbackQuery().getMessage().getMessageId();
      long chatId = update.getCallbackQuery().getMessage().getChatId();
      String callBack = update.getCallbackQuery().getData();
      Message updMessage = update.getCallbackQuery().getMessage();
      Long updUserId = update.getCallbackQuery().getFrom().getId();
      Message lastTextHistoryMessage = textMessageHistory.get(textMessageHistory.size() - 1);
      UserSession userSession = db.getUserSessionFromDb(updUserId).get(0);

      switch (callBack) {
        case "REG" -> {
          deleteMessages(chatId, 1, update);
          userReg(chatId, updUserId, messageId);

        }

        case "NOT_REG" -> {
          String editText = "Добре, але майте на увазі, щоб користуватись послугами потрібно пройти реєстрацію 👌";

          ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
              List.of(
                  new ReplyButton("Подивитись меню", "MENU"),
                  new ReplyButton("Прогноз погоди", "WEATHER"),
                  new ReplyButton("Зареєструватись", "REG")));

          setReplyButtonsOnMessage(replyButtons, chatId,
              messageId, editText);
        }

        case "SAVE_DATA" -> {
          switch (userSession.getRegisterStep()) {
            case 1 -> {
              if (inputValidation(chatId, update, updMessage, lastTextHistoryMessage)
                  && !lastTextHistoryMessage.getFrom().getIsBot()) {

                user.setUserName(lastTextHistoryMessage.getText());

                deleteMessages(chatId, 1, update);
                ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                    List.of(
                        new ReplyButton("Зберегти", "SAVE_DATA")));

                setReplyButtonsOnMessage(replyButtons, chatId, messageId,
                    "Введіть номер телефону 📱\nПриклад: +380xx-xxx-xx-xx");

                userSession.setRegisterStep(2);
                db.editUserSessionFromDb(userSession);
              }
            }
            case 2 -> {

              if (isValidPhoneNumber(lastTextHistoryMessage.getText(), chatId, update)
                  && !lastTextHistoryMessage.getFrom().getIsBot()) {

                user.setUserPhone(lastTextHistoryMessage.getText());

                deleteMessages(chatId, 1, update);

                ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                    List.of(
                        new ReplyButton("Зберегти", "SAVE_DATA"),
                        new ReplyButton("Перезаповнити дані", "RESTART")));

                setReplyButtonsOnMessage(replyButtons, chatId, messageId,
                    String.format("Перевірте дані на коректність:\n👉 Ім'я: %s\n👉 Номер телефону: %s",
                        user.getUserName(),
                        user.getUserPhone()));

                userSession.setRegisterStep(3);
                db.editUserSessionFromDb(userSession);

              }
            }
            case 3 -> {
              user.setUserId(update.getCallbackQuery().getFrom().getId());

              try {
                ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                    List.of(
                        new ReplyButton("Меню", "MENU"),
                        new ReplyButton("Профіль", "PROFILE")));

                setReplyButtonsOnMessage(replyButtons, chatId, messageId,
                    "Ви були зареєстровані. Дякую за реєстрацію 👌😊");

                db.addUserToDb(user);

                userSession.setRegisterStep(4);
                db.editUserSessionFromDb(userSession);

              } catch (Exception e) {
                e.printStackTrace();
              }
            }
            case 4 -> {

              User editUser = db.getUsersFromDb(updUserId).get(0);

              if (userSession.getEditChoice() == 1) {
                if (inputValidation(chatId, update, updMessage, lastTextHistoryMessage)) {
                  editUser.setUserName(lastTextHistoryMessage.getText());
                  // db.editUserFromDb(editUser);
                  showProfile(chatId, update, 2, updUserId);
                }
              } else if (userSession.getEditChoice() == 2) {
                if (isValidPhoneNumber(lastTextHistoryMessage.getText(), chatId, update)) {
                  editUser.setUserPhone(lastTextHistoryMessage.getText());
                  // db.editUserFromDb(editUser);
                  showProfile(chatId, update, 2, updUserId);
                }
              }
            }
          }
        }
        case "RESTART" -> {
          deleteMessages(chatId, 1, update);
          userReg(chatId, updUserId, messageId);
          userSession.setRegisterStep(1);
          ;
        }

        case "PROFILE" -> {
          showProfile(chatId, update, 0, updUserId);
        }

        case "EDIT_PROFILE" -> {
          deleteMessages(chatId, 1, update);
          var text = "Виберіть що ви хочете змінити";
          var m = sendMessage(chatId, text, null);

          ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
              List.of(
                  new ReplyButton("👤 Ім'я", "EDIT_NAME"),
                  new ReplyButton("📲 Нормер телефону", "EDIT_PHONE")));

          setReplyButtonsOnMessage(replyButtons, chatId, m.getMessageId(), text);
        }

        case "EDIT_NAME" -> {

          ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
              Arrays.asList(new ReplyButton("Зберегти", "SAVE_DATA")));

          setReplyButtonsOnMessage(replyButtons, chatId, messageId, "Введіть ім'я:");
          userSession.setEditChoice(1);
          db.editUserSessionFromDb(userSession);
        }

        case "EDIT_PHONE" -> {
          ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
              Arrays.asList(new ReplyButton("Зберегти", "SAVE_DATA")));

          setReplyButtonsOnMessage(replyButtons, chatId, messageId, "Введіть номер телефону:");
          userSession.setEditChoice(2);
          db.editUserSessionFromDb(userSession);
        }

        case "MENU" -> {
          if (db.getUsersFromDb(updUserId) != null)
            showMenu(chatId, updUserId);
          else {
            var m = sendMessage(chatId, "❗", null);

            ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                Arrays.asList(new ReplyButton("Реєстрація", "REG")));

            setReplyButtonsOnMessage(replyButtons, chatId, m.getMessageId(), "❗Будь ласка пройдіть реєстрацію❗");

          }
        }

        case "ADD_ITEM_COUNT" -> {
          var selectedItems = db.getUserSessionFromDb(updUserId).get(0).getSelectedItem();
          Long itemId = (long) digitFinder(updMessage.getText().split("\\n")[0]);
          var count = 0;

          for (ShopMenu i : selectedItems) {
            if (i.getItem().getId().compareTo(itemId) == 0) {
              count = i.getAmount() + 1;
              i.setAmount(count);
              userSession.setSelectedItem(selectedItems);
              db.editUserSessionFromDb(userSession);
              break;
            }
          }

          ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
              Arrays.asList(
                  new ReplyButton("+1", "ADD_ITEM_COUNT"),
                  new ReplyButton(String.valueOf(count), "COUNTER"),
                  new ReplyButton("-1", "REMOVE_ITEM_COUNT")));

          setReplyButtonsOnMessage(replyButtons, chatId, updMessage.getMessageId(),
              updMessage.getText());
        }

        case "REMOVE_ITEM_COUNT" -> {
          var selectedItems = db.getUserSessionFromDb(updUserId).get(0).getSelectedItem();
          Long itemId = (long) digitFinder(updMessage.getText().split("\n")[0]);
          var count = 0;

          for (ShopMenu i : selectedItems) {
            if (i.getItem().getId().compareTo(itemId) == 0) {
              count = i.getAmount() - 1;
              if (count < 0)
                count = 0;
              i.setAmount(count);
              userSession.setSelectedItem(selectedItems);
              db.editUserSessionFromDb(userSession);
              break;
            }
          }

          ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
              Arrays.asList(
                  new ReplyButton("+1", "ADD_ITEM_COUNT"),
                  new ReplyButton(String.valueOf(count), "COUNTER"),
                  new ReplyButton("-1", "REMOVE_ITEM_COUNT")));

          setReplyButtonsOnMessage(replyButtons, chatId, updMessage.getMessageId(),
              updMessage.getText());
        }

        case "COMPLETE_ORDER" -> {
          Order newOrder = db.getUserSessionFromDb(updUserId).get(0).getNewOrder();
          StringBuilder sb = new StringBuilder();
          newOrder.clear();

          ArrayList<UserSession> uSession = db.getUserSessionFromDb(updUserId);

          for (ShopMenu x : uSession.get(0).getSelectedItem()) {
            if (x.getAmount() != 0) {
              newOrder.addOrderItems(x);
            }
          }

          userSession.setNewOrder(newOrder);
          userSession.getNewOrder().setPrice(newOrder.getPrice());
          db.editUserSessionFromDb(userSession);

          sb.append("Отже ваше замовлення:\n");

          for (var x : newOrder.getOrderItems()) {

            sb.append(String.format("\n%s:    %s шт.\n%s грн\n\n",
                x.getItem().getItemName(),
                String.valueOf(x.getAmount()),
                String.valueOf(x.getItem().getItemPrice())));
          }

          sb.append("До оплати: " + newOrder.getPrice() + " грн");

          var m = sendMessage(chatId, sb.toString(), null);

          ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
              Arrays.asList(
                  new ReplyButton("Підтвердити", "SEND_ORDER"),
                  new ReplyButton("Вибрати ще раз", "ORDER_RESTART")));

          setReplyButtonsOnMessage(replyButtons, chatId, m.getMessageId(), sb.toString());
        }

        case "SEND_ORDER" -> {
          Order newOrder = db.getUserSessionFromDb(updUserId).get(0).getNewOrder();

          try {
            newOrder.setUserId(updUserId);

            db.addOrderToDb(newOrder);

            sendOrderToChannel(newOrder);

            var m = sendMessage(chatId,
                "Замовлення було успішно відправлено 👌.\nВ профілі ви можете побачити свої замовлення 👉📱",
                null);

            ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                Arrays.asList(
                    new ReplyButton("Повернутись до профілю", "PROFILE"),
                    new ReplyButton("Повернутись до меню", "MENU")));

            for (ShopMenu i : userSession.getSelectedItem()) {
              i.setAmount(0);
            }
            newOrder.clear();

            userSession.setNewOrder(newOrder);
            db.editUserSessionFromDb(userSession);
            setReplyButtonsOnMessage(replyButtons, chatId, m.getMessageId(), m.getText());

            User user = db.getUsersFromDb(updUserId).get(0);

            user.setNumberOfOrders(1);

            // db.editUserFromDb(user);

          } catch (Exception e) {
            log.info(e.getMessage(), e);
          }
        }

        case "ORDER_RESTART" -> {
          deleteMessages(chatId, 1, update);
        }

        case "CANCEL_ORDER" -> {
          delteMessageById(chatId, messageId);
          long orderId = Long.parseLong(updMessage.getText().split("\n")[0].split(" ")[1]);
          Message m = db.getOrderById(orderId).get(0);
          DeleteMessage d = new DeleteMessage();
          d.setChatId(m.getChatId());
          d.setMessageId(m.getMessageId());

          try {
            this.execute(d);
          } catch (Exception e) {
          }

          db.removeMessagesFromDbBotChannel(m.getMessageId());
          db.removeOrderFromDb(orderId);

          User user = db.getUsersFromDb(updUserId).get(0);

          user.setNumberOfOrders(-1);

          // db.editUserFromDb(user);

        }
        case "APPROVE_ORDER" -> {
          if (chatId == botChannel) {
            long orderId = Long.parseLong(updMessage.getText().split("\n")[0].split(" ")[1]);
            // db.editOrderStatus(orderId, 0, "");

            ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                Arrays.asList(
                    new ReplyButton("✅", "APPROVE_ORDER"),
                    new ReplyButton("🟢", "ORDER_STATUS"),
                    new ReplyButton("❌", "DECLINE_ORDER")));

            setReplyButtonsOnMessage(replyButtons, chatId, messageId, updMessage.getText());

            db.editMessagesFromDbBotChannel(updMessage);

          }
        }

        case "DECLINE_ORDER" -> {
          if (chatId == botChannel) {
            long orderId = Long.parseLong(updMessage.getText().split("\n")[0].split(" ")[1]);
            var mArr = db.getMessagesFromDbBotChannel(0);
            Message m = mArr.get(mArr.size() - 1);
            var reason = "";

            if (!m.getFrom().getIsBot()) {
              reason = m.getText();
              deleteMessages(botChannel, 1, update);
              db.removeMessagesFromDbBotChannel(m.getMessageId());
            }

            // db.editOrderStatus(orderId, 1, reason);

            ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
                Arrays.asList(
                    new ReplyButton("✅", "APPROVE_ORDER"),
                    new ReplyButton("🔴", "ORDER_STATUS"),
                    new ReplyButton("❌", "DECLINE_ORDER")));

            setReplyButtonsOnMessage(replyButtons, chatId, messageId, updMessage.getText());

            db.editMessagesFromDbBotChannel(updMessage);

          }
        }

        case "UPDATE_PROF_MENU" -> {

          User user = db.getUsersFromDb(updUserId).get(0);

          deleteMessages(chatId, user.getNumberOfOrders(), update);

          showProfMenu(chatId, updUserId);

        }

      }

    }

  }

  // METHODS

  @Override
  public String getBotUsername() {
    return config.getBotName();
  }

  private void showProfile(long chatId, Update update, int deleteCount, long userId) {
    deleteMessages(chatId, deleteCount, update);

    User user = db.getUsersFromDb(userId).get(0);

    String profileData = String.format(
        "Доброго дня, %s!\n\n\n  👤 Ім'я: %s\n\n  📲 Нормер телефону: %s\n\n\nВаші замовлення 👇:\n",
        user.getUserName(), user.getUserName(), user.getUserPhone());

    var profMessage = sendMessage(chatId, profileData, null);

    ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
        List.of(
            new ReplyButton("Змінити профіль", "EDIT_PROFILE"),
            new ReplyButton("Оновити меню замовлень", "UPDATE_PROF_MENU"),
            new ReplyButton("Подивитись меню", "MENU")));

    setReplyButtonsOnMessage(replyButtons, chatId, profMessage.getMessageId(), profileData);

    showProfMenu(chatId, userId);

  }

  private void showProfMenu(long chatId, long userId) {
    User user = db.getUsersFromDb(userId).get(0);
    var userOrders = db.getUserOrdersFromDb(user.getUserId());

    if (userOrders == null) {
      sendMessage(chatId, "(Замовлень немає)", null);
    } else {

      ArrayList<ReplyButton> replyButtons1 = new ArrayList<ReplyButton>(
          Arrays.asList(
              new ReplyButton("Відмінити", "CANCEL_ORDER")));

      for (Order o : userOrders) {
        StringBuilder str = new StringBuilder();
        str.append(String.format("\n№ %s\n\n", o.getOrderId()));

        for (ShopMenu i : o.getOrderItems()) {
          var text = String.format("%s:    %s шт.\n%s грн\n\n",
              i.getItem().getItemName(),
              String.valueOf(i.getAmount()),
              String.valueOf(i.getItem().getItemPrice()),
              o.getStatus());

          str.append(text);
        }

        str.append(String.format("Статус: %s\n", o.getStatus()));

        if (!o.getReason().isEmpty()) {
          str.append(String.format("Причина відмови: %s", o.getReason()));
        }

        var m = sendMessage(chatId, str.toString(), null);
        setReplyButtonsOnMessage(replyButtons1, chatId, m.getMessageId(), str.toString());
      }
    }
  }

  private void showMenu(long chatId, long userId) {
    ArrayList<UserSession> sessionUser = db.getUserSessionFromDb(userId);

    sendMessage(chatId, "Меню", null);

    ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
        Arrays.asList(
            new ReplyButton("+1", "ADD_ITEM_COUNT"),
            new ReplyButton("0", "COUNTER"),
            new ReplyButton("-1", "REMOVE_ITEM_COUNT")));

    for (ShopMenu m : sessionUser.get(0).getSelectedItem()) {
      String text = String.format("Номер: %s\nНазва: %s(%s)\nЦіна: %s грн",
          m.getItem().getId(),
          m.getItem().getItemName(),
          m.getItem().getItemDescription(),
          m.getItem().getItemPrice());

      replyButtons.set(1, new ReplyButton(String.valueOf(m.getAmount()), "COUNTER"));

      var message = sendMessage(chatId, text, null);
      setReplyButtonsOnMessage(replyButtons, chatId, message.getMessageId(), text);
    }

    ArrayList<ReplyButton> replyButtons2 = new ArrayList<ReplyButton>(
        Arrays.asList(
            new ReplyButton("Підтвердити замовлення", "COMPLETE_ORDER")));

    var m = sendMessage(chatId, "👇", null);
    setReplyButtonsOnMessage(replyButtons2, chatId, m.getMessageId(), "👇");

  }

  private void sendOrderToChannel(Order sendedOrder) {
    Order userOrder = sendedOrder;

    ArrayList<ReplyButton> replyButtons1 = new ArrayList<ReplyButton>(
        Arrays.asList(
            new ReplyButton("✅", "APPROVE_ORDER"),
            new ReplyButton("🔃", "ORDER_STATUS"),
            new ReplyButton("❌", "DECLINE_ORDER")));

    var user = db.getUsersFromDb(userOrder.getUserId()).get(0);

    StringBuilder str = new StringBuilder();
    str.append(String.format("\n№ %s | Ім'я: %s( %s )\n\n", userOrder.getOrderId(),
        user.getUserName(), user.getUserPhone()));

    for (ShopMenu i : userOrder.getOrderItems()) {
      var text = String.format("%s:    %s шт.\n%s грн\n\n",
          i.getItem().getItemName(),
          String.valueOf(i.getAmount()),
          String.valueOf(i.getItem().getItemPrice()));

      str.append(text);
    }

    str.append(String.format("Загальа ціна: %s", String.valueOf(userOrder.getPrice())));

    var m = sendMessage(botChannel, str.toString(), null);
    setReplyButtonsOnMessage(replyButtons1, botChannel, m.getMessageId(), str.toString());

    db.addMessagesToDbBotChannel(m);
  }

  private Boolean inputValidation(long chatId, Update update, Message updMessage, Message lastTextHistoryMessage) {
    if (String.valueOf(updMessage.getMessageId())
        .equals(String.valueOf(lastTextHistoryMessage.getMessageId()))) {
      sendMessage(chatId, "Невірно задано поле", null);

      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      deleteMessages(chatId, 1, update);

      return false;

    } else {
      return true;
    }
  }

  private Message sendMessage(long chatId, String senderText, String[] data) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(senderText);

    if (data != null) {
      ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
      List<KeyboardRow> keyboardRows = new ArrayList<>();
      KeyboardRow row = new KeyboardRow();

      int count = 0;
      for (var p : data) {
        if (count == 3) {
          count = 0;
          keyboardRows.add(row);
          row = new KeyboardRow();
        }
        row.add(p);
      }
      keyboardRows.add(row);
      replyKeyboardMarkup.setKeyboard(keyboardRows);

      message.setReplyMarkup(replyKeyboardMarkup);
    }

    try {
      var responce = this.execute(message);
      addHistoryMessage(responce);

      return responce;
    } catch (TelegramApiException e) {
      log.error(e.getMessage());
      return null;
    }
  }

  private void deleteMessages(long chatId, int deleteCount, Update update) {
    DeleteMessage deleteMessage = new DeleteMessage();
    deleteMessage.setChatId(chatId);

    Message lastMessage = textMessageHistory.get(textMessageHistory.size() - 1);

    try {
      for (int i = lastMessage.getMessageId(); i > lastMessage.getMessageId() - deleteCount; i--) {
        deleteMessage.setMessageId(i);

        removeHistoryMessage(deleteMessage.getMessageId());

        // log.info(textMessageHistory.size(), null);
        this.execute(deleteMessage);
      }
    } catch (TelegramApiException e) {
      log.error(e.getMessage());
    }
  }

  private void delteMessageById(long chatId, int messageId) {
    DeleteMessage deleteMessage = new DeleteMessage();
    deleteMessage.setChatId(chatId);
    deleteMessage.setMessageId(messageId);
    try {
      this.execute(deleteMessage);
    } catch (TelegramApiException e) {
      log.error(e.getMessage());
    }
  }

  private void setReplyButtonsOnMessage(
      ArrayList<ReplyButton> replyButtons,
      long chatId,
      int messageId,
      String title) {
    InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
    List<InlineKeyboardButton> row = new ArrayList<>();

    for (var replyButton : replyButtons) {

      InlineKeyboardButton button = new InlineKeyboardButton();

      button.setText(replyButton.getCallBackText());
      button.setCallbackData(replyButton.getCallBackData());

      row.add(button);
    }

    keyboard.setKeyboard(Collections.singletonList(row));

    EditMessageText messageWithKeyboard = new EditMessageText();

    messageWithKeyboard.setChatId(chatId);
    messageWithKeyboard.setMessageId(messageId);
    messageWithKeyboard.setText(title);
    messageWithKeyboard.setReplyMarkup(keyboard);
    getHistoryMessage(messageId).setReplyMarkup(keyboard);

    try {
      execute(messageWithKeyboard);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }

  }

  private void addHistoryMessage(Message messageUpdate) {
    textMessageHistory.add(messageUpdate);
  }

  private Message getHistoryMessage(int messageId) {

    for (int i = textMessageHistory.size() - 1; i > 0; i--) {
      if (textMessageHistory.get(i).getMessageId().compareTo(messageId) == 0) {
        return textMessageHistory.get(i);
      }
    }

    return null;

  }

  private void removeHistoryMessage(long deleteMessageId) {
    for (int x = textMessageHistory.size() - 1; x > 0; x--) {

      if (String.valueOf(deleteMessageId)
          .equals(String.valueOf(textMessageHistory.get(x).getMessageId()))) {
        textMessageHistory.remove(x);
        break;
      }
    }
  }

  private void userReg(long chatId, long updUserId, int messageId) {

    if (db.getUsersFromDb(updUserId) != null)
      if (db.getUsersFromDb(updUserId).get(0).getUserId().compareTo(updUserId) == 0) {
        log.info("User already exist");
        ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
            List.of(
                new ReplyButton("Профіль", "PROFILE")));

        var m = sendMessage(chatId, "Ви вже були зареєстрованні 🤔", null);

        setReplyButtonsOnMessage(replyButtons, chatId, m.getMessageId(),
            "Ви вже були зареєстрованні 🤔");

        return;
      }
    String messageText = "Будь ласка вкажіть своє ім'я 😊";

    var messegeNew = sendMessage(chatId, messageText, null);

    ArrayList<ReplyButton> replyButtons = new ArrayList<ReplyButton>(
        List.of(
            new ReplyButton("Зберегти", "SAVE_DATA")));

    setReplyButtonsOnMessage(replyButtons, chatId, messegeNew.getMessageId(), messageText);

  }

  private Boolean isValidPhoneNumber(String phoneNumber, long chatId, Update update) {
    if (phoneNumber == null) {
      sendMessage(chatId, "Невірно задано поле", null);

      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      deleteMessages(chatId, 2, update);

      return false;

    }

    String PHONE_REGEX = "^\\+(?:[0-9] ?){6,14}[0-9]$";

    Pattern pattern = Pattern.compile(PHONE_REGEX);
    Matcher matcher = pattern.matcher(phoneNumber);

    if (matcher.matches()) {
      return true;
    } else {
      sendMessage(chatId, "Невірно задано поле", null);

      try {
        Thread.sleep(2000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      deleteMessages(chatId, 2, update);

      return false;
    }
  }

  private Integer digitFinder(String str) {
    Pattern pattern = Pattern.compile("\\d+");
    Matcher matcher = pattern.matcher(str);
    if (matcher.find()) {
      int num = Integer.parseInt(matcher.group());
      return num;
    }

    return 0;
  }
}
