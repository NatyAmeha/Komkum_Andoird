package com.komkum.komkum.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.komkum.komkum.R
import com.komkum.komkum.data.model.*
import com.komkum.komkum.data.viewmodel.PaymentMethod
import com.komkum.komkum.data.viewmodel.RewardInfo
import com.komkum.komkum.ui.theme.Primary
import kotlin.math.roundToInt


@Composable
fun imageComposable(
    image: String?,
    placeholder: Int = R.drawable.ic_person_black_24dp,
    modifier: Modifier = Modifier,
    onclick: () -> Unit? = {}
) {
    var painter = rememberImagePainter(data = image, builder = {
            scale(Scale.FIT)
            placeholder(placeholder)
        })
    Image(
        painter = painter, contentDescription = "",
        modifier = modifier
            .background(Color.White)
            .clickable { onclick() }
    )

}


@Composable
fun paymentMethodListItemComposable(index : Int , paymentMethod : PaymentMethod , onclick: (index: Int) -> Unit){
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onclick(index) }
        , horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = CenterVertically){
        Column() {
            Text(text = paymentMethod.name , color = Color.White , fontWeight = FontWeight.Bold , fontSize = 17.sp)
            Text(text = paymentMethod.description , color = Color.Gray , fontWeight = FontWeight.Bold , fontSize = 14.sp)

        }
        imageComposable(image = null , placeholder = paymentMethod.image,
        modifier = Modifier
            .width(40.dp)
            .height(40.dp))
    }
}

@Composable
fun imageListItem(modifier: Modifier = Modifier , title : String, subtitle: String="", message : String,
                  image : String , placeholder: Int? = null ,  onclick: () -> Unit){
    Row(
        Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.light_secondaryLightColor))
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .clickable { onclick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(Modifier.fillMaxWidth(0.7f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Image(modifier = Modifier
                .clip(CircleShape)
                .background(Color.Gray)
                .padding(8.dp),
                painter = painterResource(id = placeholder ?: R.drawable.ic_baseline_payment_24),
                contentDescription = "")

            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = title ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = subtitle,
                    maxLines = 2,
                    style = TextStyle(fontSize = 14.sp , color = Color.DarkGray)
                )
            }
        }

        Text(
            text = message,
            textAlign = TextAlign.End,
            fontSize = 12.sp,
            modifier  = Modifier.fillMaxWidth(0.4f)
        )
    }
}

@Composable
fun profileHeaderComponent(
    modifier: Modifier = Modifier,
    iamge: String,
    name: String,
    subtitle: String,
    slot: @Composable () -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Image(
                painter = painterResource(id = R.drawable.music_placeholder),
                contentDescription = "", modifier = modifier
                    .clip(CircleShape)
                    .size(50.dp, 50.dp)

            )

            Column {
                Text(
                    text = "Bereket Tesfaye",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(text = "123 donators", fontWeight = FontWeight.Light, fontSize = 16.sp)
            }
        }

        Text(text = "9381", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)


    }
}


@Composable
fun simpleListItem(
    title: String = "Library",
    subtitle: String = "music,podcast,books",
    slot: @Composable () -> Unit = {}
) {
//    LazyColumn{
//        items(5){key ->
//            profileHeaderComponent(iamge = "", name = "", subtitle = "") {
//
//            }
//        }
//    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_library_music_24),
                contentDescription = "", modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .size(30.dp, 30.dp)

            )

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(text = subtitle, fontWeight = FontWeight.Light, fontSize = 14.sp)
            }
        }
        slot()
    }
}





@Composable
fun leaderboardWithHeader(image : String , title : String , subtitle: String , extra : String , items : List<Leaderboard>){
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Cyan),
            contentAlignment = Alignment.TopStart
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    imageComposable(
                        image = image,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(
                                dimensionResource(id = R.dimen.medium_circle_image_size),
                                dimensionResource(id = R.dimen.medium_circle_image_size)
                            )
                    )
                    Column {
                        Text(
                            text = title ?: "",
                            maxLines = 1,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = extra,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
        leaderboardList(leaderboards = items) {}
    }
}


@ExperimentalPagerApi
@Composable
fun donationListViewpager(donations : List<DonationLeaderBoard> , onclick: (creatorId: String) -> Unit){
    if(!donations.isNullOrEmpty()){
        var pagerState = rememberPagerState(
            pageCount = donations.size,
            initialOffscreenLimit = 1
        )
        Column {
            HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) { page ->
                var selectedLeaderBoard = donations[page]
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Cyan)
                            .clickable { selectedLeaderBoard._id?.let { onclick(it) } },
                        contentAlignment = Alignment.TopStart
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = CenterVertically
                        ) {

                            imageComposable(
                                image = selectedLeaderBoard.image,
                                placeholder = R.drawable.ic_person_black_24dp,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(
                                        dimensionResource(id = R.dimen.medium_circle_image_size),
                                        dimensionResource(id = R.dimen.medium_circle_image_size)
                                    )
                            )
                            Column {
                                Text(
                                    text = selectedLeaderBoard.name ?: "",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "ETB ${selectedLeaderBoard.totalAmount}" ,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                    leaderboardList(leaderboards = selectedLeaderBoard.participants) {}
                }
            }

            HorizontalPagerIndicator(pagerState = pagerState,
                activeColor = Color.White,
                inactiveColor = Color.DarkGray,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally))
        }
    }


}

