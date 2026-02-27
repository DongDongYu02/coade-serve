package cn.dong.nexus.core.handler;

import cn.dong.nexus.core.security.context.IAuthContext;
import cn.dong.nexus.core.security.context.LoginUser;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class BaseFieldAutoInjectHandler implements MetaObjectHandler {
    private final IAuthContext authContext;
    /**
     * insert sql 字段填充
     *
     * @date 17:41 2023/12/4
     **/
    @Override
    public void insertFill(MetaObject metaObject) {
        LoginUser loginUser = authContext.getLoginUser();
        if(Objects.nonNull(loginUser)) {
            this.strictInsertFill(metaObject, "createBy", String.class, loginUser.getId());
            this.strictInsertFill(metaObject, "updateBy", String.class, loginUser.getId());
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }

    /**
     * update sql 字段填充
     *
     * @date 17:41 2023/12/4
     **/
    @Override
    public void updateFill(MetaObject metaObject) {
        LoginUser loginUser = authContext.getLoginUser();
        if(Objects.nonNull(loginUser)) {
            this.strictUpdateFill(metaObject, "updateBy", String.class, loginUser.getId());
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        }
    }
}
