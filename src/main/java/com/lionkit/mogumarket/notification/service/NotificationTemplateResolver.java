// notification/service/NotificationTemplateResolver.java
package com.lionkit.mogumarket.notification.service;

import java.util.Map;

public interface NotificationTemplateResolver {

    Template groupBuyReached(Long groupBuyId);

    Template groupBuyClosingSoon(Long groupBuyId);

    Template groupBuyClosed(Long groupBuyId);

    record Template(String title, String body, Map<String,String> data) {}
}