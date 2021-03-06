package com.example.maverickmusicplayer.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.maverickmusicplayer.R
import com.example.maverickmusicplayer.activities.MainActivity
import com.example.maverickmusicplayer.adapters.SongsRecyclerAdapter
import com.example.maverickmusicplayer.constants.Constants
import com.example.maverickmusicplayer.handlers.DeviceMediaHandler
import com.example.maverickmusicplayer.interfaces.SongOnClickListener
import com.example.maverickmusicplayer.models.Album
import com.example.maverickmusicplayer.models.Music
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_album_song.*


class AlbumSongFragment : Fragment() {
    var album: Album? = null
    var task: AsyncTask<Void, Void, ArrayList<Music>>? = null
    var musicList = ArrayList<Music>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val bundle = this.arguments
        album = bundle!!.getParcelable(Constants.ALBUM_SONG_BUNDLE) as Album?


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imb_albumSong_back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        if(album?.art!=null) {

            imv_albumInfoCover.setImageURI(album?.art)

        }
        else{

                var drawable=context?.resources?.getDrawable(R.drawable.album_placeholder)
                imv_albumInfoCover.background=drawable

        }


        tv_albumInfoTitle.text=album?.name
        tv_albumInfoArtist.text="By: ${album?.artist}"
        tv_albumInfo.text="Tracks: ${album?.numberOfSongs}"


        task = object : AsyncTask<Void, Void, ArrayList<Music>>() {
            override fun doInBackground(vararg params: Void?): ArrayList<Music> {
                var songs = DeviceMediaHandler(requireContext()).getMediaFromDevice(album?.name,null)

                return songs
            }

            override fun onPostExecute(result: ArrayList<Music>?) {
                super.onPostExecute(result)
                musicList = result!!
                refreshSongs()


            }


        }.execute()


    }

    fun refreshSongs() {
        var songsRecyclerAdapter = SongsRecyclerAdapter(requireContext(), musicList,true)
        Constants.nowPlayingParent=songsRecyclerAdapter.parent
        rv_albumSongs.layoutManager = LinearLayoutManager(requireContext())



        rv_albumSongs.adapter = songsRecyclerAdapter


        songsRecyclerAdapter.setOnSongClicked(object: SongOnClickListener {
            override fun onItemClicked(position: Int) {

                (rv_albumSongs.adapter as SongsRecyclerAdapter).notifyDataSetChanged()
                (activity as MainActivity).vp_songPlaying.setCurrentItem(position,false)
            }


        })



    }

}

