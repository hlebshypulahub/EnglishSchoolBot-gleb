package org.ciaobyTestBot.states.statesservice;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.ciaobyTestBot.bot.ServiceCallback;
import org.ciaobyTestBot.dto.UserInfoDTO;
import org.ciaobyTestBot.enums.States;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@Getter
public abstract class AbstractState implements State {
    private final ServiceCallback serviceCallback;
    private final States state;

    public void sendText(Long who, String what){
        var sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();

        serviceCallback.execute(sm, null, null);
    }

    public void sendStartButton(UserInfoDTO user) {
        var sm = SendMessage.builder()
                .chatId(user.getChatId().toString())
                .text("Ну что же, приступим к тесту. Сейчас Вам нужно будет ответить на 30 вопросов.\uD83E\uDDD0 " +
                        "Ограничений по времени нет.\n" +
                        "\nНажмите кнопку \"Начать тестирование\", когда будете готовы.").build();

        var keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        var row = new KeyboardRow();
        row.add(new KeyboardButton("Начать тестирование\uD83C\uDFC1"));

        keyboardRows.add(row);

        keyboard.setKeyboard(keyboardRows);
        sm.setReplyMarkup(keyboard);

        serviceCallback.execute(sm, null, null);
    }
}