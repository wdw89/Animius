package com.lanlinju.animius.data.remote.parse.util

import android.content.Context
import android.webkit.CookieManager
import androidx.core.content.edit
import com.lanlinju.animius.application.AnimeApplication
import com.lanlinju.animius.util.SourceHolder

/**
 * 站点 Web 鉴权信息管理器
 *
 * 有些站点在浏览或播放前需要用户先在网页上完成操作（验证码 / 登录），本类按数据源保存这些
 * 操作的结果，并兼作"通知 UI 拉起网页"的通道。
 *
 * - **Cookie**：验证码通过后由 WebView 同步过来，用 [getCookies] 读取
 * - **Token**：网页登录后站点写入 localStorage/sessionStorage 的 Bearer token，用 [getToken] 读取
 *
 * 数据以 [SourceHolder.currentSourceMode] 为 key 隔离，切换数据源互不影响。
 */
object CaptchaCookieManager {

    /**
     * 待用户处理的 Web 鉴权请求（验证码或登录）。
     *
     * 数据源在检测到需要用户介入时设置 [pendingWebAuth]，UI 读取后拉起 WebView。
     */
    data class PendingWebAuth(
        /** 需要在 WebView 中打开的网址 */
        val url: String,
        /** 提示标题 */
        val title: String,
        /**
         * 用于从页面读取登录 token 的 JS 表达式（应返回 token 字符串）。
         * 为空表示按验证码模式处理：只保存 Cookie，不读取 token。
         */
        val tokenScript: String = ""
    )

    val CUR_KEY_COOKIE: String
        get() {
            return SourceHolder.currentSourceMode.name + "_Cookie"
        }

    private val CUR_KEY_TOKEN: String
        get() {
            return SourceHolder.currentSourceMode.name + "_Token"
        }

    private const val PREF_NAME = "captcha_cookies"

    /**
     * 检测到需要验证码/登录时的请求，供 ViewModel 读取
     */
    var pendingWebAuth: PendingWebAuth? = null

    private val prefs by lazy {
        AnimeApplication.getInstance().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 保存验证码 Cookie（以 URL 为 key）
     */
    fun saveCookies(key: String, cookies: String) {
        prefs.edit { putString(key, cookies) }
    }

    /**
     * 获取保存的验证码 Cookie
     */
    fun getCookies(key: String): String {
        return prefs.getString(key, "") ?: ""
    }

    /**
     * 清除指定 URL 的验证码 Cookie
     */
    fun clearCookies(key: String) {
        prefs.edit { remove(key) }
    }

    /**
     * 从 WebView CookieManager 同步 Cookie 到本地存储
     */
    fun syncFromWebView(url: String) {
        val cookies = CookieManager.getInstance().getCookie(url) ?: ""
        if (cookies.isNotEmpty()) {
            saveCookies(url, cookies)
        }
    }

    /**
     * 保存当前数据源的登录 token
     */
    fun saveToken(token: String) {
        prefs.edit { putString(CUR_KEY_TOKEN, token) }
    }

    /**
     * 获取当前数据源的登录 token，未登录时返回空串
     */
    fun getToken(): String {
        return prefs.getString(CUR_KEY_TOKEN, "") ?: ""
    }

    /**
     * 清除当前数据源的登录 token（退出登录）
     */
    fun clearToken() {
        prefs.edit { remove(CUR_KEY_TOKEN) }
    }

}