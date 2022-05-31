package Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Note implements Parcelable {
    private String title, day, time, content, id;
    private boolean done;

    public Note() {
    }

    public Note(String title, String day, String time, String content) {
        this.title = title;
        this.day = day;
        this.time = time;
        this.content = content;
        this.id = "";
        this.done = false;
    }

    protected Note(Parcel in) {
        title = in.readString ();
        day = in.readString ();
        time = in.readString ();
        content = in.readString ();
        id = in.readString ();
        done = in.readByte () != 0;
    }

    public static final Creator<Note> CREATOR = new Creator<Note> () {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note (in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isCheck() {
        return done;
    }

    public void setCheck(boolean done) {
        this.done = done;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString (title);
        parcel.writeString (day);
        parcel.writeString (time);
        parcel.writeString (content);
        parcel.writeString (id);
        parcel.writeByte ((byte) (done ? 1 : 0));
    }

    @Override
    public String toString() {
        return "Note{" +
                "title='" + title + '\'' +
                ", day='" + day + '\'' +
                ", time='" + time + '\'' +
                ", content='" + content + '\'' +
                ", id='" + id + '\'' +
                ", done=" + done +
                '}';
    }
}
