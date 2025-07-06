package me.echeung.moemoekyun.domain.songs.interactor

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.domain.songs.SongsService
import me.echeung.moemoekyun.domain.songs.model.DomainSong
import me.echeung.moemoekyun.util.PreferenceUtil
import me.echeung.moemoekyun.util.ext.toast
import me.echeung.moemoekyun.util.ext.withUIContext
import javax.inject.Inject

class RequestSong @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val songsService: SongsService,
    private val preferenceUtil: PreferenceUtil,
) {

    suspend fun await(song: DomainSong) {
        try {
            songsService.request(song.id)

            withUIContext {
                when (preferenceUtil.shouldShowRandomRequestTitle().get()) {
                    true -> context.toast(
                        context.getString(
                            R.string.requested_song,
                            song.title,
                        ),
                    )
                    false -> context.toast(R.string.requested_random_song)
                }
            }
        } catch (e: Exception) {
            withUIContext {
                context.toast(e.message.orEmpty())
            }
        }
    }
}
