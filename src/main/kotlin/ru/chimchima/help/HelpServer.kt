package ru.chimchima.help

import java.net.ServerSocket
import java.util.concurrent.Executors

const val USAGE = """Основные команды:
    !play[count] <track name / track url / playlist url> — Присоединяется к каналу и воспроизводит 1 (или count) треков/плейлистов с указанным названием (поиск по YouTube) / по указанной ссылке.
    !stop, !стоп — Прекращает воспроизведение очереди и покидает канал.
    !skip [count] — Пропускает следующие count композиций (включая текущую), по умолчанию count=1.
    !next[count] [track name / track url / playlist url] — Ставит указанный трек следующим (в начало очереди).
    !queue — Выводит текущую очередь композиций.
    !current — Выводит название текущей композиции и прогресс проигрывания трека.
    !status — !current + !status.

    !mute - Бот больше не пингует вас на каждое сообщение.
    !repeat [on/off] — Устанавливает режим повторения трека на переданный (выводит текущий при отсутствии аргументов).
    !pause - Ставит текущий трек на паузу.
    !resume - Снимает текущий трек с паузы.
    !stay [on/off] - Устанавливает режим "остаться в войсе после конца треков" (выводит текущий при отсутствии аргументов).
    !join - Джоинится в войс и не выходит (ставит `!stay on`), полезно для TTS.

    !rep/!again/!yadaun - Повторяет последнюю команду вида !play или !next пользователя.
    !seek/!ff [seconds] - Проматывает текущий трек на seconds (или 10) секунд вперед (назад при отрицательном аргументе).
    !back - Начинает текущий трек заново.
    !shuffle — Перемешать очередь композиций.
    !reverse — Перевернуть очередь композиций.
    !clear — Очистить очередь композиций.
    !help — Выводит данное сообщение.

    !say/!tts <text> - Произносит текст рандомным голосом вне очереди.
    !jane <text> - Произносит текст голосом злой Жени вне очереди.

    !restart - Перезагрузить бота (займет секунд 15).

Приколы:
    !snus [count] [next] - Окей, мы часто кидаем снюс.
    !pauk [count] [next] - В этой банке никого кроме паука...
    !sasha [count] [next] - Саша лох.
    !discord [count] [next] - Мама это дискорд.
    !sperma [count] [next] - Сперма в рот летит как будто самолет.
    !taxi [count] [next] - ДИСС НА ТИГРАНА.
    !diss [count] [next] - ДИСС НА ТИГРАНА [REMASTERED].
    !kotiki [count] [next] - Маслорий на бите.
    !кошечки [count] [next] - Кошечки хорошие лишнего не скажут.
    !satana [count] [next] - Со мною воюет сатана.

    !cocyxa [count] [next] - Предсмертный выстрел.
    !cocyxa2 [count] [next] - Предсмертный выстрел.
    !raketa [count] [next] - Ракета пошла.
    !val [count] [next] - Val - low grades.
    !valera [count] [next] - tupa valera.
    !val0 [count] [next] - tupa valera + low grades.

Плейлисты:
    !<playlist> [-s/--shuffle/--shuffled] [-a/--all/--full] [count] [limit]L
    Добавляет limit (или все) избранных треков из плейлиста, повторенных count (или 1) раз (--all для всех треков, --shuffled для случайного порядка треков).

    Пример:
    !pirat -as 3 10L
    Добавит из всех (а не только избранных) первых 10 треков плейлиста pirat в случайном порядке, повторенные 3 раза.

    !ruslan - Добавляет плейлист для игры в доту aka `https://www.youtube.com/playlist?list=PLpXSZSgpFNH-GPpNp9S_76hJBVWxUXWIR`
    !vlad - Добавляет плейлист для влада aka `https://www.youtube.com/playlist?list=PLpXSZSgpFNH-Tljl-1zF9B-JMMTxCTJlX`
    !fallout - Добавляет плейлист Fallout 3: Galaxy News Radio aka `https://www.youtube.com/playlist?list=PL63B26E837C45A200`

    !pirat - Избранные треки сереги бандита.
    !cover - Избранные каверы сереги чимичанги.

    !antihype - Три микстейпа ниже вместе.
    !nemimohype, !nemimohypa, !nemimo - #НЕМИМОХАЙПА (Mixtape) (2015)
    !hypetrain - HYPE TRAIN (Mixtape) (2016)
    !antihypetrain, !antipenis - ANTIHYPETRAIN (2021)

    !zamay - Два альбома ниже вместе.
    !mrgaslight, !gaslight - Mr. Gaslight (2022)
    !lusthero3, !lusthero, !lust - LUST HERO 3 (2022)

    !slavakpss, !slava, !kpss - Три релиза ниже вместе.
    !russianfield, !pole - Русское поле (Бутер Бродский) (2016)
    !bootlegvolume1, !bootleg - Bootleg Vol.1 (2017)
    !angelstrue, !angel, !true - Ангельское True (Mixtape) (2022)
    
    !krovostok, !krov - Восемь релизов ниже вместе.
    !bloodriver, !blood, !reka, !rekakrovi - Река крови (2004)
    !skvoznoe, !skvoz - Сквозное (2006)
    !dumbbell, !dumb, !gantelya - Гантеля (2008)
    !studen - Студень (2012)
    !lombard - Ломбард (2015)
    !cheburashka, !cheba, !chb - ЧБ (2018)
    !nauka, !science - Наука (2021)
    !krovonew, !lenin - Бабочки (2022) & Ленин (2023)

"""

class HelpServer(private val port: Int) {
    private val serverSocketService = Executors.newSingleThreadExecutor()

    fun start() {
        serverSocketService.submit {
            val serverSocket = ServerSocket(port)
            while (true) {
                val socket = serverSocket.accept()
                val output = socket.getOutputStream()
                // voprosy?
                output.write("HTTP/1.1 200 OK\r\nContent-Length: ${USAGE.toByteArray().size}\r\nContent-Type: text/plain;charset=UTF-8\r\n\r\n$USAGE".toByteArray())
                output.close()
                socket.close()
            }
        }
    }
}
