package com.johncorser.telly.core.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider

/** Builds a throwaway in-memory [TellyDatabase] for Robolectric tests. */
fun inMemoryDb(): TellyDatabase =
    Room
        .inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            TellyDatabase::class.java,
        ).allowMainThreadQueries()
        .build()
