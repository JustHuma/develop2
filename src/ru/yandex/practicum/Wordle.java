package ru.yandex.practicum;

import ru.yandex.practicum.InvalidWordLengthException;
import ru.yandex.practicum.WordNotInDictionaryException;
import ru.yandex.practicum.WordNotFoundException;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Wordle {

    private static PrintWriter log;
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            // Создаём лог-файл
            log = new PrintWriter(new FileWriter("wordle.log", StandardCharsets.UTF_8));

            // Загружаем словарь
            WordleDictionaryLoader loader = new WordleDictionaryLoader();
            WordleDictionary dictionary = loader.loadDictionary("words.txt");
            logMessage("Словарь загружен. Слов в словаре: " + dictionary.size());

            // Создаём игру
            WordleGame game = new WordleGame(dictionary);

            // Запускаем игровой цикл
            playGame(game);

        } catch (WordNotFoundException e) {
            logError(e.getMessage(), e);
            System.err.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logError("Неожиданная ошибка", e);
            System.err.println("Произошла ошибка. Подробности в логе.");
        } finally {
            if (log != null) {
                log.close();
            }
        }
    }

    private static void playGame(WordleGame game) {
        System.out.println("Добро пожаловать в Wordle!");
        System.out.println("Угадайте слово из 5 букв. У вас 6 попыток.");
        System.out.println("Подсказки: + (буква на месте), ^ (буква есть в слове), - (буквы нет)");
        System.out.println("Для подсказки слова нажмите Enter без ввода.\n");

        while (!game.isGameOver()) {
            System.out.println("Осталось попыток: " + game.getAttemptsLeft());
            System.out.print("Введите слово: ");
            String input = scanner.nextLine().trim().toLowerCase().replace('ё', 'е');

            // Обработка пустого ввода (запрос подсказки)
            if (input.isEmpty()) {
                String hintWord = game.getHintWord();
                if (hintWord == null) {
                    System.out.println("Нет доступных слов для подсказки.");
                } else {
                    System.out.println("Подсказка: " + hintWord);
                }
                continue;
            }

            try {
                String hint = game.makeGuess(input);
                System.out.println(input);
                System.out.println(hint);

                if (game.isWordGuessed()) {
                    System.out.println("Поздравляю! Вы угадали слово: " + game.getAnswer());
                    logMessage("Игра завершена победой. Загаданное слово: " + game.getAnswer());
                    return;
                }

            } catch (InvalidWordLengthException e) {
                System.out.println(e.getMessage());
                logMessage("Ошибка длины слова: " + input);
            } catch (WordNotInDictionaryException e) {
                System.out.println(e.getMessage());
                logMessage("Слово не в словаре: " + input);
            } catch (Exception e) {
                logError("Ошибка при обработке хода", e);
                System.out.println("Произошла ошибка. Попробуйте снова.");
            }
        }

        // Игра закончилась без победы
        System.out.println("Вы проиграли. Загаданное слово: " + game.getAnswer());
        logMessage("Игра завершена поражением. Загаданное слово: " + game.getAnswer());
    }

    private static void logMessage(String message) {
        if (log != null) {
            log.println(message);
            log.flush();
        }
    }

    private static void logError(String message, Throwable e) {
        if (log != null) {
            log.println("ОШИБКА: " + message);
            e.printStackTrace(log);
            log.flush();
        }
    }
}