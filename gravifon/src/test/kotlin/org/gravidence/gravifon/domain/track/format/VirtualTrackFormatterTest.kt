package org.gravidence.gravifon.domain.track.format

import org.gravidence.gravifon.TestUtil
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class VirtualTrackFormatterTest {

    lateinit var track: VirtualTrack

    @BeforeEach
    internal fun setUp() {
        track = TestUtil.fixedFileVirtualTrack(artist = "AAA", album = "BBB", date = "2022-02")
    }

    @Test
    fun format1() {
        assertThat(track.format("%artist% - %album% (%date%)"), equalTo("AAA - BBB (2022-02)"))
    }

    @Test
    fun format2() {
        assertThat(track.format("_%artist% - %album% (%date%)_"), equalTo("_AAA - BBB (2022-02)_"))
    }

    @Test
    fun formatNoTokens() {
        assertThat(track.format("Lorem ipsum dolor sit amet"), equalTo("Lorem ipsum dolor sit amet"))
    }

    @Test
    fun formatEscapedPercent() {
        assertThat(track.format("Lorem ipsum %% dolor sit amet"), equalTo("Lorem ipsum % dolor sit amet"))
    }

    @Test
    fun formatAlonePercent() {
        assertThat(track.format("Lorem ipsum % dolor sit amet"), equalTo("Lorem ipsum  dolor sit amet"))
    }

    @Test
    fun formatThreeDontMakeTwoPercent() {
        assertThat(track.format("Lorem ipsum %%% dolor sit amet"), equalTo("Lorem ipsum % dolor sit amet"))
    }

}