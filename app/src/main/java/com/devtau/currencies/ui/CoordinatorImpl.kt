package com.devtau.currencies.ui

import android.content.Context
import android.content.Intent
import com.devtau.currencies.ui.activities.DBViewerActivity

object CoordinatorImpl: Coordinator {

    //<editor-fold desc="activities">
    override fun launchDBViewerActivity(context: Context?) {
        val intent = Intent(context, DBViewerActivity::class.java)
        context?.startActivity(intent)
    }
    //</editor-fold>


    //<editor-fold desc="dialogs">

    //</editor-fold>


    //<editor-fold desc="fragments">

    //</editor-fold>
}