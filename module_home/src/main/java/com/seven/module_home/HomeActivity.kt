package com.seven.module_home

import android.view.LayoutInflater
import com.android.mylibrary.TestUtils
import com.networkbench.agent.impl.NBSAppAgent
import com.seven.data_common.BundleParams
import com.seven.data_common.RouterPath
import com.seven.lib_common.toast
import com.seven.lib_core.BaseActivity
import com.seven.lib_core.route
import com.seven.module_home.databinding.ActivityHomeBinding
import com.therouter.router.Autowired
import com.therouter.router.Route

/**
 * @author seven
 * @date 2024/9/19
 * @desc
 **/
@Route(path = RouterPath.PATH_HOME)
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    @Autowired
    var from: String? = null

    override fun inflateViewBinding(layoutInflater: LayoutInflater) =
        ActivityHomeBinding.inflate(layoutInflater)

    override fun initView() {
        from.toString().toast()
    }

    override fun initListener() {
        binding.routeUser.setOnClickListener {
            RouterPath.PATH_USER.route {
                putString(BundleParams.FROM, "home")
            }
        }
    }

    override fun initData() {
        try {
            NBSAppAgent.getTingyunDeviceId()
        }catch (_:Exception){
        }
        TestUtils.showToast(this)
    }

    override fun loadData() {

    }
}