package com.andrew.tgbot.service;


import com.andrew.tgbot.entities.RequestEntity;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class Bot extends TelegramLongPollingBot {
    final private String BOT_TOKEN = "6482858511:AAGiVHHJO1R0qhwXz60zs8Rl7sq-5wPOe6A";
    final private String BOT_NAME = "andrewOldGuapchich_bot";
    private final Map<String, String> keyMap = new HashMap<>();
    private final BotService botService = new BotService();
    private int state = 0;
    private String category = "";

    public Bot() {
        keyMap.put("Случайное фото", "random");
        keyMap.put("Группа случайных фото", "random_group");
        keyMap.put("Фото по категории", "category_photo");
        keyMap.put("Список категорий", "category_list");
        keyMap.put("Выбери категорию", "select_category");
        keyMap.put("/about", "/about");
        keyMap.put("about", "/about");
        keyMap.put("/start", "/start");
        keyMap.put("1 фото", "1");
        keyMap.put("2 фото", "2");
        keyMap.put("3 фото", "3");
        keyMap.put("4 фото", "4");
        keyMap.put("5 фото", "5");
    }
    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }
    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }


    @Override
    public void onUpdateReceived(Update update) {
        try{
            if(update.hasMessage() && update.getMessage().hasText()) {
                Message inMess = update.getMessage();
                String chatId = inMess.getChatId().toString();
                SendMessage outMess = new SendMessage();
                outMess.setChatId(chatId);
                String keyInMessage = keyMap.get(inMess.getText());
                if(state == 9){
                    if(inMess.getText().equals("Случайная категория"))
                        category = "";
                    else
                        category = inMess.getText().trim();
                    outMess.setReplyMarkup(initCountKeyboard());
                    outMess.setText("Введи количество от 1 до 5");
                    state = 10;
                }
                else if (keyInMessage == null) {
                    state = 4;
                    outMess.setText("Неизвестный ввод");
                    outMess.setReplyMarkup(initStartKeyboard());
                }
                else {
                     switch (keyInMessage) {
                        case "/start" -> {
                            outMess.setReplyMarkup(aboutKeyboard());
                            outMess.setText("Привет! Я могу показать тебе различные фотографии. Жми /about и узнаешь, как я работаю!");
                            state = 1;
                        }
                        case "about", "/about" -> {
                            outMess.setReplyMarkup(initStartKeyboard());
                            outMess.setText("Я могу послать тебе фотографии:\nСлучайное фото\nГруппу случайных фото\nФото по категориям");
                            state = 2;
                        }
                        case "random", "1" -> {
                            if (state != 10) {
                                outMess.setReplyMarkup(initStartKeyboard());
                                SendPhoto sendPhoto = getPhoto("");
                                if(sendPhoto != null) {
                                    outMess.setText("Держи фото!");
                                    sendPhoto.setChatId(chatId);
                                    execute(sendPhoto);
                                } else {
                                    outMess.setText("Не нашел фото!");
                                    outMess.setReplyMarkup(initStartKeyboard());
                                }
                            } else {
                                outMess.setReplyMarkup(initStartKeyboard());
                                SendPhoto sendPhoto = getPhoto(category);
                                if(sendPhoto != null) {
                                    outMess.setText("Держи фото!");
                                    sendPhoto.setChatId(chatId);
                                    execute(sendPhoto);
                                } else {
                                    outMess.setText("Не нашел фото!");
                                    outMess.setReplyMarkup(initStartKeyboard());
                                }
                                state = 4;
                            }

                        }
                        case "random_group" -> {
                            outMess.setText("Выбери количество фото ");
                            outMess.setReplyMarkup(initCountKeyboard());
                            state = 4;
                        }

                        case "2", "3", "4", "5" -> {
                            outMess.setReplyMarkup(initStartKeyboard());
                            if (state != 10) {
                                SendMediaGroup sendMediaGroup = getMediaGroup(Integer.parseInt(keyInMessage), "");
                                if(sendMediaGroup != null) {
                                    sendMediaGroup.setChatId(chatId);
                                    execute(sendMediaGroup);
                                    outMess.setText("Вот твои фото!");
                                } else {
                                    outMess.setText("Не нашел фото!");
                                }
                            } else {
                                SendMediaGroup sendMediaGroup = getMediaGroup(Integer.parseInt(keyInMessage), category);
                                assert sendMediaGroup != null;
                                if(!sendMediaGroup.getMedias().isEmpty() && sendMediaGroup.getMedias().size() > 1) {
                                    sendMediaGroup.setChatId(chatId);
                                    execute(sendMediaGroup);
                                    outMess.setText("Вот твои фото!");
                                } else {
                                    outMess.setText("Не нашел фото!");
                                }
                                state = 4;
                            }
                        }

                        case "category_photo" -> {
                            outMess.setReplyMarkup(createRandomKeyboard());
                            outMess.setText("Введи категорию и отправь мне.\n" +
                                    "Предупреждаю, я могу распознать только те категории, которые есть в \"Списке категорий\". " +
                                    "Если хочешь больше, то пиши категорию на английском языке!");
                            state = 9;
                        }

                        case "category_list" ->{
                            outMess.setReplyMarkup(initStartKeyboard());
                            outMess.setText(botService.categoryMessage());
                        }
                    }
                }
                execute(outMess);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private byte[] downloadPhoto(String photoUrl) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(photoUrl);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            return EntityUtils.toByteArray(entity);
        } else {
            throw new IOException("Failed to download photo. Empty response.");
        }
    }

    private SendPhoto getPhoto(String category){
        RequestEntity request = new RequestEntity(category, 1);
        SendPhoto sendPhoto = new SendPhoto();
        try {
            sendPhoto.setPhoto(new InputFile(botService.getPhotoStreams(request).get(0), "photo"));
            return sendPhoto;
        } catch (Exception e){
            return null;
        }
    }

    private SendMediaGroup getMediaGroup(int count, String category) {
        SendMediaGroup sendMediaGroup = new SendMediaGroup();
        List<InputMedia> mediaPhotoList = new ArrayList<>();
        List<InputStream> inputStreams = botService.getPhotoStreams(new RequestEntity(category, count));
        try {
            for (int i = 0; i < inputStreams.size(); i++) {
                InputMediaPhoto mediaPhoto = new InputMediaPhoto();
                mediaPhoto.setMedia(inputStreams.get(i), "photo" + i);
                mediaPhotoList.add(mediaPhoto);
            }
            sendMediaGroup.setMedias(mediaPhotoList);
            return sendMediaGroup;
        } catch (Exception e){
            return null;
        }
    }

    private ReplyKeyboardMarkup createRandomKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        ArrayList<KeyboardRow> row = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton("Случайная категория"));

        row.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(row);
        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup initCountKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true); //подгоняем размер
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        ArrayList<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow[] keyboardRows = new KeyboardRow[3];
        for(int i = 0; i < 3; i++)
            keyboardRows[i] = new KeyboardRow();

        keyboardRows[0].add(new KeyboardButton("1 фото"));
        keyboardRows[0].add(new KeyboardButton("2 фото"));
        keyboardRows[0].add(new KeyboardButton("3 фото"));
        keyboardRows[1].add(new KeyboardButton("4 фото"));
        keyboardRows[1].add(new KeyboardButton("5 фото"));
        /*keyboardRows[1].add(new KeyboardButton("6 фото"));
        keyboardRows[2].add(new KeyboardButton("7 фото"));
        keyboardRows[2].add(new KeyboardButton("8 фото"));
        keyboardRows[2].add(new KeyboardButton("9 фото"));
        keyboardRows[2].add(new KeyboardButton("10 фото"));*/

        rows.add(keyboardRows[0]);
        rows.add(keyboardRows[1]);
        //rows.add(keyboardRows[2]);

        replyKeyboardMarkup.setKeyboard(rows);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup initStartKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        ArrayList<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("Случайное фото"));
        rows.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("Группа случайных фото"));
        rows.add(row2);

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton("Фото по категории"));
        rows.add(row3);

        KeyboardRow row4 = new KeyboardRow();
        row4.add(new KeyboardButton("Список категорий"));
        rows.add(row4);

        replyKeyboardMarkup.setKeyboard(rows);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup aboutKeyboard(){
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        ArrayList<KeyboardRow> row = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRow.add(new KeyboardButton("about"));

        row.add(keyboardRow);
        replyKeyboardMarkup.setKeyboard(row);
        return replyKeyboardMarkup;
    }
}
