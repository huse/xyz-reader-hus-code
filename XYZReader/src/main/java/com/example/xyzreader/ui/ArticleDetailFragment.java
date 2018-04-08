package com.example.xyzreader.ui;
import butterknife.BindView;
import butterknife.ButterKnife;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.NestedScrollView;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import java.util.Date;
import java.util.GregorianCalendar;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Build;
import android.os.Bundle;

import android.support.v7.graphics.Palette;
import android.text.Html;

import android.text.method.LinkMovementMethod;


import com.android.volley.VolleyError;

import com.example.xyzreader.data.ArticleParser;
import org.markdown4j.Markdown4jProcessor;


public class ArticleDetailFragment extends Fragment {
    private static final int SIZE = 724;
    private static final int OFFSET = 256;
    private static final String ARTICLES = "articles";


    private ArticleParser articleParser;
    private int bodyId;
    private View rView;
    private int markColor = 0xff33bbff;
    private static final String CLASSTAGS = "ArticleDetailFragment";
    @BindView(R.id.book_date_author) TextView bookDateAuthor;
    @BindView(R.id.article_image) ImageView imageView;
    @BindView(R.id.floating_action_button) FloatingActionButton floatingActionButton;
    @BindView(R.id.article_text_view) TextView article_text_view;
    @BindView(R.id.article_title_text_view) TextView article_title_text_view;
    @BindView(R.id.body_progressbar) ProgressBar body_progressbar;
    @BindView(R.id.body_scrollview) NestedScrollView body_scrollview;

    @BindView(R.id.article_detail_content_holder) LinearLayout mContentHolder;
    private SimpleDateFormat outputSimpleDateFormat = new SimpleDateFormat();

    private GregorianCalendar START_TIME = new GregorianCalendar(2,1,1);


    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");


    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(@NonNull ArticleParser article) {
        ArticleDetailFragment fragment;

        fragment = new ArticleDetailFragment();
        Bundle arguments = new Bundle();

        arguments.putParcelable(ARTICLES, article);
        fragment.setArguments(arguments);

        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v("hhh2", "entering onCreateView ArticleDetailFragment ");

        rView = inflater.inflate(R.layout.fragment_article_detail, container, true);
        ButterKnife.bind(this, rView);

        bookDateAuthor.setMovementMethod(new LinkMovementMethod());
        article_text_view.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "GreatVibes-Regular.otf"));


        article_title_text_view.setText(articleParser.getArticleTitle());
        Date publishedDate = dateParsing();
        if (!publishedDate.before(START_TIME.getTime())) {
            bookDateAuthor.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "\n" +"  \n  <font color='#1fffff'>"
                            + articleParser.getArticleAuthor()
                            + "</font>"));

        } else {

            bookDateAuthor.setText(Html.fromHtml(
                    outputSimpleDateFormat.format(publishedDate) + "\n"+"  \n  <font color='#1fffff'>"
                            + articleParser.getArticleAuthor()
                            + "</font>"));

        }
        if (loadingText(articleParser.getArticleBodyText(), bodyId) == false) {
            body_progressbar.setVisibility(View.GONE);
        }
        bodyId++;
        ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                .get(articleParser.getArticlePhotoUrl(), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            Palette p = Palette.generate(bitmap, 5);
                            markColor = p.getDarkMutedColor(0xFF2233FF);
                            imageView.setImageBitmap(imageContainer.getBitmap());
                            rView.findViewById(R.id.title_bar)
                                    .setBackgroundColor(markColor);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });


        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });
        body_scrollview.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                if (body_progressbar.isShown() && body_progressbar.getTop() - scrollY - body_scrollview.getHeight() < OFFSET) {
                    if (loadingText(articleParser.getArticleBodyText(), bodyId) == false) {
                        body_progressbar.setVisibility(View.GONE);
                    } else {
                        Log.v("hhh", String.format("Loading article text: ", bodyId));
                        bodyId++;
                    }
                }
            }
        });
        Log.v("hhh2", "exiting onCreateView ArticleDetailFragment " + rView);
        return rView;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("hhh", "entering onCreate ArticleDetailFragment ");
        if (getArguments().containsKey(ARTICLES)) {
            bodyId = 0;
            articleParser = getArguments().getParcelable(ARTICLES);
            Log.v("hhh", "entering if onCreate ArticleDetailFragment ");
        }
        setHasOptionsMenu(true);
    }


    private boolean loadingText(@NonNull String fullText, int chunkId) {
        boolean areThereStillChunkLeft = true;
        Markdown4jProcessor mdProcessor;
        int textStartPosition;
        String textToAdd;
        Spanned htmlSpannedText;

        mdProcessor = new Markdown4jProcessor();
        textStartPosition = chunkId * SIZE;
        textToAdd = fullText.substring(textStartPosition, textStartPosition + SIZE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                htmlSpannedText = Html.fromHtml(mdProcessor.process(textToAdd), Html.FROM_HTML_MODE_COMPACT);
            } else {
                htmlSpannedText = Html.fromHtml(mdProcessor.process(textToAdd));
            }

            article_text_view.append(htmlSpannedText);
            areThereStillChunkLeft = (article_text_view.getText().length() != fullText.length());
        } catch (IOException ignored) {
        }

        return areThereStillChunkLeft;
    }
    private Date dateParsing() {
        try {
            String date = articleParser.getArticlePublishingDate();
            return simpleDateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e("hhh", ex.getMessage());
            Log.i("hhh", " date");
            return new Date();
        }
    }
    static float consentrate(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }
    static float progressive(float v, float min, float max) {
        return consentrate((v - min) / (max - min), 0, 1);
    }

}
