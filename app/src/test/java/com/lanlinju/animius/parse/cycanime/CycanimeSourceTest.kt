package com.lanlinju.animius.parse.cycanime

import com.lanlinju.animius.data.remote.parse.toBearerValue
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 次元城的纯函数单测(JVM,不需要设备/网络)。
 *
 * 只放不依赖 Android 的逻辑;需要 WebView / Context / 网络的用例见
 * `app/src/androidTest/.../parse/cycanime/CycaniLoginFlowTest.kt`。
 */
class CycanimeSourceTest {

    /**
     * 回归:站点下发的 token 可能自带 `Bearer ` 前缀。
     * 若直接拼 `"Bearer $token"` 会得到 `Bearer Bearer eyJ...`,服务端返回 401。
     */
    @Test
    fun bearerPrefixIsNormalized() {
        val raw = "eyJhbGciOiJIUzI1NiJ9.payload.sig"

        assertEquals("裸 token 应补上前缀", "Bearer $raw", raw.toBearerValue())
    }

    @Test
    fun existingBearerPrefixIsNotDuplicated() {
        val prefixed = "Bearer eyJhbGciOiJIUzI1NiJ9.payload.sig"

        // 已带前缀:原样保留,不得叠加成 "Bearer Bearer ..."
        assertEquals(prefixed, prefixed.toBearerValue())
        // 大小写不敏感
        assertEquals(
            "bearer eyJhbGciOiJIUzI1NiJ9.payload.sig",
            "bearer eyJhbGciOiJIUzI1NiJ9.payload.sig".toBearerValue()
        )
        assertEquals(
            "BEARER eyJhbGciOiJIUzI1NiJ9.payload.sig",
            "BEARER eyJhbGciOiJIUzI1NiJ9.payload.sig".toBearerValue()
        )
    }

    @Test
    fun surroundingWhitespaceIsTrimmed() {
        val raw = "eyJhbGciOiJIUzI1NiJ9.payload.sig"

        assertEquals("Bearer $raw", "  $raw  ".toBearerValue())
        assertEquals(
            "bearer eyJhbGciOiJIUzI1NiJ9.payload.sig",
            "  bearer eyJhbGciOiJIUzI1NiJ9.payload.sig  ".toBearerValue()
        )
    }
}
