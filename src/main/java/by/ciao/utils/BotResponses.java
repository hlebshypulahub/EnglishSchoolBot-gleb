package by.ciao.utils;

import by.ciao.englishtest.EnglishTestLoader;
import by.ciao.englishtest.Question;
import by.ciao.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class BotResponses {

    @Value("test_duration")
    private String testDuration;
    private EnglishTestLoader englishTestLoader;

    @Autowired
    public void setEnglishTestLoader(EnglishTestLoader englishTest) {
        this.englishTestLoader = englishTest;
    }

    public String testFinished(User user) {
        return """
                Вы ответили верно на %d вопросов.
                Ваш уровень английского %s.
                Вы молодец, Вам осталось совсем немного, и скоро мы свяжемся с Вами для прохождения устного тестирования\uD83D\uDE0A
                """.formatted(user.getTestState().getCorrectAnswers(), user.getTestState().getResult());
    }

    public String dataForAdmin(final User user) {
        return """
                Имя и Фамилия: %s
                Номер телефона: %s
                Ник в телеграмм: @%s
                Откуда узнали: %s
                Отвечено верно на: %d вопросов.
                Уровень английского: %s
                """.formatted(user.getFullName(), user.getPhone(), user.getUsername(), user.getReferral(), user.getTestState().getCorrectAnswers(), user.getTestState().getLvl());
    }

    public String start() {
        return """
                Привет!\uD83D\uDC4B

                Сейчас мы проверим Ваши знания английского\uD83D\uDD25
                Вы пройдете тест, который состоит из 30 вопросов.
                После этого, вы сможете проходить его, когда захотите.\uD83D\uDE0A

                Но для начала давайте познакомимся\uD83D\uDE09

                Введите, пожалуйста, Ваше имя и фамилию
                """;
    }

    public String infoSent() {
        return """
                Извините, не роспознал Вашу команду.
                Если хотите пройти тест заново - нажмите кнопку "Начать тестирование"\uD83E\uDD17
                """;
    }

    public String phoneFormatWarning() {
        return "Неверный формат номера. Попробуйте, пожалуйста, ещё раз";
    }

    public String replyKeyboardRemoved() {
        return "Спасибо! Можем продолжать\uD83D\uDE0A";
    }

    public String askReferral() {
        return """
                Откуда вы о нас узнали?
                Выберете один из вариантов ниже или же впишите свой
                """;
    }

    public String askForPhone() {
        return """
                Чтобы нам было удобнее с Вами связаться для согласования второго этапа (устного тестирования), укажите, пожалуйста, Ваш номер телефона\uD83D\uDE0A

                Вы можете нажать на кнопку "Поделиться номером" и указать номер, к которому привязан Ваш телеграм, либо же указать другой, вписав его в формате +1234567890.\uD83D\uDE0A
                """;
    }

    public String startTest() {
        return """
                Ну что же, приступим к тесту. Сейчас Вам нужно будет ответить на 30 вопросов за %s минут.\uD83E\uDDD0

                Нажмите кнопку "Начать тестирование", когда будете готовы.
                """.formatted(testDuration);
    }

    public String getQuestion(User user) {
        return """
                Времени осталось: %s

                %d. %s
                """.formatted(user.getTestState().countTime(), user.getTestState().getCurrentQuestion().numberOfQuestion(), user.getTestState().getCurrentQuestion().question());
    }

    public List<String> referralOptions() {
        return new ArrayList<>(Arrays.asList("Google", "Яндекс", "Instagram/Facebook/VK/Tik-Tok", "Vse-kursy", "Еnguide", "Рекомендация от друзей"));
    }

    public List<String> optionsForAnswers(final User user) {
        return new ArrayList<>(user.getTestState().getCurrentQuestion().answers());
    }

    public String startTestButton() {
        return "Начать тестирование\uD83C\uDFC1";
    }

    public String sharePhoneButton() {
        return "Поделиться номером";
    }

    public String questionAnsweringWarning() {
        return "Отвечать можно только нажав кнопку с одним из вариантов ответа";
    }

    public String noSuchCommand() {
        return "Нет такой команды";
    }

    public String notificationReceivedBy(int counter) {
        return "Уведомление получили " + counter + " пользователей";
    }

    public StringBuilder userAnswers(User user) {
        StringBuilder testWithAnswers = new StringBuilder();

        for (Question question : englishTestLoader.getQuestions()) {
            String userAnswer = user.getTestState().getUserAnswer();
            if (userAnswer == null) {
                break;
            }

            testWithAnswers.append(isAnswerCorrect(question, userAnswer)).append(question.numberOfQuestion())
                    .append(". ").append(question.question())
                    .append("\n(Правильный ответ: <b>").append(question.getCorrectAnswer()).append("</b>)\n")
                    .append(getAnswers(question, userAnswer)).append("\n\n");
        }

        return testWithAnswers;
    }

    private String isAnswerCorrect(Question question, String userAnswer) {
        if (userAnswer.equals(question.getCorrectAnswer())) {
            return "✅ ";
        }
        return "❌ ";
    }

    private StringBuilder getAnswers(Question question, String userAnswer) {
        StringBuilder answers = new StringBuilder();

        for (String answer : question.answers()) {
            if (userAnswer.equals(answer)) {
                answers.append("\t\t<b>").append(answer).append(" (Ваш ответ)</b>\n");
                continue;
            }
            answers.append("\t\t").append(answer).append("\n");
        }

        return answers;
    }

}
