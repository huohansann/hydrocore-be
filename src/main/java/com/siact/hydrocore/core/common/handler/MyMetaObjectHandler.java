package com.siact.hydrocore.core.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.siact.hydrocore.common.context.LoginContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.Date;

/**
 * MyBatis-Plus自动填充处理器
 *
 * @author example
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    private static final String SYSTEM_OPERATOR = "system";

    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        Timestamp tsNow = new Timestamp(now.getTime());
        fillAuditTimes(metaObject, tsNow, now, true);
        fillOperators(metaObject, true);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Date now = new Date();
        Timestamp tsNow = new Timestamp(now.getTime());
        fillAuditTimes(metaObject, tsNow, now, false);
        fillOperators(metaObject, false);
    }

    private void fillAuditTimes(MetaObject metaObject, Timestamp tsNow, Date now, boolean insert) {
        if (insert) {
            this.strictInsertFill(metaObject, "createTime", Timestamp.class, tsNow);
            this.strictInsertFill(metaObject, "createTime", Date.class, now);
            this.strictInsertFill(metaObject, "updateTime", Timestamp.class, tsNow);
            this.strictInsertFill(metaObject, "updateTime", Date.class, now);
            this.strictInsertFill(metaObject, "createdAt", Timestamp.class, tsNow);
            this.strictInsertFill(metaObject, "updatedAt", Timestamp.class, tsNow);
            return;
        }
        this.strictUpdateFill(metaObject, "updateTime", Timestamp.class, tsNow);
        this.strictUpdateFill(metaObject, "updateTime", Date.class, now);
        this.strictUpdateFill(metaObject, "updatedAt", Timestamp.class, tsNow);
    }

    private void fillOperators(MetaObject metaObject, boolean insert) {
        String operator = resolveOperatorAccount();
        Long operatorId = LoginContext.getUserId();

        if (insert) {
            this.strictInsertFill(metaObject, "createBy", String.class, operator);
            this.strictInsertFill(metaObject, "updateBy", String.class, operator);
            if (operatorId != null) {
                this.strictInsertFill(metaObject, "createBy", Long.class, operatorId);
                this.strictInsertFill(metaObject, "updateBy", Long.class, operatorId);
            }
            return;
        }

        this.strictUpdateFill(metaObject, "updateBy", String.class, operator);
        if (operatorId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, operatorId);
        }
    }

    private static String resolveOperatorAccount() {
        String account = LoginContext.getAccount();
        return StringUtils.hasText(account) ? account : SYSTEM_OPERATOR;
    }
}
