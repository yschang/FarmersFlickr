# FarmersFlickr

FarmersFlickr is an Android application that allows users to search for Flickr pictures and view each searched list of images in full screen.

### Features Implemented
- Search for a tag and list all related Flickr images in Staggered Grid View.
- Click on the image from Staggered Grid View to view the image in large size.

### Resources Used
http://michalu.eu/wordpress/android-flickr-api-tutorial/

https://github.com/maurycyw/StaggeredGridView

https://github.com/maurycyw/StaggeredGridViewDemo


### Issues
- Images become blurry when they are expanded.
- String search -> Empty string search -> Empty string search (Clicking on Search button twice is needed in order to display the empty Grid view).
- String search -> Empty string search -> Empty string search -> orientation change -> Empty string search -> list of images from previous search shows up instead of the empty grid view.
- App crashes if user does the String search multiple times (OutOfMemoryError).
