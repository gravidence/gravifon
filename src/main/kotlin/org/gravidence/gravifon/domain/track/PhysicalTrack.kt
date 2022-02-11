package org.gravidence.gravifon.domain.track

import org.gravidence.gravifon.domain.header.Headers
import org.gravidence.gravifon.domain.tag.FieldValues
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.KeyNotFoundException
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.Id3SupportingTag
import java.io.File
import java.net.URI
import kotlin.time.DurationUnit
import kotlin.time.toDuration

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

        return FileVirtualTrack(
            path = file.file.path,
            headers = Headers(length = file.audioHeader.preciseTrackLength.toDuration(DurationUnit.SECONDS)),
            fields = fields
        )
    }

    private fun extractFieldValues(fieldKey: FieldKey): FieldValues? {
        return try {
            if (file is MP3File && file.hasID3v2Tag()) {
                extractFieldValuesFromID3v2Tag(fieldKey, file.iD3v2Tag)
            } else if (file.tag is Id3SupportingTag) {
                extractFieldValuesFromID3v2Tag(fieldKey, (file.tag as Id3SupportingTag).iD3Tag)
            } else {
                // ID3v1 falls in here
                extractFieldValuesFromGenericTag(fieldKey, file.tag)
            }
        } catch (e: KeyNotFoundException) {
            // ignore since JAudioTagger generic fields are not supported fully by all metadata formats
            null
        }
    }

    private fun extractFieldValuesFromGenericTag(fieldKey: FieldKey, tag: Tag): FieldValues? {
        return tag.getFields(fieldKey)
            ?.ifEmpty { null }
            ?.let {
                FieldValues(it
                    .filter { tagField -> !tagField.isBinary }
                    .map { tagField -> tagField.toString() }
                    .toMutableSet())
            }
    }

    private fun extractFieldValuesFromID3v2Tag(fieldKey: FieldKey, tag: AbstractID3v2Tag): FieldValues? {
        return tag.getAll(fieldKey)
            ?.ifEmpty { null }
            ?.let { FieldValues(it.toMutableSet()) }
    }

}