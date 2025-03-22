package com.adammusic.practicelog.data.mock


import com.adammusic.practicelog.data.model.*
import java.time.LocalDateTime

object MockData {
    val categories = listOf(
        Category(1, "Skálák", 0xFF2196F3),
        Category(2, "Etűdök", 0xFF4CAF50),
        Category(3, "Dalok", 0xFFFFC107),
        Category(4, "Akkordok", 0xFFFFC107),
        Category(5, "Legato", 0xFFFFC107)
    )

    val practices = listOf(
        Practice(
            id = 1,
            name = "Pengetős skálagyakorlat",
            description = "A-moll pentatonikus skála gyakorlása alternate pickinggel",
            category = categories[0],
            sessions = listOf(
                PracticeSession(
                    id = 1,
                    practiceId = 1,
                    date = LocalDateTime.now().minusDays(7),
                    duration = 30,
                    startingBpm = 80,
                    achievedBpm = 85,
                    notes = "Kezdő tempó beállítása"
                ),
                PracticeSession(
                    id = 2,
                    practiceId = 1,
                    date = LocalDateTime.now().minusDays(5),
                    duration = 35,
                    startingBpm = 85,
                    achievedBpm = 90,
                    notes = "Kezd kényelmessé válni"
                ),
                PracticeSession(
                    id = 3,
                    practiceId = 1,
                    date = LocalDateTime.now().minusDays(2),
                    duration = 40,
                    startingBpm = 90,
                    achievedBpm = 95,
                    notes = "Ma már sokkal stabilabb"
                ),
                PracticeSession(
                    id = 4,
                    practiceId = 1,
                    date = LocalDateTime.now().minusDays(1),
                    duration = 40,
                    startingBpm = 95,
                    achievedBpm = 95,
                    notes = "Alakul, de nem gyorsul"
                )
            )
        ),
        Practice(
            id = 2,
            name = "Fingerpicking gyakorlat",
            description = "Travis picking pattern gyakorlása",
            category = categories[2],
            sessions = listOf(
                PracticeSession(
                    id = 2,
                    practiceId = 2,
                    date = LocalDateTime.now().minusDays(1),
                    duration = 45,
                    startingBpm = 60,
                    achievedBpm = 70,
                    notes = "Még nem stabil"
                )
            )
        )
    )
}