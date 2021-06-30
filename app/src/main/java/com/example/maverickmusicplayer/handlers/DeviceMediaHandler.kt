package com.example.maverickmusicplayer.handlers

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
import android.util.Size
import androidx.annotation.RequiresApi
import com.example.maverickmusicplayer.models.Album
import com.example.maverickmusicplayer.models.Artist
import com.example.maverickmusicplayer.models.Music
import java.lang.Exception

class DeviceMediaHandler(val context: Context) {

    val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

    val albumCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Albums.getContentUri(MediaStore.VOLUME_EXTERNAL)

            } else {
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
            }

    val songProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,


            )

    val songSelection = "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?"

    val selectionArgs = arrayOf("%mp3")


    val albumProjection = arrayOf(
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ALBUM_ART,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS,


            )

    val artistProjection = arrayOf(
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS

            )

    val artistCollection=
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Artists.getContentUri(MediaStore.VOLUME_EXTERNAL)

            } else {
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
            }


    fun getMediaFromDevice(): ArrayList<Music> {

        val query = context.contentResolver.query(collection, songProjection, songSelection, selectionArgs, null)
        val musicList = ArrayList<Music>()


        query.use { cursor ->


            var idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            var nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            var durationColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            var sizeColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            var artistColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            var dataColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)




            while (cursor!!.moveToNext()) {
                val id = cursor.getLong(idColumn!!)
                val name = cursor.getString(nameColumn!!)
                val duration = cursor.getInt(durationColumn!!)
                val size = cursor.getInt(sizeColumn!!)
                var artist = cursor.getString(artistColumn!!)
                val pathData = cursor.getString(dataColumn!!)
                val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                )


                var bitmap: Bitmap? = null

                try {
                    var mediaMetaDataRetriever = MediaMetadataRetriever()
                    mediaMetaDataRetriever.setDataSource(pathData)
                    val data = mediaMetaDataRetriever.embeddedPicture
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)

                    mediaMetaDataRetriever.release()
                } catch (e: Exception) {
                    bitmap = null
                }


                /*
            val artworkUri = Uri.parse("content://media/external/audio/albumart")
            val albumArtUri = ContentUris.withAppendedId(artworkUri, id)


             */



                musicList.add(Music(contentUri, id, name, artist, bitmap, duration, size))
            }








            return musicList
        }


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getAlbums(): ArrayList<Album> {

        var query = context.contentResolver.query(albumCollection, albumProjection, null, null, null)
        val albumList = ArrayList<Album>()
        var bitmap: Bitmap? = null

        query.use { cursor ->

            val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ID)
            val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
            val albumArtColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)
            val artistColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
            val noOfSongsColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)



            while (cursor!!.moveToNext()) {

                val id = cursor.getLong(idColumn!!)
                val name = cursor.getString(nameColumn!!)
                val albumArt = cursor.getString(albumArtColumn!!)
                val artist = cursor.getString(artistColumn!!)
                val noOfSongs = cursor.getInt(noOfSongsColumn!!)


                var albumArtUri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id)

                try {
                    bitmap = context.contentResolver.loadThumbnail(albumArtUri, Size(1024, 1024), null)
                } catch (e: Exception) {
                    bitmap = null
                }





                albumList.add(Album(id, name, artist, bitmap, noOfSongs))

            }


        }
        return albumList
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    fun getArtists(): ArrayList<Artist> {

        var query = context.contentResolver.query(artistCollection, artistProjection, null, null, null)
        val artistList = ArrayList<Artist>()
        var bitmap: Bitmap? = null
        var first=true

        query.use { cursor ->

            val idColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Artists._ID)
            val nameColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)
            val artistAlbumsColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
            val artistTracksColumn = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)







            while (cursor!!.moveToNext()) {

                val id = cursor.getLong(idColumn!!)
                val name = cursor.getString(nameColumn!!)

                val artistAlbums = cursor.getInt(artistAlbumsColumn!!)
                val artistTracks = cursor.getInt(artistTracksColumn!!)




//CAPTURE FIRST ALBUM ART
    val projection=arrayOf(MediaStore.Audio.Media.DATA,MediaStore.Audio.Media.ARTIST)
    val tempSelection="${MediaStore.Audio.Media.ARTIST} LIKE ?"
    val tempSelectionArgs= arrayOf(name)


    context.contentResolver.query(collection,projection,tempSelection,tempSelectionArgs,null).use {cursor1->

        val tempDataColumn=cursor1?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

        cursor1!!.moveToNext()
        val tempData=cursor1.getString(tempDataColumn!!)




        try {
            var mediaMetaDataRetriever = MediaMetadataRetriever()
            mediaMetaDataRetriever.setDataSource(tempData)
            val data = mediaMetaDataRetriever.embeddedPicture
            bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size)

            mediaMetaDataRetriever.release()
        }

        catch (e:Exception){
            bitmap=null
        }


    }










                artistList.add(Artist(id, name, artistAlbums, artistTracks,bitmap))

            }


        }
        return artistList
    }


}