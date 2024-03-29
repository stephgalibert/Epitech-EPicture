package fr.epicture.epicture.api.imgur;

import android.content.Context;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.epicture.epicture.R;
import fr.epicture.epicture.api.API;
import fr.epicture.epicture.api.APIAccount;
import fr.epicture.epicture.api.APICommentElement;
import fr.epicture.epicture.api.APIImageElement;
import fr.epicture.epicture.api.imgur.database.ImgurDatabase;
import fr.epicture.epicture.api.imgur.requests.AlbumRequest;
import fr.epicture.epicture.api.imgur.requests.GalleryRequest;
import fr.epicture.epicture.api.imgur.requests.GetFavoritesRequest;
import fr.epicture.epicture.api.imgur.requests.RefreshTokenRequest;
import fr.epicture.epicture.api.imgur.requests.UploadImageRequest;
import fr.epicture.epicture.api.imgur.requests.UserInformationRequest;
import fr.epicture.epicture.api.imgur.utils.ImgurUtils;
import fr.epicture.epicture.interfaces.AddCommentInterface;
import fr.epicture.epicture.interfaces.AuthentificationInterface;
import fr.epicture.epicture.interfaces.ImageDiskCacheInterface;
import fr.epicture.epicture.interfaces.LoadBitmapInterface;
import fr.epicture.epicture.interfaces.LoadCommentElementInterface;
import fr.epicture.epicture.interfaces.LoadImageElementInterface;
import fr.epicture.epicture.interfaces.LoadTextInterface;
import fr.epicture.epicture.interfaces.LoadUserInfoInterface;
import fr.epicture.epicture.interfaces.PhotoIsInFavoritesInterface;
import fr.epicture.epicture.utils.ImageDiskCache;

public class Imgur implements API {

    // ========================================================================
    // STATIC FIELDS
    // ========================================================================

    public static final String CLIENT_ID = "a754d8ce4c94e1c";
    public static final String CLIENT_SECRET = "e6020ba810e1e7d85a756a26a666d8ea3b60a6f1";
    private static final String authorizeLink = "https://api.imgur.com/oauth2/authorize?client_id=" + CLIENT_ID + "&response_type=token";
    private static final String name = "Imgur";

    // ========================================================================
    // FIELDS
    // ========================================================================

    private String currentAccount = null;
    private final Map<String, ImgurAccount> accountByID = new HashMap();
    private final ImgurDatabase database;
    private AuthentificationInterface listener;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public Imgur(Context context) {
        this.database = new ImgurDatabase(context);
        this.database.open();
        loadAccount();
    }

    // ========================================================================
    // METHODS
    // ========================================================================

    private void loadAccount() {
        final List<ImgurAccount> accounts = database.getAccounts();
        for (ImgurAccount imgurAccount : accounts)
            addAccount(imgurAccount);
    }

    public void addAccount(ImgurAccount imgurAccount) {
        accountByID.put(imgurAccount.id, imgurAccount);
    }

    @Override
    public Collection<APIAccount> getAccounts() {
        return new ArrayList<>(accountByID.values());
    }

    public void removeAccount(ImgurAccount imgurAccount) {
        accountByID.remove(imgurAccount.id);
    }

    @Override
    public void setAuthentificationListener(AuthentificationInterface listener) {
        this.listener = listener;
    }

    @Override
    public void authentification(Context context) {
        listener.onUserPermissionRequest(authorizeLink, "account_username");
    }

    @Override
    public void afterUserPermissionRequest(Context context, String urlResponse) {
        try {
            final ImgurAccount imgurAccount = new ImgurAccount(ImgurUtils.getQueryMap(urlResponse));
            accountByID.put(imgurAccount.getID(), imgurAccount);
            database.insertAccount(imgurAccount);
            listener.onUserPermissionGranted();
            imgurAccount.updateInformation(context);
        } catch (InstantiationException e) {
            System.err.println("Error : unable to instantiate ImgurAccount");
            e.printStackTrace();
        }
    }

    @Override
    public void loadUserInformation(Context context, LoadUserInfoInterface callback) {
        for (ImgurAccount imgurAccount : accountByID.values()) {
            if (ImgurUtils.getTime() >= imgurAccount.getAccessToken().getDate() + imgurAccount.getAccessToken().getExpiration()) {
                new RefreshTokenRequest(context, imgurAccount.getRefreshToken(), text -> {
                    imgurAccount.updateAccessToken(text);
                    new UserInformationRequest(context, imgurAccount.getUsername(), imgurAccount.getAccessToken().getAccessToken(), text1 -> {
                        imgurAccount.updateInformation(text1);
                        callback.onFinish(imgurAccount.getID(), imgurAccount);
                    });
                });
            } else {
                new UserInformationRequest(context, imgurAccount.getUsername(), imgurAccount.getAccessToken().getAccessToken(), text1 -> {
                    imgurAccount.updateInformation(text1);
                    callback.onFinish(imgurAccount.getID(), imgurAccount);
                }).execute();
            }
        }
    }

