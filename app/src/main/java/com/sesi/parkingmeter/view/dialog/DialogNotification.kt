package com.sesi.parkingmeter.view.dialog

import android.app.Dialog
import android.view.LayoutInflater
import com.sesi.parkingmeter.R
import com.sesi.parkingmeter.databinding.DialogNotificationPermissionBinding

object DialogNotification {

    fun createDialog(layoutInflater: LayoutInflater, onAction: OnAction): Dialog {
        val bsd = Dialog(layoutInflater.context, R.style.BottomSheetDialogStyle)
        val binding = DialogNotificationPermissionBinding.inflate(layoutInflater)
        bsd.setContentView(binding.root)
        bsd.show()

        binding.btnOk.setOnClickListener {
            onAction.onAccept()
            bsd.dismiss()
        }

        binding.btnSkip.setOnClickListener {
            bsd.dismiss()
        }
        return bsd
    }

    interface OnAction {
        fun onAccept()
    }
}