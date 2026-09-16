package com.johncorser.telly.features.settings

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * The direct-path branch runs against Robolectric's real filesystem; the
 * MediaStore branch runs against [FakeMediaProvider] (registered on the
 * "media" authority) with the direct directory blocked by a regular file.
 * Real MediaStore ownership semantics are covered on-device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MediaStoreBackupDocumentsTest {
    private val store = MediaStoreBackupDocuments(ApplicationProvider.getApplicationContext())
    private lateinit var provider: FakeMediaProvider

    @Before
    fun registerProvider() {
        provider = Robolectric.setupContentProvider(FakeMediaProvider::class.java, MediaStore.AUTHORITY)
    }

    private fun documentsDir(): File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)

    @Test
    fun `writes, reads back and deletes via the direct path`() {
        assertNull(store.read())
        assertFalse(store.sighted())
        store.write("{\"v\":1}")
        assertEquals("{\"v\":1}", store.read())
        assertTrue(store.sighted())
        store.write("{\"v\":2}")
        assertEquals("{\"v\":2}", store.read())
        store.delete()
        assertNull(store.read())
    }

    @Test
    fun `falls back to a MediaStore insert-then-update when the path is blocked`() {
        // A regular file where the directory belongs blocks every direct IO.
        documentsDir().mkdirs()
        File(documentsDir(), "telly").writeText("blocker")
        store.write("first")
        assertEquals(1, provider.entries.size)
        assertEquals(AutoBackupLocation.FILE_NAME, provider.entries.single().name)
        store.write("second")
        assertEquals(1, provider.entries.size)
        assertEquals("second", store.read())
        assertTrue(store.sighted())
        store.delete()
        assertTrue(provider.entries.isEmpty())
        assertNull(store.read())
    }

    @Test
    fun `a MediaStore write prunes every other backup entry it can delete`() {
        documentsDir().mkdirs()
        File(documentsDir(), "telly").writeText("blocker")
        // A previous install's collision renames left "(N)" duplicates behind.
        val kept = seedEntry(AutoBackupLocation.FILE_NAME)
        seedEntry("telly-backup (1).json")
        seedEntry("telly-backup (2).json")
        val unrelated = seedEntry("unrelated.txt")
        store.write("only")
        assertEquals(listOf(kept, unrelated), provider.entries.map { it.id })
        assertEquals("only", store.read())
    }

    private fun seedEntry(name: String): Long {
        val values = ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, name) }
        val uri = provider.insert(MediaStore.Files.getContentUri("external"), values)
        return checkNotNull(uri.lastPathSegment).toLong()
    }
}

/** Minimal in-memory stand-in for the platform MediaProvider. */
class FakeMediaProvider : ContentProvider() {
    class Entry(
        val id: Long,
        val name: String,
        val file: File,
    )

    val entries = mutableListOf<Entry>()
    private var nextId = 1L

    override fun onCreate(): Boolean = true

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor {
        val cursor = MatrixCursor(arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME))
        entries.forEach { cursor.addRow(arrayOf(it.id, it.name)) }
        return cursor
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri {
        val entry =
            Entry(
                id = nextId++,
                name = checkNotNull(values?.getAsString(MediaStore.MediaColumns.DISPLAY_NAME)),
                file = File.createTempFile("fake-media", null),
            )
        entries += entry
        return Uri.withAppendedPath(uri, entry.id.toString())
    }

    override fun openFile(
        uri: Uri,
        mode: String,
    ): ParcelFileDescriptor {
        val entry = entries.first { it.id == uri.lastPathSegment?.toLong() }
        val flags =
            if (mode.contains('w')) {
                ParcelFileDescriptor.MODE_WRITE_ONLY or ParcelFileDescriptor.MODE_CREATE or
                    ParcelFileDescriptor.MODE_TRUNCATE
            } else {
                ParcelFileDescriptor.MODE_READ_ONLY
            }
        return ParcelFileDescriptor.open(entry.file, flags)
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int {
        val id = uri.lastPathSegment?.toLong()
        return if (entries.removeAll { it.id == id }) 1 else 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?,
    ): Int = 0

    override fun getType(uri: Uri): String? = null
}
