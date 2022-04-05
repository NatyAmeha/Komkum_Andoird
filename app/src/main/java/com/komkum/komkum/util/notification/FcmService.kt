package com.komkum.komkum.util.notification
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.komkum.komkum.data.repo.UserRepository
import com.komkum.komkum.util.NotificationHelper
import com.komkum.komkum.util.PreferenceHelper
import com.komkum.komkum.util.PreferenceHelper.set

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.komkum.komkum.data.model.MNotification
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FcmService : FirebaseMessagingService() {
    @Inject
    lateinit var userREpo: UserRepository

    companion object{
        const val F_EVENT_ORDER_COMPLETE = "order_complete"
        const val F_EVENT_PARAM_PURCHASED_FROM = "purchased_from"
        const val F_EVENT_PARAM_VALUE_PURCHASED_FROM_GAME = "purchased_from_game"
        const val F_EVENT_PARAM_VALUE_PURCHASED_FROM_REMINDER = "purchased_from_reminder"
        const val F_EVENT_PARAM_VALUE_PURCHASED_FROM_DIGITAL_CONTENT = "purchased_from_digital_content"
        const val F_EVENT_VALUE_PURCHASED_FROM_TEAM = "purchased_from_team"
        const val F_EVENT_VALUE_PURCHASED_FROM_AD = "purchased_from_ad"

        const val FIREBASEANALYTICS_EVENT_AD_CLICK = "ad_click"
        const val F_EVENT_PARAM_AD_SOURCE= "ad_source"
        const val F_EVENT_PARAM_VALUE_DEIGITAL_CONTENT = "digital_content"
        const val F_EVENT_PARAM_VALUE_BANNER = "banner"


        const val F_EP_SIGN_UP_METHOD = "method"
        const val F_EP_SIGN_UP_SOURCE = "source"
        const val F_EPV_FACEBOOK = "facebook"
        const val F_EPV_PHONE = "phone"

        const val F_E_REGISTER_FOR_WALLET = "wallet_registration"

        const val F_EPV_ORGANIC="organic"
        const val F_EPV_REFERRAL="referral"
        const val F_EPV_TEAM_REFERRAL="team_referral"
        const val F_EPV_COUPON = "coupon"
        const val F_EPV_AD = "ad"


        const val F_E_JOIN_TEAM = "join_group"
        const val F_EP_TEAM_TYPE = "type"
        const val F_EPV_PACKAGE = "package"
        const val F_EPV_TEAM = "team"



        const val F_EVENT_GAME_VISIT = "game_visit"
        const val F_EVENT_PARAM_GAME_NAME = "game_name"

        const val TEAM_LINK_SHARE = "team_link_share"

        const val MEMBERSHIP_PLAN = "membership_plan"
        const val FREE_PLAN = "free_plan"

        const val F_EVENT_NEW_TRACK = "new_track"
        const val F_EVENT_PARAM_TRACK_TYPE = "track_type"
        const val F_EVENT_PARAM_VALUE_MUSIC = "music"
        const val F_EVENT_PARAM_VALUE_PODCAST = "podcast"
        const val F_EVENT_PARAM_VALUE_AUDIOBOOK = "audiobook"

        const val F_EVENT_ORDERWITH_NEAR_LOCATION = "order_with_near_location"

    }

    override fun onMessageReceived(p0: RemoteMessage) {
        Log.i("fcmtokendata", p0.data.toString())
        var data = p0.data
        var title = data["title"]
        var body = data["body"]
        var imageUrl = data["imageUrl"]
        var intent = data["intent"]
        var intentType = data["intentType"]
        var notification = MNotification(title , body , imageUrl , intent , intentType)
        NotificationHelper.showNotification(applicationContext , notification)
    }

    override fun onNewToken(p0: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.i("fcm", "getInstanceId failed", task.exception)
                return@addOnCompleteListener
            }
            task.result?.let { token ->
                Log.i("fcmtoken", token)
                var prefHelper = PreferenceHelper.getInstance(applicationContext)
                prefHelper[PreferenceHelper.FCM_TOKEN] = token
                prefHelper[PreferenceHelper.IS_NEW_FCM_TOKEN] = true
            }
        }
    }
}