@Composable
fun leaderboardList(leaderboards: List<Leaderboard>, slot: @Composable () -> Unit) {
    var leaderboardParticipant = leaderboards.groupBy { participant -> participant._id }
        .toList().sortedByDescending { (_ , value) -> value.sumOf { value -> value.amount ?: 0 }}.toMap()
    var counter = 1
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for((key , value) in leaderboardParticipant){
            var name = value[0].name
            var amount = value.map { value -> value.amount!! }.reduce { acc, i -> acc.plus(i) }
            var image = value[0].image
            var rank = counter
            leaderboardListItem(rank, name ?: "", image, amount)
            counter++
        }
        slot()
    }
}

@Composable
fun leaderboardListItem(rank: Int, name: String, image: String?, value: Int) {
    Row(
        Modifier
            .clickable { }
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$rank",  fontSize = 16.sp)
            imageComposable(
                image = image,
                placeholder = R.drawable.ic_person_black_24dp,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(
                        dimensionResource(id = R.dimen.mini_circle_image_size),
                        dimensionResource(id = R.dimen.mini_circle_image_size)
                    ),
            )
            Text(text = name, fontWeight = FontWeight.Normal , fontSize = 16.sp )
        }
        Text(text = "${stringResource(id = R.string.birr)} $value" ,  fontSize = 14.sp)
    }
}

@ExperimentalMaterialApi
@Composable
fun searchBar(onclick : () -> Unit , slot : @Composable () -> Unit = {}) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onclick() },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            Modifier
                .fillMaxWidth(0.85f)
                .align(CenterVertically)
                .padding(start = 8.dp)
                .background(Color.LightGray, RoundedCornerShape(10.dp))
        ) {
            Row(Modifier.padding(horizontal = 16.dp , vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = CenterVertically) {
                Icon(painterResource(id = R.drawable.ic_search_black_24dp),
                    modifier = Modifier.size(25.dp , 25.dp) ,  contentDescription = "")

                Text(text = stringResource(R.string.search_product), fontSize = dimensionResource(id = R.dimen.body_2).value.sp , color = Color.Gray)
            }

        }

        slot()
    }
}

@Composable
fun optionList(){
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        val state = rememberLazyListState()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)){
            items(4){ index ->
//                productSku(index)
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)){
            items(2){index ->
//                productSku(index+1)
            }
        }
    }
}


@Composable
fun productSku(index: Int = 0 , selectedIndex : Int = 0 , image : String? , values : List<String>? , onclick: (index : Int) -> Unit){
    var ischecked = index == selectedIndex

    var boarderMOdifier = if(ischecked) Modifier.border(2.dp, colorResource(id = R.color.light_primaryLightColor), RoundedCornerShape(1.dp))
    else Modifier.border(1.dp, Color.DarkGray, RoundedCornerShape(1.dp))
    Box(
        boarderMOdifier
            .clip(RoundedCornerShape(8.dp))
            .defaultMinSize(100.dp, 60.dp)
            .clickable { onclick(index) }){
        Row(Modifier.padding( 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)){
            imageComposable(image = image , placeholder = R.drawable.product_placeholder , modifier = Modifier.size(50.dp  ,50.dp))
            Column {
                values?.forEach {
                    Text(text = it ,  fontSize = 11.sp)
                }
            }
        }
    }
}



