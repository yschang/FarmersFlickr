package com.farmers.farmersflickr;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
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
import android.widget.Toast;

import com.farmers.farmersflickr.FlickrManager.GetThumbnailsThread;
import com.origamilabs.library.views.StaggeredGridView;

import java.util.ArrayList;

public class MainActivity extends Activity {

	public final String LAST_IMAGE = "lastImage";
	public UIHandler uihandler;
	public ImageAdapter imgAdapter;
	private ArrayList<ImageContener> imageList;

	// UI
	private Button downloadPhotos;
//	private Gallery gallery;
//	private ImageView imgView;
	private EditText editText;
	private StaggeredGridView gridView;

    private static final int REQUEST_WRITE_STORAGE = 112;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Init UI Handler
		uihandler = new UIHandler();

		downloadPhotos = (Button) findViewById(R.id.button);
		editText = (EditText) findViewById(R.id.editText);
//		gallery = (Gallery) findViewById(R.id.gallery1);
//		imgView = (ImageView) findViewById(R.id.imageView1);
        gridView = (StaggeredGridView) findViewById(R.id.staggeredGridView);

		// Click on thumbnail
//		gallery.setOnItemClickListener(onThumbClickListener);
		// Click on search
		downloadPhotos.setOnClickListener(onSearchButtonListener);

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

		// Get prevoiusly downloaded list after orientation change
		imageList = (ArrayList<ImageContener>) getLastNonConfigurationInstance();
		if (imageList != null) {
			imgAdapter = new ImageAdapter(getApplicationContext(), imageList);
			ArrayList<ImageContener> ic = imgAdapter.getImageContener();
//			gallery.setAdapter(imgAdapter);
			imgAdapter.notifyDataSetChanged();
			int lastImage = -1;
			if (savedInstanceState.containsKey(LAST_IMAGE)) {
				lastImage = savedInstanceState.getInt(LAST_IMAGE);
			}
			if (lastImage >= 0 && ic.size() >= lastImage) {
//				gallery.setSelection(lastImage);
				Bitmap photo = ic.get(lastImage).getPhoto();
				if (photo == null) {
                    new GetLargePhotoThread(ic.get(lastImage), uihandler).start();
                } else {
//					imgView.setImageBitmap(ic.get(lastImage).photo);
                }
			}
		}

	}

	/**
	 * Saving information about images
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (imgAdapter != null)
			return this.imgAdapter.getImageContener();
		else
			return null;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// Saving index of selected item in Gallery
//		outState.putInt(LAST_IMAGE, gallery.getSelectedItemPosition());
		super.onSaveInstanceState(outState);
	}

	/**
	 * 
	 * @author michalu
	 * 
	 *         Downloading a larger photo using Thread
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
			// TODO Auto-generated method stub
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
            Log.d("getMetadata", "Tag: " + tag);
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
	 *         UI Handler to handle messages from threads
	 */
	class UIHandler extends Handler {
		public static final int ID_METADATA_DOWNLOADED = 0;
		public static final int ID_SHOW_IMAGE = 1;
		public static final int ID_UPDATE_ADAPTER = 2;

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
                case ID_METADATA_DOWNLOADED:
                    // Set of information required to download thumbnails is
                    // available now
                    if (msg.obj != null) {
                        imageList = (ArrayList<ImageContener>) msg.obj;
                        imgAdapter = new ImageAdapter(getApplicationContext(), imageList);
//    					gallery.setAdapter(imgAdapter);

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
                    imgAdapter.notifyDataSetChanged();
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

//    private String urls[] = {
//            "http://farm7.staticflickr.com/6101/6853156632_6374976d38_c.jpg",
//            "http://farm8.staticflickr.com/7232/6913504132_a0fce67a0e_c.jpg",
//            "http://farm5.staticflickr.com/4133/5096108108_df62764fcc_b.jpg",
//            "http://farm5.staticflickr.com/4074/4789681330_2e30dfcacb_b.jpg",
//            "http://farm9.staticflickr.com/8208/8219397252_a04e2184b2.jpg",
//            "http://farm9.staticflickr.com/8483/8218023445_02037c8fda.jpg",
//            "http://farm9.staticflickr.com/8335/8144074340_38a4c622ab.jpg",
//            "http://farm9.staticflickr.com/8060/8173387478_a117990661.jpg",
//            "http://farm9.staticflickr.com/8056/8144042175_28c3564cd3.jpg",
//            "http://farm9.staticflickr.com/8183/8088373701_c9281fc202.jpg",
//            "http://farm9.staticflickr.com/8185/8081514424_270630b7a5.jpg",
//            "http://farm9.staticflickr.com/8462/8005636463_0cb4ea6be2.jpg",
//            "http://farm9.staticflickr.com/8306/7987149886_6535bf7055.jpg",
//            "http://farm9.staticflickr.com/8444/7947923460_18ffdce3a5.jpg",
//            "http://farm9.staticflickr.com/8182/7941954368_3c88ba4a28.jpg",
//            "http://farm9.staticflickr.com/8304/7832284992_244762c43d.jpg",
//            "http://farm9.staticflickr.com/8163/7709112696_3c7149a90a.jpg",
//            "http://farm8.staticflickr.com/7127/7675112872_e92b1dbe35.jpg",
//            "http://farm8.staticflickr.com/7111/7429651528_a23ebb0b8c.jpg",
//            "http://farm9.staticflickr.com/8288/7525381378_aa2917fa0e.jpg",
//            "http://farm6.staticflickr.com/5336/7384863678_5ef87814fe.jpg",
//            "http://farm8.staticflickr.com/7102/7179457127_36e1cbaab7.jpg",
//            "http://farm8.staticflickr.com/7086/7238812536_1334d78c05.jpg",
//            "http://farm8.staticflickr.com/7243/7193236466_33a37765a4.jpg",
//            "http://farm8.staticflickr.com/7251/7059629417_e0e96a4c46.jpg",
//            "http://farm8.staticflickr.com/7084/6885444694_6272874cfc.jpg"
//    };

            /**
             * to get metadata from Flickr API
             */
	OnClickListener onSearchButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
//			if (gallery.getAdapter() != null) {

//				gallery.setAdapter(imgAdapter);
//				imgView.setVisibility(View.INVISIBLE);
//			}
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
