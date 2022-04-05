package com.komkum.komkum.data.model

data class PurcahseTracker(
    var productIdentifier : String,    // identifier of product. it can be id of book, audiobook or subscription
    var purchaseToken : String         // google play purchase token
    ) {
}