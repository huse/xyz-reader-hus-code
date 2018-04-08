package com.example.xyzreader.data;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by hk640d on 4/1/2018.
 */

public class ArticleParser implements  Parcelable {
    private String authorName;
    private String URLimage;
    private float ratio;

    private String contentText;
    private long id;
    private String title;
    private String datePublished;

    private String URLtumbnail;


    private ArticleParser() {

    }
    private ArticleParser(Parcel in) {
        this.authorName = in.readString();
        this.URLimage = in.readString();
        this.ratio = in.readFloat();
        this.contentText = in.readString();

        this.id = in.readLong();
        this.title = in.readString();
        this.datePublished = in.readString();

        this.URLtumbnail = in.readString();

    }

    public static ArticleParser forParsingGetFromCursor(@NonNull Cursor cursor) {
        ArticleParser article = new ArticleParser();
        article.authorName = cursor.getString(ArticleLoader.Query.AUTHOR);
        article.URLimage = cursor.getString(ArticleLoader.Query.PHOTO_URL);
        article.ratio = cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO);
        article.contentText = cursor.getString(ArticleLoader.Query.BODY);

        article.id = cursor.getLong(ArticleLoader.Query._ID);
        article.title = cursor.getString(ArticleLoader.Query.TITLE);
        article.datePublished = cursor.getString(ArticleLoader.Query.DATE_PUBLISHED);
        article.URLtumbnail = cursor.getString(ArticleLoader.Query.THUMB_URL);

        return article;
    }

    public String getArticleBodyText() {
        return contentText;
    }
    public String getArticleTitle() {
        return title;
    }

    public String getArticlePublishingDate() {
        return datePublished;
    }
    public String getArticleAuthor() {
        return authorName;
    }

    public String getArticlePhotoUrl() {
        return URLimage;
    }
    public long getArticleId() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(authorName);
        dest.writeString(URLimage);
        dest.writeFloat(ratio);
        dest.writeString(contentText);

        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(datePublished);
        dest.writeString(URLtumbnail);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator<ArticleParser>() {

        @Override
        public ArticleParser[] newArray(int size) {
            return new ArticleParser[size];
        }
        @Override
        public ArticleParser createFromParcel(Parcel source) {
            return new ArticleParser(source);
        }

    };
}
