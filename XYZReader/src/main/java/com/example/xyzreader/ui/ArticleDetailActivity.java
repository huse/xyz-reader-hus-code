package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ArticleParser;
import com.example.xyzreader.data.ItemsContract;

public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private ViewPager viewPager;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private PagerAdapterForArticleDetail pagerAdapterForArticleDetail;
    private View containerButtonUp;
    private View buttonUp;
    private Cursor cursor;
    private long startingId;
    private long selectedItemId;
    private int maxInteger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        getLoaderManager().initLoader(0, null, this);

        pagerAdapterForArticleDetail = new PagerAdapterForArticleDetail(getFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapterForArticleDetail);
        viewPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        viewPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (cursor != null) {
                    cursor.moveToPosition(position);
                }
                selectedItemId = cursor.getLong(ArticleLoader.Query._ID);
                buttonUpPositionUpdating();
            }
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                buttonUp.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }


        });

        containerButtonUp = findViewById(R.id.to_up_container);

        buttonUp = findViewById(R.id.image_button_action_up);
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            containerButtonUp.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    view.onApplyWindowInsets(windowInsets);
                    maxInteger = windowInsets.getSystemWindowInsetTop();
                    containerButtonUp.setTranslationY(maxInteger);
                    buttonUpPositionUpdating();
                    return windowInsets;
                }
            });
        }

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                startingId = ItemsContract.Items.getItemId(getIntent().getData());
                selectedItemId = startingId;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        cursor = null;
        pagerAdapterForArticleDetail.notifyDataSetChanged();
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        this.cursor = cursor;
        pagerAdapterForArticleDetail.notifyDataSetChanged();
        if (startingId > 0) {
            this.cursor.moveToFirst();
                      while (!this.cursor.isAfterLast()) {
                if (this.cursor.getLong(ArticleLoader.Query._ID) == startingId) {
                    final int position = this.cursor.getPosition();
                    viewPager.setCurrentItem(position, false);
                    break;
                }
                this.cursor.moveToNext();
            }
            startingId = 0;
        }
    }


    private void buttonUpPositionUpdating() {
        int upButtonNormalBottom = maxInteger + buttonUp.getHeight();
        buttonUp.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }

    private class PagerAdapterForArticleDetail extends FragmentStatePagerAdapter {
        public PagerAdapterForArticleDetail(FragmentManager fm) {
            super(fm);
        }


        @Override
        public int getCount() {
            return (cursor != null) ? cursor.getCount() : 0;
        }
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
           /* ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                updateUpButtonPosition();
            }*/
        }

        @Override
        public Fragment getItem(int position) {
            cursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(ArticleParser.forParsingGetFromCursor(cursor));
        }
    }
}
