package com.komkum.komkum.util

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.Editable
import java.lang.UnsupportedOperationException

object PreferenceHelper {

    const val FCM_TOKEN = "FCM_TOKEN"
    const val IS_NEW_FCM_TOKEN = "IS_NEW_FCM_TOKEN"

    const val FIRST_TIME_FOR_WALLET = "FIRST_TIME_WALLET"
    const val FIRST_TIME_USER = "FIRST_TIME_USER"  // user opens the app from installed icon for first time
    const val FIRST_TIME_USER_FROM_LINK = "FIRST_TIME_USER_FROM_LINK"  // user opens the app from shared link
    const val FIRST_TIME_FOR_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"
    const val WALLET_BONUS_DIALOG_BEFORE_SIGN_UP = "WALLET_BONUS_DIALOG_BEFORE_SIGN_UP"
    const val WALLET_BONUS_DIALOG_AFTER_SIGN_UP = "WALLET_BONUS_DIALOG_AFTER_SIGN_UP"
    const val FIRST_TIME_FOR_RECOMMENDATION_DIALOG = "FIRST_TIME_FOR_RECOMMENDATION_DIALOG"
    const val FIRST_TIME_FOR_GAME_INSTRUCTION = "GAME_INSTRUCTION"

    const val FIRST_TIME_FOR_CASH_OUT_REQUEST = "FIRST_TIME_FOR_CASH_OUT"

    const val CASHBACK_DIALOG = "ORDER_CASHBACK_DIALOG"
    const val CASH_ON_DELIVERY_INFO = "CASH_ON_DELIVERY_INFO"

    const val NEW_ENTERTAINMENT_PREFERENCE = "NEW_ENTERTAINMENT_PREFERENCE"


    const val DOWNLOAD_QUALITY = "DOWNLOAD_QUALITY"
    const val DOWNLOAD_QUALITY_LOW = 0
    const val DOWNLOAD_QUALITY_MEDIUM = 1
    const val DOWNLOAD_QUALITY_HIGH = 2

    const val CURRENT_QUESTION_INDEX = "QUESTION_INDEX"

    const val RELOAD_STORE_PAGE ="RELOAD_STORE_PAGE"

    //for firebase analytics
    const val PURCHASED_FROM = "PURCHASED_FROM"

    const val SHOW_SHARE_DIALOG = "SHOW_SHARE_DIALOG"

    // value will be true when user unable to fully added the product from customize page
    const val CLEAR_CART = "CLEAR_CART"


    // track default location of user to give and help him to track his team
    const val DEFAULT_LATITUDE_VALUE = "DEFAULT_LAT_VALUE"
    const val DEFAULT_LONGITUDE_VALUE = "DEFAULT_LONGITUDE_VALUE"
    const val IS_CURRENT_LOCATION_SELECTED = "IS_CURRENT_LOCATION_SELECTED"
    const val ADDRESS_NAME = "ADDRESS_NAME"
    const val RELOAD_TEAM_PAGE_AFTER_DEFAULT_ADDRESS_CHANGED = "RELOAD_TEAM_PAGE_AFTER_ADDRESS_CHANGED"

    //prompt banner for user to join telegram channel
    const val SHOW_JOIN_TELEGRAM_CHANNEL_BANNER = "SHOW_JOIN_TELEGRAM_CHANNEL_BANNER"

    fun getInstance(context: Context) = PreferenceManager.getDefaultSharedPreferences(context)

    fun SharedPreferences.edit(operation: (edit: SharedPreferences.Editor) -> Unit) {
        var editor = this.edit()
        operation(editor)

    }


    operator fun SharedPreferences.set(key: String, value: Any?) {
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
            is Float -> {
                edit{
                    it.putFloat(key , value)
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


     fun SharedPreferences.delete(key : String){
         edit{
             it.remove(key)
             it.apply()
         }
    }

    inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T = when (T::class) {
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
            Float::class -> {
                getFloat(key , defaultValue as Float) as T
            }
            else -> {
                throw UnsupportedOperationException()
            }
        }
}