package com.komkum.komkum.ui.component

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.size.Scale
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.komkum.komkum.R
import com.komkum.komkum.data.model.Product
import com.komkum.komkum.ui.theme.Red
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import lt.neworld.spanner.Spans
import kotlin.concurrent.fixedRateTimer


@Composable
fun productWthQuantityComposable(
    modifier: Modifier = Modifier,
    title: String,
    price: Int,
    image: String,
    msg: String = "",
    qty: Int = 1,
    minQty : Int? = 1,
    onQtyClick: (action: String, qty: Int) -> Unit,
    onRemove: () -> Unit
) {
    Column(modifier = Modifier
        .background(colorResource(id = R.color.light_secondaryLightColor))
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 16.dp)) {

//        var quantity  by remember { mutableStateOf(qty)}
        var productPrice = price.times(qty)

        productListItemComposable(
            onclick = {},
            title = title,
            price = productPrice,
            image = image
        ) {
            Text(text = msg, fontSize = dimensionResource(id = R.dimen.caption).value.sp)
            Text(text = "${stringResource(id = R.string.minimum_order)}  $minQty", fontSize = dimensionResource(id = R.dimen.caption2).value.sp , color = Red)

        }

        productQuantityComposable(qty, minQty ?: 1, onControllerclick = { action, qty ->
            onQtyClick(action, qty)
        }) {
            TextButton(onClick = { onRemove() }) {
                Text(
                    text = stringResource(R.string.remove),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

    }
}


@Composable
fun productListItemComposable(
    onclick: () -> Unit, title: String, price: Int, image: String, slot: @Composable () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(colorResource(id = R.color.light_secondaryLightColor))
            .padding(top = 4.dp , bottom = 4.dp)
            .clickable { onclick() },
        horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        imageComposable(image = image , placeholder = R.drawable.product_placeholder ,
        modifier = Modifier
            .height(dimensionResource(R.dimen.vertical_product_list_item_size))
            .width(dimensionResource(R.dimen.vertical_product_list_item_size))
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp)))

        Column(Modifier.padding(end = 8.dp), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(text = title , maxLines = 2, fontSize = dimensionResource(id = R.dimen.body_3).value.sp,  color = Color.Gray)
            Text(
                text = "${stringResource(id = R.string.birr)} $price",
                fontSize = dimensionResource(id = R.dimen.title).value.sp , fontWeight = FontWeight.Bold
            )
            slot()
        }
    }
}


@Composable
fun productQuantityComposable(
    qty: Int,
    minQty: Int = 1,
    onControllerclick: (action: String, qty: Int) -> Unit,
    slot: @Composable () -> Unit
) {
    var context = LocalContext.current
    var currentQty = qty
    Row(
        Modifier
            .background(colorResource(id = R.color.light_secondaryLightColor))
            .padding(start = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.qty),
            fontSize = 17.sp,
            style = MaterialTheme.typography.body1
        )
        Row(Modifier.padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(id = R.drawable.ic_baseline_remove_24),
                contentDescription = "",  modifier = Modifier.clickable {
                    if (currentQty > minQty){
                        currentQty -= 1
                        onControllerclick("REMOVE", currentQty)
                    }
                    else Toast.makeText(context , "${context.getString(R.string.minimum_order_msg)} $minQty ${context.getString(R.string.above_min_qty)}" , Toast.LENGTH_LONG).show()
                })
            Surface(shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(horizontal = 16.dp , vertical = 8.dp),
            color = Color.Transparent) {
                Text(
                    text = "$qty", fontSize = 17.sp,
                    modifier = Modifier.padding(8.dp).defaultMinSize(minWidth = 25.dp),
                    fontWeight = FontWeight.Bold ,  textAlign = TextAlign.Center
                )
            }
            Icon(painter = painterResource(id = R.drawable.ic_baseline_add_24),
                contentDescription = "",  modifier = Modifier.clickable {
                    currentQty += 1
                    onControllerclick("ADD", currentQty)
                })

        }

        slot()
    }
}

@ExperimentalPagerApi

@Composable
fun AdsBannerComposable(imageList: List<String> , modifier: Modifier = Modifier, onclick: (index: Int) -> Unit) {
//     var state = rememberPa
    var pagerState = rememberPagerState(
        pageCount = imageList.size,
        initialOffscreenLimit = 2,
        infiniteLoop = true
    )

    Column(Modifier.padding(8.dp).fillMaxWidth()) {
        HorizontalPager(state = pagerState) { page ->
            imageComposable(image = imageList[page] , R.drawable.product_placeholder,
                modifier = modifier
                    .fillMaxWidth()
                    .aspectRatio(1f )
                    .clip(RoundedCornerShape(8.dp))){ onclick(page) }
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )

        LaunchedEffect(pagerState.currentPage) {
            delay(5000) // wait for 3 seconds.
            // increasing the position and check the limit
            var newPosition = pagerState.currentPage + 1
            if (newPosition > imageList.lastIndex) newPosition = 0
            // scrolling to the new position.
            pagerState.animateScrollToPage(newPosition)
        }
    }
}










@Composable
fun walletAction(name: String = "Recharge") {

    Box(
        Modifier
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_payment_24),
                contentDescription = "",
                modifier = Modifier.size(40.dp, 40.dp)
            )
            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

    }
}