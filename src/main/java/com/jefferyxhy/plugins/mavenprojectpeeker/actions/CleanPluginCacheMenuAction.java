package com.jefferyxhy.plugins.mavenprojectpeeker.actions;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.CacheService;
import com.jefferyxhy.plugins.mavenprojectpeeker.services.NotificationService;

public class CleanPluginCacheMenuAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        try {
            CacheService.getInstance().clean();
            NotificationService.getInstance().notify(e.getProject(), "Plugin cache cleaned!", NotificationType.INFORMATION);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
