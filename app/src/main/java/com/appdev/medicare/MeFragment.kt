package com.appdev.medicare


import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.appdev.medicare.databinding.FragmentMeBinding


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
                Toast.makeText(requireContext(), "已登录，点击可查看个人信息", Toast.LENGTH_SHORT).show()
            }
        }

        val llMemberManagement = binding.memberArea
        llMemberManagement.setOnClickListener {
            Log.d("MeFragment","Member clicked")
//            val intent = Intent(requireContext(), MemberManagementActivity::class.java)
//            startActivity(intent)
        }

        val llNotification = binding.notificationArea
        llNotification.setOnClickListener {
            Log.d("MeFragment","Notification clicked")
//            val intent = Intent(requireContext(), NotificationActivity::class.java)
//            startActivity(intent)
        }

        val llSettings = binding.settingsArea
        llSettings.setOnClickListener {
            Log.d("MeFragment","Settings clicked")
//            val intent = Intent(requireContext(), SettingsActivity::class.java)
//            startActivity(intent)
        }

        val llLogout = binding.logoutArea
        llLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return root
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("确认退出")
            .setMessage("你确定要退出当前账号吗？")
            .setPositiveButton("确定") { _, _ ->
                // 执行退出账号的逻辑，比如清除登录状态、返回登录页面等
                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
            .setNegativeButton("取消", null)
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}