# discord_music_bot



Бот для поиска и прослушивания музыки в чате дискорда. Создавался для использования на собственном дискорд-сервере
, так как не все сторонние решения устраивали в удобстве.

### Описание работы

После того, как бот будет зарегестрирован на сервере дискорда и ему будут выданы все соответсвующие 
права его можно начать использовать просто написав команду `$p` с ссылкой на видеоролик на youtube, 
либо просто написав название, бот автоматически распрасит страницу поиска на youtube 
и включит самый релевантный. Если добавить ещё несколько треков, то они добавятся в очередь. 
Бот реализует все основные комадны, которые необходимы для удобного прослушивания. Полный перечень 
возможностей приведен в списке команд.

### Список команд
| Название | Функция                                                  |
|----------|----------------------------------------------------------|
| $p       | Включить трек(можно вставить либо ссылку, либо название) |
| $pt      | Включить трек в начало очереди                           |
| $now     | Включить трек прямо сейчас                               |
| $mv      | Перейти к песне в списке по номеру                       |
| $pause   | Пауза                                                    |
| $resume  | Продолжить                                               |
| $vol     | Настройка громкости                                      |
| $seek    | Пропустить несколько секунд                              |
| $q       | Очередь треков                                           |
| $fs      | Пропуск текущего трека                                   |
| $rm      | Удалить трек на определенной позиции из очереди          |
| $clr     | Очистить очередь                                         |
| $np      | Что играет сейчас?                                       |
| $lp      | Проигрывание только текущего трека                       |
| $unlp    | Вернуться к очереди                                      |
| $lyrics  | Слова песни                                              |
| $grab    | Прислать название и ссылку на трек в личном сообщении    |
| $join    | Присоединиться к голосовому каналу                       |
| $exit    | Уйти из канала                                           |
| $alive   | Проверка работоспособности бота*                         |
| $help    | Список команд                                            |
*Это команда была необходима во время ручного тестирования.


### Описание внутреннего устройства

Бот работает практически также, как и rest контроллер. Он проверяет все сообщения,
которые поступают во всех доступных чатах на сервере, и если была введена команда, которая хранится 
в Map'е со всеми командами, то далее уже производится необходимое действие.  
(Проверка происходит в `CommandHandler.java` в методе `handle`)



### Используемые библиотеки

[Discord4J](https://github.com/Discord4J/Discord4J) - основная библиотека, необходиая для подключения к серверам.

[lavaplayer](https://github.com/sedmelluq/lavaplayer) - библиотека, необходимая для воспроизведения. 
