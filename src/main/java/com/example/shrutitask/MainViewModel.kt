package com.example.shrutitask

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.shrutitask.model.Video
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream


class MainViewModel : ViewModel()
{
    private var videoList: MutableLiveData<ArrayList<Video>>? = null

    private var arrVideoList = ArrayList<Video>()
    private val viewModelJob = SupervisorJob()

    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    /**
     * Cancel all coroutines when the ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun getList(): LiveData<ArrayList<Video>>? {
        videoList = MutableLiveData(null)
        return videoList
    }

    fun parseJson(context: Context){

        uiScope.launch {
            try {
                val obj = loadJSONFromAsset(context)?.let { JSONObject(it) }
                val m_jArry = obj?.getJSONArray("categories")
                var row : Video

                for (i in 0 until m_jArry!!.length()) {
                    val jo_inside = m_jArry.getJSONObject(i)

                    val name = jo_inside.getString("name")

                    val videos = jo_inside.getJSONArray("videos")

                    for(j in  0 until videos!!.length()){
                        val video_inside = videos.getJSONObject(j)

                        row = Video()
                        row.description = video_inside.getString("description")
                        row.subtitle = video_inside.getString("subtitle")
                        row.thumb = video_inside.getString("thumb")
                        row.title = video_inside.getString("title")
                        row.sources = video_inside.getJSONArray("sources").get(0).toString()

                        arrVideoList.add( row)
                        //searchAdapter?.notifyDataSetChanged()
                        videoList?.postValue(arrVideoList)
                    }

                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

    }


    fun loadJSONFromAsset(context : Context): String? {
        var json: String? = null
        json = try {
            val `is`: InputStream = context.getAssets().open("json.txt")
            val size: Int = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer)
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        }
        return json
    }
}