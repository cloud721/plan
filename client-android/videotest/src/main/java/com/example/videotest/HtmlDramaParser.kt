package com.example.videotest

import org.jsoup.Jsoup

data class DramaContent(
    val homeTabs: List<HomeTab>,
    val searchText: String,
    val giftBadge: String,
    val banners: List<BannerCard>,
    val popular: List<DramaCard>,
    val seriesTabs: List<String>,
    val series: List<SeriesItem>,
    val player: PlayerContent,
    val historyTabs: List<String>,
    val history: List<HistoryItem>,
    val bottomNavItems: List<String>
)

data class HomeTab(val title: String, val hasDot: Boolean = false, val active: Boolean = false)

data class DramaCard(
    val title: String,
    val imageUrl: String,
    val badge: String = ""
)

data class BannerCard(
    val title: String,
    val imageUrl: String,
    val badge: String = "",
    val isCenter: Boolean = false
)

data class SeriesItem(
    val rank: Int,
    val title: String,
    val imageUrl: String,
    val tags: List<String>,
    val description: String,
    val sideImageUrl: String,
    val languageCode: String,
    val episodeCount: String
)

data class PlayerContent(
    val title: String,
    val posterUrl: String,
    val discountBadge: String,
    val vipLabel: String,
    val likeCount: String,
    val commentCount: String,
    val collectLabel: String,
    val selectionsLabel: String,
    val shareLabel: String,
    val timeDisplay: String,
    val downloadLabel: String,
    val qualityLabel: String,
    val speedLabel: String,
    val commentText: String
) {
    val actions: List<String> get() = listOf(discountBadge, vipLabel, likeCount, commentCount, collectLabel, selectionsLabel, shareLabel)
}

data class HistoryItem(
    val title: String,
    val imageUrl: String,
    val episode: String,
    val time: String,
    val freeAccess: String = "",
    val isOffShelf: Boolean = false,
    val updatedText: String = ""
)

