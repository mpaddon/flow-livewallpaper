package pong.flow;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;

public class PhotoBarPreference extends Preference implements
		OnItemClickListener, OnItemSelectedListener {

	final String _photoName = "";
	public final static long NEXT_PAGE = -123;

	ArrayList<ImageItem> arrayImageItem;
	int _page;
	int _selected;

	public class ImageItem {

		Bitmap bitmapImage;
		long _id;

		ImageItem(Bitmap bm, long id) {
			// To simplify, we use a default image here
			bitmapImage = bm; // = BitmapFactory.decodeResource(
			// _context.getResources(), R.drawable.ic_launcher);
			_id = id;
		}

		public Bitmap getImage() {
			return bitmapImage;
		}

	}

	private AsyncTask<Object, ImageItem, Object> _currentTask;

	PhotoBarPrefWidget pbp;
	private TextView _titleTextView;
	String _titleText;
	boolean _loadFromGAllery;
	Integer[] custom_thumbnails = { R.raw.bg_tile_teal_thumb,
			R.raw.bg_tile_spiral_thumb, R.raw.bg_stones_thumb,
			R.raw.bg_goo_thumb, R.raw.bg_jellyfish_thumb, R.raw.bg_lava_thumb,
			R.raw.bg_sand_footprint2_thumb, };

	Long[] custom_thumbnails_ID = { (long) 7, (long) 6, (long) 9, (long) 8,
			(long) 3, (long) 10, (long) 4, };
	private Context _context;
	private MyAdapter myAdapter;
	private Gallery myPhotoBar;

	public PhotoBarPreference(Context context, AttributeSet attrs) {

		super(context, attrs);
		setWidgetLayoutResource(R.layout.photo_bar_pref_widget);
		_context = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.PhotoBarPrefWidget1);

		final int N = a.getIndexCount();
		for (int i = 0; i < N; ++i) {
			int attr = a.getIndex(i);
			switch (attr) {
			case R.styleable.PhotoBarPrefWidget1_photobar_title:
				String myText = a.getString(attr);
				_titleText = myText;
				break;
			case R.styleable.PhotoBarPrefWidget1_photobar_loadFromGallery:
				boolean loadFromGallery = a.getBoolean(attr, true);
				_loadFromGAllery = loadFromGallery;
				break;
			}
		}
		a.recycle();

		myAdapter = new MyAdapter(context); // init myAdapter without ImageItem

		_page = 0;

		if (_loadFromGAllery) {
			loadFromGallery();

		} else {

			ArrayList<Bitmap> list = new ArrayList<Bitmap>(
					custom_thumbnails.length);
			ArrayList<Long> ids = new ArrayList<Long>(custom_thumbnails.length);

			for (int i = 0; i < custom_thumbnails.length; i++) {
				list.add(BitmapFactory.decodeResource(_context.getResources(),
						custom_thumbnails[i]));
				ids.add(custom_thumbnails_ID[i]);
			}

			loadCustomThumbnails(list, ids);
		}

		_selected = 0;
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);

		// Log.d("DE", "BIND VIEW CALLED");
		// pbp = (PhotoBarPrefWidget)view.findViewById(R.id.prefWidget);
		myPhotoBar = (Gallery) view.findViewById(R.id.photobar);
		myPhotoBar.setOnItemClickListener(this);

		// Gallery gal = (Gallery)view.findViewById(R.id.photobar);

		_titleTextView = (TextView) view.findViewById(R.id.photobar_title);
		_titleTextView.setText(_titleText);

		// Set our custom views inside the layout
		// final View photoBar = view.findViewById(R.id.photobar);
		// myPhotoBar.setSelected(true);
		// myPhotoBar.setSelection(_selected);
		myPhotoBar.setAdapter(myAdapter);
		myPhotoBar.setSelection(_selected);
		myPhotoBar.setOnItemSelectedListener(this);
		Log.d("SELECTED", "" + _selected);

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// Log.d("EDE", "CLICKED");
		// _selected = arg2;
		// myPhotoBar.setSelection(arg2);
		if (arg3 == PhotoBarPrefWidget.NEXT_PAGE) {
			loadNextPage();
		} else {
			persistLong(arg3);
		}

	}

	@Override
	public View getView(final View convertView, final ViewGroup parent) {
		final View v = super.getView(convertView, parent);
		final int height = 215;
		final int width = android.view.ViewGroup.LayoutParams.MATCH_PARENT;
		final LayoutParams params = new LayoutParams(width, height);
		v.setLayoutParams(params);
		return v;
	}

	public void destroy() {
		// TODO Auto-generated method stub
		if (_currentTask != null) {
			_currentTask.cancel(true);
		}
	}

	public void loadNextPage() {
		if (_page < 10) {
			_page++;
			_currentTask = new LoadImagesFromSDCard().execute();
		} else {
			Toast.makeText(_context, "Wrap Core Breach!", Toast.LENGTH_LONG);
		}
	}

	public void loadFromGallery() {
		myAdapter.setLoadMoreButton();

		_currentTask = new LoadImagesFromSDCard().execute();

	}

	public void loadCustomThumbnails(ArrayList<Bitmap> list, ArrayList<Long> ids) {
		for (int i = 0; i < list.size(); i++) {
			myAdapter.addImageItem(new ImageItem(list.get(i), ids.get(i)));
		}
	}

	public class MyAdapter extends BaseAdapter {

		Context context;
		ArrayList<ImageItem> _arrayImageItem;
		int imageBackground;
		boolean _backButtonAdded;

		MyAdapter(Context c) {
			context = c;
			_arrayImageItem = new ArrayList<ImageItem>();
			TypedArray ta = c.obtainStyledAttributes(R.styleable.Gallery1);
			imageBackground = ta.getResourceId(
					R.styleable.Gallery1_android_galleryItemBackground, 1);
			ta.recycle();
			_backButtonAdded = false;
		}

		public void setLoadMoreButton() {
			_backButtonAdded = true;
			_arrayImageItem.add(new ImageItem(BitmapFactory.decodeResource(
					_context.getResources(), R.raw.button_add_images),
					PhotoBarPrefWidget.NEXT_PAGE));
			this.notifyDataSetChanged();

		}

		public void addImageItem(ImageItem item) {
			// _arrayImageItem.add(item);
			if (this._backButtonAdded) {
				_arrayImageItem.add(_arrayImageItem.size() - 1, item);
			} else {
				_arrayImageItem.add(_arrayImageItem.size(), item);
			}
			this.notifyDataSetChanged();

		}

		public int getCount() {
			// TODO Auto-generated method stub
			return _arrayImageItem.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return _arrayImageItem.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return _arrayImageItem.get(position)._id;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ImageView imageView;
			imageView = new ImageView(context);

			imageView.setLayoutParams(new Gallery.LayoutParams(200, 200));
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setImageBitmap(_arrayImageItem.get(position).getImage());
			imageView.setBackgroundResource(imageBackground);
			// imageView.setBackgroundColor(Color.GRAY);

			return imageView;
		}
	}

	class LoadImagesFromSDCard extends AsyncTask<Object, ImageItem, Object> {

		/**
		 * Load images from SD Card in the background, and display each image on
		 * the screen.
		 * 
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		int cur;

		protected Object doInBackground(Object... params) {
			// setProgressBarIndeterminateVisibility(true);
			Bitmap bitmap = null;

			// Set up an array of the Thumbnail Image ID column we want
			String[] projection = { MediaStore.Images.Media._ID };
			String selection = "";
			// Create the cursor pointing to the SDCard
			Cursor cursor = ((android.app.Activity) _context).managedQuery(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, // Which
																				// columns
																				// to
																				// return
					"", // Return all rows
					null, MediaStore.Images.Media.DATE_ADDED + " DESC "
							+ "limit " + 100);
			int columnIndex = cursor
					.getColumnIndex(MediaStore.Images.Media._ID);
			int size = cursor.getCount();
			// Log.d("SIZE:", "S:" + size);
			// If size is 0, there are no images on the SD Card.
			if (size == 0) {
				// No Images available, post some message to the user
			}
			int imageID = 0;
			int max = 0;
			if (size - (10 * _page) > 10) {
				max = (10 * _page) + 10;
			} else {
				max = size - (10 * _page);
			}

			cur = myAdapter.getCount() - 1;

			if (max < 10 * _page) {
				publishProgress(null);
			}
			if (isCancelled()) {
				return null;
			}
			for (int i = 10 * _page; i < max; i++) {
				if (isCancelled()) {
					return null;
				}
				cursor.moveToPosition(i);
				imageID = cursor.getInt(columnIndex);
				bitmap = MediaStore.Images.Thumbnails.getThumbnail(
						_context.getContentResolver(), imageID,
						MediaStore.Images.Thumbnails.MINI_KIND, null);// loadThumbnailImage(
																		// uri.toString()
																		// );//BitmapFactory.decodeStream(((android.app.Activity)_context).getContentResolver().openInputStream(uri));
				if (bitmap != null) {

					publishProgress(new ImageItem(bitmap, imageID));

				}
				// temp.close();
			}
			cursor.close();
			//
			return null;
		}

		/**
		 * Add a new LoadedImage in the images grid.
		 * 
		 * @param value
		 *            The image.
		 */
		@Override
		public void onProgressUpdate(ImageItem... value) {
			// Log.d("TEST", "TEMP");

			if (value == null) {
				Toast.makeText(_context, "Warp Core Breach!",
						Toast.LENGTH_SHORT);
			} else {
				myAdapter.addImageItem(value[0]);
				// myAdapter.notifyDataSetChanged();
			}
		}

		/**
		 * Set the visibility of the progress bar to false.
		 * 
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Object result) {
			// myPhotoBar.setSelection(cur);
			_selected = cur;
			if (myPhotoBar != null) {
				myPhotoBar.setSelection(_selected);
			}
			// setProgressBarIndeterminateVisibility(false);
		}

	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		_selected = arg2;

	}

	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
