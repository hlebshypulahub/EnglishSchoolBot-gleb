package by.ciao.states;

import by.ciao.bot.ServiceCallback;
import by.ciao.enums.StateEnum;
import by.ciao.states.statesservice.AbstractState;
import by.ciao.states.statesservice.UserHandlerState;
import by.ciao.user.User;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TestFinishedState extends AbstractState implements UserHandlerState {

    public TestFinishedState(final ServiceCallback serviceCallback) {
        super(serviceCallback);
    }

    @Override
    public void apply(final User user) throws Exception {
        user.setState(StateEnum.INFO_SENT);
        deleteLastMessage(user);

        sendAnswers(user);
        sendDataToAdmin(user);
        getRestController().updateTestInfoInDB(user);
    }

    private void deleteLastMessage(final User user) throws TelegramApiException {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(user.getChatId().toString());
        deleteMessage.setMessageId(user.getLastMessage().getMessageId());

        getServiceCallback().execute(deleteMessage);
    }

    private void sendDataToAdmin(final User user) throws TelegramApiException {
        getServiceCallback().execute(createMessage(Long.parseLong(getAdminId()), getBotResponses().dataForAdmin(user)));
        getServiceCallback().execute(createMessage(Long.parseLong(getTechAdminId()), getBotResponses().dataForAdmin(user)));
    }

    private void sendAnswers(final User user) throws Exception {
        StringBuilder userAnswers = getBotResponses().userAnswers(user);
        int numAnswers = userAnswers.toString().split("\n\n").length;

        if (numAnswers <= 26) {
            sendText(user.getChatId(), userAnswers.toString());
        } else {
            String[] answers = userAnswers.toString().split("\n\n");

            StringBuilder firstMessage = new StringBuilder();
            StringBuilder secondMessage = new StringBuilder();

            for (int i = 0; i < 15; i++) {
                firstMessage.append(answers[i]).append("\n\n");
            }

            for (int i = 15; i < answers.length; i++) {
                secondMessage.append(answers[i]).append("\n\n");
            }

            sendText(user.getChatId(), firstMessage.toString());
            sendText(user.getChatId(), secondMessage.toString());
        }

        sendText(user.getChatId(), getBotResponses().testFinished(user));
    }

}
