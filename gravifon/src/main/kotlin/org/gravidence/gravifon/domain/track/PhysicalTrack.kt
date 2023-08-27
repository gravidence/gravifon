package org.gravidence.gravifon.domain.track

import org.gravidence.gravifon.domain.header.Headers
import org.gravidence.gravifon.domain.tag.FieldValues
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.KeyNotFoundException
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.flac.FlacTag
import org.jaudiotagger.tag.id3.AbstractID3v1Tag
import org.jaudiotagger.tag.id3.AbstractID3v2Frame
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.Id3SupportingTag
import org.jaudiotagger.tag.id3.framebody.FrameBodyTXXX
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentFieldKey
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTag
import org.jaudiotagger.tag.vorbiscomment.VorbisCommentTagField
import org.jaudiotagger.tag.wav.WavTag
import java.io.File
import java.net.URI
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PhysicalTrack(val file: AudioFile) {

    // http://www.jthink.net/jaudiotagger/examples_read.jsp

    constructor(uri: URI) : this(AudioFileIO.read(File(uri)))

    fun toVirtualTrack(): VirtualTrack {
        val fields = mutableMapOf<FieldKey, FieldValues>()

        FieldKey.entries.forEach { fieldKey ->
            extractFieldValues(fieldKey)?.let { fieldValues ->
                fields[fieldKey] = fieldValues
            }
        }

        return FileVirtualTrack(
            path = file.file.path,
            headers = Headers(length = file.audioHeader.preciseTrackLength.toDuration(DurationUnit.SECONDS)),
            fields = fields,
            customFields = extractCustomFieldValues()
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
            ?.let {
                when (fieldKey) {
                    FieldKey.ARTIST -> FieldValues(prepareMultiValueFieldFromID3v2Tag(it, '/'))
                    // genre delimiter isn't part of the spec, but de facto standard
                    FieldKey.GENRE -> FieldValues(prepareMultiValueFieldFromID3v2Tag(it, ';'))
                    else -> FieldValues(it.toMutableSet())
                }
            }
    }

    /**
     * Splits single string value per [ID3v2 spec](https://id3.org/id3v2.3.0).
     */
    private fun prepareMultiValueFieldFromID3v2Tag(multiValueField: List<String>, delimiter: Char): MutableSet<String> {
        return multiValueField
            .flatMap { it.split(delimiter) }
            .map { it.trim() }
            .toMutableSet()
    }

    private fun extractCustomFieldValues(): MutableMap<String, FieldValues> {
        val tag: Tag = when (val rawTag = file.tag) {
            is WavTag -> rawTag.iD3Tag
            is FlacTag -> rawTag.vorbisCommentTag
            else -> rawTag
        }

        return when (tag) {
            is AbstractID3v1Tag -> mutableMapOf()
            is AbstractID3v2Tag -> {
                tag.fields
                    .asSequence()
                    .filterIsInstance<AbstractID3v2Frame>()
                    .map { it.body }
                    .filterIsInstance<FrameBodyTXXX>()
                    .map { mapCustomFieldValue(it.description, it.textWithoutTrailingNulls) }
                    // TODO optimize, as well as try to return NULL instead of empty map
                    .toMap().toMutableMap()
            }
            is VorbisCommentTag -> {
                tag.fields
                    .asSequence()
                    .filterIsInstance<VorbisCommentTagField>()
                    .filter {
                        VorbisCommentFieldKey.entries.none { fieldKey ->
                            fieldKey.name == it.id
                        }
                    }
                    .map { mapCustomFieldValue(it.id, it.content) }
                    // TODO optimize, as well as try to return NULL instead of empty map
                    .toMap().toMutableMap()
            }
            else -> mutableMapOf()
        }
    }

    private fun mapCustomFieldValue(fieldKey: String, fieldValue: String): Pair<String, FieldValues> {
        // TODO key uppercase may cause unexpected effects when persisted to file
        return Pair(fieldKey.uppercase(), FieldValues(fieldValue))
    }

}