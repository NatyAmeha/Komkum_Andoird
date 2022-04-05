package com.komkum.komkum.data.model

import androidx.annotation.Keep

enum class NotificationTypes{
    NEW_SONG , NEW_ALBUM , NEW_EPISODE,
    SUBSCRIPTION_UPGRADE , WALLET_RECHARGE ,
    REWARD , ARTIST_DONATION , PODCAST_DONATION , BOOK_DONATION , ORDER_STATUS_CHANGE ,
    TEAM_FORMATION_COMPLETE , GAME_TEAM_ACTIVE, GAME_REWARD,
    FMCG_REMINDER , TEAM_EXPIRATION_REMINDER
}

@Keep
data class MNotification(
    var title : String? = null,
    var body : String? = null,
    var image : String? = null,
    var intent : String? = null,
    var intentType : String? = null
)
