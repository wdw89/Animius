package com.lanlinju.animius.data.remote.parse

import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.AnimeDetailBean
import com.lanlinju.animius.data.remote.dto.EpisodeBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import com.lanlinju.animius.data.remote.dto.VideoBean
import com.lanlinju.animius.data.remote.parse.util.WebViewUtil
import com.lanlinju.animius.util.DownloadManager
import com.lanlinju.animius.util.getDefaultDomain
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

class XifanSource : AnimeSource {
    // 稀饭动漫:使用 anime.xifanacg.com(AniBaka 等客户端验证过的活跃域名)
    override val DEFAULT_DOMAIN: String = "https://anime.xifanacg.com/"
    override var baseUrl: String = getDefaultDomain()

    private val webViewUtil: WebViewUtil by lazy { WebViewUtil() }

    override fun onExit() {
        webViewUtil.clearWeb()
    }

    override suspend fun getHomeData(): List<HomeBean> {
        val source = DownloadManager.getHtml(baseUrl)
        val document = Jsoup.parse(source)

        val homeBeanList = mutableListOf<HomeBean>()
        document.select("div.box-width.wow").takeLast(2).forEach { element ->
            val title = element.select("h4").text()
            val moreUrl = element.select("a").attr("href")
            val homeItemBeanList = getAnimeList(element.select("div.public-list-box"))
            homeBeanList.add(HomeBean(title = title, moreUrl = moreUrl, animes = homeItemBeanList))
        }

        return homeBeanList
    }

    private fun getAnimeList(elements: Elements): List<AnimeBean> {
        val animeList = mutableListOf<AnimeBean>()
        elements.forEach { el ->
            val title = el.select("div.public-list-button > a").text()
            val url = el.select("a").attr("href")
            val imgUrl = el.select("img").attr("data-src")
            val episodeName = el.select("span.public-list-prb").text()
            animeList.add(AnimeBean(title = title, img = imgUrl, url = url, episodeName))
        }
        return animeList
    }

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        val source = DownloadManager.getHtml("$baseUrl${detailUrl.removePrefix("/")}")
        val document = Jsoup.parse(source)

        val detailInfo = document.select("div.detail-info")
        val title = detailInfo.select("h3").text()
        val desc = document.select("div#height_limit").text()
        val imgUrl = document.select("div.detail-pic > img").attr("data-src")
        val tags = detailInfo.select("span.slide-info-remarks").map { it.text() }
        val channels = getAnimeEpisodes(document)
        val relatedAnimes =
            getAnimeList(document.select("div.box-width.wow").select("div.public-list-box"))

