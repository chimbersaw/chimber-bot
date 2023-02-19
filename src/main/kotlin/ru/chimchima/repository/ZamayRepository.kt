package ru.chimchima.repository

object ZamayRepository : SongRepository() {
    override val songs = MrGaslightRepository.songs + LustHero3Repository.songs
}

object MrGaslightRepository : SongRepository() {
    override val songs = listOf(
        "Mr. Gaslight" to "https://youtu.be/xPJEUeoK8tY" to true,
        "Дорога" to "https://youtu.be/jngzaHFrzL8" to false,
        "Сюжеты" to "https://youtu.be/RG9DG2P5w-k" to false,
        "Детские травмы самые сладкие" to "https://youtu.be/pMZsat9uc1E" to true,
        "Выбора нет" to "https://youtu.be/6AWcPmX-qWc" to false,
        "Блэйд" to "https://youtu.be/gQ7Ro8Gf_Rw" to true,
        "Казакстан" to "https://youtu.be/LrX5xY0rno4" to false,
        "Мимо себя" to "https://youtu.be/ubqBdhUTnzo" to false,
        "Напоследок" to "https://youtu.be/GhCuex1RhnY" to false,
        "Исповедь полноценного человека" to "https://youtu.be/WP4NRzOpEpo" to true
    ).toSongs()
}

object LustHero3Repository : SongRepository() {
    override val songs = listOf(
        "Возвращайся" to "https://youtu.be/u3dPT3_mjz4" to true,
        "Лиза" to "https://youtu.be/7aq6l8HkwRw" to false,
        "Ригодон" to "https://youtu.be/kPg31GitWvs" to false,
        "Париж-22" to "https://youtu.be/m1oOJcv0E3o" to true,
        "Strange Love" to "https://youtu.be/9s2mhoD2ghk" to false,
        "Ая" to "https://youtu.be/d5FCrT6-FV8" to false,
        "Счастья, здоровья" to "https://youtu.be/_KJGdPvDny0" to true,
        "Здравствуй-пока" to "https://youtu.be/bZjCvIDu-aI" to true,
        "Почему" to "https://youtu.be/FmTqlroBaq0" to true,
        "Концерты" to "https://youtu.be/p8P9OZYfqgY" to true
    ).toSongs()
}
