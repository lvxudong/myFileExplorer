package com.lvxudong.tabactivity;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ActionMode;

public class MainActivity extends Activity {

	ViewPager mViewPager;
	ActionMode mActionMode;
	myPageAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.veiw_page);

		mViewPager = (ViewPager) findViewById(R.id.pager);

		final ActionBar bar = getActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE
				| ActionBar.DISPLAY_SHOW_HOME);

		mAdapter = new myPageAdapter(this, mViewPager);
		mAdapter.addTab(bar.newTab().setText("文件浏览"),
				FileExpoloerActivity.class, null);
		mAdapter.addTab(bar.newTab().setText("远程管理"),
				ServerControlActivity.class, null);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setCurrentItem(0);

	}

	public interface IBackPressedListener {

		boolean onBack();
	}

	@Override
	public void onBackPressed() {

		IBackPressedListener backListener = (IBackPressedListener) mAdapter
				.getItem(mViewPager.getCurrentItem());
		if (!backListener.onBack())
			super.onBackPressed();
	}

	public static class myPageAdapter extends FragmentPagerAdapter implements
			TabListener, OnPageChangeListener {

		private final Context mContext;
		private final ActionBar mActionBar;
		private final ViewPager mViewPager;

		static final class TabInfo {
			private final Class<?> clss;
			private final Bundle args;
			private Fragment fragment;

			TabInfo(Class<?> _class, Bundle _args) {
				clss = _class;
				args = _args;
			}
		}

		private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

		public myPageAdapter(Activity activity, ViewPager pager) {
			super(activity.getFragmentManager());
			mActionBar = activity.getActionBar();
			mContext = activity;
			mViewPager = pager;
			mViewPager.setOnPageChangeListener(this);
		}

		public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
			TabInfo info = new TabInfo(clss, args);
			tab.setTag(info);
			tab.setTabListener(this);
			mActionBar.addTab(tab);
			mTabs.add(info);
		}

		@Override
		public int getCount() {
			return mTabs.size();
		}

		@Override
		public Fragment getItem(int arg0) {
			TabInfo info = mTabs.get(arg0);
			if (info.fragment == null) {
				info.fragment = Fragment.instantiate(mContext,
						info.clss.getName(), info.args);
			}
			return info.fragment;
		}

		@Override
		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {

		}

		@Override
		public void onTabSelected(Tab tab, FragmentTransaction arg1) {
			Object tag = tab.getTag();
			for (int i = 0; i < mTabs.size(); i++) {
				if (mTabs.get(i) == tag) {
					mViewPager.setCurrentItem(i);
				}
			}
		}

		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int arg0) {
			mActionBar.setSelectedNavigationItem(arg0);
		}

	}

}
