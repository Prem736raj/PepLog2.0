package com.appvexis.peptidetracker

import android.app.Activity
import android.app.KeyguardManager
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.runtime.collectAsState
import com.appvexis.peptidetracker.core.datastore.UserPreferencesDataSource
import com.appvexis.peptidetracker.core.ui.components.PepLogButton
import com.appvexis.peptidetracker.core.ui.components.PepLogCard
import com.appvexis.peptidetracker.core.ui.components.PepLogLoadingState
import com.appvexis.peptidetracker.core.ui.theme.PepLogTheme
import com.appvexis.peptidetracker.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userPreferences: UserPreferencesDataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PepLogTheme {
                val appLockEnabled by userPreferences.isAppLockEnabled.collectAsState(initial = null)
                when (appLockEnabled) {
                    null -> PepLogLoadingState()
                    else -> AppLockGate(enabled = appLockEnabled == true) {
                        AppNavigation()
                    }
                }
            }
        }
    }
}

/**
 * A small privacy gate backed by the phone's existing screen lock. PepLog never
 * stores a PIN, password, or biometric template; Android owns authentication.
 */
@Composable
private fun AppLockGate(
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val keyguardManager = remember(context) {
        context.getSystemService(KeyguardManager::class.java)
    }
    // These states must not be saveable: recreating the activity must require
    // authentication again when the lock is enabled.
    var unlocked by remember { mutableStateOf(false) }
    var observedEnabled by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(enabled) {
        when {
            !enabled -> unlocked = true
            observedEnabled == null -> unlocked = false
            observedEnabled == false -> unlocked = true
        }
        observedEnabled = enabled
    }

    DisposableEffect(lifecycleOwner, enabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && enabled) unlocked = false
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(view, enabled) {
        val window = (view.context as? Activity)?.window
        if (enabled) window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            if (enabled) window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val unlockLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) unlocked = true
    }

    if (!enabled || unlocked) {
        content()
    } else {
        AppLockScreen(
            hasDeviceLock = keyguardManager?.isDeviceSecure == true,
            onUnlock = {
                val credentialIntent = keyguardManager?.createConfirmDeviceCredentialIntent(
                    "Unlock PepLog",
                    "Use your phone's screen lock to view your private records.",
                ) ?: Intent(Settings.ACTION_SECURITY_SETTINGS)
                unlockLauncher.launch(credentialIntent)
            },
        )
    }
}

@Composable
private fun AppLockScreen(
    hasDeviceLock: Boolean,
    onUnlock: () -> Unit,
) {
    val colors = PepLogTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PepLogCard(modifier = Modifier.fillMaxWidth(), isGlassmorphic = true) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = colors.primary,
                )
                Text(
                    text = "PepLog is locked",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                )
                Text(
                    text = if (hasDeviceLock) {
                        "Use your phone's screen lock to view your private records."
                    } else {
                        "Set a screen lock in Android Settings, then return here to protect PepLog."
                    },
                    color = colors.textSecondary,
                )
                PepLogButton(
                    text = if (hasDeviceLock) "Unlock PepLog" else "Set screen lock",
                    onClick = onUnlock,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
