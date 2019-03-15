package com.spyman.telegramconcurs.diagram;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.spyman.telegramconcurs.diagram.diagram_data.LineDiagramData;

import java.util.List;

class DiagramState extends View.BaseSavedState {
    float position;
    float graphScaleX;
    List<LineDiagramData> data;
    boolean dinamicSize;

    DiagramState(float position, float graphScaleX, List<LineDiagramData> data, boolean dinamicSize, Parcelable superState) {
        super(superState);
        this.position = position;
        this.graphScaleX = graphScaleX;
        this.data = data;
        this.dinamicSize = dinamicSize;
    }

    protected DiagramState(Parcel in) {
        super(in);
        position = in.readFloat();
        graphScaleX = in.readFloat();
        data = in.createTypedArrayList(LineDiagramData.CREATOR);
        dinamicSize = in.readByte() != 0;
    }

    public static final Creator<DiagramState> CREATOR = new Creator<DiagramState>() {
        @Override
        public DiagramState createFromParcel(Parcel in) {
            return new DiagramState(in);
        }

        @Override
        public DiagramState[] newArray(int size) {
            return new DiagramState[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(position);
        dest.writeFloat(graphScaleX);
        dest.writeTypedList(data);
        dest.writeByte((byte) (dinamicSize ? 1 : 0));
    }
}