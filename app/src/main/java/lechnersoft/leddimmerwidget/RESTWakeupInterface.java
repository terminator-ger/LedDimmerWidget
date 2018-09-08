package lechnersoft.leddimmerwidget;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

/**
 * Created by Michael on 28 Jan 2018.
 */
public interface RESTWakeupInterface {
    @PUT("wakeuptime")
    Call<ResponseBody> RestWakeUp(@Body PostWake postWake);

    @PUT("toggle")
    Call<ResponseBody> RestLightToggle();

    @PUT("incr")
    Call<ResponseBody> RestLightIncr();

    @PUT("decr")
    Call<ResponseBody> RestLightDecr();

    @PUT("sunrise")
    Call<ResponseBody> RestSunrise();
}
