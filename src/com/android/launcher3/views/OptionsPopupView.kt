/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.views

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.os.Handler
import android.util.ArrayMap
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherState
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.popup.ArrowPopup
import com.android.launcher3.shortcuts.DeepShortcutView
import com.android.launcher3.userevent.nano.LauncherLogProto
import com.android.launcher3.widget.WidgetsFullSheet
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.keyOS.fragment_password_prompt_sheet.*
import tech.DevAsh.KeyOS.Database.UserContext
import tech.DevAsh.KeyOS.Helpers.KioskHelpers.Kiosk
import java.util.*


/**
 * Popup shown on long pressing an empty space in launcher
 */
class OptionsPopupView @JvmOverloads constructor(context: Context?, attrs: AttributeSet?,
                                                 defStyleAttr: Int = 0) :
        ArrowPopup(context, attrs, defStyleAttr), View.OnClickListener, View.OnLongClickListener {
    private val mItemMap = ArrayMap<View, OptionItem>()
    private var mTargetRect: RectF? = null
    override fun onClick(view: View) {
        handleViewClick(view, LauncherLogProto.Action.Touch.TAP)
    }

    override fun onLongClick(view: View): Boolean {
        return handleViewClick(view, LauncherLogProto.Action.Touch.LONGPRESS)
    }

    private fun handleViewClick(view: View, action: Int): Boolean {
        val item = mItemMap[view] ?: return false
        if (item.mControlTypeForLog > 0) {
            logTap(action, item.mControlTypeForLog)
        }
        if (item.mClickListener.onLongClick(view)) {
            close(true)
            return true
        }
        return false
    }

    private fun logTap(action: Int, controlType: Int) {}
    override fun onControllerInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action != MotionEvent.ACTION_DOWN) {
            return false
        }
        if (mLauncher.dragLayer.isEventOverView(this, ev)) {
            return false
        }
        close(true)
        return true
    }

    override fun logActionCommand(command: Int) {
    }

    override fun isOfType(type: Int): Boolean {
        return type and TYPE_OPTIONS_POPUP != 0
    }

    override fun getTargetObjectLocation(outPos: Rect) {
        mTargetRect!!.roundOut(outPos)
    }

    class OptionItem(val mLabelRes: Int, val mIconRes: Int, val mControlTypeForLog: Int,
                     val mClickListener: OnLongClickListener)

    companion object {
        fun show(launcher: Launcher, targetRect: RectF?, items: List<OptionItem>) {
            val popup = launcher.layoutInflater
                    .inflate(R.layout.longpress_options_menu, launcher.dragLayer,
                             false) as OptionsPopupView
            popup.mTargetRect = targetRect
            for (item in items) {
                val view = popup.inflateAndAdd<DeepShortcutView>(R.layout.system_shortcut, popup)
                view.iconView.setBackgroundResource(item.mIconRes)
                view.bubbleText.setText(item.mLabelRes)
                view.setDividerVisibility(INVISIBLE)
                view.setOnClickListener(popup)
                view.setOnLongClickListener(popup)
                popup.mItemMap[view] = item
            }
            popup.reorderAndShow(popup.childCount)
        }

        @JvmStatic
        fun showDefaultOptions(launcher: Launcher, x: Float, y: Float) {
            var x = x
            var y = y
            val halfSize = launcher.resources.getDimension(R.dimen.options_menu_thumb_size) / 2
            if (x < 0 || y < 0) {
                x = launcher.dragLayer.width / 2.toFloat()
                y = launcher.dragLayer.height / 2.toFloat()
            }
            val target = RectF(x - halfSize, y - halfSize, x + halfSize, y + halfSize)
            val options = ArrayList<OptionItem>()
            options.add(OptionItem(R.string.wallpaper_button_text, R.drawable.exit,
                                   LauncherLogProto.ControlType.WALLPAPER_BUTTON) { v: View ->
                startWallpaperPicker(v)
            })
            if (!Utilities.getKioskPrefs(launcher).lockDesktop) {
                options.add(OptionItem(R.string.widget_button_text, R.drawable.ic_widget,
                                       LauncherLogProto.ControlType.WIDGETS_BUTTON) { view: View ->
                    onWidgetsClicked(view)
                })
            }
            options.add(OptionItem(R.string.button_overview_mode, R.drawable.ic_pages,
                                   -1) { view: View -> startOrganizer(view) })
            options.add(OptionItem(R.string.settings_button_text, R.drawable.ic_setting,
                                   LauncherLogProto.ControlType.SETTINGS_BUTTON) { view: View ->
                startSettings(view)
            })
            show(launcher, target, options)
        }

        fun onWidgetsClicked(view: View): Boolean {
            return openWidgets(Launcher.getLauncher(view.context))
        }

        @JvmStatic
        fun openWidgets(launcher: Launcher): Boolean {
            return if (launcher.packageManager.isSafeMode) {
                Toast.makeText(launcher, R.string.safemode_widget_error,
                                  Toast.LENGTH_SHORT).show()
                false
            } else {
                WidgetsFullSheet.show(launcher, true /* animated */)
                true
            }
        }

        fun startSettings(v: View): Boolean {
            val dialog = getDialog(v)
            val launcher = Launcher.getLauncher(v.context)
            dialog.done.setOnClickListener{
                val password  = dialog.password.query.toString()
                if(password == UserContext.user!!.password) {
                    dialog.dismiss()
                    getProgress(v,"Loading settings")
                    Handler()
                            .postDelayed({
                                             Kiosk.openKioskSettings(launcher)
                                         }, 2000)
                }
            }
            dialog.show()
            return true
        }

        private fun startOrganizer(view: View): Boolean {
            val launcher = Launcher.getLauncher(view.context)
            launcher.stateManager.goToState(LauncherState.OPTIONS, true)
            return true
        }

        /**
         * Event handler for the wallpaper picker button that appears after a long press
         * on the home screen.
         */

        fun startWallpaperPicker(v: View): Boolean {
            val dialog = getDialog(v)
            val launcher = Launcher.getLauncher(v.context)
            dialog.done.setOnClickListener{
                val password  = dialog.password.query.toString()
                if(password == UserContext.user!!.password) {
                    dialog.dismiss()
                    getProgress(v,"Exiting keyOS")
                    Handler()
                            .postDelayed({
                                             Kiosk.exitKiosk(launcher)
                                         }, 2000)
                }
            }
            dialog.show()
            return true
        }

        private fun getDialog(v: View):BottomSheetDialog{
            val launcher = Launcher.getLauncher(v.context)
            val dialog = BottomSheetDialog(launcher)
            dialog.setContentView(R.layout.fragment_password_prompt_sheet)
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }


        private fun getProgress(v:View,string:String){
            val launcher = Launcher.getLauncher(v.context)
            val mProgressDialog = ProgressDialog(launcher)
            mProgressDialog.setMessage(string)
            mProgressDialog.setCanceledOnTouchOutside(false)
            mProgressDialog.show()
            Handler().postDelayed({
                mProgressDialog.dismiss()
                                  },1500)
        }

    }
}

