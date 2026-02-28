package me.echeung.moemoekyun.service

import androidx.core.net.toUri
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata.PICTURE_TYPE_FRONT_COVER
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import me.echeung.moemoekyun.domain.radio.RadioService
import me.echeung.moemoekyun.util.AlbumArtUtil
import javax.inject.Inject

@UnstableApi
class PlaybackMediaItemConverter @Inject constructor(
    private val radioService: RadioService,
    private val albumArtUtil: AlbumArtUtil,
    private val delegate: DefaultMediaItemConverter,
) : MediaItemConverter {

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val item = delegate.toMediaQueueItem(mediaItem)
        radioService.state.value.albumArtUrl?.let {
            item.media?.metadata?.addImage(WebImage(it.toUri()))
        }
        return item
    }

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
        val item = delegate.toMediaItem(mediaQueueItem)
        return item.buildUpon()
            .setMediaMetadata(
                item.mediaMetadata.buildUpon()
                    .apply {
                        setArtworkUri(null) // Local player uses artwork data
                        albumArtUtil.getCurrentAlbumArt(500)?.let {
                            setArtworkData(
                                it,
                                PICTURE_TYPE_FRONT_COVER,
                            )
                        }
                    }
                    .build(),
            )
            .build()
    }
}
