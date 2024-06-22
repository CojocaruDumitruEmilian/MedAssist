package com.example.proiectdiploma

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CommonDiseaseAdapter(private val context: Context, private val dataSource: List<CommonDisease>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.common_disease_item, parent, false)

        val cityTextView = rowView.findViewById<TextView>(R.id.cityTextView)
        val diseaseTextView = rowView.findViewById<TextView>(R.id.diseaseTextView)

        val commonDisease = getItem(position) as CommonDisease

        // Log data to see what is being set
        Log.d("CommonDiseaseAdapter", "City: ${commonDisease.city}, Disease: ${commonDisease.disease}")

        cityTextView.text = commonDisease.city
        diseaseTextView.text = commonDisease.disease

        // Check if the TextViews are correctly assigned
        Log.d("CommonDiseaseAdapter", "cityTextView text: ${cityTextView.text}")
        Log.d("CommonDiseaseAdapter", "diseaseTextView text: ${diseaseTextView.text}")

        return rowView
    }
}
