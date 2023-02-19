package ru.chimchima.repository

object SlavaKPSSRepository : SongRepository() {
    override val songs = RussianFieldRepository.songs + BootlegVolume1Repository.songs + AngelsTrueRepository.songs
}

object RussianFieldRepository : SongRepository() {
    override val songs = listOf(
        "Red Widow" to "https://youtu.be/2Ov-IdQUoPY" to true,
        "Burlit (feat. White Spruce)" to "https://youtu.be/46GwOxx4MgY" to false,
        "Вичхаус лобня (feat. Мц Лучник)" to "https://youtu.be/Dnz_6Av_4DA" to false,
        "Русская музыка для тренировок (feat. Мц Лучник)" to "https://youtu.be/JfJ1MBkEZ3Y" to false,
        "Социалочка" to "https://youtu.be/sRfXJESQ6-U" to true,
        "Осадки" to "https://youtu.be/YifdV2Rjwsk" to false,
        "Русское поле" to "https://youtu.be/dHdy2S1Npvs" to true
    ).toSongs()
}

object BootlegVolume1Repository : SongRepository() {
    override val songs = listOf(
        "Я мечтаю (Oxxxymiron Diss)" to "https://youtu.be/f9c4dp0VyKc" to true,
        "Волчок (Walkie Diss)" to "https://youtu.be/S3g_RNe-RW4" to false,
        "Дедушка ждёт" to "https://youtu.be/dDHg10Cj7IA" to false,
        "Охлади мой пыл" to "https://youtu.be/MMeSweeouoI" to false,
        "King Ring (Freestyle)" to "https://youtu.be/ffMuQtR1h44" to true,
        "Versus zoo" to "https://youtu.be/Bgp-H8BpWvU" to false,
        "Руди (feat. Lokos)" to "https://youtu.be/QAaMtNM56h8" to false,
        "Магарыч" to "https://youtu.be/m2vGabTDCVo" to false,
        "Black Stalin" to "https://youtu.be/3Kzlyw6C4LA" to false,
        "100 barz (Нахуй всех!)" to "https://youtu.be/TR_Bd-QCScM" to true,
        "Шмелиный хайп" to "https://youtu.be/kxljr6lxOmU" to false,
        "Grime thing" to "https://youtu.be/ZfFzCQIUmaQ" to false,
        "Бесконечность" to "https://youtu.be/v4ttXUFDff0" to true,
        "Свободный голос" to "https://youtu.be/D9h5r_9AshE" to false,
        "Плохие белые (feat. Ленина Пакет, Саша Скул & СД)" to "https://youtu.be/OcNdfqc1V4Y" to true,
        "Далеко за горизонт" to "https://youtu.be/RjgboqRa6Iw" to true,
        "Переживаю тяготу (feat. Овсянкин & Касим)" to "https://youtu.be/CPpDUioJ9wE" to false,
        "Leave em wondering (feat. XTheDolphin & LieOfLife)" to "https://youtu.be/rzuYbkIk96g" to false,
        "Родина (feat. Кропаль)" to "https://youtu.be/CHAvBdFoSYo" to false,
        "Раб или царь" to "https://youtu.be/9uy0QOqvX_M" to true,
        "Правая лирика" to "https://youtu.be/UkVyl7u8bqg" to false,
        "Ненужный (feat. Овсянкин & Скорости Нет)" to "https://youtu.be/XaCfnK4UV5U" to false,
        "Не найдёте (feat. Овсянкин & Константин Крестов)" to "https://youtu.be/gMwF5bwuAVo" to false,
        "12 мая" to "https://youtu.be/CFuH94vN4h4" to false,
        "Напоследок" to "https://youtu.be/kSxZA2EN3ZQ" to true
    ).toSongs()
}


object AngelsTrueRepository : SongRepository() {
    override val songs = listOf(
        "Eminem Show" to "https://youtu.be/6ZWmSSDDdvY" to true,
        "Ангельское True" to "https://youtu.be/-KHTVnFIcV4" to false,
        "Дубай (feat. GoKilla)" to "https://youtu.be/rQxbT2Tw9EY" to false,
        "Биография" to "https://youtu.be/jR-PA6hz1NQ" to false,
        "Космос (feat. DEAD BLONDE)" to "https://youtu.be/yU9kCkCRtzk" to false,
        "РКН (feat. pyrokinesis)" to "https://youtu.be/hrfLuHm8fDc" to true,
        "Закалка" to "https://youtu.be/1uUWA20zXjI" to false,
        "Стас Ай, Как Просто" to "https://youtu.be/_psj59G0c7c" to false,
        "Мемы-4 (feat. CMH)" to "https://youtu.be/T3jjZK0if5Y" to false,
        "Токсики (feat. Aikko)" to "https://youtu.be/d5rstosMvLY" to false,
        "Пачка" to "https://youtu.be/_F68yY-16mI" to false,
        "Умри ты сегодня, а я завтра" to "https://youtu.be/rWEeklGBE94" to false,
        "Диско-кактус" to "https://youtu.be/KlhbV-Ts0zc" to true,
        "Benzo (feat. дима бамберг, BOOKER & The Cold Dicks)" to "https://youtu.be/e6_iUHK1ktk" to false,
        "Hollywood" to "https://youtu.be/fNFRw1h5lPY" to false,
        "Созависимая сука" to "https://youtu.be/XZfYzhyPL14" to false,
        "Шизодискотека (feat. DK)" to "https://youtu.be/zsMX377gwO8" to false,
        "Mike Killer" to "https://youtu.be/Hso21LNVNqE" to false,
        "Bring Me the Horizon" to "https://youtu.be/jHONdOijaEM" to true,
        "Суетолог" to "https://youtu.be/Z8-fLnymz90" to false,
        "НЕ РАБОТАЙ НА ДЯДЮ" to "https://youtu.be/vvrrvmOU4hI" to false,
        "Пост-взрослый" to "https://youtu.be/AuTqTdyFBxM" to false,
        "Гав-Гав (feat. Lida)" to "https://youtu.be/rtGlYm2t45U" to false,
        "Мама, я левак!" to "https://youtu.be/n3emXVt6mq8" to true,
        "Skinny Noski" to "https://youtu.be/KtogUauqglg" to false,
        "Лесби" to "https://youtu.be/NXeW3icaGR0" to false,
        "Super Ex" to "https://youtu.be/Jk_hEGBKNOY" to false,
        "Расставашки" to "https://youtu.be/othwOImbe-U" to false,
        "Ленинград" to "https://youtu.be/hhM_luig6P4" to false,
        "Всегда можно" to "https://youtu.be/gCfXJtCdrdI" to false,
        "Стейбл Coin" to "https://youtu.be/oIm6iSxox1I" to false,
        "Пафос (feat. Джигли)" to "https://youtu.be/S_KP-wQQUTQ" to false,
        "Завтра" to "https://youtu.be/57RtXHjC8yo" to true,
        "Ураган" to "https://youtu.be/RjwSMNle9ss" to false,
        "Супервсратый движ (feat. Мэйби Хмурый)" to "https://youtu.be/f9doNznfPvU" to false
    ).toSongs()
}
