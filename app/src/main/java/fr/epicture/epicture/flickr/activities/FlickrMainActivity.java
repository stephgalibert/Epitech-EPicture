package fr.epicture.epicture.flickr.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.epicture.epicture.R;
import fr.epicture.epicture.flickr.fragments.ImageListFragment;
import fr.epicture.epicture.flickr.interfaces.ImageDiskCacheInterface;
import fr.epicture.epicture.flickr.interfaces.ImageListInterface;
import fr.epicture.epicture.flickr.interfaces.InterestingnessRequestInterface;
import fr.epicture.epicture.flickr.requests.InterestingnessRequest;
import fr.epicture.epicture.flickr.utils.FlickrClient;
import fr.epicture.epicture.flickr.utils.ImageDiskCache;
import fr.epicture.epicture.flickr.utils.ImageElement;
import fr.epicture.epicture.flickr.utils.UserElement;
import jp.wasabeef.blurry.Blurry;


public class FlickrMainActivity extends AppCompatActivity implements ImageListInterface {

    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private CircleImageView profilePic;
    private ImageView profilePicBlurred;
    private TextView myPictures;

    private InterestingnessRequest interestingnessRequest;

    private ImageListFragment imageListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_flickr_activity);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout)findViewById(R.id.main_drawer);
        profilePic = (CircleImageView)findViewById(R.id.main_profilepic);
        profilePicBlurred = (ImageView)findViewById(R.id.main_profilepic_blurred);
        myPictures = (TextView)findViewById(R.id.drawer_mypictures);

        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        toggle.setDrawerIndicatorEnabled(false);
        toggle.setHomeAsUpIndicator(R.mipmap.hamburger);
        toggle.syncState();

        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawer = (DrawerLayout)findViewById(R.id.main_drawer);
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                } else {
                    drawer.openDrawer(GravityCompat.START);
                }

                refreshProfilePicBlurred();
            }
        });

        myPictures.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FlickrMainActivity.this, MyPicturesActivity.class);
                startActivity(intent);
            }
        });

        refreshFragment();
    }

    private void refreshProfilePicBlurred() {
        UserElement user = FlickrClient.user;
        ImageElement imageElement = new ImageElement(user.iconfarm, user.iconserver, user.nsid);
        new ImageDiskCache().load(this, imageElement, new ImageDiskCacheInterface() {
            @Override
            public void onFinish(ImageElement imageElement, Bitmap bitmap) {
                profilePic.setImageBitmap(bitmap);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Blurry.with(FlickrMainActivity.this).radius(25).sampling(2).capture(profilePic).into(profilePicBlurred);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }, 10);
            }
        });
    }

    private void refreshFragment() {
        imageListFragment = new ImageListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, imageListFragment).commit();
    }

    @Override
    public void onRequestImageList(int page) {
        if (!isRequestingImageList()) {
            interestingnessRequest = new InterestingnessRequest(this, FlickrClient.tokenAccess, page,
                    new InterestingnessRequestInterface() {
                @Override
                public void onFinish(JSONObject jsonObject) {
                    interestingnessRequest = null;
                    if (jsonObject != null) {
                        try {
                            int maxPage = jsonObject.getJSONObject("photos").getInt("pages");
                            imageListFragment.setMaxPage(maxPage);
                            List<ImageElement> imageElementList = new ArrayList<>();
                            JSONArray jsonArray = jsonObject.getJSONObject("photos").getJSONArray("photo");
                            if (jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    ImageElement imageElement = new ImageElement(jsonArray.getJSONObject(i),
                                            ImageElement.TYPE_IMAGE, ImageElement.SIZE_PREVIEW);
                                    imageElementList.add(imageElement);
                                }
                                imageListFragment.refreshList(imageElementList);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onError(int responseCode) {
                }
            });
            interestingnessRequest.execute();
            imageListFragment.refreshList(null);
        }
    }

    public boolean isRequestingImageList() {
        return (interestingnessRequest != null && !interestingnessRequest.isRunning());
    }

}
