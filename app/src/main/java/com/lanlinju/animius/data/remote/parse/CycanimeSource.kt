package com.lanlinju.animius.data.remote.parse

import com.lanlinju.animius.data.remote.dto.AnimeBean
import com.lanlinju.animius.data.remote.dto.AnimeDetailBean
import com.lanlinju.animius.data.remote.dto.EpisodeBean
import com.lanlinju.animius.data.remote.dto.HomeBean
import com.lanlinju.animius.data.remote.dto.VideoBean
import com.lanlinju.animius.data.remote.parse.util.CaptchaCookieManager
import com.lanlinju.animius.util.DownloadManager
import com.lanlinju.animius.util.getDefaultDomain
import org.json.JSONArray
import org.json.JSONObject

/**
 * 规范化成 `Bearer <token>`。
 *
 * 站点下发的 token 本身可能已经带 `Bearer ` 前缀——站点前端也是这么防御的:
 * `p2(n){return /^Bearer\s+/i.test(n.trim()) ? n.trim() : "Bearer " + n}`
 *
 * 若直接拼 `"Bearer $token"` 会得到 `Bearer Bearer eyJ...`,服务端返回 401。
 * (实测:重复前缀 401,单个前缀 200)
 */
internal fun String.toBearerValue(): String {
    val t = trim()
    return if (t.startsWith("Bearer", ignoreCase = true)) t else "Bearer $t"
}

/**
 * Cycani(次元城动画) 数据源
 *
 * 官网: https://www.cycani.org/
 * 说明:
 * - 站点前端是 SPA,Jsoup 无法解析 HTML;改用官方 JSON API(参考 AniBaka cycani.json):
 *   - 搜索: /api/videos/search?q={kw}&page=1&page_size=20
 *   - 首页: /api/videos?zone_id={N}&page=1&page_size=20
 *   - 详情: /api/videos/{id} + /api/videos/{id}/sections?player_code=cychub&page=1&page_size=100
 *   - 播放: /api/sections/{id}/play-url
 *   - 时间表: /api/index/weekday (返回 7 天 {weekday, videos[]})
 * - 需要特殊请求头(X-App-Name / X-Time-Zone / X-App-Version / Accept: application/json)。
 */
object CycanimeSource : AnimeSource {

    override val DEFAULT_DOMAIN: String = "https://www.cycani.org/"
    override var baseUrl: String = getDefaultDomain()

    private val baseHeaders = mapOf(
        "Accept" to "application/json",
        "X-App-Name" to "cyc_web",
        "X-Time-Zone" to "Asia/Shanghai",
        "X-App-Version" to "cycweb",
        "Referer" to baseUrl
    )

    /**
     * 带上登录态的请求头。播放地址接口需要登录,登录后由 [CaptchaCookieManager] 提供 token。
     */
    private fun authHeaders(): Map<String, String> {
        val token = CaptchaCookieManager.getToken().trim()
        return if (token.isEmpty()) baseHeaders
        else baseHeaders + ("Authorization" to token.toBearerValue())
    }

    /**
     * 从页面读取登录 token 的 JS 表达式。
     * 站点把会话写在 Web Storage:`cycweb:auth:v2`(localStorage,勾选"保持登录")
     * 或 `cycweb:auth:v1`(sessionStorage),值为 `{"token":"...","expiresAt":...}`。
     */
    internal val LOGIN_TOKEN_SCRIPT = """
        (function() {
            try {
                var raw = localStorage.getItem('cycweb:auth:v2')
                    || sessionStorage.getItem('cycweb:auth:v1');
                if (!raw) return '';
                var session = JSON.parse(raw);
                return (session && session.token) ? String(session.token) : '';
            } catch (e) {
                return '';
            }
        })()
    """.trimIndent()

    private fun String.withoutSlash() = removeSuffix("/")

    // ---------- 首页 ----------

