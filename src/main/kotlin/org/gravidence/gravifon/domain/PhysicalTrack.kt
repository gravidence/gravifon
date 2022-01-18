package org.gravidence.gravifon.domain

import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import java.io.File

class PhysicalTrack(val file: AudioFile) {

    // http://www.jthink.net/jaudiotagger/examples_read.jsp

    constructor(path: String) : this(AudioFileIO.read(File(path)))

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

        return VirtualTrack(file.file.path, fields)
    }

}