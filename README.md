# StrikedImageView
Animate a strike over any image to indicate on/off states. [As seen in the Material Guidelines.](https://material.io/design/iconography/animated-icons.html#transitions)

![StrikedImageView](https://user-images.githubusercontent.com/7334346/132612839-2592f6ec-82bd-4509-805a-f67627324d6a.gif)

[![Platform](https://img.shields.io/badge/platform-android-green.svg)](http://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)

## Gradle
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
```
dependencies {
    implementation 'com.github.DamonBaker:StrikedImageView:1.0.0'
}
```

## Usage
Set base drawable like you would a regular ImageView.
It is recommended that you also set the `android:tint` attribute so the view knows what color to tint the strike.
```
<xyz.damonbaker.strikedimageview.StrikedImageView
    android:id="@+id/striked_image"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:tint="#FFC107"
    app:srcCompat="@drawable/ic_camera_black_24dp"
    app:striked="false" />
```
```
val strikedImage: StrikedImageView = findViewById(R.id.striked_image)
strikedImage.setImageDrawable(drawable)
```
Set `isStriked` to apply the animation.
```
strikedImage.isStriked = true
```

## Limitations
- StrikedImageView works best with vector drawables, if you are not using a vector drawable as the image source then the `strikeColor` attribute must be set if you want the strike color to match the color of the drawable.
- A tint should be set on the StrikedImageView so the view knows what color to tint the strike, otherwise you can set `strikeColor` manually.
- In order to simulate the clip-through effect of the strike, the view hierarchy is traversed until the first parent with a `ColorDrawable` is found. This color will be used as the `strikeBackgroundColor`.
    - If the parent's background is not an instance of `ColorDrawable` (i.e. a drawable instead of a color), then the `strikeBackgroundColor` attribute must be manually set.
