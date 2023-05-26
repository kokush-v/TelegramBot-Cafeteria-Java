package uzhnu.bot;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import uzhnu.bot.configuration.BotConfig;
import uzhnu.bot.methods.db;
import uzhnu.bot.myclasses.ShopItem;
import uzhnu.bot.myclasses.User;
import uzhnu.bot.service.TelegramBot;

public class Main {
    public static void main(String[] args) throws Exception {

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TelegramBot(new BotConfig()));

        db.init();
        // var promiseArr = ;
        var promiseInt = db.removeOrderFromDb(1001928215363l);

        promiseInt.thenAccept(resp -> {
            if (resp != null) {

                System.out.println(resp);

            }
        }).exceptionally(ex -> {
            System.out.println("Error: " + ex.getMessage());
            return null;
        });

        // promiseArr.thenAccept(resp -> {
        // if (resp != null) {
        // for (var e : resp) {
        // System.out.println(e.getUserId());
        // }
        // }
        // }).exceptionally(ex -> {
        // System.out.println("Error: " + ex.getMessage());
        // return null;
        // });

    }
}