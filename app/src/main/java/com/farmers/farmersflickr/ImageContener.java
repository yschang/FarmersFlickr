package com.farmers.farmersflickr;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.farmers.farmersflickr.FlickrManager.GetThumbnailsThread;
import com.farmers.farmersflickr.MainActivity.UIHandler;

import java.io.Serializable;

public class ImageContener implements IThumb, Serializable, Parcelable {
	String id;
	int position;
	String thumbURL;
	Bitmap thumb;
	Bitmap photo;
	String largeURL;
	String owner;
	String secret;
	String server;
	String farm;

	public ImageContener(String id, String thumbURL, String largeURL, String owner, String secret, String server, String farm) {
		super();
		this.id = id;
		this.owner = owner;
		this.secret = secret;
		this.server = server;
		this.farm = farm;
	}

	public ImageContener(String id, String owner, String secret, String server, String farm) {
		super();
		this.id = id;
		this.owner = owner;
		this.secret = secret;
		this.server = server;
		this.farm = farm;
		setThumbURL(createPhotoURL(FlickrManager.PHOTO_THUMB, this));
		setLargeURL(createPhotoURL(FlickrManager.PHOTO_LARGE, this));
	}

	public String getThumbURL() {
		return thumbURL;
	}

	public void setThumbURL(String thumbURL) {
		this.thumbURL = thumbURL;
		onSaveThumbURL(FlickrManager.uihandler, this);
	}

	public String getLargeURL() {
		return largeURL;
	}

	public void setLargeURL(String largeURL) {
		this.largeURL = largeURL;
	}

	@Override
	public String toString() {
		return "ImageContener [id=" + id + ", thumbURL=" + thumbURL + ", largeURL=" + largeURL + ", owner=" + owner + ", secret=" + secret + ", server=" + server + ", farm="
				+ farm + "]";
	}

	private String createPhotoURL(int photoType, ImageContener imgCon) {
		String tmp = null;
		tmp = "http://farm" + imgCon.farm + ".staticflickr.com/" + imgCon.server + "/" + imgCon.id + "_" + imgCon.secret;// +".jpg";
		switch (photoType) {
		case FlickrManager.PHOTO_THUMB:
			tmp += "_t";
			break;
		case FlickrManager.PHOTO_LARGE:
			tmp += "_z";
			break;

		}
		tmp += ".jpg";
		return tmp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Bitmap getThumb() {
		return thumb;
	}

	public void setThumb(Bitmap thumb) {
		this.thumb = thumb;
	}

	public Bitmap getPhoto() {
		return photo;
	}

	public void setPhoto(Bitmap photo) {
		this.photo = photo;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getFarm() {
		return farm;
	}

	public void setFarm(String farm) {
		this.farm = farm;
	}

	@Override
	public void onSaveThumbURL(UIHandler uih, ImageContener ic) {
		// TODO Auto-generated method stub
		new GetThumbnailsThread(uih, ic).start();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {

	}
}
