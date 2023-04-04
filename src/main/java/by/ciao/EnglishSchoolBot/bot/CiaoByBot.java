package by.ciao.EnglishSchoolBot.bot;

import by.ciao.EnglishSchoolBot.userinfo.UserInfo;
import by.ciao.EnglishSchoolBot.enums.StateEnum;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CiaoByBot extends TelegramLongPollingBot {
    private final BotService service = new BotService((sm, dm, em) -> {
        Message msg = null;

        try {
            if (dm == null && em == null) msg = execute(sm);
            else if (sm == null && em == null) execute(dm);
            else if (dm == null && sm == null) execute(em);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }

        return msg;
    });

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            var msg = update.getMessage();
            var id = msg.getChatId();

            if(service.getRegisteredUsers().containsKey(id) && service.getRegisteredUsers().get(id).getState() == StateEnum.CHECK_ANSWER) {
                sendText(service.getRegisteredUsers().get(id).getChatId(), "Отвечать можно только нажав кнопку с одним из вариантов ответа");
                return;
            }
            if (!service.getRegisteredUsers().containsKey(id))
                service.getRegisteredUsers().put(id, new UserInfo(id, msg.getFrom().getUserName()));

            parseMessage(msg.getText(), service.getRegisteredUsers().get(id));

        }  else if (update.hasMessage() && update.getMessage().getContact() != null) {
            service.getPhoneHandler(update.getMessage().getContact().getPhoneNumber(),
                                    service.getRegisteredUsers().get(update.getMessage().getChatId()));

        } else if (update.hasCallbackQuery() && service.getRegisteredUsers().containsKey(update.getCallbackQuery().getFrom().getId())) {
            var qry = update.getCallbackQuery();
            parseMessage(qry.getData(), service.getRegisteredUsers().get(qry.getFrom().getId()));

            try {
                execute(AnswerCallbackQuery.builder()
                        .callbackQueryId(qry.getId()).build());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "testForProd_bot";
    }

    @Override
    public String getBotToken() {
        return "6167400176:AAGg4892WjlM0mMxUgdmdoQXID2X9UPw4lo";
    }

    private void parseMessage(String textMsg, UserInfo user) {
        if (textMsg.equals("/start")) {
            user.setState(StateEnum.START);
            user.clearTest();
        } else if (user.getState() == null) {
            sendText(user.getChatId(), "Нет такой команды");
            return;
        }

        if (textMsg.equals("Начать тестирование\uD83C\uDFC1")) {
            user.setState(StateEnum.SEND_QUESTION);
            user.clearTest();
        }

        switch (user.getState()) {
            case START:
                service.startHandler(user);
                break;
            case GET_FULL_NAME:
                service.getFullNameHandler(textMsg, user);
                break;
            case GET_PHONE:
                service.getPhoneHandler(textMsg, user);
                break;
            case GET_REFERRAL:
                service.getReferralHandler(textMsg, user);
                break;
            case START_TEST:
                service.startTestHandler(textMsg, user);
                break;
            case SEND_QUESTION:
                service.sendQuestionHandler(user);
                break;
            case CHECK_ANSWER:
                service.checkAnswerHandler(textMsg, user);
                break;
            case TEST_FINISHED:
                service.testFinishedHandler(user);
                break;
            case INFO_SENT:
                service.infoSentHandler(user);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public void sendText(final Long who, final String what){
        var sm = SendMessage.builder()
                .chatId(who.toString())
                .text(what).build();

        try {
            execute(sm);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}