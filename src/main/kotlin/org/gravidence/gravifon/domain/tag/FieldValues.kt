package org.gravidence.gravifon.domain.tag

import kotlinx.serialization.Serializable

@Serializable
data class FieldValues(val values: MutableSet<String>) {

    constructor(value: String) : this(mutableSetOf(value))

}