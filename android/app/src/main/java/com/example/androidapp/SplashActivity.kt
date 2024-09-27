package com.example.androidapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val requiredPermissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
    private val optionalPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (requiredPermissions.any {
            checkSelfPermission(it) == PackageManager.PERMISSION_DENIED
        }) {
            requestPermissions((requiredPermissions + optionalPermissions).toTypedArray(), 1001)
        } else {
            navigateToConfig()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissions.forEachIndexed { index, permission ->
            if (requiredPermissions.contains(permission) &&
                grantResults[index] == PackageManager.PERMISSION_DENIED
            ) {
                AlertDialog.Builder(this)
                    .setMessage("You cannot use this app because the required permissions are missing.")
                    .setPositiveButton(android.R.string.ok) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                        finish()
                    }
                    .show()
                return
            }
        }

        navigateToConfig()
    }

    private fun navigateToConfig() {
        startActivity(Intent(this, PersoConfigActivity::class.java))
        finish()
    }

}