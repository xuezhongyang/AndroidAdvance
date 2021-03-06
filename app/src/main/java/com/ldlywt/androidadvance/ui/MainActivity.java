package com.ldlywt.androidadvance.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.flyco.tablayout.listener.CustomTabEntity;
import com.flyco.tablayout.listener.OnTabSelectListener;
import com.google.android.material.tabs.TabLayout;
import com.ldlywt.androidadvance.R;
import com.ldlywt.androidadvance.databinding.ActivityMainBinding;
import com.ldlywt.androidadvance.utils.MainTabData;
import com.ldlywt.base.view.BaseActivity;
import com.ldlywt.base.widget.CommonTextView;
import com.ldlywt.xeventbus.Subscribe;
import com.ldlywt.xeventbus.ThreadMode;
import com.ldlywt.xeventbus.XEventBus;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * <pre>
 *     author : lex
 *     e-mail : ldlywt@163.com
 *     time   : 2018/08/28
 *     desc   :
 *     version: 1.0
 *
 * </pre>
 */
public class MainActivity extends BaseActivity {


    private Fragment[] mFragments;
    private int curIndex;
    private com.ldlywt.androidadvance.databinding.ActivityMainBinding mBinding;

    @Override
    public void initData(Bundle savedInstanceState) {
        XEventBus.getDefault().register(this);
        //自定义全局异常
//        Logger.i(1/0 + "");
    }


    @Override
    public void initView() {

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mFragments = MainTabData.getFragments("Main");

        mBinding.viewPage2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return mFragments[position];
            }

            @Override
            public int getItemCount() {
                return mFragments.length;
            }
        });


        mBinding.tab.setTabData(MainTabData.getTabV2());
        mBinding.tab.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelect(int position) {
                mBinding.viewPage2.setCurrentItem(position);
            }

            @Override
            public void onTabReselect(int position) {
            }
        });

        mBinding.viewPage2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mBinding.tab.setCurrentTab(position);
            }
        });
//        mBinding.viewPage2.setUserInputEnabled(false);

//        initTabWayOne();
    }

    private void initTabWayOne() {
        FragmentUtils.add(getSupportFragmentManager(), mFragments, R.id.fl_container, curIndex);
        mBinding.bottomTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showCurrentFragment(tab.getPosition());
                // Tab 选中之后，改变各个Tab的状态
                for (int i = 0; i < mBinding.bottomTab.getTabCount(); i++) {
                    View view = mBinding.bottomTab.getTabAt(i).getCustomView();
                    ImageView icon = (ImageView) view.findViewById(R.id.tab_content_image);
                    TextView text = (TextView) view.findViewById(R.id.tab_content_text);
                    if (i == tab.getPosition()) { // 选中状态
                        icon.setImageResource(MainTabData.tabResPressed[i]);
                        text.setTextColor(getResources().getColor(android.R.color.black));
                    } else {// 未选中状态
                        icon.setImageResource(MainTabData.tabResNormal[i]);
                        text.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        // 提供自定义的布局添加Tab
        for (int i = 0; i < 4; i++) {
            mBinding.bottomTab.addTab(mBinding.bottomTab.newTab().setCustomView(MainTabData.getTabView(this, i)));
        }
    }

    private void showCurrentFragment(int index) {
        FragmentUtils.showHide(curIndex = index, mFragments);
    }

    @Override
    protected void initTitle() {
//        XToolBar toolBar = (XToolBar) new XToolBar
//                .Builder(this)
//                .builder();
//        toolBar.getTitle().setLeftTextString("标题");
        super.initTitle();
        getTitleBar()
                .setWidthAndHeight(LinearLayout.LayoutParams.MATCH_PARENT, SizeUtils.dp2px(40))
                .setBackColor(R.color.colorPrimary)
                .setCenterTextColor(R.color.white)
                .setCenterTextString("自定义标题")
                .setRightTextString("添加")
                .setRightTextColor(R.color.colorAccent)
                .setRightViewIsClickable(true)
                .setOnCommonTextViewClickListener(new CommonTextView.OnCommonTextViewClickListener() {
                    @Override
                    public void onRightViewClick() {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XEventBus.getDefault().unRegister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiverMsg(String msg) {
        Logger.i("来自自定义的EventBus： " + msg);
    }
}