    override suspend fun getHomeData(): List<HomeBean> {
        // 拉取几个 zone 作为首页板块
        val zoneNames = listOf("日番", "国漫", "剧场", "合集")
        val homeBeanList = mutableListOf<HomeBean>()
        for (zoneId in 1..4) {
            val list = fetchZone(zoneId, 20)
            if (list.isNotEmpty()) {
                homeBeanList.add(
                    HomeBean(
                        title = zoneNames.getOrElse(zoneId - 1) { "分区$zoneId" },
                        moreUrl = "",
                        animes = list
                    )
                )
            }
        }
        return homeBeanList
    }

    private suspend fun fetchZone(zoneId: Int, pageSize: Int): List<AnimeBean> {
        val json = requestJson("$baseUrl/api/videos?zone_id=$zoneId&page=1&page_size=$pageSize") ?: return emptyList()
        return parseVideoList(json)
    }

    // ---------- 详情 ----------

    override suspend fun getAnimeDetail(detailUrl: String): AnimeDetailBean {
        // detailUrl 形如 /anime/3861
        val id = Regex("""(\d+)""").find(detailUrl)?.groupValues?.get(1)
            ?: throw IllegalStateException("无效的详情地址: $detailUrl")

        val emptyChannels: Map<Int, List<EpisodeBean>> = emptyMap()
        val emptyEpisodes: List<EpisodeBean> = emptyList()
        val emptyAnimes: List<AnimeBean> = emptyList()
        val infoJson = requestJson("$baseUrl/api/videos/$id") ?: return AnimeDetailBean(
            "", "", "", emptyList(), emptyAnimes, emptyEpisodes, emptyChannels
        )
        val data = infoJson.optJSONObject("data") ?: return AnimeDetailBean(
            "", "", "", emptyList(), emptyAnimes, emptyEpisodes, emptyChannels
        )
        val title = data.optString("title")
        val desc = data.optString("description")
        val img = data.optString("cover_url")
        val tags = data.optJSONArray("tags")?.let { arr ->
            (0 until arr.length()).map { arr.optString(it) }
        } ?: emptyList()

        // 剧集列表
        val secJson = requestJson("$baseUrl/api/videos/$id/sections?player_code=cychub&page=1&page_size=100")
        val channels = mutableMapOf<Int, List<EpisodeBean>>()
        val episodes = mutableListOf<EpisodeBean>()
        secJson?.optJSONObject("data")?.optJSONArray("list")?.let { list ->
            for (i in 0 until list.length()) {
                val item = list.optJSONObject(i)
                val eid = item.optString("id")
                val name = item.optString("title")
                if (eid.isNotBlank()) {
                    episodes.add(EpisodeBean(name = name.ifBlank { "第${episodes.size + 1}集" }, url = "/api/v2/sections/$eid/play-url"))
                }
            }
        }
        if (episodes.isNotEmpty()) channels[0] = episodes

        return AnimeDetailBean(
            title = title,
            imgUrl = img,
            desc = desc,
            tags = tags,
            relatedAnimes = emptyList(),
            channels = channels
        )
    }

    // ---------- 播放 ----------

