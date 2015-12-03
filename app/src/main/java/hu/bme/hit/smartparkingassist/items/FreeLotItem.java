package hu.bme.hit.smartparkingassist.items;

import android.os.Parcel;
import android.os.Parcelable;

public class FreeLotItem implements Parcelable {

    private Integer ID;
    private Integer gpsTime;
    private Double latitude;
    private Double longitude;
    private Integer userId;
    private String parkingLotAvailability;
    private String address;
    private Double distance;

    protected FreeLotItem(Parcel in) {
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        distance = in.readDouble();
    }

    public static final Creator<FreeLotItem> CREATOR = new Creator<FreeLotItem>() {
        @Override
        public FreeLotItem createFromParcel(Parcel in) {
            return new FreeLotItem(in);
        }

        @Override
        public FreeLotItem[] newArray(int size) {
            return new FreeLotItem[size];
        }
    };

    /**
     *
     * @return
     * The ID
     */
    public Integer getID() {
        return ID;
    }

    /**
     *
     * @param ID
     * The ID
     */
    public void setID(Integer ID) {
        this.ID = ID;
    }

    /**
     *
     * @return
     * The gpsTime
     */
    public Integer getGpsTime() {
        return gpsTime;
    }

    /**
     *
     * @param gpsTime
     * The gpsTime
     */
    public void setGpsTime(Integer gpsTime) {
        this.gpsTime = gpsTime;
    }

    /**
     *
     * @return
     * The latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     *
     * @param latitude
     * The latitude
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     *
     * @return
     * The longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     *
     * @param longitude
     * The longitude
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @return
     * The userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     *
     * @param userId
     * The userId
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     *
     * @return
     * The parkingLotAvailability
     */
    public String getParkingLotAvailability() {
        return parkingLotAvailability;
    }

    /**
     *
     * @param parkingLotAvailability
     * The parkingLotAvailability
     */
    public void setParkingLotAvailability(String parkingLotAvailability) {
        this.parkingLotAvailability = parkingLotAvailability;
    }

    /**
     *
     * @return
     * The address
     */
    public String getAddress() {
        return address;
    }

    /**
     *
     * @param address
     * The address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     *
     * @return
     * The distance
     */
    public Double getDistance() {
        return distance;
    }

    /**
     *
     * @param distance
     * The distance
     */
    public void setDistance(Double distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeDouble(distance);
    }
}