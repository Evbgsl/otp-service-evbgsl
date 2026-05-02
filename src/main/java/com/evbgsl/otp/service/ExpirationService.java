package com.evbgsl.otp.service;

import com.evbgsl.otp.dao.OtpCodeDao;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ExpirationService {

    private static final long INITIAL_DELAY_SECONDS = 5;
    private static final long CHECK_INTERVAL_SECONDS = 10;

    private final OtpCodeDao otpCodeDao;
    private final ScheduledExecutorService scheduler;

    public ExpirationService(OtpCodeDao otpCodeDao) {
        this.otpCodeDao = otpCodeDao;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("otp-expiration-worker");
            thread.setDaemon(true);
            return thread;
        });
    }

    public void start() {
        scheduler.scheduleAtFixedRate(
                this::expireCodesSafely,
                INITIAL_DELAY_SECONDS,
                CHECK_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );

        System.out.println("OTP expiration service started");
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void expireCodesSafely() {
        try {
            int expiredCount = otpCodeDao.markExpiredCodes();

            if (expiredCount > 0) {
                System.out.println("Expired OTP codes: " + expiredCount);
            }

        } catch (Exception e) {
            System.err.println("Failed to expire OTP codes: " + e.getMessage());
        }
    }
}