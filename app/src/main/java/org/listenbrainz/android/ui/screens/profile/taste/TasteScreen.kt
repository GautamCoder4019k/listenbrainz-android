package org.listenbrainz.android.ui.screens.profile.taste

import LovedHated
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HeartBroken
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.PinnedRecording
import org.listenbrainz.android.model.SocialUiState
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.model.feed.ReviewEntityType
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.SuccessBar
import org.listenbrainz.android.ui.components.dialogs.Dialog
import org.listenbrainz.android.ui.components.dialogs.rememberDialogsState
import org.listenbrainz.android.ui.screens.feed.FeedUiState
import org.listenbrainz.android.ui.screens.feed.SocialDropdown
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.screens.profile.listens.Dialogs
import org.listenbrainz.android.ui.screens.profile.listens.ListenDialogBundleKeys
import org.listenbrainz.android.ui.screens.profile.listens.LoadMoreButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple_night
import org.listenbrainz.android.util.Utils.getCoverArtUrl
import org.listenbrainz.android.viewmodel.FeedViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun TasteScreen(
    viewModel: UserViewModel,
    socialViewModel: SocialViewModel,
    feedViewModel : FeedViewModel,
    snackbarState : SnackbarHostState,
    goToArtistPage: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val socialUiState by socialViewModel.uiState.collectAsState()
    val feedUiState by feedViewModel.uiState.collectAsState()

    val dropdownItemIndex: MutableState<Int?> = rememberSaveable {
        mutableStateOf(null)
    }
    TasteScreen(
        uiState = uiState,
        socialUiState = socialUiState,
        feedUiState = feedUiState,
        snackbarState = snackbarState,
        dropdownItemIndex = dropdownItemIndex,
        playListen = {
            socialViewModel.playListen(it)
        },
        onPin = {
                metadata, blurbContent -> socialViewModel.pin(metadata , blurbContent)
            dropdownItemIndex.value = null
        },
        onRecommend = {metadata ->
            socialViewModel.recommend(metadata)
            dropdownItemIndex.value = null
        },
        searchUsers = {
                query -> feedViewModel.searchUser(query)
        },
        isCritiqueBrainzLinked = {
            feedViewModel.isCritiqueBrainzLinked()
        },
        onReview = {
                type, blurbContent, rating, locale, metadata ->  socialViewModel.review(metadata , type , blurbContent , rating , locale)
        },
        onPersonallyRecommend = {
                metadata, users, blurbContent ->  socialViewModel.personallyRecommend(metadata, users, blurbContent)
        },
        onErrorShown = {
            socialViewModel.clearErrorFlow()
        },
        onMessageShown = {
            socialViewModel.clearMsgFlow()
        },
        goToArtistPage = goToArtistPage,
    )
}

