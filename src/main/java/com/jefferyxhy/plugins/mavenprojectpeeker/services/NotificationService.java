package com.jefferyxhy.plugins.mavenprojectpeeker.services;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Service
public final class NotificationService {

    private final NotificationGroup notificationGroup;

    public NotificationService() {
        notificationGroup = NotificationGroupManager.getInstance()
                .getNotificationGroup("Maven Project Peeker Notification Group");
    }

    public static NotificationService getInstance() {
        return ApplicationManager.getApplication().getService(NotificationService.class);
    }

    public void notify(@Nullable Project project, @NlsContexts.NotificationContent @NotNull String content, @NotNull NotificationType type) {
        notificationGroup.createNotification("Maven Project Peeker", content, type).notify(project);
    }

    public void notify(@Nullable Project project, @NlsContexts.NotificationContent @NotNull String content) {
        notify(project, content, NotificationType.INFORMATION);
    }

    public void notify(@NlsContexts.NotificationContent @NotNull String content) {
        notify(null, content, NotificationType.INFORMATION);
    }
}
