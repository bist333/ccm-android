package com.comic_con.museum.ar.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.GridView
import com.comic_con.museum.ar.R
import com.comic_con.museum.ar.experience.progress.ProgressModel

class ProgressGridView(c: Context, a: AttributeSet): GridView(c, a) {

    fun setUp(progressModel: ProgressModel) {
        val adapter = ProgressAdapter(context)
        adapter.progressModel = progressModel
        this.adapter = adapter

        this.onItemClickListener = adapter.getItemClickListener()
    }

    inner class ProgressAdapter(private val context: Context): BaseAdapter() {

        lateinit var progressModel: ProgressModel

        override fun getCount(): Int = progressModel.progressItems.size

        override fun getItem(position: Int) = progressModel.progressItems[position]

        override fun getItemId(position: Int) = this.getItem(position).progressId.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            (convertView as? ProgressView)?.let {
                return it
            }
            val newProgressView = LayoutInflater.from(context).inflate(R.layout.component_progress_view, parent, false) as ProgressView
            newProgressView.setProgress(getItem(position))
            return newProgressView
        }

        fun getItemClickListener(): AdapterView.OnItemClickListener {
            return AdapterView.OnItemClickListener { parent, view, position, id ->
                // TODO
            }
        }
    }
}