package com.survey_engine.rewards.service.notifications;

/**
 * Interface for sending various types of notifications.
 * This abstracts the underlying notification mechanism, improving testability and flexibility.
 */
public interface NotificationService {

    /**
     * Sends an SMS message to a specified recipient.
     *
     * @param to The recipient's phone number.
     * @param message The content of the SMS message.
     */
    void sendSms(String to, String message);
}