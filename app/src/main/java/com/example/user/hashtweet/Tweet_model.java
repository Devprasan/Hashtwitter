package com.example.user.hashtweet;

import android.media.Image;

/**
 * Created by user on 12/26/2015.
 */
public class Tweet_model {
    private String username;
    private String tweets;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    private Image image;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTweets() {
        return tweets;
    }

    public void setTweets(String tweets) {
        this.tweets = tweets;
    }
}
