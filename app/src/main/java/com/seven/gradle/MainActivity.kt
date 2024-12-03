package com.seven.gradle

import android.view.LayoutInflater
import com.seven.data_common.BundleParams
import com.seven.data_common.RouterPath
import com.seven.gradle.databinding.ActivityMainBinding
import com.seven.lib_common.load
import com.seven.lib_common.toast
import com.seven.lib_core.BaseActivity
import com.seven.lib_core.route
import com.therouter.router.Route

@Route(path = RouterPath.PATH_MAIN)
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateViewBinding(layoutInflater: LayoutInflater) =
        ActivityMainBinding.inflate(layoutInflater)

    override fun initView() {

    }

    override fun initListener() {
        binding.toast.setOnClickListener {
            "blankJ".toast()
        }
        binding.routeHome.setOnClickListener {
            RouterPath.PATH_HOME.route {
                putString(BundleParams.FROM, "main")
            }
        }
        binding.routeReview.setOnClickListener {
            RouterPath.PATH_REVIEW.route {
                putString(BundleParams.FROM, "review")
            }
        }
        binding.routeUser.setOnClickListener {
            RouterPath.PATH_USER.route {
                putString(BundleParams.FROM, "user")
            }
        }
    }

    override fun initData() {
        binding.ivCover.load(
            "https://inews.gtimg.com/om_bt/OE8piEBa-tbqn-wNvWZl8coi4AlzoUD43upEkoAnIkYL8AA/641"
        )
//        binding.sivCover.load(
//            "https://inews.gtimg.com/om_bt/OE8piEBa-tbqn-wNvWZl8coi4AlzoUD43upEkoAnIkYL8AA/641"
//        )
    }

    override fun loadData() {

    }

}