package com.lifeoneuropa.idothecooking.state

import android.os.AsyncTask

class ModelAsyncTask<D, T>(
    private val dao: D,
    private val taskListener: ModelAsyncListener<D, T>?) : AsyncTask<T, Void?, T>() {
    interface ModelAsyncListener<D, T> {
        fun onExecute(dao: D, obj: T)
    }

    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg params: T): T {
        taskListener?.onExecute(dao, params[0])
        return params[0]
    }
}