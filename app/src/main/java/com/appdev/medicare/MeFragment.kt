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
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appdev.medicare.databinding.FragmentMeBinding
import com.appdev.medicare.utils.buildAlertDialog
import com.appdev.medicare.utils.buildInputAlertDialog


class MeFragment : Fragment() {

    private var _binding: FragmentMeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeBinding.inflate(inflater, container, false)

        val root: View = binding.root

        val llLoginArea = binding.loginArea
        val tvLoginText = binding.textLogin
        llLoginArea.setOnClickListener {
            if (tvLoginText.text == "点击登录") {
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                // 登录成功后 text 更换为用户名 + 欢迎语
            } else {
                Toast.makeText(requireContext(), "已登录，点击可查看个人信息", Toast.LENGTH_SHORT)
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
            val preferences = mainContext.getSharedPreferences("MediCare", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.remove("login_token")
            editor.apply()
            // TODO: 移除数据库数据
            dialog.dismiss()
        }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}