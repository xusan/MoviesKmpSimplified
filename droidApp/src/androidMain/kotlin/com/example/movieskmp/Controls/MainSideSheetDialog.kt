package com.example.movieskmp.Controls

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import com.app.shared.ViewModels.MenuItem
import com.app.shared.ViewModels.MenuType
import com.app.shared.ViewModels.MoviesPageViewModel
import com.base.abstractions.Diagnostic.ILoggingService
import com.base.abstractions.Essentials.IAppInfo
import com.base.impl.ContainerLocator
import com.base.impl.Droid.Utils.CurrentActivity
import com.google.android.material.sidesheet.SideSheetDialog
import com.example.movieskmp.MainActivity
import com.example.movieskmp.R


class MainSideSheetDialog(context: Context) : SideSheetDialog(context) {

    private var btnLogout: ViewGroup? = null
    private var btnShare: ViewGroup? = null
    private var lblVersion: TextView? = null

    override fun setContentView(layoutResId: Int) {
        super.setContentView(layoutResId)

        btnLogout = findViewById(R.id.btnLogout)
        btnShare = findViewById(R.id.btnShare)
        lblVersion = findViewById(R.id.lblVersion)

        btnShare?.setOnClickListener { view ->
            onMenuItemClicked(view as ViewGroup)
        }

        btnLogout?.setOnClickListener { view ->
            onMenuItemClicked(view as ViewGroup)
        }

        var appInfo = ContainerLocator.Resolve<IAppInfo>()
        lblVersion?.text = appInfo.BuildString
    }

    private fun onMenuItemClicked(btn: ViewGroup) {
        try {
            dismiss()

            val menuItem = getClickedMenu(btn)
            val mainVm = (CurrentActivity.Instance as MainActivity).GetRootPageViewModel() as MoviesPageViewModel

            mainVm?.let { vm ->
                vm.Services.LoggingService.Log("Selected menu details: menuItem: $menuItem, rootPage: $vm")
                vm.MenuTappedCommand.Execute(menuItem)
            }
        } catch (ex: Exception) {
            val logger = ContainerLocator.Resolve<ILoggingService>()
            logger.TrackError(ex)
        }
    }

    private fun getClickedMenu(btn: ViewGroup): MenuItem
    {
        if(btn == btnShare) {
            val mi = MenuItem()
            mi.Type = MenuType.ShareLogs
            return mi
        }
        else if(btn == btnLogout) {
            val mi = MenuItem()
            mi.Type = MenuType.Logout
            return mi
        }

        return MenuItem()
    }
}