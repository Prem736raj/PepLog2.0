package com.example.solodoc.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class DocFile(
    val id: Long,
    val name: String,
    val size: Long,
    val dateAdded: Long,
    val uri: Uri,
    val type: DocType
)

enum class DocType { PDF, PPT, UNKNOWN }

class FileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getAllDocuments(): List<DocFile> = withContext(Dispatchers.IO) {
        val docList = mutableListOf<DocFile>()
        
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MIME_TYPE
        )

        // Optimized Query for PDF and PPT
        val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR " +
                        "${MediaStore.Files.FileColumns.MIME_TYPE} = ? OR " +
                        "${MediaStore.Files.FileColumns.DISPLAY_NAME} LIKE '%.pptx'"
                        
        val selectionArgs = arrayOf("application/pdf", "application/vnd.ms-powerpoint")
        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"

        val queryUri = MediaStore.Files.getContentUri("external")

        try {
            context.contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameCol)
                    val mime = cursor.getString(mimeCol)
                    val uri = ContentUris.withAppendedId(queryUri, cursor.getLong(idCol))

                    val type = when {
                        mime.contains("pdf") -> DocType.PDF
                        name.endsWith(".ppt") || name.endsWith(".pptx") -> DocType.PPT
                        else -> DocType.UNKNOWN
                    }

                    if (type != DocType.UNKNOWN) {
                        docList.add(DocFile(cursor.getLong(idCol), name, cursor.getLong(sizeCol), cursor.getLong(dateCol), uri, type))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext docList
    }
}