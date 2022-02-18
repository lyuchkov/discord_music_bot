package ru.lyuchkov.utils;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Queue;

public final class OutputUtils {
    public static String printQueue(Queue<AudioTrack> deque) {
        StringBuilder builder = new StringBuilder();
        String first = "На очереди: \n";
        builder.append(first);
        int c = 1;
        for (AudioTrack a :
                deque) {
            builder.append(c).append(". ").append(a.getInfo().title).append("\n");
            c++;
        }
        if (builder.toString().equals(first))
            return "Очередь пуста";
        else
            return builder.toString();
    }

    public static String printCommands() {
        return
                "Список команд:\n" +
                        "$p- Включить трек(можно вставить и ссылку и название)\n" +
                        "$pt - Включить трек в начало очереди\n" +
                        "$now - Включить трек прямо сейчас\n" +
                        "$mv - Перейти к песне в списке по номеру\n" +
                        "$pause - Пауза\n" +
                        "$resume - Продолжить \n" +
                        "$vol - Настроить громкость\n" +
                        "$seek - Пропустить несколько секунд\n" +
                        "$q - Очередь треков\n" +
                        "$fs - Пропуск текущего трека\n" +
                        "$rm - Удалить трек на определенной позиции из очереди\n" +
                        "$clr - Очистить очередь\n" +
                        "$np - Что играет сейчас?\n" +
                        "$lp - Проигрывание только текущего трека\n" +
                        "$unlp - Вернуться к очереди\n" +
                        "$lyrics - Слова песни\n" +
                        "$grab - Прислать название и ссылку на трек в лс\n" +
                        "$join- Присоединиться к каналу(можно и через запуск трека)\n" +
                        "$exit- Уйти из канала\n" +
                        "$alive- Проверить работоспособность\n" +
                        "$help - Список команд";
    }

}
