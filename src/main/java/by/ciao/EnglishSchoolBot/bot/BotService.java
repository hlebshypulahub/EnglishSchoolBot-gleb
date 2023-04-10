package by.ciao.EnglishSchoolBot.bot;

import by.ciao.EnglishSchoolBot.enums.StateEnum;
import by.ciao.EnglishSchoolBot.states.*;
import by.ciao.EnglishSchoolBot.states.statesservice.UserHandlerState;
import by.ciao.EnglishSchoolBot.states.statesservice.UserMessageHandlerState;
import by.ciao.EnglishSchoolBot.user.User;
import by.ciao.EnglishSchoolBot.utils.BotResponses;
import by.ciao.EnglishSchoolBot.utils.ExceptionLogger;
import by.ciao.EnglishSchoolBot.utils.ExceptionMessages;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Getter
public class BotService {

    private final Map<Long, User> registeredUsers;
    private final ServiceCallback serviceCallback;

    BotService(final ServiceCallback serviceCallback) {
        this.serviceCallback = serviceCallback;
        this.registeredUsers = new HashMap<>();
    }

    boolean msgHasText(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    boolean isCheckAnswerState(Long id) {
        return getRegisteredUsers().containsKey(id) && getRegisteredUsers().get(id).getState() == StateEnum.CHECK_ANSWER;
    }

    boolean hasContact(Update update) {
        return update.hasMessage() && update.getMessage().getContact() != null;
    }

    boolean hasCallbackAndCorrectState(Update update) {
        return update.hasCallbackQuery() && getRegisteredUsers().containsKey(update.getCallbackQuery().getFrom().getId())
                && (registeredUsers.get(update.getCallbackQuery().getFrom().getId()).getState() == StateEnum.GET_REFERRAL ||
                registeredUsers.get(update.getCallbackQuery().getFrom().getId()).getState() == StateEnum.CHECK_ANSWER);
    }

    void sendWarning(Long id) {
        sendText(getRegisteredUsers().get(id).getChatId(), BotResponses.questionAnsweringWarning());
    }

    void addUserIfAbsent(Long id, Message msg) {
        try {
            getRegisteredUsers().putIfAbsent(id, new User(id, msg.getFrom().getUserName()));
        } catch (Exception e) {
            ExceptionLogger.logException(Level.SEVERE, ExceptionMessages.addUserIfAbsentException(), new RuntimeException(e));
        }
    }

    void addPhone(Update update, Long id) {
        try {
            getPhoneHandler(update.getMessage().getContact().getPhoneNumber(), getRegisteredUsers().get(id));
        } catch (Exception e) {
            ExceptionLogger.logException(Level.SEVERE, ExceptionMessages.addPhoneException(), e);
        }
    }

    void closeQuery(String id) {
        try {
            serviceCallback.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(id).build());
        } catch (TelegramApiException e) {
            ExceptionLogger.logException(Level.SEVERE, ExceptionMessages.closeQueryException(), e);
        }
    }

    boolean startBot(String textMsg, User user) throws Exception {
        if (textMsg.equals("/start")) {
            user.setState(StateEnum.START);
            user.clearTest();
        } else if (user.getState() == StateEnum.NEW_USER) {
            sendText(user.getChatId(), BotResponses.noSuchCommand());
            return true;
        }

        return false;
    }

    void startTest(String textMsg, User user) {
        if (textMsg.equals("Начать тестирование\uD83C\uDFC1") && user.isUserDataCollected()) {
            user.setState(StateEnum.START_TEST);
        }
    }

    void sendText(final Long id, final String textMsg) {
        var sm = SendMessage.builder()
                .chatId(id.toString())
                .text(textMsg).build();

        try {
            serviceCallback.execute(sm);
        } catch (TelegramApiException e) {
            ExceptionLogger.logException(Level.SEVERE, ExceptionMessages.sendTextException(), e);
        }
    }

    void startHandler(final User user) throws Exception {
        UserHandlerState state = new StartState(serviceCallback);
        state.apply(user);
    }

    void getFullNameHandler(final String textMsg, final User user) throws Exception {
        UserMessageHandlerState state = new GetFullNameState(serviceCallback);
        state.apply(textMsg, user);
    }

    void getPhoneHandler(final String textMsg, final User user) throws Exception {
        UserMessageHandlerState state = new GetPhoneState(serviceCallback);
        state.apply(textMsg, user);
    }

    void getReferralHandler(final String textMsg, final User user) throws Exception {
        UserMessageHandlerState state = new GetReferralState(serviceCallback);
        state.apply(textMsg, user);
    }

    void startTestHandler(final String textMsg, final User user) throws Exception {
        UserMessageHandlerState state = new StartTestState(serviceCallback);
        state.apply(textMsg, user);
    }

    void sendQuestionHandler(final User user) throws Exception {
        UserHandlerState state = new SendQuestionState(serviceCallback);
        state.apply(user);
    }

    void checkAnswerHandler(final String answer, final User user) throws Exception {
        UserMessageHandlerState state = new CheckAnswerState(serviceCallback);
        state.apply(answer, user);
    }

    void testFinishedHandler(final User user) throws Exception {
        UserHandlerState state = new TestFinishedState(serviceCallback);
        state.apply(user);
    }

    void infoSentHandler(final User user) throws Exception {
        UserHandlerState state = new InfoSentState(serviceCallback);
        state.apply(user);
    }

    Map<Long, User> getRegisteredUsers() {
        return registeredUsers;
    }
}
