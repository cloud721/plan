package com.example.videotest

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.net.URL
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private lateinit var content: DramaContent
    private val handler = Handler(Looper.getMainLooper())
    private var screen = Screen.Home
    private var countdownSeconds = 2 * 24 * 3600 + 12 * 3600 + 54 * 60 + 36
    private var countdownView: TextView? = null
    private var activeHomeTab = 0
    private var homeTabViews = mutableListOf<TextView>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = color("#0A0A12")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        setContentView(R.layout.activity_main)
        root = findViewById(R.id.rootContainer)
        content = assets.open("Mobo_drama_app.html").bufferedReader().use {
            HtmlDramaParser.parse(it.readText())
        }
        showHome()
        startCountdown()
    }

    override fun onBackPressed() {
        if (screen == Screen.Player || screen == Screen.History) {
            showHome()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    // ── Screen Navigation ────────────────────────────────────────────

    private fun showHome() {
        screen = Screen.Home
        root.fadeReplace(buildHomeScreen())
    }

    private fun showPlayer(card: DramaCard? = null) {
        screen = Screen.Player
        root.fadeReplace(buildPlayerScreen(card))
    }

    private fun showHistory() {
        screen = Screen.History
        root.fadeReplace(buildHistoryScreen())
    }

    // ── Home Screen ──────────────────────────────────────────────────

    private fun buildHomeScreen(): View {
        val frame = FrameLayout(this).match()
        val scroll = ScrollView(this).match().apply {
            setFillViewport(true)
            setPadding(0, 0, 0, dp(86))
            isVerticalScrollBarEnabled = false
        }

        val column = LinearLayout(this).vertical().apply {
            setPadding(0, dp(18), 0, dp(80))
            background = verticalGradient("#0A0A12", "#12121C")
        }
        scroll.addView(column)

        // Sticky header with glass effect
        column.addView(buildHomeHeader())
        // Banner carousel
        column.addView(buildBannerCarousel())
        // Popular Short section
        column.addView(buildPopularSection())
        // Latest Series section
        column.addView(buildSeriesSection())

        frame.addView(scroll)
        frame.addView(buildHomeBottomNav())
        return frame
    }

    private fun buildHomeHeader(): View {
        val header = LinearLayout(this).vertical().apply {
            background = verticalGradient("#E60F0F19", "#B312121C")
            setPadding(0, dp(4), 0, dp(4))
        }

        // Status bar row
        val statusRow = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(32), dp(4), dp(24), dp(2))
        }
        statusRow.addView(label("5:40", 15f, Color.WHITE, Typeface.BOLD).weighted())
        val statusIcons = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        statusIcons.addView(buildSignalIcon())
        statusIcons.addView(space(dp(5), 1))
        statusIcons.addView(buildWifiIcon())
        statusIcons.addView(space(dp(5), 1))
        statusIcons.addView(buildBatteryIcon())
        statusRow.addView(statusIcons)
        header.addView(statusRow)

        // Search bar + gift button
        val searchRow = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(6), dp(16), dp(4))
        }
        val searchBar = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            background = rounded("#66404060", 20f, strokeColor = "#18FFFFFF")
            setPadding(dp(14), 0, dp(14), 0)
            setOnClickListener { /* could open search */ }
        }
        searchBar.addView(
            label("🔍", 12f, color("#8A8A9A")).apply {
                alpha = 0.45f
            }
        )
        searchBar.addView(space(dp(8), 1))
        searchBar.addView(
            label(content.searchText.ifBlank { "Search dramas..." }, 14f, color("#8A8A9A")).apply {
                maxLines = 1
            },
            LinearLayout.LayoutParams(0, dp(40), 1f).apply { gravity = Gravity.CENTER_VERTICAL }
        )
        searchRow.addView(searchBar, LinearLayout.LayoutParams(0, dp(40), 1f))
        searchRow.addView(space(dp(10), 1))
        // Gift button
        val giftBtn = FrameLayout(this)
        giftBtn.addView(
            label("🎁", 15f, Color.WHITE).apply {
                gravity = Gravity.CENTER
                background = rounded("#EE5A7C", 12f)
            },
            FrameLayout.LayoutParams(dp(40), dp(40))
        )
        // Gift badge
        giftBtn.addView(
            label(content.giftBadge.ifBlank { "+120" }, 9f, Color.WHITE, Typeface.BOLD).apply {
                gravity = Gravity.CENTER
                background = rounded("#FF4D6D", 8f)
                setPadding(dp(5), dp(2), dp(5), dp(2))
            },
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END
            ).apply {
                topMargin = dp(-6)
                rightMargin = dp(-6)
            }
        )
        searchRow.addView(giftBtn)
        header.addView(searchRow)

        // Navigation tabs
        val tabScroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val tabRow = LinearLayout(this).horizontal().apply {
            setPadding(dp(4), dp(8), dp(16), dp(2))
        }
        homeTabViews.clear()
        content.homeTabs.forEachIndexed { index, tab ->
            val tv = label(tab.title, if (index == 0) 20f else 15f,
                if (index == 0) Color.WHITE else color("#6A6A7A"),
                if (index == 0) Typeface.BOLD else Typeface.NORMAL
            ).apply {
                setPadding(dp(16), dp(4), dp(4), dp(4))
                setOnClickListener {
                    setActiveTab(index)
                }
            }
            homeTabViews.add(tv)
            tabRow.addView(tv)
        }
        tabScroll.addView(tabRow)
        header.addView(tabScroll)
        return header
    }

    private fun setActiveTab(index: Int) {
        activeHomeTab = index
        homeTabViews.forEachIndexed { i, tv ->
            tv.setTextColor(if (i == index) Color.WHITE else color("#6A6A7A"))
            tv.textSize = if (i == index) 20f else 15f
            tv.typeface = if (i == index) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    // ── Banner Carousel ──────────────────────────────────────────────

    private fun buildBannerCarousel(): View {
        val section = LinearLayout(this).vertical().apply {
            setPadding(0, dp(14), 0, dp(8))
        }
        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            setPadding(0, 0, 0, dp(8))
        }
        val row = LinearLayout(this).horizontal().apply {
            setPadding(dp(16), 0, dp(16), 0)
            gravity = Gravity.CENTER_VERTICAL
        }
        content.banners.forEachIndexed { index, card ->
            val isCenter = index == 1
            row.addView(buildBannerCard(card, isCenter), margin(end = if (index < content.banners.size - 1) dp(10) else 0))
        }
        scroll.addView(row)
        section.addView(scroll)
        return section
    }

    private fun buildBannerCard(card: BannerCard, isCenter: Boolean): View {
        val w = if (isCenter) 260 else 200
        val h = if (isCenter) 340 else 260
        val scale = if (isCenter) 1f else 0.88f

        val frame = FrameLayout(this).apply {
            background = rounded("#00000000", if (isCenter) 24f else 18f)
            clipToOutline = true
            alpha = if (isCenter) 1f else 0.85f
            scaleX = scale
            scaleY = scale
            setOnClickListener { showPlayer(DramaCard(card.title, card.imageUrl, card.badge)) }
        }

        val img = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(color("#252536"))
        }
        frame.addView(img, FrameLayout.LayoutParams(dp(w), dp(h)))
        ImageLoader.load(card.imageUrl, img)

        // Gradient overlay at bottom
        frame.addView(
            View(this).apply {
                background = verticalGradient("#00000000", "#E6000000")
            },
            FrameLayout.LayoutParams(dp(w), dp((h * 0.6f).toInt()), Gravity.BOTTOM)
        )

        // Play button
        val playSize = if (isCenter) 56 else 40
        frame.addView(
            label("▶", if (isCenter) 20f else 14f, Color.WHITE, Typeface.BOLD).apply {
                gravity = Gravity.CENTER
                background = rounded("#26FFFFFF", (playSize / 2).toFloat(), strokeColor = "#1AFFFFFF")
                setOnClickListener { showPlayer(DramaCard(card.title, card.imageUrl, card.badge)) }
            },
            FrameLayout.LayoutParams(dp(playSize), dp(playSize), Gravity.CENTER)
        )

        // Badge
        if (card.badge.isNotBlank()) {
            frame.addView(
                label(card.badge, if (isCenter) 12f else 9f, Color.WHITE, Typeface.BOLD).apply {
                    gravity = Gravity.CENTER
                    background = rounded("#FF5A6A", 10f)
                    setPadding(dp(10), dp(5), dp(10), dp(5))
                },
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP or Gravity.END
                ).apply {
                    topMargin = dp(12)
                    rightMargin = dp(12)
                }
            )
        }

        frame.layoutParams = LinearLayout.LayoutParams(dp(w), dp(h))
        return frame
    }

    // ── Popular Short Section ────────────────────────────────────────

    private fun buildPopularSection(): View {
        val section = LinearLayout(this).vertical().apply {
            setPadding(0, dp(10), 0, 0)
        }

        // Header
        val header = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(8), dp(20), dp(8))
        }
        val titleGroup = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
        }
        titleGroup.addView(label("Popular Short", 18f, Color.WHITE, Typeface.BOLD))
        titleGroup.addView(space(dp(8), 1))
        // Countdown
        countdownView = label(formatCountdown(), 11f, color("#D4A574"), Typeface.BOLD).apply {
            setPadding(dp(6), dp(4), dp(6), dp(4))
            background = rounded("#99B0805A", 6f)
        }
        titleGroup.addView(countdownView!!)
        header.addView(titleGroup.weighted())
        header.addView(label("More ›", 13f, color("#7A7A8A"), Typeface.BOLD))
        section.addView(header)

        // Card rows
        if (content.popular.isNotEmpty()) {
            section.addView(buildCardRow(content.popular.take(4)))
            if (content.popular.size > 4) {
                section.addView(buildCardRow(content.popular.drop(4), topMargin = 6))
            }
        }
        return section
    }

    private fun buildCardRow(cards: List<DramaCard>, topMargin: Int = 0): View {
        val scroll = HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled = false }
        val row = LinearLayout(this).horizontal().apply {
            setPadding(dp(24), dp(topMargin), dp(20), dp(10))
        }
        cards.forEach { card ->
            row.addView(buildDramaCardItem(card), margin(end = dp(11)))
        }
        scroll.addView(row)
        return scroll
    }

    private fun buildDramaCardItem(card: DramaCard): View {
        val column = LinearLayout(this).vertical()

        val posterFrame = FrameLayout(this).apply {
            background = rounded("#242434", 16f)
            clipToOutline = true
            setOnClickListener { showPlayer(card) }
        }
        val img = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(color("#252536"))
        }
        posterFrame.addView(img, FrameLayout.LayoutParams(dp(130), dp(175)))
        ImageLoader.load(card.imageUrl, img)

        // Badge
        if (card.badge.isNotBlank()) {
            val isFree = card.badge.contains("FREE", ignoreCase = true)
            posterFrame.addView(
                label(card.badge, 9f, Color.WHITE, Typeface.BOLD).apply {
                    gravity = Gravity.CENTER
                    background = rounded(if (isFree) "#FF7A9A" else "#FF6A6A", 8f)
                    setPadding(dp(8), dp(3), dp(8), dp(3))
                },
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP or Gravity.END
                ).apply {
                    topMargin = dp(8)
                    rightMargin = dp(8)
                }
            )
        }

        column.addView(posterFrame, LinearLayout.LayoutParams(dp(130), dp(175)))
        column.addView(
            label(card.title, 12f, color("#E0E0EA")).apply {
                maxLines = 2
                setPadding(0, dp(8), 0, 0)
            },
            LinearLayout.LayoutParams(dp(130), ViewGroup.LayoutParams.WRAP_CONTENT)
        )
        return column
    }

    // ── Latest Series Section ────────────────────────────────────────

    private fun buildSeriesSection(): View {
        val section = LinearLayout(this).vertical().apply {
            setPadding(0, dp(10), 0, dp(14))
            background = verticalGradient("#1A1A28", "#0A0A12")
        }

        val tabRow = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(16), dp(20), dp(10))
        }
        content.seriesTabs.forEachIndexed { index, name ->
            val isActive = index == 0
            val tv = label(name, 17f, if (isActive) color("#D4A574") else color("#6A6A7A"),
                if (isActive) Typeface.BOLD else Typeface.NORMAL
            ).apply {
                setPadding(0, 0, dp(16), 0)
                setOnClickListener { /* toggle series tab */ }
            }
            tabRow.addView(tv)
        }
        tabRow.addView(
            label("See all ›", 13f, color("#7A7A8A")).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    gravity = Gravity.END
                }
            }
        )
        section.addView(tabRow)

        content.series.forEach { item ->
            section.addView(buildSeriesItem(item))
        }
        return section
    }

    private fun buildSeriesItem(item: SeriesItem): View {
        val row = LinearLayout(this).horizontal().apply {
            setPadding(dp(20), dp(8), dp(20), dp(10))
            gravity = Gravity.TOP
            setOnClickListener { showPlayer(DramaCard(item.title, item.imageUrl)) }
        }

        // Rank + thumbnail wrapper
        val rankWrap = FrameLayout(this)
        val thumb = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(color("#1A1A2E"))
        }
        rankWrap.addView(thumb, FrameLayout.LayoutParams(dp(95), dp(125)))
        ImageLoader.load(item.imageUrl, thumb)

        // Rank badge
        val rankColor = when (item.rank) {
            1 -> "#F5B800"
            2 -> "#FF8C42"
            3 -> "#C44FD9"
            else -> "#777787"
        }
        rankWrap.addView(
            label(item.rank.toString(), 13f, if (item.rank == 1) color("#4A3500") else Color.WHITE, Typeface.BOLD).apply {
                gravity = Gravity.CENTER
                background = rounded(rankColor, 8f)
                clipToOutline = true
            },
            FrameLayout.LayoutParams(dp(24), dp(24), Gravity.TOP or Gravity.START).apply {
                topMargin = dp(-6)
                leftMargin = dp(-5)
            }
        )
        row.addView(rankWrap)

        // Info column
        val info = LinearLayout(this).vertical().apply {
            setPadding(dp(14), dp(2), dp(8), 0)
        }
        info.addView(label(item.title, 15f, color("#F0F0F5"), Typeface.BOLD).apply { maxLines = 2 })

        // Tags row
        if (item.tags.isNotEmpty()) {
            val tagsRow = LinearLayout(this).horizontal().apply {
                setPadding(0, dp(6), 0, dp(6))
            }
            item.tags.forEach { tag ->
                tagsRow.addView(
                    label(tag, 11f, color("#9A9AAA")).apply {
                        setPadding(dp(10), dp(3), dp(10), dp(3))
                        background = rounded("#3330303C", 8f)
                    },
                    margin(end = dp(6))
                )
            }
            info.addView(tagsRow)
        }

        info.addView(label(item.description.take(80) + if (item.description.length > 80) "..." else "", 12f, color("#8A8A9A")).apply { maxLines = 2 })
        row.addView(info, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))

        // Side thumbnail
        val sideWrap = LinearLayout(this).vertical().apply {
            gravity = Gravity.CENTER
        }
        val sideThumb = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(color("#1A1A2E"))
        }
        sideWrap.addView(sideThumb, LinearLayout.LayoutParams(dp(62), dp(82)))
        ImageLoader.load(item.sideImageUrl, sideThumb)
        sideWrap.addView(
            label("${item.languageCode}\n${item.episodeCount}", 8f, color("#D4A574"), Typeface.BOLD).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(4), 0, 0)
            }
        )
        row.addView(sideWrap)
        return row
    }

    // ── Player Screen ─────────────────────────────────────────────────

    private fun buildPlayerScreen(card: DramaCard?): View {
        val posterUrl = card?.imageUrl?.ifBlank { content.player.posterUrl }
            ?: content.player.posterUrl
        val title = card?.title?.takeIf { it.isNotBlank() } ?: content.player.title
        val playerContent = content.player

        return LinearLayout(this).vertical().apply {
            setBackgroundColor(Color.BLACK)
            // Top status bar
            addView(LinearLayout(context).horizontal().apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(32), dp(18), dp(24), dp(8))
                addView(label("1:40", 15f, Color.WHITE, Typeface.BOLD).weighted())
                addView(label("5G   82%", 12f, color("#D8D8E4"), Typeface.BOLD))
            })

            // Player header
            addView(LinearLayout(context).horizontal().apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(12), dp(8), dp(12), dp(8))
                addView(
                    label("‹", 28f, Color.WHITE, Typeface.BOLD).apply {
                        gravity = Gravity.CENTER
                        setOnClickListener { showHome() }
                    },
                    LinearLayout.LayoutParams(dp(44), dp(44))
                )
                addView(
                    label(title, 15f, Color.WHITE).apply {
                        maxLines = 1
                    },
                    LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                )
                addView(
                    label("···", 20f, Color.WHITE, Typeface.BOLD).apply {
                        gravity = Gravity.CENTER
                    },
                    LinearLayout.LayoutParams(dp(44), dp(44))
                )
            })

            // Video area
            val videoFrame = FrameLayout(context).apply {
                setBackgroundColor(Color.BLACK)
            }
            val poster = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            videoFrame.addView(poster, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            ))
            ImageLoader.load(posterUrl, poster)

            // Play overlay
            videoFrame.addView(
                View(this@MainActivity).apply {
                    background = GradientDrawable().apply {
                        setColor(color("#33000000"))
                    }
                },
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            videoFrame.addView(
                label("▶", 32f, Color.WHITE, Typeface.BOLD).apply {
                    gravity = Gravity.CENTER
                    background = rounded("#33FFFFFF", 38f, strokeColor = "#1AFFFFFF")
                },
                FrameLayout.LayoutParams(dp(70), dp(70), Gravity.CENTER)
            )

            // Right actions panel
            val actionsPanel = LinearLayout(context).vertical().apply {
                gravity = Gravity.CENTER
            }
            // Discount badge
            if (playerContent.discountBadge.isNotBlank()) {
                actionsPanel.addView(
                    label(playerContent.discountBadge, 11f, Color.WHITE, Typeface.BOLD).apply {
                        gravity = Gravity.CENTER
                        background = rounded("#FF5A6A", 12f)
                        setPadding(dp(8), dp(4), dp(8), dp(4))
                    },
                    margin(bottom = dp(14))
                )
            }
            // VIP icon
            actionsPanel.addView(
                label("♦", 16f, color("#8B4513"), Typeface.BOLD).apply {
                    gravity = Gravity.CENTER
                    background = rounded("#FFD700", 16f)
                },
                LinearLayout.LayoutParams(dp(32), dp(32)).apply { gravity = Gravity.CENTER }
            )
            actionsPanel.addView(
                label("VIP", 11f, Color.WHITE).apply { gravity = Gravity.CENTER },
                margin(top = dp(4), bottom = dp(12))
            )
            // Like
            actionsPanel.addView(
                label("♥", 20f, Color.WHITE).apply { gravity = Gravity.CENTER },
                LinearLayout.LayoutParams(dp(32), dp(32)).apply { gravity = Gravity.CENTER }
            )
            if (playerContent.likeCount.isNotBlank()) {
                actionsPanel.addView(
                    label(playerContent.likeCount, 12f, Color.WHITE).apply { gravity = Gravity.CENTER },
                    margin(top = dp(2), bottom = dp(10))
                )
            }
            // Comment
            actionsPanel.addView(
                label("💬", 18f, Color.WHITE).apply { gravity = Gravity.CENTER },
                LinearLayout.LayoutParams(dp(32), dp(32)).apply { gravity = Gravity.CENTER }
            )
            if (playerContent.commentCount.isNotBlank()) {
                actionsPanel.addView(
                    label(playerContent.commentCount, 12f, Color.WHITE).apply { gravity = Gravity.CENTER },
                    margin(top = dp(2), bottom = dp(10))
                )
            }
            // Collect
            actionsPanel.addView(
                label("★", 20f, Color.WHITE).apply { gravity = Gravity.CENTER },
                LinearLayout.LayoutParams(dp(32), dp(32)).apply { gravity = Gravity.CENTER }
            )
            actionsPanel.addView(
                label("collect", 11f, Color.WHITE).apply { gravity = Gravity.CENTER },
                margin(top = dp(2), bottom = dp(10))
            )
            // Selections
            actionsPanel.addView(
                label("☐", 18f, Color.WHITE).apply { gravity = Gravity.CENTER },
                LinearLayout.LayoutParams(dp(32), dp(32)).apply { gravity = Gravity.CENTER }
            )
            actionsPanel.addView(
                label("selections", 11f, Color.WHITE).apply { gravity = Gravity.CENTER },
                margin(top = dp(2), bottom = dp(10))
            )
            // Share
            actionsPanel.addView(
                label("↗", 20f, Color.WHITE).apply { gravity = Gravity.CENTER },
                LinearLayout.LayoutParams(dp(32), dp(32)).apply { gravity = Gravity.CENTER }
            )
            actionsPanel.addView(
                label("Share", 11f, Color.WHITE).apply { gravity = Gravity.CENTER },
                margin(top = dp(2))
            )

            videoFrame.addView(
                actionsPanel,
                FrameLayout.LayoutParams(dp(78), ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.END or Gravity.CENTER_VERTICAL).apply {
                    rightMargin = dp(12)
                }
            )

            // Comment bar (floating)
            if (playerContent.commentText.isNotBlank()) {
                videoFrame.addView(
                    label(playerContent.commentText, 13f, Color.WHITE).apply {
                        gravity = Gravity.CENTER
                        background = horizontalGradient("#FF8C42", "#FF6B8A")
                        setPadding(dp(18), dp(8), dp(18), dp(8))
                    },
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.BOTTOM or Gravity.END
                    ).apply {
                        bottomMargin = dp(100)
                        rightMargin = dp(60)
                    }
                )
            }

            addView(videoFrame, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f
            ))

            // Progress bar
            addView(
                buildProgressBar(),
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(14))
            )

            // Player controls
            addView(LinearLayout(context).horizontal().apply {
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), dp(10), dp(16), dp(24))
                addView(label(
                    playerContent.timeDisplay.ifBlank { "00:10/12:00" },
                    13f, Color.WHITE
                ).weighted())
                // Download
                addView(
                    label("⬇ ${playerContent.downloadLabel.ifBlank { "Download" }}", 12f, color("#D4A574")).apply {
                        gravity = Gravity.CENTER
                        background = rounded("#99403328", 20f)
                        setPadding(dp(14), dp(7), dp(14), dp(7))
                    },
                    margin(end = dp(8))
                )
                // Quality
                addView(
                    label(playerContent.qualityLabel.ifBlank { "1080P" }, 12f, Color.WHITE).apply {
                        gravity = Gravity.CENTER
                        background = rounded("#33282834", 16f)
                        setPadding(dp(10), dp(7), dp(10), dp(7))
                    },
                    margin(end = dp(8))
                )
                // Speed
                addView(
                    label(playerContent.speedLabel.ifBlank { "1.75X" }, 12f, Color.WHITE).apply {
                        gravity = Gravity.CENTER
                        background = rounded("#33282834", 16f)
                        setPadding(dp(10), dp(7), dp(10), dp(7))
                    }
                )
            })
        }
    }

    private fun buildProgressBar(): View {
        val frame = FrameLayout(this).apply {
            setPadding(dp(16), dp(4), dp(16), dp(4))
        }
        frame.addView(
            View(this).apply {
                background = rounded("#4DFFFFFF", 2f)
            },
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3), Gravity.CENTER_VERTICAL)
        )
        frame.addView(
            View(this).apply {
                background = horizontalGradient("#FF8C42", "#FF6B8A")
            },
            FrameLayout.LayoutParams(dp(60), dp(3), Gravity.CENTER_VERTICAL or Gravity.START)
        )
        frame.addView(
            View(this).apply {
                background = rounded("#FFFFFFFF", 6f)
            },
            FrameLayout.LayoutParams(dp(12), dp(12), Gravity.CENTER_VERTICAL or Gravity.START).apply {
                leftMargin = dp(60) - dp(6)
            }
        )
        return frame
    }

    // ── History Screen ────────────────────────────────────────────────

    private fun buildHistoryScreen(): View {
        val frame = FrameLayout(this).match()
        val scroll = ScrollView(this).match().apply {
            setFillViewport(true)
            setPadding(0, 0, 0, dp(86))
            isVerticalScrollBarEnabled = false
        }
        val column = LinearLayout(this).vertical().apply {
            setPadding(0, dp(8), 0, dp(100))
            background = verticalGradient("#0B0B0F", "#0B0B0F")
        }
        scroll.addView(column)

        // Status bar
        val statusRow = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(32), dp(8), dp(24), dp(6))
        }
        statusRow.addView(label("1:40", 15f, Color.WHITE, Typeface.BOLD).weighted())
        statusRow.addView(label("5G   82%", 12f, color("#D8D8E4"), Typeface.BOLD))
        column.addView(statusRow)

        // Header with logo
        val headerRow = LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), dp(10), dp(20), dp(4))
        }
        headerRow.addView(
            label("MoboReels", 24f, Color.WHITE, Typeface.BOLD).weighted()
        )
        headerRow.addView(
            label("🔍", 18f, Color.WHITE).apply { gravity = Gravity.CENTER },
            margin(end = dp(20))
        )
        headerRow.addView(
            label("✎", 20f, Color.WHITE).apply { gravity = Gravity.CENTER }
        )
        column.addView(headerRow)

        // History tabs
        val tabRow = LinearLayout(this).horizontal().apply {
            setPadding(dp(20), dp(12), dp(20), dp(6))
        }
        content.historyTabs.forEachIndexed { index, name ->
            val isActive = index == 1 // "History" is active by default
            tabRow.addView(
                label(name, if (isActive) 22f else 15f,
                    if (isActive) Color.WHITE else color("#666666"),
                    if (isActive) Typeface.BOLD else Typeface.NORMAL
                ).apply {
                    setPadding(0, 0, dp(24), dp(8))
                }
            )
        }
        column.addView(tabRow)

        // History list
        content.history.forEach { item ->
            column.addView(buildHistoryItem(item))
        }

        frame.addView(scroll)
        frame.addView(buildHistoryBottomNav())
        return frame
    }

    private fun buildHistoryItem(item: HistoryItem): View {
        val row = LinearLayout(this).horizontal().apply {
            setPadding(dp(20), dp(10), dp(20), dp(6))
            gravity = Gravity.TOP
            alpha = if (item.isOffShelf) 0.6f else 1f
        }

        val thumbFrame = FrameLayout(this).apply {
            setBackgroundColor(color("#1A1A2E"))
        }
        val img = ImageView(this).apply {
            scaleType = ImageView.ScaleType.CENTER_CROP
            setBackgroundColor(color("#1A1A2E"))
        }
        thumbFrame.addView(img, FrameLayout.LayoutParams(dp(100), dp(140)))
        ImageLoader.load(item.imageUrl, img)

        if (item.isOffShelf) {
            thumbFrame.addView(
                label("Off shelf", 10f, Color.WHITE).apply {
                    setPadding(dp(8), dp(4), dp(8), dp(4))
                    background = rounded("#B3000000", 4f)
                },
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM or Gravity.START
                ).apply {
                    bottomMargin = dp(8)
                    leftMargin = dp(8)
                }
            )
        }
        row.addView(thumbFrame, LinearLayout.LayoutParams(dp(100), dp(140)))

        val info = LinearLayout(this).vertical().apply {
            setPadding(dp(14), dp(4), 0, 0)
        }
        info.addView(label(item.title, 17f, Color.WHITE, Typeface.BOLD).apply { maxLines = 2 })
        if (item.episode.isNotBlank()) {
            info.addView(label(item.episode, 14f, color("#888888")), margin(top = dp(6)))
        }
        if (item.time.isNotBlank()) {
            info.addView(label(item.time, 13f, color("#666666")), margin(top = dp(4)))
        }
        if (item.freeAccess.isNotBlank()) {
            info.addView(label(item.freeAccess, 13f, color("#888888")), margin(top = dp(4)))
        }
        if (item.updatedText.isNotBlank()) {
            info.addView(label(item.updatedText, 13f, color("#666666")), margin(top = dp(4)))
        }
        row.addView(info, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        return row
    }

    // ── Bottom Navigation ─────────────────────────────────────────────

    private fun buildHomeBottomNav(): View {
        return LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(4), dp(10), dp(8))
            background = verticalGradient("#000A0A12", "#B30A0A12")
            val items = listOf("My playlist", "Home", "For you", "Me")
            val icons = listOf("☰", "⌂", "★", "👤")
            items.forEachIndexed { index, title ->
                val isActive = title == "Home"
                val itemCol = LinearLayout(this@MainActivity).vertical().apply {
                    gravity = Gravity.CENTER
                    setPadding(dp(8), dp(4), dp(8), 0)
                    setOnClickListener {
                        when (title) {
                            "My playlist" -> showHistory()
                            "Home" -> showHome()
                        }
                    }
                }
                itemCol.addView(
                    label(icons[index], 20f, if (isActive) color("#FF6B8A") else Color.WHITE).apply {
                        gravity = Gravity.CENTER
                        alpha = if (isActive) 1f else 0.5f
                    }
                )
                itemCol.addView(
                    label(title, 11f, if (isActive) color("#FF6B8A") else Color.WHITE).apply {
                        gravity = Gravity.CENTER
                        alpha = if (isActive) 1f else 0.5f
                    },
                    margin(top = dp(4))
                )
                addView(itemCol, LinearLayout.LayoutParams(0, dp(64), 1f))
            }
        }.apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(76), Gravity.BOTTOM
            )
        }
    }

    private fun buildHistoryBottomNav(): View {
        return LinearLayout(this).horizontal().apply {
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(4), dp(10), dp(8))
            background = verticalGradient("#000B0B0F", "#B30B0B0F")
            val items = listOf("My playlist", "Home", "For you", "Me")
            val icons = listOf("☰", "⌂", "★", "👤")
            items.forEachIndexed { index, title ->
                val isActive = title == "My playlist"
                val itemCol = LinearLayout(this@MainActivity).vertical().apply {
                    gravity = Gravity.CENTER
                    setPadding(dp(8), dp(4), dp(8), 0)
                    setOnClickListener {
                        when (title) {
                            "My playlist" -> showHistory()
                            "Home" -> showHome()
                        }
                    }
                }
                itemCol.addView(
                    label(icons[index], 20f, if (isActive) color("#FF6B8A") else Color.WHITE).apply {
                        gravity = Gravity.CENTER
                        alpha = if (isActive) 1f else 0.5f
                    }
                )
                itemCol.addView(
                    label(title, 11f, if (isActive) color("#FF6B8A") else Color.WHITE).apply {
                        gravity = Gravity.CENTER
                        alpha = if (isActive) 1f else 0.5f
                    },
                    margin(top = dp(4))
                )
                addView(itemCol, LinearLayout.LayoutParams(0, dp(64), 1f))
            }
        }.apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(76), Gravity.BOTTOM
            )
        }
    }

    // ── Countdown ────────────────────────────────────────────────────

    private fun startCountdown() {
        handler.post(object : Runnable {
            override fun run() {
                countdownView?.text = formatCountdown()
                countdownSeconds = (countdownSeconds - 1).coerceAtLeast(0)
                handler.postDelayed(this, 1000L)
            }
        })
    }

    private fun formatCountdown(): String {
        val days = countdownSeconds / 86400
        val hours = countdownSeconds % 86400 / 3600
        val minutes = countdownSeconds % 3600 / 60
        val seconds = countdownSeconds % 60
        return "${days}D  ${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
    }

    private fun Int.pad(): String = toString().padStart(2, '0')

    // ── Status bar icons ─────────────────────────────────────────────

    private fun buildSignalIcon(): View {
        return LinearLayout(this).horizontal().apply {
            gravity = Gravity.BOTTOM
            listOf(dp(4), dp(7), dp(10), dp(12)).forEachIndexed { i, h ->
                addView(View(context).apply {
                    background = rounded("#FFFFFFFF", 0.8f)
                }, LinearLayout.LayoutParams(dp(3), h).apply {
                    marginEnd = if (i < 3) dp(1.5f).toInt() else 0
                })
            }
        }
    }

    private fun buildWifiIcon(): View {
        return label("WiFi", 10f, Color.WHITE).apply { alpha = 0.9f }
    }

    private fun buildBatteryIcon(): View {
        val frame = FrameLayout(this)
        frame.addView(
            View(this@MainActivity).apply {
                background = rounded("#59FFFFFF", 1.2f)
            },
            FrameLayout.LayoutParams(dp(22), dp(11))
        )
        frame.addView(
            View(this@MainActivity).apply {
                background = rounded("#FFFFFFFF", 1f)
            },
            FrameLayout.LayoutParams(dp(18), dp(8), Gravity.CENTER or Gravity.START).apply {
                leftMargin = dp(2)
            }
        )
        frame.addView(
            View(this@MainActivity).apply {
                background = rounded("#FFFFFFFF", 1f)
            },
            FrameLayout.LayoutParams(dp(2), dp(4), Gravity.END or Gravity.CENTER_VERTICAL).apply {
                rightMargin = dp(-2)
            }
        )
        return frame
    }

    // ── Helpers ──────────────────────────────────────────────────────

    private fun label(text: String, sp: Float, textColor: Int, style: Int = Typeface.NORMAL): TextView {
        return TextView(this).apply {
            this.text = text
            setTextColor(textColor)
            textSize = sp
            typeface = if (style == Typeface.BOLD) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            includeFontPadding = true
        }
    }

    private fun LinearLayout.vertical(): LinearLayout =
        apply { orientation = LinearLayout.VERTICAL }

    private fun LinearLayout.horizontal(): LinearLayout =
        apply { orientation = LinearLayout.HORIZONTAL }

    private fun View.weighted(): View = apply {
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    }

    private fun <T : View> T.match(): T = apply {
        layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun space(width: Int, height: Int): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(width, height)
    }

    private fun margin(
        start: Int = 0, top: Int = 0, end: Int = 0, bottom: Int = 0
    ): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = start
            topMargin = top
            marginEnd = end
            bottomMargin = bottom
        }
    }

    private fun rounded(fill: String, radiusDp: Float, strokeColor: String? = null): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color(fill))
            cornerRadius = dp(radiusDp)
            strokeColor?.let { setStroke(dp(1), color(it)) }
        }
    }

    private fun verticalGradient(top: String, bottom: String): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color(top), color(bottom))
        )
    }

    private fun horizontalGradient(start: String, end: String): GradientDrawable {
        return GradientDrawable(
            GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(color(start), color(end))
        )
    }

    private fun color(hex: String): Int = Color.parseColor(hex)

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun dp(value: Float): Float = value * resources.displayMetrics.density

    private fun FrameLayout.fadeReplace(view: View) {
        view.alpha = 0f
        removeAllViews()
        addView(view, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ))
        view.animate().alpha(1f).setDuration(200)
            .setInterpolator(DecelerateInterpolator()).start()
    }

    private enum class Screen { Home, Player, History }
}

private object ImageLoader {
    private val executor = Executors.newFixedThreadPool(4)
    private val main = Handler(Looper.getMainLooper())

    fun load(url: String, target: ImageView) {
        if (url.isBlank()) return
        target.tag = url
        executor.execute {
            runCatching {
                URL(url).openStream().use { BitmapFactory.decodeStream(it) }
            }.onSuccess { bitmap ->
                main.post {
                    if (target.tag == url && bitmap != null) {
                        target.setImageBitmap(bitmap)
                    }
                }
            }
        }
    }
}