object HtmlDramaParser {
    fun parse(html: String): DramaContent {
        val document = Jsoup.parse(html)

        val homeTabs = document.select(".nav-tabs .nav-tab").map { tab ->
            HomeTab(
                title = tab.ownText().ifBlank { tab.text() }.cleanText(),
                hasDot = tab.selectFirst(".dot") != null,
                active = tab.hasClass("active")
            )
        }.filter { it.title.isNotBlank() }

        val searchText = document.selectFirst(".search-text")?.text().orEmpty().cleanText()

        val giftBadge = document.selectFirst(".gift-badge")?.text().orEmpty().cleanText()

        val banners = document.select("#bannerContainer .banner-card").mapIndexed { index, card ->
            val img = card.selectFirst("img")
            BannerCard(
                title = img?.attr("alt").orEmpty().cleanText(),
                imageUrl = img?.attr("src").orEmpty().cleanUrl(),
                badge = card.selectFirst(".banner-badge")?.text().orEmpty().cleanText(),
                isCenter = index == 1
            )
        }

        val popular = document.select("#popularRow1 .drama-card, #popularRow2 .drama-card")
            .mapNotNull { card ->
                val img = card.selectFirst("img") ?: return@mapNotNull null
                val titleEl = card.selectFirst(".card-title")
                DramaCard(
                    title = titleEl?.text().orEmpty().cleanText().ifBlank { img.attr("alt").cleanText() },
                    imageUrl = img.attr("src").cleanUrl(),
                    badge = card.selectFirst(".card-badge")?.text().orEmpty().cleanText()
                )
            }

        val seriesTabs = document.select(".series-tabs .series-tab").map {
            it.text().cleanText()
        }.filter { it.isNotBlank() }

        val series = document.select("#seriesList .series-item").mapNotNull { item ->
            val thumb = item.selectFirst(".series-thumb img")
            val sideThumb = item.selectFirst(".side-thumb img")
            val labelParts = item.selectFirst(".side-thumb-label")?.wholeText().orEmpty().cleanText().split(" ")
            SeriesItem(
                rank = item.selectFirst(".rank-num")?.text()?.trim()?.toIntOrNull() ?: 0,
                title = item.selectFirst(".series-info h3")?.text().orEmpty().cleanText(),
                imageUrl = thumb?.attr("src").orEmpty().cleanUrl(),
                tags = item.select(".series-tags .tag").map { it.text().cleanText() },
                description = item.selectFirst(".series-desc")?.text().orEmpty().cleanText(),
                sideImageUrl = sideThumb?.attr("src").orEmpty().cleanUrl(),
                languageCode = labelParts.getOrElse(0) { "" },
                episodeCount = if (labelParts.size >= 3) "${labelParts[1]} ${labelParts[2]}" else labelParts.lastOrNull().orEmpty()
            )
        }

        val player = document.selectFirst("#playerScreen").let { screen ->
            if (screen == null) {
                PlayerContent("", "", "", "", "", "", "", "", "", "", "", "", "", "")
            } else {
                PlayerContent(
                    title = screen.selectFirst(".player-title")?.text().orEmpty().cleanText(),
                    posterUrl = screen.selectFirst(".video-poster")?.attr("src").orEmpty().cleanUrl(),
                    discountBadge = screen.selectFirst(".discount-badge")?.text().orEmpty().cleanText(),
                    vipLabel = screen.selectFirst(".vip-icon + .action-label, .action-item:nth-child(2) .action-label")?.text().orEmpty().cleanText(),
                    likeCount = screen.select(".action-count").getOrNull(0)?.text().orEmpty().cleanText(),
                    commentCount = screen.select(".action-count").getOrNull(1)?.text().orEmpty().cleanText(),
                    collectLabel = screen.select(".action-label").getOrNull(0)?.text().orEmpty().cleanText(),
                    selectionsLabel = screen.select(".action-label").getOrNull(1)?.text().orEmpty().cleanText(),
                    shareLabel = screen.select(".action-label").lastOrNull()?.text().orEmpty().cleanText(),
                    timeDisplay = screen.selectFirst(".time-display")?.text().orEmpty().cleanText(),
                    downloadLabel = screen.selectFirst(".download-btn")?.text().orEmpty().cleanText(),
                    qualityLabel = screen.selectFirst(".quality-btn")?.text().orEmpty().cleanText(),
                    speedLabel = screen.selectFirst(".speed-btn")?.text().orEmpty().cleanText(),
                    commentText = screen.selectFirst(".comment-bar")?.text().orEmpty().cleanText()
                )
            }
        }

        val historyTabs = document.select(".history-nav-tabs .history-nav-tab").map {
            it.text().cleanText()
        }.filter { it.isNotBlank() }

        val history = document.select(".history-list-container .history-item").mapNotNull { item ->
            val img = item.selectFirst(".history-thumb img") ?: return@mapNotNull null
            HistoryItem(
                title = item.selectFirst(".history-title")?.text().orEmpty().cleanText(),
                imageUrl = img.attr("src").cleanUrl(),
                episode = item.selectFirst(".history-episode")?.text().orEmpty().cleanText(),
                time = item.selectFirst(".history-time")?.text().orEmpty().cleanText(),
                freeAccess = item.select(".history-extra").firstOrNull { !it.hasClass("updated-text") }?.text().orEmpty().cleanText(),
                isOffShelf = item.hasClass("off-shelf-item"),
                updatedText = item.selectFirst(".updated-text")?.text().orEmpty().cleanText()
            )
        }

        val bottomNavItems = document.select(".home-bottom-nav .home-nav-item span").map {
            it.text().cleanText()
        }.filter { it.isNotBlank() }

        return DramaContent(
            homeTabs = homeTabs,
            searchText = searchText,
            giftBadge = giftBadge,
            banners = banners,
            popular = popular,
            seriesTabs = seriesTabs,
            series = series,
            player = player,
            historyTabs = historyTabs,
            history = history,
            bottomNavItems = bottomNavItems
        )
    }
}

private fun String.cleanText(): String = replace(Regex("\\s+"), " ").trim()

private fun String.cleanUrl(): String = trim().replace("//backend", "/backend")
