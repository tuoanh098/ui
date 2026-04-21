Android example — Retrofit usage (Java)

Mô tả: Các ví dụ nhỏ để FE Android (native Java) tích hợp nhanh với backend.

1) Gradle dependencies
Add to app/build.gradle:

implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:okhttp:4.11.0'

2) JwtInterceptor.java
```java
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class JwtInterceptor implements Interceptor {
    private final String token;
    public JwtInterceptor(String token) { this.token = token; }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request req = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer " + token)
            .build();
        return chain.proceed(req);
    }
}
```

3) RetrofitClient.java
```java
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;

public class RetrofitClient {
    public static Retrofit create(String baseUrl, String token) {
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(new JwtInterceptor(token))
            .build();
        return new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }
}
```

4) AuthApi.java
```java
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest req);
}
```

5) Usage (example)
```java
// 1) Login using a Retrofit instance without interceptor
Retrofit authRetrofit = new Retrofit.Builder()
    .baseUrl("http://10.0.2.2:8081") // emulator
    .addConverterFactory(GsonConverterFactory.create())
    .build();
AuthApi authApi = authRetrofit.create(AuthApi.class);

LoginRequest req = new LoginRequest("admin","Admin@123");
Call<LoginResponse> call = authApi.login(req);
// execute async or sync, then get accessToken from response

// 2) Create secured client
Retrofit secured = RetrofitClient.create("http://10.0.2.2:8081", accessToken);
PhongApi phongApi = secured.create(PhongApi.class);
Call<List<PhongDto>> phongsCall = phongApi.list();
```

Ghi chú:
- Trên thiết bị thật, thay baseUrl bằng IP máy dev hoặc public URL của backend.
- Sử dụng 10.0.2.2 cho Android emulator (AVD). For Genymotion use 10.0.3.2.

Nếu cần, mình có thể thêm một module Android sample (gradle project) để bạn chạy trực tiếp.

6) Upload incident attachment (multipart)
```java
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface SuCoApi {
    @Multipart
    @POST("/api/incidents/{id}/attachments")
    Call<SuCoDto> uploadAttachment(@Path("id") long id, @Part MultipartBody.Part file);
}

// Usage example
File file = new File("/sdcard/Download/image.jpg");
RequestBody reqFile = RequestBody.create(file, MediaType.parse("image/jpeg"));
MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), reqFile);

SuCoApi suCoApi = secured.create(SuCoApi.class);
Call<SuCoDto> uploadCall = suCoApi.uploadAttachment(123L, body);
// execute and handle response
```

Notes: allowed content types: image/png, image/jpeg, image/jpg, image/gif, application/pdf; max file size: 5 MB.

