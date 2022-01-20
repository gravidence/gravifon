package org.gravidence.gravifon.domain.track

import org.gravidence.gravifon.domain.tag.FieldKey
import org.gravidence.gravifon.domain.tag.FieldValues
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import java.io.File
import java.net.URI

class PhysicalTrack(val file: AudioFile) {

    // http://www.jthink.net/jaudiotagger/examples_read.jsp

    constructor(filepath: String) : this(AudioFileIO.read(File(filepath)))
    constructor(uri: URI) : this(AudioFileIO.read(File(uri)))

    fun toVirtualTrack(): VirtualTrack {
        val fields = mutableMapOf<FieldKey, FieldValues>()

        file.tag.fields.forEach {
            if (!it.isBinary) {
                val values = fields[it.id]
                if (values == null) {
                    fields[it.id] = FieldValues(it.toString())
                } else {
                    values.values.add(it.toString())
                }
            }
        }

        return FileVirtualTrack(file.file.path, fields)
    }

}