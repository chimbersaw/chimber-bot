package ru.chimchima.repository

object KrovostokRepository : SongRepository {
    override val songs = BloodRiverRepository.songs +
            SkvoznoeRepository.songs +
            DumbbellRepository.songs +
            StudenRepository.songs +
            LombardRepository.songs +
            CheburashkaRepository.songs +
            NaukaRepository.songs +
            KrovostokMisc.songs
}

object BloodRiverRepository : SongRepository {
    override val songs = listOf(
        "Интро" fav "https://youtu.be/oG4nHT5IKag",
        "Разговоры о напасах" fav "https://youtu.be/QW4RtCHsxvo",
        "Биография" fav "https://youtu.be/nFwcl-tKJdU",
        "Лобстер-пицца" fav "https://youtu.be/gGsBWkTcfSk",
        "Бакланы" fav "https://youtu.be/IyD3755N9X0",
        "Теряю голову" fav "https://youtu.be/SNoOPX1uysw",
        "Жесть" fav "https://youtu.be/aIWEbZHsXtE",
        "Белый ягуар" fav "https://youtu.be/oEicZwuMvT0",
        "Пурга" fav "https://youtu.be/vaXO-vAAh8E",
        "Гидропон (Дунул)" fav "https://youtu.be/IfBNksh7VWw"
    )
}

object SkvoznoeRepository : SongRepository {
    override val songs = listOf(
        "Хочешь?" kal "y6y_RqokTB8",
        "Сдавать говно" kal "FsaI3tPqjf4",
        "Бритни" kal "3hnGsalQBdg",
        "Людоед" fav "fUSoa7Vuggo",
        "Простые слова" fav "mVWOa9Q7N8Q",
        "Мой ствол" kal "pkHLbaqqwq8",
        "Приснился" kal "NhWvucjrGsw",
        "С.Б.Ш." fav "m6syAAEEo0Q",
        "Приоритеты" kal "c6z4jQ2-Dn4",
        "Трусы" kal "MG-5mEsz104",
        "Юрик Паршев" kal "G6klJyWp_dg",
        "Скажи раз" kal "uD7J-9QgRFA",
        "Сдохнуть" fav "D0y-wc_jEW4",
        "Нина-Карина" kal "7aQfWW1RSWI",
        "Синее небо" kal "f9MrVMDa6Ko"
    )
}

object DumbbellRepository : SongRepository {
    override val songs = listOf(
        "Интро" kal "https://youtu.be/aV3E6NATx38",
        "Зимняя" kal "https://youtu.be/-p1LBcUR6XA",
        "Быть плохим" kal "https://youtu.be/Px9Mh2y1sN8",
        "Колхозники" kal "https://youtu.be/FKulho3Sieg",
        "Порно" kal "https://youtu.be/qMsgeRdDvRI",
        "Беспорядки" kal "https://youtu.be/KklBtpIdyME",
        "Гантеля" fav "https://youtu.be/CVR0qtSV3KQ",
        "Органы" kal "https://youtu.be/Fs6XeYPgfjI",
        "Глаза" kal "https://youtu.be/F7MvkLyAqNg",
        "Ночь" kal "https://youtu.be/964Hu5CUAgU",
        "Метадон" kal "https://youtu.be/IIMHhEJMTu8",
        "Киса" kal "https://youtu.be/lXY64ftD1KU",
        "Шурик" kal "https://youtu.be/6ibOZHaJAHk",
        "Г.Э.С." fav "https://youtu.be/Tp4T1Pqo8LU",
        "Аутро" kal "https://youtu.be/gflUEvOFDQg"
    )
}

