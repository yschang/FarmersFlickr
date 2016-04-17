package com.farmers.farmersflickr;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;

import com.farmers.farmersflickr.FlickrManager.GetThumbnailsThread;
import com.origamilabs.library.views.StaggeredGridView;

import java.util.ArrayList;

public class MainActivity extends Activity {

	public UIHandler uihandler;
	public ImageAdapter imgAdapter;
	private ArrayList<ImageContener> imageList;

	// UI
	private Button downloadPhotos;
	private EditText editText;
	private StaggeredGridView gridView;

    private static final int REQUEST_WRITE_STORAGE = 112;
    private static final String STATE_IMAGES = "images";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Init UI Handler
		uihandler = new UIHandler();

		downloadPhotos = (Button) findViewById(R.id.button);
		editText = (EditText) findViewById(R.id.editText);
        gridView = (StaggeredGridView) findViewById(R.id.staggeredGridView);

        downloadPhotos.setOnClickListener(onSearchButtonListener);

        // Storage Permission
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

        // Get recently searched list of images after the orientation change
        if (savedInstanceState != null) {
            imageList = (ArrayList<ImageContener>) savedInstanceState.getSerializable(STATE_IMAGES);

            if (imageList != null) {
                imgAdapter = new ImageAdapter(getApplicationContext(), imageList);

                String urls[] = new String[imageList.size()];

                for (int i = 0; i < imageList.size(); ++i) {
                    urls[i] = imageList.get(i).getThumbURL();
                }

                StaggeredAdapter adapter = new StaggeredAdapter(MainActivity.this, R.id.imageView, urls);
                gridView.setAdapter(adapter);

                for (int i = 0; i < imgAdapter.getCount(); i++) {
                    new GetThumbnailsThread(uihandler, imgAdapter.getImageContener().get(i)).start();
                }
            }
        }
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_IMAGES, imageList);
    }

	/**
	 * 
	 * @author michalu
	 * 
	 * Downloading a larger photo using Thread
	 */
	public class GetLargePhotoThread extends Thread {
		ImageContener ic;
		UIHandler uih;

		public GetLargePhotoThread(ImageContener ic, UIHandler uih) {
			this.ic = ic;
			this.uih = uih;
		}

		@Override
		public void run() {
			if (ic.getPhoto() == null) {
				ic.setPhoto(FlickrManager.getImage(ic));
			}
			Bitmap bmp = ic.getPhoto();
			if (ic.getPhoto() != null) {
				Message msg = Message.obtain(uih, UIHandler.ID_SHOW_IMAGE);
				msg.obj = bmp;
				uih.sendMessage(msg);
			}
		}
	}

	/**
	 * Runnable to get metadata from Flickr API
	 */
	Runnable getMetadata = new Runnable() {
		@Override
		public void run() {
			String tag = editText.getText().toString().trim();
            if (tag != null && tag.length() >= 3) {
                FlickrManager.searchImagesByTag(uihandler, getApplicationContext(), tag);
            }
		}
	};

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;
		private int defaultItemBackground;
		private ArrayList<ImageContener> imageContener;

		public ArrayList<ImageContener> getImageContener() {
			return imageContener;
		}

		public void setImageContener(ArrayList<ImageContener> imageContener) {
			this.imageContener = imageContener;
		}

		public ImageAdapter(Context c, ArrayList<ImageContener> imageContener) {
			mContext = c;
			this.imageContener = imageContener;
			TypedArray styleAttrs = c.obtainStyledAttributes(R.styleable.PicGallery);
			styleAttrs.getResourceId(R.styleable.PicGallery_android_galleryItemBackground, 0);
			defaultItemBackground = styleAttrs.getResourceId(R.styleable.PicGallery_android_galleryItemBackground, 0);
			styleAttrs.recycle();
		}

		public int getCount() {
			return imageContener.size();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView i = new ImageView(mContext);
			if (imageContener.get(position).thumb != null) {
				i.setImageBitmap(imageContener.get(position).thumb);
				i.setLayoutParams(new Gallery.LayoutParams(75, 75));
				i.setBackgroundResource(defaultItemBackground);
			} else
				i.setImageDrawable(getResources().getDrawable(android.R.color.black));
			return i;
		}

	}

	/**
	 * 
	 * @author michalu
	 * 
	 * UI Handler to handle messages from threads
	 */
	class UIHandler extends Handler {
		public static final int ID_METADATA_DOWNLOADED = 0;
		public static final int ID_SHOW_IMAGE = 1;
		public static final int ID_UPDATE_ADAPTER = 2;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
                case ID_METADATA_DOWNLOADED:
                    // Set of information required to download thumbnails is available now
                    if (msg.obj != null) {
                        imageList = (ArrayList<ImageContener>) msg.obj;
                        imgAdapter = new ImageAdapter(getApplicationContext(), imageList);

                        String urls[] = new String[imageList.size()];

                        for (int i = 0; i < imageList.size(); ++i) {
                            urls[i] = imageList.get(i).getThumbURL();
                        }

                        StaggeredAdapter adapter = new StaggeredAdapter(MainActivity.this, R.id.imageView, urls);
                        gridView.setAdapter(adapter);

                        for (int i = 0; i < imgAdapter.getCount(); i++) {
                            new GetThumbnailsThread(uihandler, imgAdapter.getImageContener().get(i)).start();
                        }
                    }
                    break;
                case ID_SHOW_IMAGE:
                    // Display large image
                    if (msg.obj != null) {
    //					imgView.setImageBitmap((Bitmap) msg.obj);
    //					imgView.setVisibility(View.VISIBLE);
                    }
                    break;
                case ID_UPDATE_ADAPTER:
                    // Update adapter with thumnails
                    if (imgAdapter != null) {
                        imgAdapter.notifyDataSetChanged();
                    }
                    break;
			}
			super.handleMessage(msg);
		}
	}

	OnItemClickListener onThumbClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
			// Get large image of selected thumnail
            new GetLargePhotoThread(imageList.get(position), uihandler).start();
		}
	};

    /**
     * to get metadata from Flickr API
     */
	OnClickListener onSearchButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
        if (gridView.getAdapter() != null) {
            imgAdapter.imageContener = new ArrayList<ImageContener>();

            int margin = getResources().getDimensionPixelSize(R.dimen.margin);

            gridView.setItemMargin(margin); // set the GridView margin

            gridView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well

            String urls[] = new String[imageList.size()];

            for (int i = 0; i < imageList.size(); ++i) {
                urls[i] = imageList.get(i).getThumbURL();
            }

            StaggeredAdapter adapter = new StaggeredAdapter(MainActivity.this, R.id.imageView, urls);

            gridView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        new Thread(getMetadata).start();
		}
	};

}
