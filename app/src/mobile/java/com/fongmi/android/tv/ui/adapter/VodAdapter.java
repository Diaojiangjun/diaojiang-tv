package com.fongmi.android.tv.ui.adapter;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fongmi.android.tv.R;
import com.fongmi.android.tv.bean.Style;
import com.fongmi.android.tv.bean.Vod;
import com.fongmi.android.tv.databinding.AdapterVodListBinding;
import com.fongmi.android.tv.databinding.AdapterVodOvalBinding;
import com.fongmi.android.tv.databinding.AdapterVodRectBinding;
import com.fongmi.android.tv.ui.base.BaseVodHolder;
import com.fongmi.android.tv.ui.base.ViewType;
import com.fongmi.android.tv.ui.holder.VodListHolder;
import com.fongmi.android.tv.ui.holder.VodOvalHolder;
import com.fongmi.android.tv.ui.holder.VodRectHolder;

import java.util.List;

public class VodAdapter extends BaseDiffAdapter<Vod, BaseVodHolder> {

    private static final int TYPE_FOOTER = 1000;
    private static final int LOADING_TIMEOUT_MS = 5000; // 5秒超时，符合项目环境配置要求

    public enum FooterState {
        NONE,
        LOADING,
        NO_MORE,
        EXPIRED
    }

    private final OnClickListener listener;
    private final Style style;
    private final int[] size;

    private FooterState footerState = FooterState.NONE;
    private Handler loadingTimeoutHandler;
    private Runnable loadingTimeoutRunnable;
    private boolean isDestroyed = false;

