package com.lanlinju.animius.parse.xifan

import com.lanlinju.animius.data.remote.parse.parseXifanVideoUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

/**
 * 稀饭动漫播放地址解析的纯函数单测(JVM,不需要设备/网络)。
 *
 * 固定样本取自线上真实播放页(2026-09-16,`/watch/3559/{1,2,3}/1.html`),
 * 只截取了 `player_aaaa` 所在的那一行 <script>,见 `html/line*.html`。
 *
 * 这三条线路是站点的全部线路,所以它们同时也是"这个解析器要覆盖的全部形态":
 *  - 线路1 apn.moedot.net       路径中带 %XX
 *  - 线路2 play.xfvod.pro:8088  带端口
 *  - 线路3 dl.playxf.top        路径含中文 + \uXXXX 转义 + 扩展名 m3u8
 */
class XifanSourceTest {

    private fun fixture(name: String): String =
        File("./src/test/java/com/lanlinju/animius/parse/xifan/html/$name")
            .readText(Charsets.UTF_8)

    @Test
    fun line1IsParsed() {
        // 路径里的 %XX 原样保留,不要在解析阶段再编码一次
        assertEquals(
            "https://apn.moedot.net/d/wo/2607/%E9%BE%9901.mp4",
            parseXifanVideoUrl(fixture("line1.html"))
        )
    }

    @Test
    fun line2IsParsed() {
        assertEquals(
            "https://play.xfvod.pro:8088/temp/2607/%E9%BE%9901.mp4",
            parseXifanVideoUrl(fixture("line2.html"))
        )
    }

    @Test
    fun line3IsParsedWithUnicodeDecoded() {
        // \u65b0\u756a 等转义必须还原成中文,否则 CDN 取不到文件
        assertEquals(
            "https://dl.playxf.top/新番/2607/M-猫与龙/01/龙01.m3u8",
            parseXifanVideoUrl(fixture("line3.html"))
        )
    }

    @Test
    fun allThreeLinesAreNonEmpty() {
        // 三条线路是站点的全部线路,任何一条解析不出来都应当在这一层暴露
        listOf("line1.html", "line2.html", "line3.html").forEach { name ->
            val url = parseXifanVideoUrl(fixture(name))
            assertEquals("$name 应能解析出地址", true, !url.isNullOrBlank())
        }
    }

    @Test
    fun takesCurrentEpisodeNotTheNextOne() {
        // player_aaaa 同时含 url(本集)与 url_next(下一集)。
        // 若正则不够精确而先命中 url_next,用户点第 1 集会播成第 2 集。
        parseXifanVideoUrl(fixture("line1.html"))!!.let {
            assertEquals("应当取本集(01)", true, it.contains("01.mp4"))
            assertEquals("不能取到下一集(02)", false, it.contains("02.mp4"))
        }
    }

    @Test
    fun fallsBackToAnyDirectLink() {
        // 站点改模板、url 字段改名时的兜底:整页找直链
        val html = """<script>var cfg={"src":"https:\/\/cdn.example.com\/a\/b.m3u8"};</script>"""

        assertEquals("https://cdn.example.com/a/b.m3u8", parseXifanVideoUrl(html))
    }

    @Test
    fun ignoresRelativeOrNonMediaUrls() {
        // 相对地址与页面里的普通链接都不能当成播放地址返回,
        // 否则播放器会拿它去请求,报出来的是难懂的解析错误
        val html = """
            <script>
            var player_aaaa={"link":"\/watch\/3559\/1\/1.html","pic":"\/upload\/a.jpg",
                             "web":"https://anime.xifanacg.com/about.html"};
            </script>
        """.trimIndent()

        assertNull(parseXifanVideoUrl(html))
    }

    @Test
    fun returnsNullWhenNothingFound() {
        assertNull(parseXifanVideoUrl(""))
        assertNull(parseXifanVideoUrl("<html><body>no player here</body></html>"))
    }
}
