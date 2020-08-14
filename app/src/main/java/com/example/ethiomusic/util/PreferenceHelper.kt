package com.example.ethiomusic.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.Editable
import java.lang.UnsupportedOperationException

object PreferenceHelper {
    fun getInstance(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    fun SharedPreferences.edit(operation: (edit: SharedPreferences.Editor) -> Unit) {
        var editor = this.edit()
        operation(editor)
    }

    operator fun SharedPreferences.set(key: String, value: Any) {
        when (value) {
            is Editable -> {
                if (value.toString().isNotBlank()) {
                    edit {
                        it.putString(key, value.toString())
                        it.commit()
                    }
                }
            }
            is String -> {
                if (value.isNotBlank()) {
                    edit {
                        it.putString(key, value)
                        it.apply()
                    }
                }
            }
            is Int -> {
                edit {
                    it.putInt(key, value)
                    it.apply()
                }
            }
            is Long -> {
                edit {
                    it.putLong(key, value)
                    it.apply()
                }
            }
            is Boolean -> {
                edit {
                    it.putBoolean(key, value)
                    it.apply()
                }
            }
            else -> {
            }
        }
    }

    inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T =
        when (T::class) {
            String::class -> {
                getString(key, defaultValue as String) as T
            }
            Int::class -> {
                getInt(key, defaultValue as Int) as T
            }
            Boolean::class -> {
                getBoolean(key, defaultValue as Boolean) as T
            }
            Long::class -> {
                getLong(key, defaultValue as Long) as T
            }
            else -> {
                throw UnsupportedOperationException()
            }
        }
}