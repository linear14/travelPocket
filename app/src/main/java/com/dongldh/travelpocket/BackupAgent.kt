package com.dongldh.travelpocket

import android.app.backup.BackupAgentHelper
import android.app.backup.FileBackupHelper
import android.app.backup.SharedPreferencesBackupHelper

const val sharedPref = "pref"
const val backup_key = "backup"

class BackupAgent: BackupAgentHelper() {
    override fun onCreate() {
        val helper_shared = SharedPreferencesBackupHelper(this, sharedPref)
        addHelper(backup_key, helper_shared)
    }
}