@Composable
fun TriviaCard(modifier : Modifier = Modifier , ad : Ads , onclick: () -> Unit){

    Card(modifier = modifier
        .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 10.dp) {

        Box(
            Modifier
                .fillMaxSize()
                .clickable { onclick() }){

            imageComposable(image = ad.banner , R.drawable.sp_image , modifier = modifier){onclick()}

            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd),
                elevation = 8.dp,
                shape = RoundedCornerShape(8.dp),
                color = Color.Magenta,
            ) {
                Row(modifier = Modifier.toggleable(true , onValueChange = {})) {
                    Text(
                        text = "Win ETB ${ad.prize}",
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.DarkGray,
                                Color.DarkGray
                            )
                        )
                    )
                    .padding(8.dp) ,
                verticalArrangement = Arrangement.spacedBy(8.dp),) {
                Column(Modifier.fillMaxWidth()) {
                    Text(text = ad.ownerName?.joinToString(", ") ?: "" , style = TextStyle(color = Color.White , fontSize = 15.sp))
                    Text(text = "${ad.subtitle}" , style = TextStyle(color = Color.White , fontSize = 18.sp , fontWeight = FontWeight.Bold))
                    Text(text = "${ad.discount}% discount for top ${ad.discountFor} players",
                        maxLines = 1 , style = TextStyle(color = Primary, fontSize = 13.sp , fontWeight = FontWeight.Medium)
                    )

                }

                Row{
                    ad.tags?.forEach {
                        Box(
                            Modifier
                                .padding(end = 8.dp)
                                .background(Color.Gray, RoundedCornerShape(8.dp)),
                            contentAlignment = Center){
                            Text(text = it ,
                                modifier = Modifier.padding(4.dp),
                                style = TextStyle(color = Color.White , fontSize = 10.sp , fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

        }
    }

}





@ExperimentalPagerApi
@Composable
fun viewPagerComponent(modifier: Modifier = Modifier , imageList : List<ProductGallery> ,
                       onvideoClick : (path : String) ->Unit = {}){
    var pagerState = rememberPagerState(
        pageCount = imageList.size,
        initialOffscreenLimit = 2
    )

    Box(Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState, modifier = modifier
                .fillMaxWidth()
        ) { page ->
            imageComposable(
                image = if(imageList[page].type == "video")
                    imageList[if(page < imageList.size -1) page+1 else page].path
                else imageList[page].path ,
                placeholder = R.drawable.product_placeholder,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .aspectRatio(1f))

            if(imageList[page].type == "video"){
                Box(
                    Modifier
                        .background(Color.Gray, RoundedCornerShape(8.dp))
                        .align(Center)){
                    Icon(Icons.Outlined.PlayCircleOutline , contentDescription = "" , tint = MaterialTheme.colors.onSurface,
                        modifier = Modifier
                            .size(75.dp, 75.dp)
                            .align(Center)
                            .clickable { onvideoClick(imageList[page].path ?: "") })
                }
            }

            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .align(BottomCenter))

            Row(Modifier.align(BottomEnd)){
                Surface(modifier = Modifier
                    .padding(horizontal = 10.dp , vertical = 4.dp) , color = Color.Black) {
                    Text(text = "${page+1}/${imageList.size}" , color = Color.White)
                }
            }
        }

    }
}


@Composable
fun rewardCard(reward : RewardInfo , background : Color = Color.Gray , onclick: (rewardInfo : RewardInfo) -> Unit){
    Card(modifier = Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .clickable { onclick(reward) },
    shape = RoundedCornerShape(6.dp)) {
        Box(){
            Row(
                Modifier
                    .background(colorResource(id = R.color.light_secondaryLightColor))
                    .padding(start = 8.dp)
                    .fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = CenterVertically) {
                var amount = if(reward.rewarded == true && reward.amount ?: 0.0 > 0.0)
                    "${stringResource(id = R.string.birr)} ${reward.amount?.roundToInt()} (payed)"
                else if(reward.amount == 0.0 && reward.rewarded == false)
                    stringResource(id = R.string.discount_code)
                else "${stringResource(id = R.string.birr)} ${reward.amount?.roundToInt()}"

                Column {
//                    Spacer(modifier = Modifier.padding(12.dp))
                    Text(
                        text = amount,
                        color = Color.Black,
                        textAlign = TextAlign.Start,
                        fontSize = dimensionResource(id = R.dimen.title).value.sp,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                    )

                    Text(
                        text = "From '${reward.title}'" ?: "",
                        color = Color.Gray,
                        textAlign = TextAlign.Start,
                        fontSize = dimensionResource(id = R.dimen.body_2).value.sp,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Normal,
                    )

                    Spacer(modifier = Modifier.padding(top = 8.dp))

                    var rewardInfo = mutableListOf(stringResource(R.string.cash_prize),
                        stringResource(R.string.discount_code))
                    if((reward.amount ?: 0.0) <= 0.0 || reward.rewarded == true)
                        rewardInfo.removeAt(0)
                    if(reward.type == Coupon.REWARD_TYPE_COMMISSION) rewardInfo.removeAt(1)
                    LazyRow{
                        items(rewardInfo){ item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = colorResource(id = R.color.primaryColor),
                            ){
                                Text(
                                    text = item,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.padding(horizontal = 8.dp , vertical = 4.dp),
                                    fontSize = 11.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                        }
                    }

                }
                imageComposable(image = if(reward.type ==  Coupon.REWARD_TYPE_GAME) reward.image else null , R.drawable.ic_baseline_card_giftcard_24,
                    modifier = Modifier
                        .padding(16.dp)
                        .width(90.dp)
                        .height(90.dp))
            }

            Surface(
                modifier = Modifier.align(Alignment.TopEnd),
                shape = RoundedCornerShape(4.dp),
                color = if(reward.type == Coupon.REWARD_TYPE_GAME) colorResource(id = R.color.yellow)
                else colorResource(id = R.color.light_primaryLightColor)
            ) {
                var rewardType = when (reward.type) {
                    Coupon.REWARD_TYPE_GAME -> stringResource(id = R.string.games)
                    Coupon.REWARD_TYPE_COMMISSION -> stringResource(id = R.string.commission)
                    else -> ""
                }
                Row(modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .toggleable(true, onValueChange = {})) {
                    Text(
                        text = rewardType,
                        fontSize = 11.sp,
                        color = Color.White)
                }
            }
        }
    }
}
