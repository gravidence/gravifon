package org.gravidence.gravifon.domain.track

import org.gravidence.gravifon.domain.tag.FieldValues
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.KeyNotFoundException
import java.io.File
import java.net.URI

class PhysicalTrack(val file: AudioFile) {

    // http://www.jthink.net/jaudiotagger/examples_read.jsp

    constructor(filepath: String) : this(AudioFileIO.read(File(filepath)))
    constructor(uri: URI) : this(AudioFileIO.read(File(uri)))

    fun toVirtualTrack(): VirtualTrack {
        val fields = mutableMapOf<FieldKey, FieldValues>()

        FieldKey.values().forEach { fieldKey ->
            extractFieldValues(fieldKey)?.let { fieldValues ->
                fields[fieldKey] = fieldValues
            }
        }

        return FileVirtualTrack(file.file.path, fields)
    }

    private fun extractFieldValues(fieldKey: FieldKey): FieldValues? {
        return try {
            when (file) {
                is MP3File -> extractFieldValuesFromMp3File(fieldKey, file)
                else -> extractFieldValuesFromGenericFile(fieldKey, file)
            }
        } catch (e: KeyNotFoundException) {
            // ignore since JAudioTagger generic fields are not supported fully by all metadata formats
            null
        }
    }

    private fun extractFieldValuesFromGenericFile(fieldKey: FieldKey, f: AudioFile): FieldValues? {
        val tagFields = f.tag.getFields(fieldKey)
        return if (tagFields.isNotEmpty()) {
            FieldValues(tagFields
                .filter { tagField -> !tagField.isBinary }
                .map { tagField -> tagField.toString() }
                .toMutableSet())
        } else {
            null
        }
    }

    private fun extractFieldValuesFromMp3File(fieldKey: FieldKey, f: MP3File): FieldValues? {
        if (!f.hasID3v2Tag()) {
            // fortunately ID3v1 is compatible with generic tag approach (though supports very limited set of fields)
            return extractFieldValuesFromGenericFile(fieldKey, f)
        }

        val tagFields = f.iD3v2Tag.getAll(fieldKey)
        return if (tagFields.isNotEmpty()) {
            FieldValues(tagFields.toMutableSet())
        } else {
            null
        }
    }

}