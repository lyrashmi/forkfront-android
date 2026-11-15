package com.tbd.forkfront;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

public class EInkPager {

    private static class ListState {
        int currentPage = 1;
        int totalPages = 1;
        int pageSize = 0;
    }

    private static class ScrollState {
        int currentPage = 1;
        int totalPages = 1;
        int viewportHeight = 0;
    }

    public static void setupListViewPager(final ListView listView, final View pagerBar, final Button btnPrev, final TextView txtPage, final Button btnNext, final View pagerDivider) {
        if(listView == null || pagerBar == null || btnPrev == null || txtPage == null || btnNext == null)
            return;

        // Suppress drag/kinetic scrolling to avoid e-ink dithering and ghosting
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        });

        final ListState state = new ListState();

        final Runnable updatePager = new Runnable() {
            @Override
            public void run() {
                int total = listView.getCount();
                int first = listView.getFirstVisiblePosition();
                int last = listView.getLastVisiblePosition();

                if(total <= 0 || last < 0) {
                    pagerBar.setVisibility(View.GONE);
                    if(pagerDivider != null) pagerDivider.setVisibility(View.GONE);
                    return;
                }

                int visible = last - first + 1;
                if(first == 0 && last >= total - 1) {
                    pagerBar.setVisibility(View.GONE);
                    if(pagerDivider != null) pagerDivider.setVisibility(View.GONE);
                    return;
                }

                if(first == 0 || state.pageSize <= 0) {
                    state.pageSize = Math.max(1, visible);
                }

                state.totalPages = Math.max(1, (total + state.pageSize - 1) / state.pageSize);
                if(state.totalPages <= 1) {
                    pagerBar.setVisibility(View.GONE);
                    if(pagerDivider != null) pagerDivider.setVisibility(View.GONE);
                    return;
                }

                if(first == 0) {
                    state.currentPage = 1;
                } else if(last >= total - 1) {
                    state.currentPage = state.totalPages;
                } else {
                    int calcPage = (int) Math.round((double) first / state.pageSize) + 1;
                    state.currentPage = Math.min(state.totalPages, Math.max(1, calcPage));
                }

                pagerBar.setVisibility(View.VISIBLE);
                if(pagerDivider != null) pagerDivider.setVisibility(View.VISIBLE);
                txtPage.setText(String.format("Page %d/%d", state.currentPage, state.totalPages));
                btnPrev.setEnabled(state.currentPage > 1);
                btnNext.setEnabled(state.currentPage < state.totalPages);
            }
        };

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.currentPage > 1) {
                    state.currentPage--;
                    int target = (state.currentPage - 1) * state.pageSize;
                    if(target < 0) target = 0;
                    listView.setSelection(target);
                    txtPage.setText(String.format("Page %d/%d", state.currentPage, state.totalPages));
                    btnPrev.setEnabled(state.currentPage > 1);
                    btnNext.setEnabled(state.currentPage < state.totalPages);
                    listView.post(updatePager);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.currentPage < state.totalPages) {
                    state.currentPage++;
                    int target = Math.min(listView.getCount() - 1, (state.currentPage - 1) * state.pageSize);
                    listView.setSelection(target);
                    txtPage.setText(String.format("Page %d/%d", state.currentPage, state.totalPages));
                    btnPrev.setEnabled(state.currentPage > 1);
                    btnNext.setEnabled(state.currentPage < state.totalPages);
                    listView.post(updatePager);
                }
            }
        });

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                view.post(updatePager);
            }
        });

        listView.post(updatePager);
    }

    public static void setupScrollViewPager(final ScrollView scrollView, final View pagerBar, final Button btnPrev, final TextView txtPage, final Button btnNext, final View pagerDivider) {
        if(scrollView == null || pagerBar == null || btnPrev == null || txtPage == null || btnNext == null)
            return;

        // Suppress kinetic smooth drag on e-ink
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
                return false;
            }
        });

        final ScrollState state = new ScrollState();

        final Runnable updatePager = new Runnable() {
            @Override
            public void run() {
                if(scrollView.getChildCount() == 0) {
                    pagerBar.setVisibility(View.GONE);
                    if(pagerDivider != null) pagerDivider.setVisibility(View.GONE);
                    return;
                }

                int vHeight = scrollView.getHeight() - scrollView.getPaddingTop() - scrollView.getPaddingBottom();
                int totalHeight = scrollView.getChildAt(0).getHeight();

                if(vHeight <= 0 || totalHeight <= vHeight) {
                    pagerBar.setVisibility(View.GONE);
                    if(pagerDivider != null) pagerDivider.setVisibility(View.GONE);
                    return;
                }

                state.viewportHeight = vHeight;
                state.totalPages = Math.max(1, (int) Math.ceil((double) totalHeight / vHeight));
                if(state.totalPages <= 1) {
                    pagerBar.setVisibility(View.GONE);
                    if(pagerDivider != null) pagerDivider.setVisibility(View.GONE);
                    return;
                }

                int currentY = scrollView.getScrollY();
                int maxScrollY = Math.max(0, totalHeight - vHeight);

                if(currentY <= 0) {
                    state.currentPage = 1;
                } else if(currentY >= maxScrollY - 5) {
                    state.currentPage = state.totalPages;
                } else {
                    int calcPage = (int) Math.round((double) currentY / vHeight) + 1;
                    state.currentPage = Math.min(state.totalPages, Math.max(1, calcPage));
                }

                pagerBar.setVisibility(View.VISIBLE);
                if(pagerDivider != null) pagerDivider.setVisibility(View.VISIBLE);
                txtPage.setText(String.format("Page %d/%d", state.currentPage, state.totalPages));
                btnPrev.setEnabled(state.currentPage > 1);
                btnNext.setEnabled(state.currentPage < state.totalPages);
            }
        };

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.currentPage > 1) {
                    state.currentPage--;
                    int vHeight = state.viewportHeight > 0 ? state.viewportHeight :
                            (scrollView.getHeight() - scrollView.getPaddingTop() - scrollView.getPaddingBottom());
                    int targetY = Math.max(0, (state.currentPage - 1) * vHeight);
                    scrollView.scrollTo(0, targetY);
                    txtPage.setText(String.format("Page %d/%d", state.currentPage, state.totalPages));
                    btnPrev.setEnabled(state.currentPage > 1);
                    btnNext.setEnabled(state.currentPage < state.totalPages);
                    scrollView.post(updatePager);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state.currentPage < state.totalPages && scrollView.getChildCount() > 0) {
                    state.currentPage++;
                    int vHeight = state.viewportHeight > 0 ? state.viewportHeight :
                            (scrollView.getHeight() - scrollView.getPaddingTop() - scrollView.getPaddingBottom());
                    int totalHeight = scrollView.getChildAt(0).getHeight();
                    int maxScrollY = Math.max(0, totalHeight - vHeight);
                    int targetY = Math.min(maxScrollY, (state.currentPage - 1) * vHeight);
                    scrollView.scrollTo(0, targetY);
                    txtPage.setText(String.format("Page %d/%d", state.currentPage, state.totalPages));
                    btnPrev.setEnabled(state.currentPage > 1);
                    btnNext.setEnabled(state.currentPage < state.totalPages);
                    scrollView.post(updatePager);
                }
            }
        });

        if(scrollView.getTag(R.id.pager_bar) == null) {
            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    updatePager.run();
                }
            });
            scrollView.setTag(R.id.pager_bar, Boolean.TRUE);
        }

        scrollView.post(updatePager);
    }
}
