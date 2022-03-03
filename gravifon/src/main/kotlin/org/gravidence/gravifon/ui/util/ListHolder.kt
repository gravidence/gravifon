package org.gravidence.gravifon.ui.util

/**
 * Simple list holder to control recomposition.
 * That allows domain objects to be shared between all components.
 */
class ListHolder<E>(val list: List<E>)