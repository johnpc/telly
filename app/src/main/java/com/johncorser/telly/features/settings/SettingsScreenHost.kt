package com.johncorser.telly.features.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

/**
 * Wires the settings view model into the shell: routes Add playlist into
 * the wizard and hosts the SAF launchers for Back up / Restore data. The
 * document-picker plumbing is deliberately thin (logic is in
 * SettingsBackupManager); VERIFY-ON-DEVICE.
 */
@Composable
fun SettingsScreenHost(
    graph: SettingsGraph,
    onAddPlaylist: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingExport by remember { mutableStateOf("") }
    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument(SettingsBackupManager.BACKUP_MIME_TYPE),
        ) { uri ->
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use {
                        out ->
                    out.write(pendingExport.toByteArray())
                }
            }
        }
    val model =
        remember {
            graph.viewModel(
                scope = scope,
                callbacks =
                    SettingsCallbacks(
                        onAddPlaylist = onAddPlaylist,
                        onExportBackup = { json ->
                            pendingExport = json
                            exportLauncher.launch(SettingsBackupManager.BACKUP_FILE_NAME)
                        },
                        onImportBackup = {},
                    ),
            )
        }
    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            uri
                ?.let {
                    context.contentResolver.openInputStream(it)?.use {
                            input ->
                        input.readBytes().decodeToString()
                    }
                }
                ?.let(model::importBackup)
        }
    model.callbacks.onImportBackup = {
        importLauncher.launch(arrayOf(SettingsBackupManager.BACKUP_MIME_TYPE, "text/*"))
    }
    SettingsScreen(model, onClose)
}
