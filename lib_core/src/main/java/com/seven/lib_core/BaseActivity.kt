package com.seven.lib_core

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.therouter.TheRouter

/**
 * @author seven
 * @date 2024/9/19
 * @desc
 **/
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TheRouter.inject(this)
        enableEdgeToEdge()
        binding = inflateViewBinding(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initView()
        initListener()
        initData()
        loadData()
    }

    abstract fun inflateViewBinding(layoutInflater: LayoutInflater): VB

    abstract fun initView()

    abstract fun initListener()

    abstract fun initData()

    abstract fun loadData()

    override fun onDestroy() {
        super.onDestroy()
    }
}