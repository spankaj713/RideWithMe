package smartparadise.ridewithme.DataModels;

/**
 * Created by HP on 12-05-2018.
 */

public class HistoryModels {

    private String rideId,time;

    public HistoryModels(String rideId,String time){
        this.time=time;
        this.rideId=rideId;
    }

    public String getTime(){
        return time;
    }

    public String getRideId() {
        return rideId;
    }
}
