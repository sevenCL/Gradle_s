package com.seven.module_user

import android.view.LayoutInflater
import com.seven.data_common.RouterPath
import com.seven.lib_common.toast
import com.seven.lib_core.BaseActivity
import com.seven.module_user.databinding.ActivityUserBinding
import com.therouter.router.Autowired
import com.therouter.router.Route

/**
 * @author seven
 * @date 2024/9/19
 * @desc
 **/
@Route(path = RouterPath.PATH_USER)
class UserActivity : BaseActivity<ActivityUserBinding>() {

    @Autowired
    var from: String? = null

    override fun inflateViewBinding(layoutInflater: LayoutInflater) =
        ActivityUserBinding.inflate(layoutInflater)

    override fun initView() {
        from.toString().toast()
    }

    override fun initListener() {

    }

    override fun initData() {

    }

    override fun loadData() {

    }

}