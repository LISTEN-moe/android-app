package me.echeung.moemoekyun.adapter

import android.app.Activity
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.databinding.SongItemBinding
import me.echeung.moemoekyun.util.SongActionsUtil
import me.echeung.moemoekyun.util.SongSortUtil
import java.lang.ref.WeakReference
import java.util.*

class SongsAdapter(activity: Activity, private val listId: String) : ListAdapter<Song, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

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
            return if (songs == null || songs.isEmpty())
                null
            else
                songs[Random().nextInt(songs.size)]
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
        return SongViewHolder(binding, this)
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
        return if (visibleSongs != null) visibleSongs!!.size else 0
    }

    fun filter(query: String) {
        this.filterQuery = query
        updateSongs()
    }

    fun sortType(sortType: String) {
        val activityRef = activity.get() ?: return

        SongSortUtil.setListSortType(activityRef, listId, sortType)
        updateSongs()
    }

    fun sortDescending(descending: Boolean) {
        val activityRef = activity.get() ?: return

        SongSortUtil.setListSortDescending(activityRef, listId, descending)
        updateSongs()
    }

    private fun updateSongs() {
        val activityRef = activity.get() ?: return

        if (allSongs == null || allSongs!!.isEmpty()) return

        visibleSongs = allSongs

        if (!TextUtils.isEmpty(filterQuery)) {
            visibleSongs = allSongs!!.filter { song -> song.search(filterQuery!!) }.toList()
        }

        SongSortUtil.sort(activityRef, listId, visibleSongs!!)

        notifyDataSetChanged()
    }

    private fun getActivity(): Activity {
        return activity.get()!!
    }

    private class SongViewHolder internal constructor(private val binding: SongItemBinding, adapter: SongsAdapter) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val song = adapter.songs!![layoutPosition]
                    SongActionsUtil.showSongsDialog(adapter.getActivity(), null, song)
                }
            }

            binding.root.setOnLongClickListener {
                val song = adapter.songs!![layoutPosition]
                SongActionsUtil.copyToClipboard(adapter.getActivity(), song)
                true
            }
        }

        internal fun bind(song: Song) {
            binding.song = song

            binding.executePendingBindings()
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldSong: Song, newSong: Song): Boolean {
                return oldSong.id == newSong.id
            }

            override fun areContentsTheSame(oldSong: Song, newSong: Song): Boolean {
                return oldSong == newSong
            }
        }
    }
}
