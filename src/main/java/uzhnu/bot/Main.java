package uzhnu.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import uzhnu.bot.configuration.BotConfig;
import uzhnu.bot.methods.db;
import uzhnu.bot.service.TelegramBot;

public class Main {
    public static void main(String[] args) throws Exception {

        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TelegramBot(new BotConfig()));
        db.init();

    }
}