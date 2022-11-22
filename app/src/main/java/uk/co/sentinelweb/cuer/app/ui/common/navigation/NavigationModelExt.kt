package uk.co.sentinelweb.cuer.app.ui.common.navigation

import android.content.Intent
import android.os.Bundle
import uk.co.sentinelweb.cuer.app.ui.common.navigation.NavigationModel.Param


fun Param.getLong(b: Bundle?) = b?.getLong(name)
fun Param.getLong(i: Intent?) =
    i?.let { if (it.hasExtra(name)) it.getLongExtra(name, -1) else null }

fun Param.getBoolean(b: Bundle?, def: Boolean = false) = b?.getBoolean(name, def) ?: def
fun Param.getBoolean(i: Intent?) = i?.getBooleanExtra(name, false) ?: false

fun Param.getString(b: Bundle?) = b?.getString(name)
fun Param.getString(i: Intent?) = i?.getStringExtra(name)

inline fun <reified T : Enum<T>> Param.getEnum(b: Bundle?): T? =
    b?.getString(name)?.let { pref -> enumValues<T>().find { it.name == pref } }

inline fun <reified T : Enum<T>> Param.getEnum(i: Intent?): T? =
    i?.getStringExtra(name)?.let { pref -> enumValues<T>().find { it.name == pref } }