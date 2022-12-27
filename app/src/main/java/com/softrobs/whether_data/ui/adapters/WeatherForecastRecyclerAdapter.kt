package com.softrobs.whether_data.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.softrobs.whether_data.R
import com.softrobs.whether_data.data.remote.response.WeatherData
import com.softrobs.whether_data.utils.IMAGE_URL
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat

class WeatherForecastRecyclerAdapter(private val dataList: List<WeatherData>,
                                     private val context: Context):
    RecyclerView.Adapter<WeatherForecastRecyclerAdapter.ForecastHolder>() {

    class ForecastHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView){
            val timeTxt:TextView = itemView.findViewById(R.id.wetherTime)
            val temText:TextView = itemView.findViewById(R.id.wetherTemperature)
            val img:ImageView = itemView.findViewById(R.id.wetherIcon)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.weather_recycler_layout,parent,false)
        return ForecastHolder(itemView)
    }

    override fun onBindViewHolder(holder: ForecastHolder, position: Int) {
        val items = dataList[position]
        val dateTime = items.dt_txt.toString()
        val parser =  SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm")
        val formattedDate = formatter.format(parser.parse(dateTime.toString()))
        val formattedTime = formattedDate.split(" ")

        holder.timeTxt.text = formattedTime[1]
        holder.temText.text = "${items.main.temp.toString()}°"
        Picasso.get().load(IMAGE_URL+"${items.weather[0].icon}.png").into(holder.img)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }


}