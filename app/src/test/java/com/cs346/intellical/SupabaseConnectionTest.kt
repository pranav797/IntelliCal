package com.cs346.intellical

import com.cs346.intellical.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*

import org.junit.Ignore

@Ignore("Requires live Supabase credentials — run manually against a real backend")
class SupabaseConnectionTest {
    @Test
    fun testSupabaseConnection() = runTest {
        val url = BuildConfig.SUPABASE_URL
        val key = BuildConfig.SUPABASE_KEY
        
        assertNotNull(url)
        assertNotNull(key)
        assertTrue(url.isNotEmpty())
        assertTrue(key.isNotEmpty())

        val client = createSupabaseClient(url, key) {
            install(Auth)
        }

        try {
            val session = client.auth.currentSessionOrNull()
            assertNull(session)
        } catch (e: Exception) {
            fail(e.message)
        }
    }
}
