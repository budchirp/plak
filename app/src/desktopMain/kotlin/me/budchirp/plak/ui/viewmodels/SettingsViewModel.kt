package me.budchirp.plak.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import me.budchirp.plak.data.manager.ConfigManager

class SettingsViewModel : ViewModel() {
    private val configManager = ConfigManager()
    private var config = configManager.get()

    var darkMode by mutableStateOf(config.ui.darkMode)
        private set

    var useDedicatedGPU by mutableStateOf(config.launch.useDedicatedGPU)
        private set

    var useGamemodeRun by mutableStateOf(config.launch.useGamemodeRun)
        private set

    var useMangoHUD by mutableStateOf(config.launch.useMangoHUD)
        private set

    var dataDir by mutableStateOf(config.dataDir)
        private set

    fun updateDarkMode(darkMode: Boolean) {
        this.darkMode = darkMode
    }

    fun updateUseDedicatedGPU(useDedicatedGPU: Boolean) {
        this.useDedicatedGPU = useDedicatedGPU
    }

    fun updateUseGamemodeRun(useGamemodeRun: Boolean) {
        this.useGamemodeRun = useGamemodeRun
    }

    fun updateUseMangoHUD(useMangoHUD: Boolean) {
        this.useMangoHUD = useMangoHUD
    }

    fun updateDataDir(dataDir: String) {
        this.dataDir = dataDir
    }

    fun refresh() {
        config = configManager.get()
    }

    fun submit(onSuccess: () -> Unit) {
        configManager.set(
            config.copy(
                ui = config.ui.copy(
                    darkMode = darkMode
                ),
                launch = config.launch.copy(
                    useDedicatedGPU = useDedicatedGPU,
                    useGamemodeRun = useGamemodeRun,
                    useMangoHUD = useMangoHUD
                ),
                dataDir = dataDir
            )
        )

        onSuccess()
    }
}