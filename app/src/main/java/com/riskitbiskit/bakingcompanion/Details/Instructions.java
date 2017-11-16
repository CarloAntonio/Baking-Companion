package com.riskitbiskit.bakingcompanion.Details;

import android.os.Parcel;
import android.os.Parcelable;

public class Instructions implements Parcelable {
    private int id;
    private String shortDescription;
    private String instruction;
    private String videoUrl;
    private String thumbnailUrl;

    public Instructions(int id, String shortDescription, String instruction, String videoUrl, String thumbnailUrl) {
        this.id = id;
        this.shortDescription = shortDescription;
        this.instruction = instruction;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    protected Instructions(Parcel in) {
        id = in.readInt();
        shortDescription = in.readString();
        instruction = in.readString();
        videoUrl = in.readString();
        thumbnailUrl = in.readString();
    }

    public static final Creator<Instructions> CREATOR = new Creator<Instructions>() {
        @Override
        public Instructions createFromParcel(Parcel in) {
            return new Instructions(in);
        }

        @Override
        public Instructions[] newArray(int size) {
            return new Instructions[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(shortDescription);
        parcel.writeString(instruction);
        parcel.writeString(videoUrl);
        parcel.writeString(thumbnailUrl);
    }
}