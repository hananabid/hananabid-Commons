package com.hananabid.commons.dialogs

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.text.Html
import android.text.method.LinkMovementMethod
import com.hananabid.commons.R
import com.hananabid.commons.extensions.launchViewIntent
import com.hananabid.commons.extensions.setupDialogStuff
import kotlinx.android.synthetic.main.dialog_purchase_thank_you.view.*

class PurchaseThankYouDialog(val activity: Activity) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_purchase_thank_you, null).apply {
            purchase_thank_you.text = Html.fromHtml(activity.getString(R.string.purchase_thank_you))
            purchase_thank_you.movementMethod = LinkMovementMethod.getInstance()
        }

        AlertDialog.Builder(activity)
                .setPositiveButton(R.string.purchase, { dialog, which -> activity.launchViewIntent(R.string.thank_you_url) })
                .setNegativeButton(R.string.cancel, null)
                .create().apply {
                    activity.setupDialogStuff(view, this)
                }
    }
}
