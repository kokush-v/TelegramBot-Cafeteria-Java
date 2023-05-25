package uzhnu.bot;

import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import uzhnu.bot.configuration.BotConfig;
import uzhnu.bot.service.TelegramBot;

public class Main {
    public static void main(String[] args) {
        Logger log = Logger.getLogger(Main.class);
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TelegramBot(new BotConfig()));

            FileInputStream serviceAccount = new FileInputStream("src/main/java/uzhnu/bot/database/tokenFirebase.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://cafeteriabotdatabase-default-rtdb.europe-west1.firebasedatabase.app")
                    .build();

            FirebaseApp.initializeApp(options);

            DatabaseReference database = FirebaseDatabase.getInstance().getReference();

            database.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String key = childSnapshot.getKey();
                            Object value = childSnapshot.getValue();

                            System.out.println("Key: " + key + ", Value: " + value);
                        }
                    } else {
                        System.out.println("No data found");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("Error: " + databaseError.getMessage());
                }
            });
        } catch (Exception e) {
            log.info(e.getMessage());
        }

    }
}