    public VodAdapter(OnClickListener listener, Style style, int[] size) {
        this.listener = listener;
        this.style = style;
        this.size = size;
        this.loadingTimeoutHandler = new Handler(Looper.getMainLooper());

        // 注册数据更新监听器
        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                // 数据插入后，检查是否需要更新Footer状态
                checkFooterStateAfterDataChange();
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                checkFooterStateAfterDataChange();
            }
        });
    }

    public interface OnClickListener {
        void onItemClick(Vod item);
        boolean onLongClick(Vod item);
    }

    public Style getStyle() {
        return style;
    }

    /**
     * 更新数据并处理Footer状态
     */
    public void submitListWithFooter(List<Vod> newList, boolean hasMore) {
        if (isDestroyed) return;

        // 取消超时定时器
        cancelLoadingTimeout();

        // 更新数据
        differ.submitList(newList, () -> {
            // 在数据更新完成后处理Footer状态
            updateFooterState(hasMore);
        });
    }

    /**
     * 根据是否有更多数据更新Footer状态
     */
    private synchronized void updateFooterState(boolean hasMore) {
        if (isDestroyed) return;

        if (hasMore) {
            // 如果还有更多数据，显示加载Footer以便下次加载更多
            showLoadingFooterInternal();
        } else {
            // 没有更多数据，显示无更多Footer
            showNoMoreFooterInternal();
        }
    }

    /**
     * 显示加载Footer（内部方法）
     */
    private synchronized void showLoadingFooterInternal() {
        if (isDestroyed || footerState == FooterState.EXPIRED) {
            return;
        }

        // 如果已经在加载中，不重复处理
        if (footerState == FooterState.LOADING) {
            return;
        }

        FooterState previousState = footerState;
        footerState = FooterState.LOADING;
        notifyFooterChanged(previousState);

        // 启动超时定时器
        scheduleLoadingTimeout();
    }

    /**
     * 显示加载Footer（公共方法）
     */
    public synchronized void showLoadingFooter() {
        if (isDestroyed || footerState == FooterState.EXPIRED) {
            return;
        }

        // 如果已经在加载中，不重复处理
        if (footerState == FooterState.LOADING) {
            return;
        }

        showLoadingFooterInternal();
    }

    /**
     * 显示无更多数据Footer
     */
    public synchronized void showNoMoreFooter() {
        if (isDestroyed) return;
        showNoMoreFooterInternal();
    }

    private synchronized void showNoMoreFooterInternal() {
        cancelLoadingTimeout();

        if (footerState == FooterState.NO_MORE) {
            return; // 避免重复设置
        }

        FooterState previousState = footerState;
        footerState = FooterState.NO_MORE;
        notifyFooterChanged(previousState);
    }

    /**
     * 隐藏Footer
     */
    public synchronized void hideFooter() {
        if (isDestroyed) return;
        hideFooterInternal();
    }

    private synchronized void hideFooterInternal() {
        cancelLoadingTimeout();

        if (footerState == FooterState.NONE) {
            return; // 已经是隐藏状态
        }

        FooterState previousState = footerState;
        footerState = FooterState.NONE;
        notifyFooterChanged(previousState);
    }

    /**
     * 数据变化后检查Footer状态
     */
    private void checkFooterStateAfterDataChange() {
        if (isDestroyed) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            // 如果当前是加载状态，且数据已经更新，说明加载完成
            if (footerState == FooterState.LOADING) {
                // 保持加载状态，等待外部调用submitListWithFooter来更新状态
                // 这里不做任何操作，避免干扰正常的加载流程
            }
        });
    }

    /**
     * 启动加载超时定时器
     */
    private void scheduleLoadingTimeout() {
        cancelLoadingTimeout();

        loadingTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed) return;

                synchronized (VodAdapter.this) {
                    if (footerState == FooterState.LOADING) {
                        // 超时处理：将加载状态改为无更多数据状态
                        footerState = FooterState.NO_MORE;
                        notifyFooterChanged(FooterState.LOADING);
                    }
                }
            }
        };

        if (loadingTimeoutHandler != null) {
            loadingTimeoutHandler.postDelayed(loadingTimeoutRunnable, LOADING_TIMEOUT_MS);
        }
    }

    private void cancelLoadingTimeout() {
        if (loadingTimeoutRunnable != null && loadingTimeoutHandler != null) {
            loadingTimeoutHandler.removeCallbacks(loadingTimeoutRunnable);
            loadingTimeoutRunnable = null;
        }
    }

    private void notifyFooterChanged(FooterState previousState) {
        if (isDestroyed) return;

        int itemCount = differ.getCurrentList().size();

        try {
            if (previousState == FooterState.NONE && footerState != FooterState.NONE) {
                // 添加Footer
                notifyItemInserted(itemCount);
            } else if (previousState != FooterState.NONE && footerState == FooterState.NONE) {
                // 移除Footer
                if (itemCount > 0) {
                    notifyItemRemoved(itemCount);
                } else {
                    // 如果列表为空，使用notifyDataSetChanged
                    notifyDataSetChanged();
                }
            } else if (previousState != FooterState.NONE && footerState != FooterState.NONE) {
                // 更新Footer状态
                if (itemCount > 0) {
                    notifyItemChanged(itemCount);
                } else {
                    // 如果列表为空，使用notifyDataSetChanged
                    notifyDataSetChanged();
                }
            }
        } catch (Exception e) {
            // 避免在数据更新过程中的异常
            notifyDataSetChanged();
        }
    }

    private boolean hasFooter() {
        return footerState != FooterState.NONE;
    }

    public FooterState getFooterState() {
        return footerState;
    }

    public synchronized void destroy() {
        isDestroyed = true;
        cancelLoadingTimeout();

        if (loadingTimeoutHandler != null) {
            loadingTimeoutHandler.removeCallbacksAndMessages(null);
            loadingTimeoutHandler = null;
        }
    }

    @Override
    public int getItemCount() {
        int count = differ.getCurrentList().size();
        return hasFooter() ? count + 1 : count;
    }

    @Override
    public int getItemViewType(int position) {
        if (hasFooter() && position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }
        return style.getViewType();
    }

    @Override
    public void onBindViewHolder(@NonNull BaseVodHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_FOOTER) {
            return;
        }
        holder.initView(getItem(position));
    }

    @Override
    public void onBindViewHolder(@NonNull BaseVodHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads == null || payloads.size() == 0) {
            onBindViewHolder(holder, position);
        } else {
            if (holder.getItemViewType() != TYPE_FOOTER) {
                holder.initView(getItem(position));
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull BaseVodHolder holder) {
        holder.unbind();
    }

    @NonNull
    @Override
    public BaseVodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == TYPE_FOOTER) {
            View footerView;
            if (footerState == FooterState.LOADING) {
                footerView = inflater.inflate(R.layout.view_loading_footer, parent, false);
            } else {
                footerView = inflater.inflate(R.layout.view_no_more_footer, parent, false);
            }
            return new FooterHolder(footerView);
        }

        switch (viewType) {
            case ViewType.LIST:
                return new VodListHolder(
                        AdapterVodListBinding.inflate(inflater, parent, false),
                        listener
                );
            case ViewType.OVAL:
                return new VodOvalHolder(
                        AdapterVodOvalBinding.inflate(inflater, parent, false),
                        listener
                ).size(size);
            default:
                return new VodRectHolder(
                        AdapterVodRectBinding.inflate(inflater, parent, false),
                        listener
                ).size(size);
        }
    }

    private static class FooterHolder extends BaseVodHolder {
        public FooterHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void initView(Vod item) {}

        @Override
        public void unbind() {}
    }
}