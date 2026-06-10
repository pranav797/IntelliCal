package com.cs346.intellical.data

import com.cs346.intellical.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

object AuthRepository {
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_KEY
        ) {
            install(Auth)
        }
    }

    suspend fun signIn(emailStr: String, passwordStr: String): Int = withContext(Dispatchers.IO) {
        try {
            client.auth.signInWith(Email) {
                email = emailStr
                password = passwordStr
            }
            val userId = getCurrentUserId()
            if (userId == null || userId == 0) {
                throw Exception("Sign in failed to fetch user ID")
            }
            userId
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign-in error", e)
            throw e
        }
    }

    suspend fun signUp(emailStr: String, passwordStr: String): Int = withContext(Dispatchers.IO) {
        try {
            val res = client.auth.signUpWith(Email) {
                email = emailStr
                password = passwordStr
            }
            val userId = res?.id?.hashCode() ?: getCurrentUserId()
            if (userId == null || userId == 0) {
                throw Exception("Sign up failed to retrieve user ID")
            }
            userId
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign-up error", e)
            throw e
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            client.auth.signOut()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign-out error", e)
        }
    }

    suspend fun restoreSession(): Boolean = withContext(Dispatchers.IO) {
        try {
            val sessionLoaded = client.auth.loadFromStorage()
            sessionLoaded
        } catch (e: Exception) {
            Log.e("AuthRepository", "Session restore error", e)
            false
        }
    }

    fun getCurrentUserId(): Int? {
        val user = client.auth.currentUserOrNull()
        return user?.id?.hashCode()
    }
}
