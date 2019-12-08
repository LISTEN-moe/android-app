package me.echeung.moemoekyun.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import me.echeung.moemoekyun.App
import me.echeung.moemoekyun.R
import me.echeung.moemoekyun.client.model.Song
import me.echeung.moemoekyun.databinding.SongDetailsBinding
import me.echeung.moemoekyun.util.SongActionsUtil

class SongDetailAdapter(
        private val activity: Activity,
        songs: List<Song>,
        private val songActionsUtil: SongActionsUtil
) : ArrayAdapter<Song>(activity, 0, songs) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val binding: SongDetailsBinding
        var view = convertView

        if (view == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.song_details, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = view.tag as SongDetailsBinding
        }

        val song = getItem(position) ?: return binding.root

        binding.song = song
        binding.isAuthenticated = App.authTokenUtil.isAuthenticated
        binding.isFavorite = song.favorite

        binding.requestBtn.setOnClickListener { songActionsUtil.request(activity, song) }

        binding.favoriteBtn.setOnClickListener {
            songActionsUtil.toggleFavorite(activity, song)

            song.favorite = !song.favorite
            binding.isFavorite = song.favorite
        }

        binding.root.setOnLongClickListener { v ->
            songActionsUtil.copyToClipboard(v.context, song)
            true
        }

        return binding.root
    }
}
