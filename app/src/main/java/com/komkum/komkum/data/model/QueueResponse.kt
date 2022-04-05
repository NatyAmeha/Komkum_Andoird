package com.komkum.komkum.data.model

data class Queue(var id : String , var name : String)

data class QueueResponse<T : BaseModel>(
    var mainResponse : T,
    var queueResult : Array<Queue>

)