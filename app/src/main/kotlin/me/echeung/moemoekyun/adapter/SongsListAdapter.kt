package me.echeung.moemoekyun.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.api.model.Song
import me.echeung.moemoekyun.databinding.SongItemBinding
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import me.echeung.moemoekyun.util.ext.launchIO
import me.echeung.moemoekyun.util.ext.launchUI
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.ref.WeakReference
import java.util.Random

class SongsListAdapter(
    activity: Activity,
    private val listId: String
) : ListAdapter<Song, RecyclerView.ViewHolder>(DIFF_CALLBACK), KoinComponent {

    private val songActionsUtil: SongActionsUtil by inject()
    private val songSortUtil: SongSortUtil by inject()

    private val activity: WeakReference<Activity> = WeakReference(activity)

    private var allSongs: List<Song>? = null
    private var visibleSongs: List<Song>? = null

    private var filterQuery: String? = null

    /**
     * Gets a random song from the filtered list.
     */
    val randomRequestSong: Song?
        get() {
            val songs = songs
            return if (songs == null || songs.isEmpty()) {
                null
            } else {
                songs[Random().nextInt(songs.size)]
            }
        }

    var songs: List<Song>?
        get() = visibleSongs
        set(songs) {
            this.allSongs = songs
            updateSongs()
        }

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<SongItemBinding>(layoutInflater, R.layout.song_item, parent, false)
        return SongViewHolder(binding, this, songActionsUtil)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val song = visibleSongs!![position]
        val songHolder = holder as SongViewHolder
        songHolder.bind(song)
    }

    override fun getItemId(position: Int): Long {
        return visibleSongs!![position].id.toLong()
    }

    override fun getItemCount(): Int {
        return visibleSongs?.size ?: 0
    }

    fun filter(query: String) {
        this.filterQuery = query
        updateSongs()
    }

    fun sortType(sortType: String) {
        songSortUtil.setListSortType(listId, sortType)
        updateSongs()
    }

    fun sortDescending(descending: Boolean) {
        songSortUtil.setListSortDescending(listId, descending)
        updateSongs()
    }

    private fun updateSongs() {
        if (allSongs == null || allSongs!!.isEmpty()) return

        launchIO {
            visibleSongs = allSongs!!.asSequence()
                .filter { song -> song.search(filterQuery) }
                .sortedWith(songSortUtil.getComparator(listId))
                .toList()

            launchUI {
                notifyDataSetChanged()
            }
        }
    }

    private fun getActivity(): Activity {
        return activity.get()!!
    }

    private class SongViewHolder internal constructor(
        private val binding: SongItemBinding,
        adapter: SongsListAdapter,
        songActionsUtil: SongActionsUtil
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    val song = adapter.songs!![layoutPosition]
                    songActionsUtil.showSongsDialog(adapter.getActivity(), null, song)
                }
            }

            binding.root.setOnLongClickListener {
                val song = adapter.songs!![layoutPosition]
                songActionsUtil.copyToClipboard(adapter.getActivity(), song)
                true
            }
        }

        fun bind(song: Song) {
            binding.song = song

            binding.executePendingBindings()
        }
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Song>() {
    override fun areItemsTheSame(oldSong: Song, newSong: Song): Boolean {
        return oldSong.id == newSong.id
    }

    override fun areContentsTheSame(oldSong: Song, newSong: Song): Boolean {
        return oldSong.id == newSong.id
    }
}
