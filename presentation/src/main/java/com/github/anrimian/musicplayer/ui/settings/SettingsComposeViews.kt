package com.github.anrimian.musicplayer.ui.settings

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import com.github.anrimian.musicplayer.R

@Composable
@Preview
fun SettingTextCellExample() {
    SettingTextCell("hello world")
}

@Composable
fun SettingTextCell(title: String) {

    Text(
            text = title,
//            style = MaterialTheme.typography.h4
            style = TextStyle(),
            modifier = Modifier.padding(
                    start = dimensionResource(R.dimen.toolbar_content_start),
                    top = dimensionResource(R.dimen.content_vertical_margin),
                    bottom = dimensionResource(R.dimen.content_vertical_margin),
                    end = dimensionResource(R.dimen.content_horizontal_margin)
            ),
            fontSize = 17.sp
    )
}

/*@Composable
@Preview
fun DetailsContent() {
    Column(verticalArrangement = Arrangement.Center) {
        Spacer(Modifier.preferredHeight(32.dp))
        Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "exploreModel.city.nameToDisplay",
                style = MaterialTheme.typography.h4
        )
        Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = "exploreModel.description",
                style = MaterialTheme.typography.h6
        )
        Spacer(Modifier.preferredHeight(16.dp))
    }
}*/
