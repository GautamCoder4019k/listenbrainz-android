package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingTag(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("name")
    val name: String? = null
)