package com.trohub.ui.api;

import java.util.List;
import java.util.Map;

import com.trohub.ui.api.models.Phong;
import com.trohub.ui.api.models.Tenant;
import com.trohub.ui.api.models.Incident;
import com.trohub.ui.api.models.ToaNha;
import com.trohub.ui.api.models.Contract;
import com.trohub.ui.api.models.Invoice;
import com.trohub.ui.api.models.Landlord;
import com.trohub.ui.api.models.BankInfo;
import com.trohub.ui.api.models.BankInfoRequest;
import com.trohub.ui.api.models.GuestEntry;
import com.trohub.ui.api.models.QrRequest;
import com.trohub.ui.api.models.QrResponse;
import com.trohub.ui.api.models.TaiKhoanDto;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<TaiKhoanDto> register(@Body RegisterRequest request);

    @GET("api/test/whoami")
    Call<Map<String, Object>> whoAmI();

    @GET("api/phongs")
    Call<List<Phong>> getPhongs();
    @GET("api/phongs")
    Call<List<Phong>> getPhongs(@Query("q") String q);

    @POST("api/phongs")
    Call<Phong> createPhong(@Body Phong room);

    @PUT("api/phongs/{id}")
    Call<Phong> updatePhong(@Path("id") Long id, @Body Phong room);

    @DELETE("api/phongs/{id}")
    Call<Void> deletePhong(@Path("id") Long id);

    @GET("api/buildings")
    Call<List<ToaNha>> getBuildings();
    @GET("api/buildings")
    Call<List<ToaNha>> getBuildings(@Query("q") String q);

    @POST("api/buildings")
    Call<ToaNha> createBuilding(@Body ToaNha building);

    @PUT("api/buildings/{id}")
    Call<ToaNha> updateBuilding(@Path("id") Long id, @Body ToaNha building);

    @DELETE("api/buildings/{id}")
    Call<Void> deleteBuilding(@Path("id") Long id);

    @GET("api/contracts")
    Call<List<Contract>> getContracts();

    @POST("api/contracts")
    Call<Contract> createContract(@Body Contract contract);

    @PUT("api/contracts/{id}")
    Call<Contract> updateContract(@Path("id") Long id, @Body Contract contract);

    @GET("api/contracts/tenant/{tenantId}")
    Call<List<Contract>> getContractsByTenant(@Path("tenantId") Long tenantId);

    @GET("api/tenants")
    Call<List<Tenant>> getTenants();
    @GET("api/tenants")
    Call<List<Tenant>> getTenants(@Query("q") String q);

    @POST("api/tenants")
    Call<Tenant> createTenant(@Body Tenant tenant);

    @PUT("api/tenants/{id}")
    Call<Tenant> updateTenant(@Path("id") Long id, @Body Tenant tenant);

    @DELETE("api/tenants/{id}")
    Call<Void> deleteTenant(@Path("id") Long id);

    @GET("api/landlords")
    Call<List<Landlord>> getLandlords();
    @GET("api/landlords")
    Call<List<Landlord>> getLandlords(@Query("q") String q);

    @GET("api/auth/users")
    Call<List<TaiKhoanDto>> getAuthUsers(@Query("q") String q, @Query("role") String role);

    @GET("api/incidents")
    Call<List<Incident>> getIncidents();

    @POST("api/incidents")
    Call<Incident> createIncident(@Body Incident incident);

    @PUT("api/incidents/{id}")
    Call<Incident> updateIncident(@Path("id") Long id, @Body Incident incident);

    @DELETE("api/incidents/{id}")
    Call<Void> deleteIncident(@Path("id") Long id);

    @Multipart
    @POST("api/incidents/{id}/attachments")
    Call<ResponseBody> uploadAttachment(@Path("id") Long id, @Part MultipartBody.Part file);

    @POST("api/billing/generate")
    Call<List<Invoice>> generateInvoices(@Query("ky") String ky);

    @POST("api/billing/generate-async")
    Call<Object> generateInvoicesAsync(@Query("ky") String ky);

    @GET("api/billing/invoices")
    Call<List<Invoice>> getInvoices(@Query("nam") Integer nam, @Query("thang") Integer thang);

    @GET("api/billing/invoices")
    Call<List<Invoice>> getInvoicesByTenant(@Query("nam") Integer nam, @Query("thang") Integer thang, @Query("tenantId") Long tenantId);

    @GET("api/billing/invoices/{id}")
    Call<Invoice> getInvoiceDetail(@Path("id") Long id);

    @POST("api/billing/qr/create")
    Call<QrResponse> createQrCode(@Body QrRequest request);

    // Billing - QA / admin endpoints
    @POST("api/billing/admin/prices")
    Call<Object> createPrice(@Body com.trohub.ui.api.models.PriceRequest request);

    @POST("api/billing/admin/readings")
    Call<Object> createReading(@Body com.trohub.ui.api.models.ReadingRequest request);

    @POST("api/billing/admin/apply-daily-fee")
    Call<Object> applyDailyFee(@Body com.trohub.ui.api.models.ApplyDailyFeeRequest request);

    @POST("api/billing/payments/qr/simulate")
    Call<Object> simulateQrPayment(@Body com.trohub.ui.api.models.SimulateQrRequest request);

    @POST("api/billing/admin/invoices/{id}/simulate-paid")
    Call<Object> simulateInvoicePaid(@Path("id") Long invoiceId);

    @POST("api/billing/admin/invoices/regenerate")
    Call<List<Invoice>> regenerateInvoices(@Body com.trohub.ui.api.models.RegenerateInvoicesRequest request);

    @GET("api/billing/admin/bank")
    Call<BankInfo> getBankInfo();

    @POST("api/billing/admin/bank")
    Call<BankInfo> upsertBankInfo(@Body BankInfoRequest request);

    @GET("api/billing/invoices/{id}/payments")
    Call<List<com.trohub.ui.api.models.PaymentDto>> getInvoicePayments(@Path("id") Long invoiceId);

    @POST("api/billing/invoices/{id}/payments/manual")
    Call<Object> createManualPayment(@Path("id") Long invoiceId, @Body com.trohub.ui.api.models.ManualPaymentRequest body);

    @GET("api/guest-entries")
    Call<List<GuestEntry>> getGuestEntries();

    @GET("api/guest-entries/review-items")
    Call<List<GuestEntry>> getGuestEntriesForReview();

    @POST("api/guest-entries")
    Call<GuestEntry> createGuestEntry(@Body GuestEntry entry);

    @PUT("api/guest-entries/{id}")
    Call<GuestEntry> updateGuestEntry(@Path("id") Long id, @Body GuestEntry entry);

    @DELETE("api/guest-entries/{id}")
    Call<Void> deleteGuestEntry(@Path("id") Long id);

    @POST("api/guest-entries/{id}/approve")
    Call<GuestEntry> approveGuestEntry(@Path("id") Long id);

    @POST("api/guest-entries/{id}/reject")
    Call<GuestEntry> rejectGuestEntry(@Path("id") Long id);

    @POST("api/guest-entries/{id}/request-info")
    Call<GuestEntry> requestGuestInfo(@Path("id") Long id, @Body com.trohub.ui.api.models.GuestReviewRequest body);
}
