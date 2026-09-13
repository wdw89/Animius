package com.lanlinju.animius.data.remote.parse.util

import android.content.Context
import androidx.core.content.edit
import com.lanlinju.animius.application.AnimeApplication
import com.lanlinju.animius.util.SourceHolder

/**
 * 站点 Web 鉴权管理器
 *
 * 有些数据源必须先登录网页才能取到播放地址（如次元城），本类按数据源保存登录结果，
 * 并兼作"通知 UI 拉起网页"的通道。
 *
 * - **Token**：网页登录后站点写入 localStorage/sessionStorage 的 Bearer token，用 [getToken] 读取
 * - **请求**：数据源检测到需要登录时设置 [pendingWebAuth]，UI 读取后拉起
 *   [com.lanlinju.animius.presentation.screen.webauth.WebAuthActivity]
 *
 * 数据以 [SourceHolder.currentSourceMode] 为 key 隔离，切换数据源互不影响。
 */
object SourceAuthManager {

    /**
     * 待用户处理的登录请求。
     *
     * 数据源在检测到需要用户介入时设置 [pendingWebAuth]，UI 读取后拉起 WebView。
     */
    data class PendingWebAuth(
        /** 需要在 WebView 中打开的网址 */
        val url: String,
        /** 提示标题 */
        val title: String,
        /** 用于从页面读取登录 token 的 JS 表达式（应返回 token 字符串） */
        val tokenScript: String
    )

    private val CUR_KEY_TOKEN: String
        get() {
            return SourceHolder.currentSourceMode.name + "_Token"
        }

    private const val PREF_NAME = "source_auth"

    /**
     * 检测到需要登录时的请求，供 ViewModel 读取
     */
    var pendingWebAuth: PendingWebAuth? = null

    private val prefs by lazy {
        AnimeApplication.getInstance().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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