    override suspend fun getVideoData(episodeUrl: String): VideoBean {
        // episodeUrl 形如 /api/v2/sections/{id}/play-url
        val playUrl = if (episodeUrl.startsWith("http")) episodeUrl else "$baseUrl${episodeUrl.removePrefix("/")}"
        val json = requestJson(playUrl) ?: throw IllegalStateException("播放地址请求失败")

        // 播放地址接口需要登录鉴权:未登录时服务端返回
        // 401 {"code":401,"msg":"unauthorized","data":null}
        // 站点自身也是这样处理的——未登录时播放页只显示"请登录后观看"占位。
        val code = json.optInt("code", -1)
        if (code == 401) {
            // 通知 UI 拉起网页登录(复用验证码的 WebView 通道),登录成功后由调用方重试
            CaptchaCookieManager.pendingWebAuth = CaptchaCookieManager.PendingWebAuth(
                url = "${baseUrl}login",
                title = "登录次元城",
                tokenScript = LOGIN_TOKEN_SCRIPT
            )
            throw IllegalStateException("次元城需要登录后才能播放,请在弹出的页面完成登录")
        }

        // data 为 JSON null 时 org.json 的 optString 会返回字面量 "null",必须一并判掉,
        // 否则会把 "null" 当成播放地址交给播放器,报 MalformedURLException: no protocol: null。
        val url = json.optJSONObject("data")?.optString("url").orEmpty()
        if (url.isBlank() || url == "null") {
            // 优先透出服务端的失败原因(如"播放地址解析失败"),便于用户判断是换线路还是稍后重试
            val msg = json.optString("msg").takeIf { it.isNotBlank() && it != "null" }
            throw IllegalStateException(msg ?: "该线路无视频资源,请切换其他线路")
        }
        return VideoBean(url)
    }

    // ---------- 搜索 ----------

    override suspend fun getSearchData(query: String, page: Int): List<AnimeBean> {
        val url = "$baseUrl/api/videos/search?q=${query.encodeForUrl()}&page=1&page_size=20"
        val json = requestJson(url) ?: return emptyList()
        return parseVideoList(json)
    }

    private fun parseVideoList(json: JSONObject): List<AnimeBean> {
        val list = json.optJSONObject("data")?.optJSONArray("list") ?: return emptyList()
        val result = mutableListOf<AnimeBean>()
        for (i in 0 until list.length()) {
            val item = list.optJSONObject(i)
            val id = item.optString("video_id").ifBlank { item.optString("id") }
            val title = item.optString("title")
            val img = item.optString("cover_url")
            if (id.isNotBlank() && title.isNotBlank()) {
                result.add(AnimeBean(title = title, img = img, url = "/anime/$id"))
            }
        }
        return result
    }

    // ---------- 周番(/api/index/weekday 时间表接口) ----------

    override suspend fun getWeekData(): Map<Int, List<AnimeBean>> {
        // 新版 SPA 时间表接口(从站点 JS 分包逆向): GET /api/index/weekday
        // 一次返回 7 天: data.list = [{weekday:1..7, videos:[{video_id,title,cover_url,remarks,...}]}]
        // weekday 1=周一 ... 7=周日,映射为 map key 0..6
        val json = requestJson("$baseUrl/api/index/weekday") ?: return emptyMap()
        val list = json.optJSONObject("data")?.optJSONArray("list") ?: return emptyMap()

        val weekMap = mutableMapOf<Int, List<AnimeBean>>()
        for (i in 0 until list.length()) {
            val item = list.optJSONObject(i)
            val weekday = item.optInt("weekday")
            val videos = item.optJSONArray("videos") ?: continue

            val animes = mutableListOf<AnimeBean>()
            for (j in 0 until videos.length()) {
                val v = videos.optJSONObject(j)
                val id = v.optString("video_id")
                val title = v.optString("title")
                val img = v.optString("cover_url")
                val remarks = v.optString("remarks")
                if (id.isNotBlank() && title.isNotBlank()) {
                    animes.add(
                        AnimeBean(
                            title = title,
                            img = img,
                            url = "/anime/$id",
                            episodeName = remarks
                        )
                    )
                }
            }
            if (animes.isNotEmpty()) weekMap[weekday - 1] = animes
        }
        return weekMap
    }

    // ---------- 内部工具 ----------

    private suspend fun requestJson(url: String): JSONObject? {
        return runCatching {
            val source = DownloadManager.getHtml(url, authHeaders())
            if (source.isBlank()) null else JSONObject(source)
        }.getOrNull()
    }

    private fun String.encodeForUrl(): String =
        java.net.URLEncoder.encode(this, "UTF-8")
}