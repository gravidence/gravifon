package org.gravidence.gravifon.domain.tag

import kotlinx.serialization.Serializable

typealias FieldValue = String

@Serializable
data class FieldValues(val values: MutableSet<FieldValue>) {

    constructor(value: FieldValue) : this(mutableSetOf(value))

}