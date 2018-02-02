package lechnersoft.leddimmerwidget;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;

/**
 * Created by Michael on 28 Jan 2018.
 */
public interface WakeupInterface {
    @PUT("wakeuptime")
    Call<ResponseBody> createPostWake(@Body PostWake postWake);

    @PUT("ON")
    Call<ResponseBody> on_light();

    @PUT("OFF")
    Call<ResponseBody> off_light();

    @PUT("INCR")
    Call<ResponseBody> incr_light();

    @PUT("DECR")
    Call<ResponseBody> decr_light();
}
