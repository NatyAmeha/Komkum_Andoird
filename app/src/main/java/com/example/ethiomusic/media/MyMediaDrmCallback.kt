package com.example.ethiomusic.media

import com.google.android.exoplayer2.drm.ExoMediaDrm
import com.google.android.exoplayer2.drm.MediaDrmCallback
import java.util.*

class MyMediaDrmCallback() : MediaDrmCallback {
    var key : String = ""

    init {
        key = "{\"keys\":[{\"kty\":\"oct\",\"k\":\"a4631a153a443df9eed0593043db7519\", \"kid\":\"f3c5e0361e6654b28f8049c778b23946\"},{\"kty\":\"oct\",\"k\":\"69eaa802a6763af979e8d1940fb88392\", \"kid\":\"abba271e8bcf552bbd2e86a434a9a5d9\"},{\"kty\":\"oct\",\"k\":\"cb541084c99731aef4fff74500c12ead\", \"kid\":\"6d76f25cb17f5e16b8eaef6bbf582d8e\"}],\"type\":\"temporary\"}"
    }
    override fun executeKeyRequest(uuid: UUID?, request: ExoMediaDrm.KeyRequest?): ByteArray {
        return key.toByteArray()
    }


    override fun executeProvisionRequest(uuid: UUID?, request: ExoMediaDrm.ProvisionRequest?): ByteArray {
        return byteArrayOf(0)
    }


}