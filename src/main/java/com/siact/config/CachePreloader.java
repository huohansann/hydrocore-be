//package com.siact.config;
//
//import com.siact.module.permission.cache.RedisUserCache;
//import com.siact.module.permission.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.context.event.ApplicationReadyEvent;
//import org.springframework.context.ApplicationListener;
//import org.springframework.stereotype.Component;
//
/**
* 缓存预加载
*/
//@Component
//@RequiredArgsConstructor
//public class CachePreloader implements ApplicationListener<ApplicationReadyEvent> {
//
//    private final UserService userService;
//    private final RedisUserCache redisUserCache;
//
//    @Override
//    public void onApplicationEvent(ApplicationReadyEvent event) {
//        List<UserDetails> allUsers = userService.loadAllUsers();
//        for (UserDetails user : allUsers) {
//            redisUserCache.putUserInCache(user);
//        }
//    }
//}