@Composable
fun TasteScreen(
    uiState: ProfileUiState,
    socialUiState: SocialUiState,
    feedUiState: FeedUiState,
    snackbarState: SnackbarHostState,
    uriHandler: UriHandler = LocalUriHandler.current,
    dropdownItemIndex : MutableState<Int?>,
    playListen: (TrackMetadata) -> Unit,
    onPin : (metadata : Metadata, blurbContent : String) -> Unit,
    onRecommend : (metadata : Metadata) -> Unit,
    searchUsers: (String) -> Unit,
    isCritiqueBrainzLinked: suspend () -> Boolean?,
    onReview: (type: ReviewEntityType, blurbContent: String, rating: Int?, locale: String, metadata: Metadata) -> Unit,
    onPersonallyRecommend: (metadata: Metadata, users: List<String>, blurbContent: String) -> Unit,
    onErrorShown : () -> Unit,
    onMessageShown : () -> Unit,
    goToArtistPage: (String) -> Unit,
){
    val lovedHatedState: MutableState<LovedHated> = remember { mutableStateOf(LovedHated.loved) }

    val lovedHatedCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }

    val pinsCollapsibleState: MutableState<Boolean> = remember {
        mutableStateOf(true)
    }

    val dialogsState = rememberDialogsState()

    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    LazyColumn {
        item {
            LovedHatedBar(
                lovedHatedState = lovedHatedState.value,
                onLovedClick = {
                    lovedHatedState.value = LovedHated.loved
                },
                onHatedClick = {
                    lovedHatedState.value = LovedHated.hated
                }
            )
        }
        itemsIndexed(items = when(lovedHatedState.value){
            LovedHated.loved -> when(lovedHatedCollapsibleState.value){
                true -> uiState.tasteTabUIState.lovedSongs?.feedback?.take(5) ?: listOf()
                false -> uiState.tasteTabUIState.lovedSongs?.feedback ?: listOf()
            }
            LovedHated.hated -> when(lovedHatedCollapsibleState.value){
                true -> uiState.tasteTabUIState.hatedSongs?.feedback?.take(5) ?: listOf()
                false -> uiState.tasteTabUIState.hatedSongs?.feedback ?: listOf()
            }
        }){
                index, feedback ->
            val metadata = Metadata(trackMetadata = feedback.trackMetadata)
            ListenCardSmall(
                modifier = Modifier
                    .padding(
                        horizontal = 16.dp,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                trackName = feedback.trackMetadata?.trackName ?: "", artists = feedback.trackMetadata?.mbidMapping?.artists ?: listOf(
                    FeedListenArtist(feedback.trackMetadata?.artistName ?: "", null, "")
                ), coverArtUrl = getCoverArtUrl(
                    caaReleaseMbid = feedback.trackMetadata?.mbidMapping?.caaReleaseMbid,
                    caaId = feedback.trackMetadata?.mbidMapping?.caaId
                ),
                onDropdownIconClick = {
                    dropdownItemIndex.value = index
                },
                dropDown = {
                    SocialDropdown(
                        isExpanded = dropdownItemIndex.value == index,
                        onDismiss = {
                            dropdownItemIndex.value = null
                        },
                        metadata = metadata,
                        onRecommend = { onRecommend(metadata) },
                        onPersonallyRecommend = {
                            dialogsState.activateDialog(Dialog.PERSONAL_RECOMMENDATION , ListenDialogBundleKeys.listenDialogBundle(0, index))
                            dropdownItemIndex.value = null
                        },
                        onReview = {
                            dialogsState.activateDialog(Dialog.REVIEW , ListenDialogBundleKeys.listenDialogBundle(0, index))
                            dropdownItemIndex.value = null
                        },
                        onPin = {
                            dialogsState.activateDialog(Dialog.PIN , ListenDialogBundleKeys.listenDialogBundle(0, index))
                            dropdownItemIndex.value = null
                        },
                        onOpenInMusicBrainz = {
                            try {
                                uriHandler.openUri("https://musicbrainz.org/recording/${metadata.trackMetadata?.mbidMapping?.recordingMbid}")
                            }
                            catch(e : Error) {
                                scope.launch {
                                    snackbarState.showSnackbar(context.getString(R.string.err_generic_toast))
                                }
                            }
                            dropdownItemIndex.value = null
                        }

                    )
                },
                goToArtistPage = goToArtistPage
            ) {
                if(feedback.trackMetadata != null){
                    playListen(feedback.trackMetadata)
                }
            }
        }
        item{
            if((uiState.tasteTabUIState.lovedSongs?.count
                    ?: 0) > 5 || (uiState.tasteTabUIState.hatedSongs?.count ?: 0) > 5
            ){
                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    LoadMoreButton(
                        modifier = Modifier.padding(16.dp),
                        state = lovedHatedCollapsibleState.value
                    ) {
                        lovedHatedCollapsibleState.value = !lovedHatedCollapsibleState.value
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        item {
            val pinnedRecordings = when(pinsCollapsibleState.value){
                true -> uiState.tasteTabUIState.pins?.pinnedRecordings?.take(5) ?: listOf()
                false -> uiState.tasteTabUIState.pins?.pinnedRecordings ?: listOf()
            }
            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Pins",
                        color = ListenBrainzTheme.colorScheme.text,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    pinnedRecordings.mapIndexed { index, recording: PinnedRecording ->
                        val metadata = Metadata(trackMetadata = recording.trackMetadata)
                        ListenCardSmall(
                            blurbContent = if (!recording.blurbContent.isNullOrBlank()) {
                                { modifier ->
                                    Text(
                                        modifier = modifier,
                                        text = recording.blurbContent,
                                        color = ListenBrainzTheme.colorScheme.text,
                                    )
                                }
                            } else null,
                            modifier = Modifier
                                .padding(
                                    vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                                ),
                            trackName = recording.trackMetadata?.trackName ?: "",
                            artists = recording.trackMetadata?.mbidMapping?.artists ?: listOf(FeedListenArtist(recording.trackMetadata?.artistName ?: "", null, "")),
                            coverArtUrl = getCoverArtUrl(
                                caaReleaseMbid = recording.trackMetadata?.mbidMapping?.caaReleaseMbid,
                                caaId = recording.trackMetadata?.mbidMapping?.caaId
                            ),
                            onDropdownIconClick = {
                                dropdownItemIndex.value = index
                            },
                            dropDown = {
                                SocialDropdown(
                                    isExpanded = dropdownItemIndex.value == index,
                                    onDismiss = {
                                        dropdownItemIndex.value = null
                                    },
                                    metadata = metadata,
                                    onRecommend = { onRecommend(metadata) },
                                    onPersonallyRecommend = {
                                        dialogsState.activateDialog(
                                            Dialog.PERSONAL_RECOMMENDATION,
                                            ListenDialogBundleKeys.listenDialogBundle(0, index)
                                        )
                                        dropdownItemIndex.value = null
                                    },
                                    onReview = {
                                        dialogsState.activateDialog(
                                            Dialog.REVIEW,
                                            ListenDialogBundleKeys.listenDialogBundle(0, index)
                                        )
                                        dropdownItemIndex.value = null
                                    },
                                    onPin = {
                                        dialogsState.activateDialog(
                                            Dialog.PIN,
                                            ListenDialogBundleKeys.listenDialogBundle(0, index)
                                        )
                                        dropdownItemIndex.value = null
                                    },
                                    onOpenInMusicBrainz = {
                                        try {
                                            uriHandler.openUri("https://musicbrainz.org/recording/${metadata.trackMetadata?.mbidMapping?.recordingMbid}")
                                        } catch (e: Error) {
                                            scope.launch {
                                                snackbarState.showSnackbar(context.getString(R.string.err_generic_toast))
                                            }
                                        }
                                        dropdownItemIndex.value = null
                                    }

                                )
                            },
                            goToArtistPage = goToArtistPage
                        ) {
                            if (recording.trackMetadata != null) {
                                playListen(recording.trackMetadata)
                            }
                        }

                    }
                }
            }
        }
        item {
            if((uiState.tasteTabUIState.pins?.count ?: 0) > 5){
                Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    LoadMoreButton(
                        modifier = Modifier.padding(16.dp),
                        state = pinsCollapsibleState.value
                    ) {
                        pinsCollapsibleState.value = !pinsCollapsibleState.value
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    ErrorBar(error = socialUiState.error, onErrorShown = onErrorShown )
    SuccessBar(resId = socialUiState.successMsgId, onMessageShown = onMessageShown, snackbarState = snackbarState)

    Dialogs(
        deactivateDialog = {
            dialogsState.deactivateDialog()
        },
        currentDialog = dialogsState.currentDialog,
        currentIndex = dialogsState.metadata?.getInt(ListenDialogBundleKeys.EVENT_INDEX.name),
        listens = uiState.listensTabUiState.recentListens ?: listOf(),
        onPin = {metadata, blurbContent ->  onPin(metadata, blurbContent)},
        searchUsers = { query -> searchUsers(query) },
        feedUiState = feedUiState,
        isCritiqueBrainzLinked = isCritiqueBrainzLinked,
        onReview = {type, blurbContent, rating, locale, metadata -> onReview(type, blurbContent, rating, locale, metadata) },
        onPersonallyRecommend = {metadata, users, blurbContent -> onPersonallyRecommend(metadata, users, blurbContent)},
        snackbarState = snackbarState,
        socialUiState = socialUiState)
}

@Composable
private fun LovedHatedBar(
    lovedHatedState: LovedHated,
    onLovedClick: () -> Unit,
    onHatedClick: () -> Unit
){
    Row (modifier = Modifier.padding(start = 16.dp)) {
        ElevatedSuggestionChip(
            onClick = onLovedClick,
            label = {
                Row (verticalAlignment = Alignment.CenterVertically) {
                    Text("Loved", color = when(lovedHatedState == LovedHated.loved){
                        true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                        false -> ListenBrainzTheme.colorScheme.followerChipSelected
                    })
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.heart),
                        contentDescription = "",
                        modifier = Modifier.height(15.dp),
                        tint = when(lovedHatedState == LovedHated.loved){
                            true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                            false -> ListenBrainzTheme.colorScheme.followerChipSelected
                        }
                    )
                }
            },
            shape = RoundedCornerShape(10.dp),
            border = when(lovedHatedState == LovedHated.loved){
                true -> null
                false -> BorderStroke(1.dp, lb_purple_night)
            },
            colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                if (lovedHatedState == LovedHated.loved) {
                    ListenBrainzTheme.colorScheme.followerChipSelected
                } else {
                    ListenBrainzTheme.colorScheme.followerChipUnselected
                }
            ),
        )
        Spacer(modifier = Modifier.width(10.dp))
        ElevatedSuggestionChip(
            onClick = onHatedClick,
            label = { Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hated", color = when(lovedHatedState == LovedHated.hated){
                    true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                    false -> ListenBrainzTheme.colorScheme.followerChipSelected
                })
                Spacer(modifier = Modifier.width(5.dp))
                Icon(Icons.Default.HeartBroken, contentDescription = "", modifier = Modifier.height(15.dp), tint = when(lovedHatedState == LovedHated.hated){
                    true -> ListenBrainzTheme.colorScheme.followerChipUnselected
                    false -> ListenBrainzTheme.colorScheme.followerChipSelected
                })
            } },
            shape = RoundedCornerShape(10.dp),
            border = when(lovedHatedState == LovedHated.hated){
                true -> null
                false -> BorderStroke(1.dp, lb_purple_night)
            },
            colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                if (lovedHatedState == LovedHated.hated) {
                    ListenBrainzTheme.colorScheme.followerChipSelected
                } else {
                    ListenBrainzTheme.colorScheme.followerChipUnselected
                }
            ),
        )
    }
}