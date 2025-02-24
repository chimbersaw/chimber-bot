package ru.chimchima.repository

object CrystalCastlesRepository : SongRepository {
    override val songs = CC1Repository.songs + CC2Repository.songs + CC3Repository.songs
}

object CC1Repository : SongRepository {
    override val songs = listOf(
        "Untrust Us" fav "https://youtu.be/oDHAEME84Og",
        "Alice Practice" fav "https://youtu.be/TxBXaMQP2Kg",
        "Crimewave" fav "https://youtu.be/ayc4Nv1fnZY",
        "Magic Spells" kal "https://youtu.be/fUTJa00puDU",
        "XXZXCUZX Me" kal "https://youtu.be/mSIzybHQtNw",
        "Air War" fav "https://youtu.be/2dK3Tzf8KwA",
        "Courtship Dating" fav "https://youtu.be/BnQKxTxOYB0",
        "Good Time" kal "https://youtu.be/SnTGSHhEOfI",
        "1991" kal "https://youtu.be/hYf20ovQm5g",
        "Vanished" kal "https://youtu.be/6e6Hj7MwWaI",
        "Knights" kal "https://youtu.be/oSgTIlfREn8",
        "Love And Caring" fav "https://youtu.be/9YqhhPGe8eM",
        "Through The Hosiery" fav "https://youtu.be/KghmoR9Rzvc",
        "Reckless" kal "https://youtu.be/thflPl1-4uE",
        "Black Panther" fav "https://youtu.be/zQXHAPf5ijQ",
        "Tell Me What To Swallow" fav "https://youtu.be/vb7FmY7JOY0",
        "Trash Hologram" kal "https://youtu.be/UhhR_h8f5Lo",
        "Air War (David Wolf Remix)" kal "https://youtu.be/UZ063QV9HMk"
    )
}

object CC2Repository : SongRepository {
    override val songs = listOf(
        "Fainting Spells" kal "https://youtu.be/Mi1Q8O0WHT4",
        "Celestica" fav "https://youtu.be/Y05H4Snyuz8",
        "Doe Deer" fav "https://youtu.be/0ruvmkCq4es",
        "Baptism" fav "https://youtu.be/vStjmYxetY0",
        "Year Of Silence" kal "https://youtu.be/F2as7j0mK9I",
        "Empathy" kal "https://youtu.be/NLi2v-Gq-5A",
        "Suffocation" kal "https://youtu.be/Z0NGdLr4img",
        "Violent Dreams" kal "https://youtu.be/esRAdBdox3E",
        "Vietnam" kal "https://youtu.be/V3e90yExv38",
        "Birds" kal "https://youtu.be/rcmalrmv6UM",
        "Pap Smear" fav "https://youtu.be/dGsdg2YYTCY",
        "Not In Love" kal "https://youtu.be/32udqal_lyQ",
        "Intimate" kal "https://youtu.be/FBsEoOM3dkU",
        "I Am Made Of Chalk" kal "https://youtu.be/wezdatnTE-c"
    )
}

object CC3Repository : SongRepository {
    override val songs = listOf(
        "Plague" fav "https://youtu.be/cx2lJIOTBjs",
        "Kerosene" fav "https://youtu.be/qR2QIJdtgiU",
        "Wrath of God" fav "https://youtu.be/o6ugOBCZAVk",
        "Affection" fav "https://youtu.be/-RqqXT44veI",
        "Pale Flesh" fav "https://youtu.be/SmTWi6kz3-k",
        "Sad Eyes" kal "https://youtu.be/JjAdfDrh8v0",
        "Insulin" kal "https://youtu.be/PioZ7-3OPCM",
        "Transgender" fav "https://youtu.be/EgKdyHcZJcs",
        "Violent Youth" kal "https://youtu.be/aa04QySzh8o",
        "Telepath" kal "https://youtu.be/05F8PJMqalw",
        "Mercenary" kal "https://youtu.be/IffPAJC9G9Q",
        "Child I Will Hurt You" kal "https://youtu.be/NBaM8eutHOg"
    )
}
