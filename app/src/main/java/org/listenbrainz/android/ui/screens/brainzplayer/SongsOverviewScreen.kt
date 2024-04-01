package org.listenbrainz.android.ui.screens.brainzplayer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.BrainzPlayerListenCard
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.ArtistViewModel

@Composable
fun SongsOverviewScreen(
    songs: List<Song>,
    onPlayIconClick: (Song, List<Song>) -> Unit,
) {
    val songsStarting : MutableMap<Char, MutableList<Song>> = mutableMapOf()
    for (i in 0..25) {
        songsStarting['A' + i] = mutableListOf()
    }

    for (i in 1..songs.size) {
        songsStarting[songs[i - 1].title[0]]?.add(songs[i-1])
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        for (i in 0..25) {
            val startingLetter: Char = ('A' + i)
            if (songsStarting[startingLetter]!!.size > 0) {
                Column(
                    modifier = Modifier.background(
                        brush = ListenBrainzTheme.colorScheme.gradientBrush
                    ).padding(top = 15.dp, bottom = 15.dp)
                ) {
                    Text(
                        startingLetter.toString(),
                        modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 5.dp),
                        style = TextStyle(
                            color = ListenBrainzTheme.colorScheme.lbSignature,
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_bold)),
                        )
                    )
                    for (j in 1..songsStarting[startingLetter]!!.size) {
                        val song: Song = songsStarting[startingLetter]!![j-1]
                        var coverArt: String? = null
                        coverArt = songsStarting[startingLetter]!![j - 1].albumArt
                        BrainzPlayerListenCard(title = songsStarting[startingLetter]!![j - 1].title, subTitle = songsStarting[startingLetter]!![j - 1].artist, coverArtUrl = coverArt){
                            Log.v("pranav", song.title)
                            onPlayIconClick(song,songsStarting[startingLetter]!!)
                        }
                    }
                }
            }
        }
    }
}