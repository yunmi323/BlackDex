package top.niunaijun.blackdex.view.setting

import android.os.Bundle
import android.os.Environment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import top.niunaijun.blackdex.app.App
import top.niunaijun.blackdex.R
import top.niunaijun.blackdex.app.AppManager
import top.niunaijun.blackdex.app.BlackDexLoader
import java.io.File


/**
 *
 * @Description:
 * @Author: wukaicheng
 * @CreateDate: 2021/5/28 19:55
 */
class SettingFragment : PreferenceFragmentCompat() {

    private lateinit var savePathPreference: Preference

    private lateinit var saveEnablePreference: SwitchPreferenceCompat

    private lateinit var fixCodeItemPreference: SwitchPreferenceCompat

    private lateinit var hookDumpPreference: SwitchPreferenceCompat

    private val initialDirectory = AppManager.mBlackBoxLoader.getSavePath()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.setting)
        savePathPreference = findPreference("save_path")!!
        savePathPreference.onPreferenceClickListener = mSavedPathClick
        savePathPreference.summary = initialDirectory

        saveEnablePreference = findPreference("save_enable")!!
        saveEnablePreference.onPreferenceChangeListener = mSaveEnableChange
        saveEnablePreference.isChecked = AppManager.mBlackBoxLoader.saveEnable()

        fixCodeItemPreference = findPreference("fix_code_item")!!
        fixCodeItemPreference.onPreferenceChangeListener = mFixCodeItemChange
        fixCodeItemPreference.isChecked = AppManager.mBlackBoxLoader.isFixCodeItem()

        hookDumpPreference = findPreference("hook_dump")!!
        hookDumpPreference.onPreferenceChangeListener = mHookDumpChange
        hookDumpPreference.isChecked = AppManager.mBlackBoxLoader.isHookDump()

    }

    private val mSavedPathClick = Preference.OnPreferenceClickListener {
        val initialFile = with(initialDirectory) {
            if (initialDirectory.isEmpty()) {
                Environment.getExternalStorageDirectory()
            } else {
                File(this)
            }
        }

        MaterialDialog(requireContext()).show {
            folderChooser(
                requireContext(),
                initialDirectory = initialFile,
                allowFolderCreation = true
            ) { _, file ->
                AppManager.mBlackBoxLoader.setSavePath(file.absolutePath)
                savePathPreference.summary = file.absolutePath
            }
            negativeButton(res = R.string.cancel)
        }
        return@OnPreferenceClickListener true
    }

    private val mSaveEnableChange = Preference.OnPreferenceChangeListener { _, newValue ->
        if (newValue == false) {
            (requireActivity() as SettingActivity).setRequestCallback(requestResult)
        } else {
            AppManager.mBlackBoxLoader.saveEnable(true)
            saveEnablePreference.isChecked = true
        }
        return@OnPreferenceChangeListener true
    }

    private val mHookDumpChange = Preference.OnPreferenceChangeListener { _, newValue ->
        AppManager.mBlackBoxLoader.setHookDump(newValue as Boolean)
        return@OnPreferenceChangeListener true
    }

    private val mFixCodeItemChange = Preference.OnPreferenceChangeListener { _, newValue ->
        if (newValue as Boolean) {

            MaterialDialog(requireContext()).show {
                title(R.string.warn)
                message(R.string.fix_code_item_message)
                positiveButton(R.string.confirm) {
                    AppManager.mBlackBoxLoader.setFixCodeItem(true)
                }
                negativeButton(R.string.cancel) {
                    fixCodeItemPreference.isChecked = false
                    AppManager.mBlackBoxLoader.setFixCodeItem(false)
                }
            }

        } else {
            AppManager.mBlackBoxLoader.setFixCodeItem(newValue)
        }
        return@OnPreferenceChangeListener true
    }


    private val requestResult = { hasPermission: Boolean ->
        AppManager.mBlackBoxLoader.saveEnable(!hasPermission)
        saveEnablePreference.isChecked = !hasPermission

        if (AppManager.mBlackBoxLoader.getSavePath().isEmpty()) {
            val path = BlackDexLoader.getDexDumpDir(App.getContext())
            AppManager.mBlackBoxLoader.setSavePath(path)
            savePathPreference.summary = path
        }
    }
}
