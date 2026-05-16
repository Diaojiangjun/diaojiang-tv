package com.fongmi.android.tv.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewbinding.ViewBinding;

import com.fongmi.android.tv.Product;
import com.fongmi.android.tv.R;
import com.fongmi.android.tv.api.config.VodConfig;
import com.fongmi.android.tv.bean.Result;
import com.fongmi.android.tv.bean.Site;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Value;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.FragmentTypeBinding;
import com.fongmi.android.tv.model.SiteViewModel;
import com.fongmi.android.tv.ui.activity.SearchActivity;
import com.fongmi.android.tv.ui.activity.VideoActivity;
import com.fongmi.android.tv.ui.adapter.VodAdapter;
import com.fongmi.android.tv.ui.base.BaseFragment;
import com.fongmi.android.tv.ui.custom.CustomScroller;
import com.fongmi.android.tv.utils.Notify;
import com.fongmi.android.tv.utils.ResUtil;

import java.util.HashMap;

public class TypeFragment extends BaseFragment implements CustomScroller.Callback, VodAdapter.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private HashMap<String, String> mExtends;
    private FragmentTypeBinding mBinding;
    private CustomScroller mScroller;
    private SiteViewModel mViewModel;
    private VodAdapter mAdapter;

    public static TypeFragment newInstance(String key, String typeId, Style style, HashMap<String, String> extend, boolean folder, int y) {
        Bundle args = new Bundle();
        args.putInt("y", y);
        args.putString("key", key);
        args.putString("typeId", typeId);
        args.putBoolean("folder", folder);
        args.putParcelable("style", style);
        args.putSerializable("extend", extend);
        TypeFragment fragment = new TypeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String getKey() {
        return getArguments().getString("key");
    }

    private String getTypeId() {
        return getArguments().getString("typeId");
    }

    private Style getStyle() {
        return isFolder() ? Style.list() : getSite().getStyle(getArguments().getParcelable("style"));
    }

    private HashMap<String, String> getExtend() {
        return (HashMap<String, String>) getArguments().getSerializable("extend");
    }

    private int getY() {
        return getArguments().getInt("y");
    }

    private boolean isFolder() {
        return getArguments().getBoolean("folder");
    }

    private boolean isHome() {
        return "home".equals(getTypeId());
    }

    private Site getSite() {
        return VodConfig.get().getSite(getKey());
    }

    private FolderFragment getParent() {
        return (FolderFragment) getParentFragment();
    }

    @Override
    protected ViewBinding getBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return mBinding = FragmentTypeBinding.inflate(inflater, container, false);
    }

    @Override
    protected void initView() {
        mBinding.swipeLayout.setColorSchemeResources(R.color.accent);
        mBinding.progressLayout.showProgress();
        mScroller = new CustomScroller(this);
        mExtends = getExtend();
        setRecyclerView();
        setViewModel();
        getVideo();
    }

    @Override
    protected void initEvent() {
        mBinding.swipeLayout.setOnRefreshListener(this);
        mBinding.recycler.addOnScrollListener(mScroller);
    }

    private void setRecyclerView() {
        mBinding.recycler.setTranslationY(-ResUtil.dp2px(getY()));
        mBinding.recycler.setHasFixedSize(true);
        setStyle(getStyle());
    }

    private void setStyle(Style style) {
        mBinding.recycler.setAdapter(mAdapter = new VodAdapter(this, style, Product.getSpec(requireActivity(), style)));
        mBinding.recycler.setLayoutManager(style.isList() ? new LinearLayoutManager(requireActivity()) : new GridLayoutManager(getContext(), Product.getColumn(requireActivity(), style)));
    }

    private void setViewModel() {
        mViewModel = new ViewModelProvider(this).get(SiteViewModel.class);
        mViewModel.getResult().observe(getViewLifecycleOwner(), this::setAdapter);
        mViewModel.getAction().observe(getViewLifecycleOwner(), result -> Notify.show(result.getMsg()));
    }

    private void getHome() {
        mAdapter.clear(() -> mViewModel.homeContent());
    }

    private void getVideo() {
        mScroller.reset();
        mAdapter.clear(() -> {
            mAdapter.hideFooter();
            if (!mBinding.swipeLayout.isRefreshing()) mBinding.progressLayout.showProgress();
            if (isHome()) setAdapter(getParent().getResult());
            else getVideo(getTypeId(), "1");
        });
    }

    private void getVideo(String typeId, String page) {
        mViewModel.categoryContent(getKey(), typeId, page, true, mExtends);
    }

    private void setAdapter(Result result) {
        boolean first = mScroller.first();
        int size = result.getList().size();
        mBinding.progressLayout.showContent(first, size);
        mBinding.swipeLayout.setRefreshing(false);
        mScroller.endLoading(result);
        if (size > 0) addVideo(result);
        
        // 加载完成后的Footer处理
        // 注意：不要显示LOADING Footer，因为会启动超时定时器，5秒后自动变成NO_MORE
        if (first) {
            if (size == 0) {
                // 第一页没有数据，显示无更多Footer
                mAdapter.showNoMoreFooter();
            } else {
                // 第一页有数据，隐藏Footer（等待用户滚动到底部时再显示LOADING）
                mAdapter.hideFooter();
            }
        } else {
            // 加载更多完成后的处理
            if (size == 0) {
                // 没有更多数据，显示无更多Footer
                mAdapter.showNoMoreFooter();
            } else {
                // 还有更多数据，隐藏Footer（等待下次滚动到底部时再显示LOADING）
                mAdapter.hideFooter();
            }
        }
    }

    private void addVideo(Result result) {
        Style style = result.getVod().getStyle(getStyle());
        if (!style.equals(mAdapter.getStyle())) setStyle(style);
        // 不再使用 checkMore 回调，只在用户滚动到底部时通过 CustomScroller 触发加载
        mAdapter.addAll(result.getList());
    }

    public void scrollToTop() {
        mBinding.recycler.smoothScrollToPosition(0);
    }

    public void setFilter(String key, Value value) {
        if (value.isActivated()) mExtends.put(key, value.getV());
        else mExtends.remove(key);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        // 刷新时重置所有状态
        mScroller.reset();
        mAdapter.hideFooter();
        if (isHome()) getHome();
        else getVideo();
    }

    @Override
    public void onLoadMore(String page) {
        if (isHome()) return;
        
        // 防止重复加载：检查当前Footer状态
        VodAdapter.FooterState currentState = mAdapter.getFooterState();
        if (currentState == VodAdapter.FooterState.LOADING) {
            return; // 已经在加载中，不重复请求
        }
        if (currentState == VodAdapter.FooterState.NO_MORE) {
            return; // 已经没有更多数据，不再加载
        }
        
        mScroller.setLoading(true);
        mAdapter.showLoadingFooter();
        getVideo(getTypeId(), page);
    }

    @Override
    public void onItemClick(Vod item) {
        if (item.isAction()) {
            mViewModel.action(getKey(), item.getAction());
        } else if (item.isFolder()) {
            getParent().openFolder(item.getId(), mExtends);
        } else {
            if (getSite().isIndex()) SearchActivity.start(requireActivity(), item.getName());
            else VideoActivity.start(requireActivity(), getKey(), item.getId(), item.getName(), item.getPic(), isFolder() ? item.getName() : null);
        }
    }

    @Override
    public boolean onLongClick(Vod item) {
        if (item.isAction() || item.isFolder()) return false;
        SearchActivity.start(requireActivity(), item.getName());
        return true;
    }
}
