package com.hxyc.canal;

import com.hxyc.canal.util.CanalUtils;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;

/**
 * @ClassName StartCanal
 * @Description  SpringBoot启动时，就执行postConstruct方法
 * @Author admin
 * @Date 2020/11/6 15:32
 **/
@Configuration
public class StartCanal {

    @PostConstruct
    public void postConstruct(){
        CanalUtils.executeSyncSql();
    }
}
