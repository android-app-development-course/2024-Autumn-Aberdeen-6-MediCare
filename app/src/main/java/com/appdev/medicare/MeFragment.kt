package com.appdev.medicare


import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.appdev.medicare.api.RetrofitClient
import com.appdev.medicare.databinding.FragmentMeBinding
import com.appdev.medicare.utils.DatabaseSync
import com.appdev.medicare.utils.buildAlertDialog
import com.appdev.medicare.utils.buildInputAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MeFragment : Fragment() {

    private var _binding: FragmentMeBinding? = null
    private val binding get() = _binding!!
    private lateinit var tvLoginText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeBinding.inflate(inflater, container, false)

        val root: View = binding.root

        val llLoginArea = binding.loginArea
        tvLoginText = binding.textLogin
        llLoginArea.setOnClickListener {
            val prefs = requireContext().getSharedPreferences("MediCare", Context.MODE_PRIVATE)
            val token = prefs.getString("loginToken", null)
            val username = prefs.getString("username", "") as String
            if (token.isNullOrEmpty()) {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                // 登录成功后 text 更换为用户名 + 欢迎语
            } else {
                tvLoginText.text = username
                Toast.makeText(requireContext(), requireContext().getString(R.string.welcomeMsg, username), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val llMemberManagement = binding.memberArea
        llMemberManagement.setOnClickListener {
            Log.d("MeFragment", "Member clicked")
//            val intent = Intent(requireContext(), MemberManagementActivity::class.java)
//            startActivity(intent)
        }

        val llNotification = binding.notificationArea
        llNotification.setOnClickListener {
            Log.d("MeFragment", "Notification clicked")
//            val intent = Intent(requireContext(), NotificationActivity::class.java)
//            startActivity(intent)
        }

        val llSettings = binding.settingsArea
        llSettings.setOnClickListener {
            val mainContext = requireContext()
            val preferences = mainContext.getSharedPreferences("MediCare", Context.MODE_PRIVATE)
            val serverBaseUrl = preferences.getString(
                "serverBaseUrl",
                "http://10.0.2.2:5000/"
            ) as String

            Log.d("MeFragment", "Settings clicked")
            buildInputAlertDialog(
                mainContext,
                mainContext.getString(R.string.serverBaseUrl),
                mainContext.getString(R.string.serverBaseUrlDes),
                serverBaseUrl
            ) { baseUrl, dialog ->
                val editor = preferences.edit()
                editor.putString("serverBaseUrl", baseUrl)
                editor.apply()
                Log.d("MeFragment", "Server base URL set to $baseUrl")
                dialog.dismiss()
                Toast.makeText(
                    mainContext,
                    mainContext.getString(R.string.restartToTakeEffect),
                    Toast.LENGTH_SHORT
                ).show()
            }
                .show()
        }

        val llLogout = binding.logoutArea
        llLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return root
    }

    private fun showLogoutConfirmationDialog() {
        val mainContext = requireContext()
        buildAlertDialog(
            mainContext,
            mainContext.getString(R.string.logout),
            mainContext.getString(R.string.confirmLogout),
            true
        ) { dialog: DialogInterface ->
            if (DatabaseSync.isOnline) {
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        RetrofitClient.api.logout().execute()
                    }
                    val preferences = mainContext.getSharedPreferences("MediCare", Context.MODE_PRIVATE)
                    val editor = preferences.edit()
                    editor.remove("loginToken")
                    editor.apply()
                    // TODO: 移除数据库数据

                    tvLoginText.text = requireContext().getString(R.string.clickToLogin)
                    Toast.makeText(
                        mainContext,
                        mainContext.getString(R.string.logoutSuccess),
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }

            } else {

            }
        }
            .show()
    }

    override fun onResume() {
        super.onResume()

        // 判断登录状态
        val prefs = requireContext().getSharedPreferences("MediCare", Context.MODE_PRIVATE)
        val token = prefs.getString("loginToken", null)
        val username = prefs.getString("username", "") as String

        if (!token.isNullOrEmpty()) {
            tvLoginText.text = username
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}