    @Override
    public void loadUserInformation(Context context, String id, LoadUserInfoInterface callback) {

    }

    @Override
    public void loadUserAvatar(Context context, APIAccount user, LoadBitmapInterface callback) {

    }

    @Override
    public void loadUserAvatar(Context context, APICommentElement commentElement, LoadBitmapInterface callback) {

    }

    @Override
    public void loadImage(Context context, APIImageElement element, LoadBitmapInterface callback) {
        ImageDiskCache.load(context, element, new ImageDiskCacheInterface() {
            @Override
            public void onFinish(APIImageElement element, Bitmap bitmap) {
                callback.onFinish(bitmap);
            }
        });
    }

    @Override
    public void uploadImage(Context context, APIAccount user, String path, String title, String description, String tags, LoadTextInterface callback) {
        ImgurAccount account = accountByID.get(currentAccount);
        ImgurImageElement element = new ImgurImageElement(path, title, description, tags);
        new UploadImageRequest(context, element, account.getAccessToken().getAccessToken(), new LoadTextInterface() {
            @Override
            public void onFinish(String text) {
                callback.onFinish(text);
            }
        });
    }

    @Override
    public void getInterestingnessList(Context context, int page, LoadImageElementInterface callback) {
        new GalleryRequest(context, "hot", "viral", page, text -> {
            try {
                final JSONArray jsonArray = new JSONObject(text).getJSONArray("data");
                final List<APIImageElement> imgurImageElements = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ImgurImageElement element = new ImgurImageElement(jsonObject, APIImageElement.SIZE_PREVIEW);
                    imgurImageElements.add(element);
                }
                callback.onFinish(imgurImageElements, imgurImageElements.isEmpty());
            } catch (JSONException | ClassCastException e) {
                System.err.println("Error : Unable to convert request data to json.");
                e.printStackTrace();
            }
        }).execute();
    }

    @Override
    public void getMyPictures(Context context, int page, LoadImageElementInterface callback) {
        ImgurAccount account = accountByID.get(currentAccount);
        String accessToken = account.getAccessToken().getAccessToken();
        new AlbumRequest(context, accessToken, text -> {
            try {
                final JSONArray jsonArray = new JSONObject(text).getJSONArray("data");
                final List<APIImageElement> imgurImageElements = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    ImgurImageElement element = new ImgurImageElement(jsonObject, APIImageElement.SIZE_PREVIEW);
                    imgurImageElements.add(element);
                }
                callback.onFinish(imgurImageElements, imgurImageElements.isEmpty());
            } catch (JSONException | ClassCastException e) {
                System.err.println("Error : Unable to convert request data to json.");
                e.printStackTrace();
            }
        }).execute();
    }

    @Override
    public void addComment(Context context, String userid, String photoid, String comment, AddCommentInterface callback) {

    }

    @Override
    public void getComments(Context context, String userid, String photoid, LoadCommentElementInterface callback) {

    }

    @Override
    public void addFavorite(Context context, String userid, String photoid, LoadTextInterface callback) {
        ImgurAccount account = accountByID.get(currentAccount);
        String accessToken = account.getAccessToken().getAccessToken();
        new fr.epicture.epicture.api.imgur.requests.AddFavoriteRequest(context, photoid, accessToken, new LoadTextInterface() {
            @Override
            public void onFinish(String text) {
                callback.onFinish(text);
            }
        });
    }

    @Override
    public void deleteFavorite(Context context, String userid, String photoid, LoadTextInterface callback) {

    }

    @Override
    public void isFavorite(Context context, String userid, String photoid, PhotoIsInFavoritesInterface callback) {
        // unused
    }

    @Override
    public void getFavorites(Context context, String userid, int page, LoadImageElementInterface callback) {
        ImgurAccount account = accountByID.get(currentAccount);
        String accessToken = account.getAccessToken().getAccessToken();

        new GetFavoritesRequest(context, account.getUsername(), accessToken, page - 1, new LoadTextInterface() {
            @Override
            public void onFinish(String text) {
                try {
                    final JSONArray jsonArray = new JSONObject(text).getJSONArray("data");
                    final List<APIImageElement> imgurImageElements = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ImgurImageElement element = new ImgurImageElement(jsonObject, APIImageElement.SIZE_PREVIEW);
                        imgurImageElements.add(element);
                    }
                    callback.onFinish(imgurImageElements, imgurImageElements.isEmpty());
                } catch (JSONException | ClassCastException e) {
                    System.err.println("Error : Unable to convert request data to json.");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void search(Context context, String search, String userid, int page, LoadImageElementInterface callback) {

    }

    @Override
    public void deletePhoto(Context context, String photoid, String userid, LoadTextInterface callback) {

    }

    @Override
    public void setCurrentAccount(APIAccount account) {
        currentAccount = account.getID();
    }

    @Override
    public APIAccount getCurrentAccount() {
        return accountByID.get(currentAccount);
    }

    @Override
    public int getLogo() {
        return R.drawable.imgur_logo;
    }

    @Override
    public String getName() {
        return name;
    }
}
