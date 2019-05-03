// INotificationService.aidl
package com.token.v1.os.launcher;

interface INotificationService {
    void setNotification(String message, int lightColorARGB, int timeoutMs);
}
