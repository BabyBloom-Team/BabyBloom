package com.babybloom.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.babybloom.data.local.entity.UserEntity
import com.babybloom.di.DatabaseModule
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DatabaseReleaseSafetyTest {
    private lateinit var isolatedContext: Context
    private lateinit var databaseFile: File
    private lateinit var databaseName: String

    @Before
    fun createIsolatedDatabaseLocation() {
        isolatedContext = InstrumentationRegistry.getInstrumentation().targetContext
        databaseName = "release-safety-${UUID.randomUUID()}.db"
        databaseFile = isolatedContext.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()
    }

    @After
    fun removeTestDatabase() {
        SQLiteDatabase.deleteDatabase(databaseFile)
    }

    @Test
    fun currentDatabaseRetainsItsUsersWhenReopened() = runBlocking {
        val original = UserEntity(id = 1, name = "Test parent", email = "parent@example.com", passwordHash = "test-only")
        val database = DatabaseModule.createDatabase(isolatedContext, databaseName)
        try {
            database.userDao().insert(original)
        } finally {
            database.close()
        }
        val reopened = DatabaseModule.createDatabase(isolatedContext, databaseName)
        try {
            assertEquals(original, reopened.userDao().getById(1))
        } finally {
            reopened.close()
        }
    }

    @Test
    fun migrationFromFourteenCreatesNotificationsWithoutDeletingUsers() = runBlocking {
        val original = UserEntity(id = 1, name = "Test parent", email = "parent@example.com", passwordHash = "test-only")
        val current = DatabaseModule.createDatabase(isolatedContext, databaseName)
        try {
            current.userDao().insert(original)
        } finally {
            current.close()
        }
        SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READWRITE).use { database ->
            database.execSQL("DROP TABLE app_notifications")
            database.version = 14
        }

        val upgraded = DatabaseModule.createDatabase(isolatedContext, databaseName)
        try {
            assertEquals(original, upgraded.userDao().getById(1))
            upgraded.openHelper.readableDatabase.query("SELECT COUNT(*) FROM app_notifications").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
        } finally {
            upgraded.close()
        }
    }

    @Test
    fun missingMigrationFailsWithoutErasingTheExistingDatabase() {
        SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { database ->
            database.execSQL("CREATE TABLE preserved_data (value TEXT NOT NULL)")
            database.execSQL("INSERT INTO preserved_data VALUES ('keep this data')")
            database.version = 8
        }
        val unsupported = DatabaseModule.createDatabase(isolatedContext, databaseName)
        try {
            unsupported.openHelper.writableDatabase
            fail("An unsupported database must not be silently recreated")
        } catch (exception: IllegalStateException) {
            assertTrue(exception.message.orEmpty().contains("migration", ignoreCase = true))
        } finally {
            unsupported.close()
        }

        SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READONLY).use { database ->
            database.rawQuery("SELECT value FROM preserved_data", null).use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("keep this data", cursor.getString(0))
            }
            assertEquals(8, database.version)
        }
    }
}
