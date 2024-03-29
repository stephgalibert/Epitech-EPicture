package fr.epicture.epicture.api.imgur;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import fr.epicture.epicture.api.APIImageElement;

public class ImgurImageElement extends APIImageElement {
    //private long dateTime;

    //private String tags = "";

    //private String accountUrl;
    //private int accountID;
    private static final String BASE_URL = "https://i.imgur.com/%s.jpg";

    private String url;
    private int ups;
    private int downs;
    private int points;
    private int score;

    // ========================================================================
    // CONSTRUCTOR
    // ========================================================================

    public ImgurImageElement(JSONObject jsonObject, int size) {
        try {
            setID(jsonObject.getString("id"));
            setSize(size);
            title = jsonObject.getString("title");
            description = jsonObject.getString("description");
            if (description.equals("null")) {
                description = "";
            }
            date = jsonObject.getLong("datetime");
            ownername = jsonObject.getString("account_url");
            ownerid = jsonObject.getString("account_id");
            url = jsonObject.getString("link");
            commentCount = jsonObject.optInt("comment_count");
            ups = jsonObject.optInt("ups");
            downs = jsonObject.optInt("downs");
            points = jsonObject.optInt("points");
            score = jsonObject.optInt("score");
            favorite = jsonObject.getBoolean("favorite");

            tags = "";
            final JSONArray jsonArray = jsonObject.optJSONArray("tags");
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    final JSONObject tag = ((JSONObject) jsonArray.get(i));
                    tags += tag.getString("name");
                    if (i + 1 < jsonArray.length())
                        tags += " ";
                }
            }
        } catch (JSONException | ClassCastException e) {
            System.err.println("Error : Unable to convert Json object to ImgurImageElement.\n" + jsonObject.toString());
            e.printStackTrace();
        }
    }

    public ImgurImageElement(String path, String title, String description, String tags) {
        super(path, title, description, tags);
    }

    private ImgurImageElement(Parcel in) {
        setID(in.readString());
        setSize(in.readInt());
        ownerid = in.readString();
        title = in.readString();
        description = in.readString();
        date = in.readLong();
        ownername = in.readString();
        url = in.readString();
        commentCount = in.readInt();
        ups = in.readInt();
        downs = in.readInt();
        points = in.readInt();
        score = in.readInt();
        tags = in.readString();
        favorite = in.readInt() == 1;
    }

    // ========================================================================
    // METHODS
    // ========================================================================

    public static final Parcelable.Creator<ImgurImageElement> CREATOR = new Parcelable.Creator<ImgurImageElement>() {

        @Override
        public ImgurImageElement createFromParcel(Parcel in) {
            return new ImgurImageElement(in);
        }

        @Override
        public ImgurImageElement[] newArray(int size) {
            return new ImgurImageElement[size];
        }
    };

    @Override
    public String getURL() {
        //return url;
        return String.format(BASE_URL, getID());
    }

    @Override
    public float getWidthSize() {
        return 0;
    }

    @Override
    public float getHeightSize() {
        return 0;
    }

    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getID());
        parcel.writeInt(getSize());
        parcel.writeString(ownerid);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeLong(date);
        parcel.writeString(ownername);
        parcel.writeString(url);
        parcel.writeInt(commentCount);
        parcel.writeInt(ups);
        parcel.writeInt(downs);
        parcel.writeInt(points);
        parcel.writeInt(score);
        parcel.writeString(tags);
        parcel.writeInt(favorite?1:0);
    }

    // ------------------------------------------------------------------------
    // Getter / Setter
    // ------------------------------------------------------------------------


    public long getDateTime() {
        return date;
    }

    public void setDateTime(long dateTime) {
        this.date = dateTime;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getAccountUrl() {
        return ownername;
    }

    public void setAccountUrl(String accountUrl) {
        this.ownername = accountUrl;
    }

    public String getAccountID() {
        return ownerid;
    }

    public void setAccountID(String accountID) {
        this.ownerid = accountID;
    }

    /*public String getUrl() {
        //return url;
        return String.format(BASE_URL, getID());
    }*/

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getUps() {
        return ups;
    }

    public void setUps(int ups) {
        this.ups = ups;
    }

    public int getDowns() {
        return downs;
    }

    public void setDowns(int downs) {
        this.downs = downs;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
