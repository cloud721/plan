package com.example.videotest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HtmlDramaParserTest {
    private val html = File("src/main/assets/Mobo_drama_app.html").readText()

    @Test
    fun parsesHomeContentFromStaticHtmlElements() {
        val content = HtmlDramaParser.parse(html)

        // Home tabs
        assertEquals(5, content.homeTabs.size)
        assertEquals("Home", content.homeTabs[0].title)
        assertTrue(content.homeTabs[0].active)
        assertEquals("Free", content.homeTabs[1].title)
        assertTrue(content.homeTabs[1].hasDot)

        // Search & gift
        assertEquals("The Mafia's Obsession", content.searchText)
        assertEquals("+120", content.giftBadge)

        // Banners
        assertEquals(3, content.banners.size)
        assertEquals("La Divorziata Torna", content.banners[0].title)
        assertEquals("FREE", content.banners[0].badge)
        assertFalse(content.banners[0].isCenter)
        assertTrue(content.banners[1].isCenter)

        // Popular cards
        assertEquals(7, content.popular.size)
        assertEquals("La Divorziata Torna come Ereditaria", content.popular[1].title)
        assertEquals("-80%", content.popular[1].badge)

        // Series
        assertEquals(2, content.seriesTabs.size)
        assertEquals("Latest Series", content.seriesTabs[0])
        assertEquals(3, content.series.size)
        assertEquals(1, content.series.first().rank)
        assertEquals("AR", content.series[0].languageCode)
        assertEquals("76 EP", content.series[0].episodeCount)
        assertTrue(content.series[0].tags.isNotEmpty() || content.series[0].description.contains("إيلي"))

        // Bottom nav
        assertEquals(4, content.bottomNavItems.size)
        assertEquals("My playlist", content.bottomNavItems[0])
    }

    @Test
    fun parsesPlayerContentFromStaticHtmlElements() {
        val content = HtmlDramaParser.parse(html)
        val player = content.player

        assertTrue(player.title.startsWith("Episode 2"))
        assertEquals("00:10/12:00", player.timeDisplay)
        assertEquals("-20%", player.discountBadge)
        assertEquals("Download", player.downloadLabel)
        assertEquals("1080P", player.qualityLabel)
        assertEquals("1.75X", player.speedLabel)
    }

    @Test
    fun parsesHistoryContentFromStaticHtmlElements() {
        val content = HtmlDramaParser.parse(html)

        assertEquals(3, content.historyTabs.size)
        assertEquals("History", content.historyTabs[1])

        assertEquals(5, content.history.size)
        assertEquals("Outcast The Lycan King", content.history.first().title)
        assertEquals("Watched EP 01", content.history.first().episode)
        assertEquals("Today", content.history.first().time)

        val lastItem = content.history.last()
        assertTrue(lastItem.isOffShelf)
        assertEquals("Outcast The Lycan King", lastItem.title)
        assertEquals("Updated EP 48", lastItem.updatedText)
        assertTrue(lastItem.time.isEmpty())
    }
}
