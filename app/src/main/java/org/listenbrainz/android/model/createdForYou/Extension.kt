package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class Extension(
    @SerializedName("https://musicbrainz.org/doc/jspf#playlist")
    val createdForYouExtensionData: CreatedForYouExtensionData = CreatedForYouExtensionData()
)