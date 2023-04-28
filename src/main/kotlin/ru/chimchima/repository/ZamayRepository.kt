package ru.chimchima.repository

object ZamayRepository : SongRepository {
    override val songs = MrGaslightRepository.songs + LustHero3Repository.songs
}

object MrGaslightRepository : SongRepository {
    override val songs = listOf(
        "Mr. Gaslight" fav "https://youtu.be/xPJEUeoK8tY",
        "Дорога" kal "https://youtu.be/jngzaHFrzL8",
        "Сюжеты" kal "https://youtu.be/RG9DG2P5w-k",
        "Детские травмы самые сладкие" fav "https://youtu.be/pMZsat9uc1E",
        "Выбора нет" kal "https://youtu.be/6AWcPmX-qWc",
        "Блэйд" fav "https://youtu.be/gQ7Ro8Gf_Rw",
        "Казакстан" kal "https://youtu.be/LrX5xY0rno4",
        "Мимо себя" kal "https://youtu.be/ubqBdhUTnzo",
        "Напоследок" kal "https://youtu.be/GhCuex1RhnY",
        "Исповедь полноценного человека" fav "https://youtu.be/WP4NRzOpEpo"
    )
}

object LustHero3Repository : SongRepository {
    override val songs = listOf(
        "Возвращайся" fav "https://youtu.be/u3dPT3_mjz4",
        "Лиза" kal "https://youtu.be/7aq6l8HkwRw",
        "Ригодон" kal "https://youtu.be/kPg31GitWvs",
        "Париж-22" fav "https://youtu.be/m1oOJcv0E3o",
        "Strange Love" kal "https://youtu.be/9s2mhoD2ghk",
        "Ая" kal "https://youtu.be/d5FCrT6-FV8",
        "Счастья, здоровья" fav "https://youtu.be/_KJGdPvDny0",
        "Здравствуй-пока" fav "https://youtu.be/bZjCvIDu-aI",
        "Почему" fav "https://youtu.be/FmTqlroBaq0",
        "Концерты" fav "https://youtu.be/p8P9OZYfqgY"
    )
}
