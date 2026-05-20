package com.appsc.salmo23.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.appsc.salmo23.Principal_Oracao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.appsc.salmo23.R;
import com.appsc.salmo23.utils.StickerPacksManager;
import com.appsc.salmo23.whatsapp_api.StickerPack;
import com.appsc.salmo23.whatsapp_api.StickerPackListAdapter;
import com.appsc.salmo23.whatsapp_api.StickerPackListItemViewHolder;

import java.util.List;

public class MyStickersFragment extends Fragment {

    private static final int STICKER_PREVIEW_DISPLAY_LIMIT = 5;
    private LinearLayoutManager layoutManager;
    private RecyclerView stickersRecyclerView;
    private final StickerPackListAdapter.OnAddButtonClickedListener onAddButtonClickedListener = pack -> {
        ((Principal_Oracao) getActivity()).addStickerPackToWhatsApp(pack.identifier, pack.name);
    };
    private StickerPackListAdapter stickerListAdapter;

    public MyStickersFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        stickerListAdapter.setStickerPackList(StickerPacksManager.getStickerPacks(getActivity()));
        stickerListAdapter.notifyDataSetChanged();
        verifyStickersCount();
    }

    private View view;

    private void initRecyclerView() {
        List<StickerPack> stickersPacks = StickerPacksManager.stickerPacksContainer.getStickerPacks();
        layoutManager = new GridLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL);
        stickersRecyclerView = view.findViewById(R.id.stickers_recycler_list);
        stickersRecyclerView.setLayoutManager(layoutManager);
        stickerListAdapter = new StickerPackListAdapter(stickersPacks, onAddButtonClickedListener, this);

        stickersRecyclerView.setAdapter(stickerListAdapter);
        stickersRecyclerView.setItemViewCacheSize(20);
        stickersRecyclerView.setDrawingCacheEnabled(true);
        stickersRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        stickersRecyclerView.setNestedScrollingEnabled(true);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(stickersRecyclerView.getContext(), layoutManager.getOrientation());
        stickersRecyclerView.addItemDecoration(dividerItemDecoration);
        stickersRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(this::recalculateColumnCount);
    }

    private void initSwipeRefresh() {
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.refresh_my_stickers_swiper);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            List<StickerPack> stickersPacks = StickerPacksManager.getStickerPacks(getActivity());
            stickerListAdapter.setStickerPackList(stickersPacks);
            swipeRefreshLayout.setRefreshing(false);
            verifyStickersCount();
        });
    }

    private void recalculateColumnCount() {
        final int previewSize = getActivity().getResources().getDimensionPixelSize(R.dimen.sticker_pack_list_item_preview_image_size);
        int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
        StickerPackListItemViewHolder viewHolder = (StickerPackListItemViewHolder) stickersRecyclerView.findViewHolderForAdapterPosition(firstVisibleItemPosition);
        if (viewHolder != null) {
            final int max = Math.max(viewHolder.imageRowView.getMeasuredWidth() / previewSize, 1);
            int numColumns = Math.min(STICKER_PREVIEW_DISPLAY_LIMIT, max);
            stickerListAdapter.setMaxNumberOfStickersInARow(numColumns);
        }
    }

    public void initButtons() {
        FloatingActionButton floatingActionButtonAdd = view.findViewById(R.id.floating_button_add_sticker_pack);
        floatingActionButtonAdd.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), RequestPermissionActivity.class);
            startActivity(intent);
        });
    }

    public void verifyStickersCount() {
        View linearLayout = view.findViewById(R.id.no_stickerspacks_icon);
        if (stickerListAdapter.getItemCount() == 0) {
            linearLayout.setVisibility(View.VISIBLE);

        } else {
            linearLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_stickers, container, false);
        this.initRecyclerView();
        this.initButtons();
        this.initSwipeRefresh();
        this.verifyStickersCount();
        return view;
    }
}