object StudenRepository : SongRepository {
    override val songs = listOf(
        "Память" fav "https://youtu.be/6VLSTXo0c38",
        "Пора Домой" kal "https://youtu.be/_qbzM3bEgO8",
        "Овощ" fav "https://youtu.be/rNztMIp_bPg",
        "Весна" kal "https://youtu.be/lhGnElXezYc",
        "Куртец" fav "https://youtu.be/Mu9G2zm1PhE",
        "Молния" kal "https://youtu.be/0ZT1nHA2EbM",
        "Думай позитивно" fav "https://youtu.be/c7YQnCn5NqQ",
        "Представьте" kal "https://youtu.be/Zy7s0dc3LME",
        "Ужален" kal "https://youtu.be/YjPyX2QNyxs",
        "Генетика" kal "https://youtu.be/dA2fvG5jms8",
        "Душно" kal "https://youtu.be/cwQA5ictnbE",
        "Деревня" kal "https://youtu.be/X2SrgZY-srs",
        "Цветы в вазе" fav "https://youtu.be/3ZB6Qla2wbQ",
        "Ребята" kal "https://youtu.be/5DCQMhfiK9g"
    )
}

object LombardRepository : SongRepository {
    override val songs = listOf(
        "В баре" fav "https://youtu.be/lMbhoAKTztE",
        "Патологоанатом" kal "https://youtu.be/Rywx73ZU5Pc",
        "Загробная" fav "https://youtu.be/1hpWFT_EWk4",
        "Секс это" fav "https://youtu.be/8tLkosX4HNI",
        "Зёма" kal "https://youtu.be/O8lYqRe20mk",
        "Ломбард" kal "https://youtu.be/qOLW4t1ZwP0",
        "Череповец" kal "https://youtu.be/ErT0DLss9Xo",
        "То, что ползает..." kal "https://youtu.be/-vgVTRKoPck",
        "Летом" fav "https://youtu.be/rkGJnzIKris",
        "Ногти" kal "https://youtu.be/zHHk4yx63MQ",
        "Снайпер" kal "https://youtu.be/_jSBxU0dugw"
    )
}

object CheburashkaRepository : SongRepository {
    override val songs = listOf(
        "Лоси" fav "https://youtu.be/Cp3WxPf-ATk",
        "Душ" kal "https://youtu.be/IqHWYFwOj6k",
        "Столярка" fav "https://youtu.be/aYoat1k95Yk",
        "Чебурашка" fav "https://youtu.be/-mR78CXk-b8",
        "Голова" fav "https://youtu.be/9PPZqk4BU1U",
        "Пожар" kal "https://youtu.be/UAD8ZIrz5tM",
        "Холодок" fav "https://youtu.be/Avl5ZLVflQ4",
        "Москва-область" kal "https://youtu.be/flGpKKrtXDs",
        "Вишенка" fav "https://youtu.be/RJTCI0RUYbI",
        "Наёк ёк" kal "https://youtu.be/X6ZRene9VcE",
        "Злые голуби" fav "https://youtu.be/QXA2mHs1NdA"
    )
}

object NaukaRepository : SongRepository {
    override val songs = listOf(
        "Делают" fav "https://youtu.be/a15yEtsiMJU",
        "Зашёл, вышел" fav "https://youtu.be/HWXLGn0YPKc",
        "Поточное" fav "https://youtu.be/dmq58CSVoYI",
        "Сердце майора" fav "https://youtu.be/A2_9ktPMwpA",
        "Дети" fav "https://youtu.be/ukWu2TkTIr0",
        "Коромысло" fav "https://youtu.be/8bN_s4cAptQ",
        "Сквирт" fav "https://youtu.be/7EPeC6v5nZI",
        "Амфибия" fav "https://youtu.be/AsZ_AT3cWSY",
        "Кого-то помнишь" fav "https://youtu.be/VsPmR01DRCM",
        "Шепоток" fav "https://youtu.be/dIT6tPptwXY"
    )
}

object KrovostokMisc : SongRepository {
    override val songs = listOf(
        "Бабочки" fav "https://youtu.be/5qbs5p00Zck",
        "Ленин" fav "https://youtu.be/WlpKAmK0TnU"
    )
}