        return AnimeDetailBean(title, imgUrl, desc, tags, relatedAnimes, channels = channels)
    }

    private fun getAnimeEpisodes(document: Document): Map<Int, List<EpisodeBean>> {
        val channels = mutableMapOf<Int, List<EpisodeBean>>()
        document.select("div.anthology-list.top20.select-a > div.anthology-list-box")
            .forEachIndexed { i, e ->
                val dramaElements = e.select("li").select("a")//剧集列表
                val episodes = mutableListOf<EpisodeBean>()
                dramaElements.forEach { el ->
                    val name = el.text()
                    val url = el.attr("href")
                    episodes.add(EpisodeBean(name, url))
                }
                channels[i] = episodes
            }
        return channels
    }

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        val videoUrl = getVideoUrl("$baseUrl${episodeUrl.removePrefix("/")}")
        // 对 URL 做安全编码(中文路径等非 ASCII 字符需编码,否则 dl.playxf.top 等 CDN 返回 400)
        val encoded = encodeUrlNonAscii(videoUrl)
        // 只带 UA,不要带站点 Referer:
        // 线路1 会 302 跳到联通沃云盘(hydownload.pan.wo.cn/openapi/download),
        // 该 CDN 拒绝非同站 Referer 并返回 400,且 ExoPlayer 会把 Referer 带到重定向后的请求上。
        // 线路2(play.xfvod.pro:8088)已验证对 Referer 完全不敏感,去掉不回归。
        return VideoBean(
            videoUrl = encoded,
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36"
            )
        )
    }

    /**
     * 只编码 URL 中的非 ASCII 字符(中文等),保留协议/域名/路径分隔符/查询参数。
     * 因为播放器拿到的 URL 含未编码中文(如 /新番/...),部分 CDN 会返回 400。
     */
    private fun encodeUrlNonAscii(url: String): String {
        val sb = StringBuilder()
        for (ch in url) {
            val c = ch.code
            if (ch == '%' || ch == '/' || ch == ':' || ch == '?' || ch == '&' || ch == '=' ||
                ch == '-' || ch == '_' || ch == '.' || ch == '~' || ch == '+' ||
                (c in 0x30..0x39) || (c in 0x41..0x5A) || (c in 0x61..0x7A)
            ) {
                sb.append(ch)
            } else {
                // 非 ASCII 或特殊字符,UTF-8 编码
                val bytes = ch.toString().toByteArray(Charsets.UTF_8)
                for (b in bytes) {
                    sb.append('%').append(String.format("%02X", b))
                }
            }
        }
        return sb.toString()
    }

    override suspend fun getSearchData(
        query: String,
        page: Int
    ): List<AnimeBean> {
        // 稀饭动漫搜索页是 JS 渲染,HTML 解析无结果;改用 maccmsSuggest API(AniBaka 验证方案)
        val suggestUrl = "${baseUrl}index.php/ajax/suggest?mid=1&wd=${query.encodeForUrl()}"
        val json = DownloadManager.getHtml(suggestUrl)
        val animeList = mutableListOf<AnimeBean>()
        try {
            val obj = org.json.JSONObject(json)
            val list = obj.optJSONArray("list") ?: org.json.JSONArray()
            for (i in 0 until list.length()) {
                val item = list.getJSONObject(i)
                val id = item.optString("id")
                val title = item.optString("name")
                val img = item.optString("pic")
                if (id.isNotBlank() && title.isNotBlank()) {
                    animeList.add(AnimeBean(title = title, img = img, url = "/bangumi/$id.html"))
                }
            }
        } catch (e: Exception) {
            // 解析失败返回空列表
        }
        return animeList
    }

    override suspend fun getWeekData(): Map<Int, List<AnimeBean>> {
        val source = DownloadManager.getHtml(baseUrl)
        val document = Jsoup.parse(source)
        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        document.select("div#week-module-box")
            .select("div.public-r").forEachIndexed { index, element ->
                val dayList = getAnimeList(element.select("div.public-list-box"))
                weekMap[index] = dayList
            }
        return weekMap
    }

    private suspend fun getVideoUrl(url: String): String {
        // 播放页内嵌 player_aaaa 对象,url 字段可能是 JSON 转义形式(\/ 和 \uXXXX),需解码
        val source = DownloadManager.getHtml(url)

        // 分支1: player_aaaa / player_aaaa 对象的 url 字段(JSON 转义) → 反转义后即真实地址
        val aaaa = Regex("var player_aaaa=\\{([\\s\\S]*?)\\}\\s*;").find(source)?.groupValues?.get(1)
        if (aaaa != null) {
            Regex("\"url\"\\s*:\\s*\"([^\"]+)\"").find(aaaa)?.groupValues?.get(1)
                ?.let { return unescapeJson(it) }
        }

        // 分支2: 任意 "url" 字段(JSON 转义) → 反转义
        Regex("\"url\"\\s*:\\s*\"((?:https?|//)[^\"]+)\"").find(source)
            ?.groupValues?.get(1)
            ?.let { return unescapeJson(it) }

        // 分支3: 已解码的 next 字段(直链 mp4/m3u8)
        Regex("\"next\"\\s*:\\s*\"(https?://[^\"]+)\"").find(source)
            ?.groupValues?.get(1)
            ?.let { return it }

        // 分支4: 页面中任意 mp4/mkv/m3u8 直链(含转义 \/ 形式)
        Regex("https?:\\\\?/\\\\?/[^\"'\\s]+\\.(mp4|mkv|m3u8)(\\?[^\"'\\s]*)?")
            .find(source)?.groupValues?.get(0)?.let { return unescapeJson(it) }
        Regex("https?://[^\"'\\s]+\\.(mp4|mkv|m3u8)(\\?[^\"'\\s]*)?")
            .find(source)?.groupValues?.get(0)?.let { return it }

        // 分支5: iframe data-src / src(嵌套解析器,直接返回交给播放器或 WebView 跟进)
        Regex("<iframe[^>]+data-src=\"([^\"]+)\"")
            .find(source)?.groupValues?.get(1)?.let { return it }
        Regex("<iframe[^>]+src=\"([^\"]+)\"")
            .find(source)?.groupValues?.get(1)?.let { return it }

        // 分支6: WebView 拦截兜底
        return webViewUtil.interceptRequest(
            url = url,
            regex = ".*\\.(mp4|mkv|m3u8).*|akamaized|bilivideo.com|play.xfvod.pro|playxf.top",
            timeoutMs = 25_000,
            userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/137.0.0.0 Safari/537.36",
        )
    }

    /**
     * 解码 JSON 字符串转义: \/ → /, \uXXXX → Unicode 字符
     */
    private fun unescapeJson(s: String): String {
        if (!s.contains("\\")) return s
        var out = s.replace("\\/", "/")
        // 解码 \uXXXX(正则 \\u 匹配字面量反斜杠+u)
        val re = Regex("\\\\u([0-9a-fA-F]{4})")
        var prev: String
        do {
            prev = out
            out = re.replace(out) { m ->
                m.groupValues[1].toInt(16).toChar().toString()
            }
        } while (out != prev && out.contains("\\u"))
        return out
    }

    private fun String.encodeForUrl(): String =
        java.net.URLEncoder.encode(this, "UTF-8")
}