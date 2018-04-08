package com.example.xyzreader.ui;

import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ArticleListActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String CLASSTAG = ArticleListActivity.class.toString();

    @BindView(R.id.swiping_refresh_layout_view_behavior) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view_article_list) RecyclerView recycler_view_article_list;

    private boolean isReRendering = false;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (UpdaterService.STATE_CHANGE.equals(intent.getAction())) {
                isReRendering = intent.getBooleanExtra(UpdaterService.RERENDERING, false);
                updateRefreshingUI();
            }
        }
    };
    private void refreshing() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);
        ButterKnife.bind(this);

        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refreshing();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(broadcastReceiver,
                new IntentFilter(UpdaterService.STATE_CHANGE));
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }


    private void updateRefreshingUI() {
        swipeRefreshLayout.setRefreshing(isReRendering);
    }



    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Adapter adapter = new Adapter(cursor);
        adapter.setHasStableIds(true);
        recycler_view_article_list.setAdapter(adapter);
        int columnCount = getResources().getInteger(R.integer.list_column_number);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        recycler_view_article_list.setLayoutManager(sglm);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        recycler_view_article_list.setAdapter(null);
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private Cursor cursor;

        Adapter(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public long getItemId(int position) {
            cursor.moveToPosition(position);
            return cursor.getLong(ArticleLoader.Query._ID);
        }

        @Override
        public int getItemCount() {
            return cursor.getCount();
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            ItemsContract.Items.buildItemUri(getItemId(vh.getAdapterPosition()))));
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            cursor.moveToPosition(position);
            holder.bind(cursor);
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private Context classContext;
        private SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

        private SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
        private GregorianCalendar gregorianCalendar = new GregorianCalendar(2,1,1);


        @BindView(R.id.article_sub_text_view) TextView article_sub_text_view;
        @BindView(R.id.dynamic_image_thumbnail) DynamicHeightNetworkImageView thumbnailImage;
        @BindView(R.id.article_title_text_view) TextView titleArticleTextView;


        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);

            classContext = view.getContext();
        }

        void bind(@NonNull Cursor cursor) {
            titleArticleTextView.setText(cursor.getString(ArticleLoader.Query.TITLE));
            Date publishedDate = parsePublishedDate(cursor);
            if (!publishedDate.before(gregorianCalendar.getTime())) {

                article_sub_text_view.setText(Html.fromHtml(
                        DateUtils.getRelativeTimeSpanString(
                                publishedDate.getTime(),
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                                DateUtils.FORMAT_ABBREV_ALL).toString()
                                + "<br/>" + " by "
                                + cursor.getString(ArticleLoader.Query.AUTHOR)));
            } else {
                article_sub_text_view.setText(Html.fromHtml(
                        simpleDateFormat.format(publishedDate)
                                + "<br/>" + " by "
                                + cursor.getString(ArticleLoader.Query.AUTHOR)));
            }
            thumbnailImage.setImageUrl(
                    cursor.getString(ArticleLoader.Query.THUMB_URL),
                    ImageLoaderHelper.getInstance(classContext).getImageLoader()
            );
            thumbnailImage.setDefaultImageResId(R.drawable.article_image);
            thumbnailImage.setAspectRatio(cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        }

        private Date parsePublishedDate(@NonNull Cursor cursor) {
            try {
                String date = cursor.getString(ArticleLoader.Query.DATE_PUBLISHED);
                return formatDate.parse(date);
            } catch (ParseException ex) {
                Log.e("hhh34", ex.getMessage());
                Log.i("hhh34", "today date:");
                return new Date();
            }
        }
    }
}
