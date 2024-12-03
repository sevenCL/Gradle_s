package com.seven.module_reviews

import android.view.LayoutInflater
import com.seven.data_common.RouterPath
import com.seven.lib_common.toast
import com.seven.lib_core.BaseActivity
import com.seven.module_reviews.databinding.ActivityReviewBinding
import com.therouter.router.Autowired
import com.therouter.router.Route

/**
 * @author seven
 * @date 2024/9/19
 * @desc
 **/
@Route(path = RouterPath.PATH_REVIEW)
class ReviewActivity : BaseActivity<ActivityReviewBinding>() {

    @Autowired
    var from: String? = null

    override fun inflateViewBinding(layoutInflater: LayoutInflater) =
        ActivityReviewBinding.inflate(layoutInflater)

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